package de.riemerjonas.openrouter.graph.core;

import de.riemerjonas.openrouter.core.*;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

import java.io.*;
import java.util.*;

public class ORGraphHandler
{
    private static final String TAG = "ORGraphHandler";
    private static Set<String> carHighways = Set.of(
            "motorway", "trunk", "primary", "secondary", "tertiary",
            "unclassified", "residential", "motorway_link", "trunk_link",
            "primary_link", "secondary_link", "tertiary_link", "living_street",
            "service", "bus_guideway", "road"
    );

    /**
     * Saves the given graph to the given file.
     * @param file the file to save to
     * @param graph the graph to save
     */
    public static void save(File file, OpenRouterGraph graph)
    {
        OpenRouterTileMap tileMap  = graph.getTileMap();
        try
        {
            byte[] serializedTileMap = OpenRouterTileMap.serializeTileMap(tileMap.getTileMap());
            byte[] serializedEdges = OpenRouterEdge.serialize(graph.getEdges());

            try (FileOutputStream fos = new FileOutputStream(file))
            {
                fos.write(serializedTileMap);
                fos.write(serializedEdges);
                fos.flush();
            }
            OpenRouterLog.d(TAG, "Saved " + graph.getEdges().size() + " edges");
            OpenRouterLog.d(TAG, "Saved " + tileMap.getTileMap().size() + " tiles");
            OpenRouterLog.i(TAG, "Graph saved to file: " + file.getAbsolutePath());
        }
        catch (Exception e)
        {
            OpenRouterLog.e(TAG, "Unable to save graph to file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Loads the graph from the given file.
     * @param file the file to load from
     * @return the loaded graph
     */
    public static OpenRouterGraph load(File file)
    {
        try (FileInputStream fis = new FileInputStream(file))
        {
            byte[] fullData = fis.readAllBytes();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(fullData));

            // --- TILEMAP: Header auslesen ---
            int tileCount = in.readUnsignedShort();
            Map<Short, TileIndex> tileIndexMap = new HashMap<>();

            for (int i = 0; i < tileCount; i++)
            {
                short tileId = in.readShort();
                int offset = in.readInt();
                int length = in.readInt();
                tileIndexMap.put(tileId, new TileIndex(offset, length));
            }

            // --- TILEMAP: TileNodes einlesen ---
            Map<Short, List<OpenRouterNode>> tileMap = new HashMap<>();
            int tileDataStart = fullData.length - in.available(); // Start der TileData
            for (Map.Entry<Short, TileIndex> entry : tileIndexMap.entrySet())
            {
                short tileId = entry.getKey();
                TileIndex idx = entry.getValue();

                List<OpenRouterNode> nodes = new ArrayList<>();
                ByteArrayInputStream tileStream = new ByteArrayInputStream(fullData, tileDataStart + idx.offset, idx.length);
                DataInputStream tileIn = new DataInputStream(tileStream);

                while (tileStream.available() > 0)
                {
                    int len = tileIn.readUnsignedShort();
                    byte[] nodeBytes = new byte[len];
                    tileIn.readFully(nodeBytes);
                    nodes.add(OpenRouterNode.fromByteArray(nodeBytes));
                }

                tileMap.put(tileId, nodes);
            }

            // --- EDGES: restliche Daten parsen ---
            int edgeDataOffset = tileDataStart + tileIndexMap.values().stream().mapToInt(t -> t.length).sum();
            int edgeBytes = fullData.length - edgeDataOffset;
            int edgeCount = edgeBytes / 12;

            List<OpenRouterEdge> edges = new ArrayList<>(edgeCount);
            for (int i = 0; i < edgeCount; i++)
            {
                int base = edgeDataOffset + i * 12;

                int fromID = ((fullData[base] & 0xFF) << 24) | ((fullData[base + 1] & 0xFF) << 16) |
                        ((fullData[base + 2] & 0xFF) << 8) | (fullData[base + 3] & 0xFF);

                int toID = ((fullData[base + 4] & 0xFF) << 24) | ((fullData[base + 5] & 0xFF) << 16) |
                        ((fullData[base + 6] & 0xFF) << 8) | (fullData[base + 7] & 0xFF);

                int metaData = ((fullData[base + 8] & 0xFF) << 24) | ((fullData[base + 9] & 0xFF) << 16) |
                        ((fullData[base + 10] & 0xFF) << 8) | (fullData[base + 11] & 0xFF);

                edges.add(new OpenRouterEdge(fromID, toID, metaData));
            }

            // --- Aufbau Graph-Objekt ---
            OpenRouterLog.i(TAG, "Loaded graph from file: " + file.getAbsolutePath());
            OpenRouterLog.d(TAG, "Loaded " + edges.size() + " edges");
            OpenRouterLog.d(TAG, "Loaded " + tileMap.size() + " tiles");
            OpenRouterLog.d(TAG, "Loaded " + tileIndexMap.size() + " tile indices");

            OpenRouterTileMap orm = new OpenRouterTileMap(tileMap);
            return new OpenRouterGraph(orm, edges);
        }
        catch (IOException e)
        {
            OpenRouterLog.e("OpenRouter", "Failed to load graph", e);
            return null;
        }
    }


    private static class TileIndex {
        final int offset;
        final int length;
        TileIndex(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }

    /**
     * Builds the graph from PBF file.
     * @param file the PBF file
     * @return the graph
     */
    public static OpenRouterGraph buildFromPBF(File file)
    {
        OpenRouterLog.i(TAG, "Building graph from PBF file: " + file.getAbsolutePath());
        try
        {
            // Step 1: Collect all nodes of relevant highway ways
            OpenRouterLog.d(TAG, "Collecting relevant nodes from PBF file");
            Set<Long> relevantNodeIds = new HashSet<>();
            InputStream stream = new FileInputStream(file);
            PbfIterator iterator = new PbfIterator(stream, true);
            for (EntityContainer container : iterator)
            {
                if (container.getType().equals(EntityType.Way))
                {
                    OsmWay way = (OsmWay) container.getEntity();
                    if (!isHighway(way)) continue;

                    for (int i = 0; i < way.getNumberOfNodes(); i++)
                    {
                        relevantNodeIds.add(way.getNodeId(i));
                    }
                }
            }
            OpenRouterLog.d(TAG, "Collected " + relevantNodeIds.size() + " relevant nodes");

            // Step 2: Load only relevant nodes
            OpenRouterLog.d(TAG, "Loading relevant nodes from PBF file");
            Map<Long, OpenRouterNode> nodeMap = new HashMap<>();
            iterator = new PbfIterator(new FileInputStream(file), false);
            for (EntityContainer container : iterator)
            {
                if (container.getType().equals(EntityType.Node))
                {
                    OsmNode node = (OsmNode) container.getEntity();
                    if (relevantNodeIds.contains(node.getId()))
                    {
                        nodeMap.put(node.getId(), new OpenRouterNode(nodeMap.size(), node.getLatitude(), node.getLongitude()));
                    }
                }
            }
            OpenRouterLog.d(TAG, "Loaded " + nodeMap.size() + " relevant nodes");

            // 3. Create edges between all consecutive nodes
            OpenRouterLog.d(TAG, "Creating edges from PBF file");
            List<OpenRouterEdge> edges = new ArrayList<>();
            iterator = new PbfIterator(new FileInputStream(file), false);
            for (EntityContainer container : iterator) {
                if (container.getType().equals(EntityType.Way)) {
                    OsmWay way = (OsmWay) container.getEntity();
                    if (!isHighway(way)) continue;

                    short maxSpeed = parseShortTag(way, "maxspeed", (short) 100);

                    // Roundabouts sind immer Einbahnstraße
                    boolean isOneway = isOneway(way) || isRoundabout(way);

                    for (int i = 1; i < way.getNumberOfNodes(); i++) {
                        long fromOsm = way.getNodeId(i - 1);
                        long toOsm   = way.getNodeId(i);

                        OpenRouterNode fromNode = nodeMap.get(fromOsm);
                        OpenRouterNode toNode = nodeMap.get(toOsm);
                        if (fromNode == null || toNode == null)
                        {
                            OpenRouterLog.w(TAG, "Node not found in map: " + fromOsm + " or " + toOsm);
                            continue;
                        };

                        int from = fromNode.getId();
                        int to   = toNode.getId();
                        short distance = (short) Math.round(fromNode.distanceTo(toNode));

                        OpenRouterEdgeMeta meta = new OpenRouterEdgeMeta((short) Math.round(maxSpeed * 3.6), distance, (short) 0);
                        edges.add(new OpenRouterEdge(from, to, meta));

                        if (!isOneway) edges.add(new OpenRouterEdge(to, from, meta));
                    }
                }
            }
            OpenRouterLog.d(TAG, "Created " + edges.size() + " edges");
            OpenRouterLog.i(TAG, "Finished building graph from PBF file");

            // Step 4: Create tile map
            OpenRouterLog.d(TAG, "Creating tile map");
            List<OpenRouterNode> nodes = new ArrayList<>(nodeMap.values());
            OpenRouterTileMap tileMap = OpenRouterTileMap.create(nodes);

            return new OpenRouterGraph(tileMap, edges);
        }
        catch (Exception e)
        {
            OpenRouterLog.e(TAG, "Unable to build graph from PBF file: " + file.getAbsolutePath(), e);
            return null;
        }
    }

    private static boolean isHighway(OsmWay way)
    {
        for (int i = 0; i < way.getNumberOfTags(); i++)
        {
            if (way.getTag(i).getKey().equals("highway") && carHighways.contains(way.getTag(i).getValue()))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isRoundabout(OsmWay way)
    {
        for (int i = 0; i < way.getNumberOfTags(); i++)
        {
            if (way.getTag(i).getKey().equals("junction") && way.getTag(i).getValue().equals("roundabout"))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isOneway(OsmWay way)
    {
        for (int i = 0; i < way.getNumberOfTags(); i++)
        {
            if (way.getTag(i).getKey().equals("oneway") && way.getTag(i).getValue().equals("yes"))
            {
                return true;
            }
        }
        return false;
    }

    private static short parseShortTag(OsmWay way, String key, short defaultValue)
    {
        for (int i = 0; i < way.getNumberOfTags(); i++)
        {
            if (way.getTag(i).getKey().equals(key))
            {
                try
                {
                    String val = way.getTag(i).getValue().replaceAll("[^0-9.]", "");
                    return (short) Float.parseFloat(val);
                }
                catch (Exception ignored) {}
            }
        }
        return defaultValue;
    }
}
