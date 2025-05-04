package de.riemerjonas.openrouter.graph.core;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;
import de.topobyte.osm4j.core.model.iface.*;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class OpenRouterGraphBuilder
{
    private static final String TAG = "OpenRouterGraphBuilder";

    public static OpenRouterGraph buildFromPbf(File file)
    {
        OpenRouterLog.i(TAG, "Building graph from PBF file: " + file.getAbsolutePath());
        Set<String> carHighways = Set.of(
                "motorway", "trunk", "primary", "secondary", "tertiary",
                "unclassified", "residential", "motorway_link", "trunk_link",
                "primary_link", "secondary_link", "tertiary_link", "living_street"
        );

        try
        {
            // Fetching all relevant data from the PBF file
            OpenRouterLog.d(TAG, "Fetching all relevant data from the PBF file");
            Set<Long> routingNodeIds = new HashSet<>();
            Set<OsmWay> osmWays = new HashSet<>();
            try(InputStream stream = new FileInputStream(file))
            {
                PbfIterator iterator = new PbfIterator(stream, true);
                for(EntityContainer container : iterator)
                {
                    if(container.getType().equals(EntityType.Way))
                    {
                        OsmWay way = (OsmWay) container.getEntity();
                        for(int tagIndex = 0; tagIndex < way.getNumberOfTags(); tagIndex++)
                        {
                            OsmTag tag = way.getTag(tagIndex);
                            if(tag.getKey().equals("highway") && carHighways.contains(tag.getValue()))
                            {
                                osmWays.add(way);
                                for(int nodeIndex = 0; nodeIndex < way.getNumberOfNodes(); nodeIndex++)
                                {
                                    long nodeId = way.getNodeId(nodeIndex);
                                    routingNodeIds.add(nodeId);
                                }
                            }
                        }
                    }
                }
            }
            OpenRouterLog.d(TAG, "Fetched all relevant data from the PBF file");

            // Filtering nodes from the PBF file
            OpenRouterLog.d(TAG, "Filtering nodes from PBF file");
            ArrayList<OsmNode> nodes = new ArrayList<>();
            try (InputStream stream = new FileInputStream(file))
            {
                PbfIterator iterator = new PbfIterator(stream, true);

                for (EntityContainer container : iterator)
                {
                    if (container.getType() == EntityType.Node)
                    {
                        OsmNode node = (OsmNode) container.getEntity();
                        if (routingNodeIds.contains(node.getId()))
                        {
                            nodes.add(node);
                        }
                    }
                }
            }

            // Creating the graph nodes
            OpenRouterLog.d(TAG, "Creating the graph nodes");
            ArrayList<OpenRouterNode> openRouterNodes = new ArrayList<>();
            int baseId = nodes.size() / 2;
            for(int index = 0; index < nodes.size(); index++)
            {
                int deltaId = index - baseId;
                OsmNode node = nodes.get(index);
                openRouterNodes.add(new OpenRouterNode(deltaId, node.getLatitude(), node.getLongitude()));
            }

            // Creating the graph edges
            OpenRouterLog.d(TAG, "Creating the graph edges");
            // Mapping node IDs to delta IDs
            OpenRouterLog.d(TAG, "Mapping node IDs to delta IDs");
            Map<Long, OpenRouterNode> nodeMap = new HashMap<>();
            for (int i = 0; i < nodes.size(); i++)
            {
                nodeMap.put(nodes.get(i).getId(), openRouterNodes.get(i));
            }

            // Creating edges
            OpenRouterLog.d(TAG, "Creating edges");
            ArrayList<byte[]> edges = new ArrayList<>();
            for (OsmWay way : osmWays)
            {
                byte foundTag = 0;
                boolean isBidirectional = true;
                short maxSpeed = 100; // Default-Wert
                for (int tagIndex = 0; tagIndex < way.getNumberOfTags(); tagIndex++)
                {
                    OsmTag tag = way.getTag(tagIndex);
                    String key = tag.getKey();
                    String value = tag.getValue();
                    if (key.equalsIgnoreCase("oneway") && value.equalsIgnoreCase("yes"))
                    {
                        foundTag++;
                        isBidirectional = false;
                    }
                    else if (key.equalsIgnoreCase("maxspeed"))
                    {
                        foundTag++;
                        try
                        {
                            maxSpeed = Short.parseShort(value.replaceAll("[^0-9]", ""));
                        }
                        catch (NumberFormatException ignored) {}
                    }

                    if(foundTag == 2) break;
                }

                for (int i = 1; i < way.getNumberOfNodes(); i++)
                {

                    OpenRouterNode fromNode = nodeMap.get(way.getNodeId(i - 1));
                    OpenRouterNode toNode = nodeMap.get(way.getNodeId(i));

                    if (fromNode != null && toNode != null)
                    {
                        short distance = (short) fromNode.distanceMeter(toNode);
                        edges.add(new OpenRouterEdge(fromNode.getDeltaId(), toNode.getDeltaId(), distance, maxSpeed, isBidirectional).toByteArray());
                    }
                }
            }
            OpenRouterLog.d(TAG, "Found nodes: " + nodes.size());
            OpenRouterLog.d(TAG, "Found edges: " + edges.size());

            return new OpenRouterGraph(openRouterNodes, edges, baseId);
        }
        catch (Exception e)
        {
            OpenRouterLog.e(TAG, "Error while building graph from PBF file", e);
            e.printStackTrace();
        }

        return null;
    }

    private static int haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371_000; // Meter
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return (int)(R * c);
    }
}
