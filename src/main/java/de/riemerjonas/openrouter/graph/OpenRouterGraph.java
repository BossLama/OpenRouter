package de.riemerjonas.openrouter.graph;

import de.riemerjonas.openrouter.compressor.Compressor;
import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.*;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenRouterGraph
{
    private static final String TAG = "OpenRouterGraph";
    private static final double TILE_SIZE = 0.1;

    private List<OpenRouterEdge> edges;
    private HashMap<TileCoordinates, List<OpenRouterNode>> tileMap;

    public OpenRouterGraph(List<OpenRouterEdge> edges,HashMap<TileCoordinates, List<OpenRouterNode>> tileMap)
    {
        this.edges = edges;
        this.tileMap = tileMap;
    }

    public HashMap<TileCoordinates, List<OpenRouterNode>> getTileMap()
    {
        return tileMap;
    }

    public List<OpenRouterEdge> getEdges()
    {
        return edges;
    }

    public void save(File file)
    {
        OpenRouterLog.i(TAG, "Saving graph to file: " + file.getAbsolutePath());
        ORGraphSaver.save(this, file);
    }


    public static OpenRouterGraph loadFromFile(File file)
    {
        OpenRouterLog.i(TAG, "Loading graph from file: " + file.getAbsolutePath());
        return ORGraphSaver.load(file);
    }

    /**
     * Builds a graph from a PBF file.
     * @param file The PBF file to read from.
     * @return The OpenRouterGraph object.
     */
    public static OpenRouterGraph buildFromPbf(File file)
    {
        try
        {
            ArrayList<OpenRouterNode> nodes = new ArrayList<>();
            ArrayList<OpenRouterEdge> edges = new ArrayList<>();

            InputStream inputStream = new FileInputStream(file);
            OsmIterator iterator = new PbfIterator(inputStream, false);

            OpenRouterLog.i(TAG, "Reading PBF file: " + file.getAbsolutePath());
            OpenRouterLog.i(TAG, "Fetching nodes from PBF file...");
            for(EntityContainer container : iterator)
            {
                if(container.getType().equals(EntityType.Node))
                {
                    OsmNode node = (OsmNode) container.getEntity();
                    OpenRouterNode openRouterNode = new OpenRouterNode(node.getId(), node.getLatitude(), node.getLongitude());
                    nodes.add(openRouterNode);
                    OpenRouterLog.i(TAG, "Fetching node " + node.getId());
                }
            }
            OpenRouterLog.i(TAG, "Fetched " + nodes.size() + " nodes from PBF file.");

            OpenRouterLog.i(TAG, "Fetching edges from PBF file...");
            iterator = new PbfIterator(new FileInputStream(file), false);
            for(EntityContainer container : iterator)
            {
                if(container.getType().equals(EntityType.Way))
                {
                    OsmWay way = (OsmWay) container.getEntity();
                    boolean isHighway = false;
                    for(int index = 0; index < way.getNumberOfTags(); index++)
                    {
                        OsmTag tag = way.getTag(index);
                        if(tag.getKey().equals("highway"))
                        {
                            isHighway = true;
                            break;
                        }
                    }
                    if(!isHighway) continue;
                    for (int i = 1; i < way.getNumberOfNodes(); i++) {
                        long from = way.getNodeId(i - 1);
                        long to = way.getNodeId(i);

                        //TODO: Implement check if possible to get from from to to
                        edges.add(new OpenRouterEdge(from, to));
                        edges.add(new OpenRouterEdge(to, from));
                        OpenRouterLog.i(TAG, "Fetching edge from " + from + " to " + to);
                    }
                }
            }
            OpenRouterLog.i(TAG, "Fetched " + edges.size() + " edges from PBF file.");

            OpenRouterLog.i(TAG, "Building tile map...");
            HashMap<TileCoordinates, List<OpenRouterNode>> tileMap = toTileMap(nodes);

            return new OpenRouterGraph(edges, tileMap);

        }
        catch (Exception e)
        {
            OpenRouterLog.e(TAG, "Error while building graph from PBF file: " + e.getMessage());
        }

        return null;
    }

    public static record TileCoordinates(short x, short y) {}

    /**
     * Converts a list of OpenRouterNode objects to a tile map.
     * @param nodes The list of OpenRouterNode objects to convert.
     * @return A HashMap where the keys are TileCoordinates and the values are lists of OpenRouterNode objects.
     */
    private static HashMap<TileCoordinates, List<OpenRouterNode>> toTileMap(List<OpenRouterNode> nodes)
    {
        HashMap<TileCoordinates, List<OpenRouterNode>> tileMap = new HashMap<>();
        for(OpenRouterNode node : nodes)
        {
            short x = (short) ((node.getLatitudeDouble()) / TILE_SIZE);
            short y = (short) ((node.getLongitudeDouble()) / TILE_SIZE);
            TileCoordinates tile = new TileCoordinates(x, y);
            tileMap.computeIfAbsent(tile, k -> new ArrayList<>()).add(node);
            OpenRouterLog.i(TAG, "Adding node " + node.getId() + " to tile " + x + ", " + y);
            OpenRouterLog.i(TAG, "Tile nodes: " + tileMap.get(tile).size());
        }

        return tileMap;
    }


}
