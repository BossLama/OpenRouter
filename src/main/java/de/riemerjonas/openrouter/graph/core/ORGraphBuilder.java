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

public class ORGraphBuilder
{
    private static final String TAG = "ORGraphBuilder";

    public static OpenRouterGraph buildFromPbf(File file) {
        OpenRouterLog.info(TAG, "Building graph from PBF file: " + file.getAbsolutePath());

        try {
            Map<Long, OpenRouterNode> nodeMap = new HashMap<>();
            Map<Long, double[]> coordMap = new HashMap<>();
            List<OpenRouterEdge> edges = new ArrayList<>();
            Set<Long> usedNodeIds = new HashSet<>();

            // 1. Durchlauf: Sammle nur Node-IDs aus relevanten Ways
            try (InputStream stream1 = new FileInputStream(file)) {
                PbfIterator firstPass = new PbfIterator(stream1, true);

                for (EntityContainer container : firstPass) {
                    if (container.getEntity().getType() == EntityType.Way) {
                        OsmWay way = (OsmWay) container.getEntity();
                        for (int i = 0; i < way.getNumberOfTags(); i++) {
                            if (way.getTag(i).getKey().equalsIgnoreCase("highway")) {
                                for (int j = 0; j < way.getNumberOfNodes(); j++) {
                                    usedNodeIds.add(way.getNodeId(j));
                                }
                                break; // highway gefunden → Way ist relevant
                            }
                        }
                    }
                }
            }

            // 2. Durchlauf: Knoten + Kanten aufbauen
            try (InputStream stream2 = new FileInputStream(file)) {
                PbfIterator secondPass = new PbfIterator(stream2, true);

                for (EntityContainer container : secondPass) {
                    OsmEntity entity = container.getEntity();
                    switch (entity.getType()) {
                        case Node -> {
                            OsmNode node = (OsmNode) entity;
                            long id = node.getId();
                            if (!usedNodeIds.contains(id)) continue;

                            double lat = node.getLatitude();
                            double lon = node.getLongitude();
                            OpenRouterNode openRouterNode = new OpenRouterNode(id, lat, lon);
                            nodeMap.put(id, openRouterNode);
                            coordMap.put(id, new double[]{lat, lon});
                        }

                        case Way -> {
                            OsmWay way = (OsmWay) entity;
                            boolean isHighway = false;

                            for (int i = 0; i < way.getNumberOfTags(); i++) {
                                if (way.getTag(i).getKey().equalsIgnoreCase("highway")) {
                                    isHighway = true;
                                    break;
                                }
                            }

                            if (!isHighway) continue;

                            List<Long> nodeRefs = new ArrayList<>();
                            for (int i = 0; i < way.getNumberOfNodes(); i++) {
                                nodeRefs.add(way.getNodeId(i));
                            }

                            for (int i = 0; i < nodeRefs.size() - 1; i++) {
                                long from = nodeRefs.get(i);
                                long to = nodeRefs.get(i + 1);

                                if (coordMap.containsKey(from) && coordMap.containsKey(to)) {
                                    double[] fromCoord = coordMap.get(from);
                                    double[] toCoord = coordMap.get(to);
                                    short weight = (short) Math.min(calculateDistance(fromCoord, toCoord), Short.MAX_VALUE);

                                    OpenRouterEdge edge1 = new OpenRouterEdge(from, to, weight);
                                    OpenRouterEdge edge2 = new OpenRouterEdge(to, from, weight);

                                    edges.add(edge1);
                                    edges.add(edge2);

                                    OpenRouterNode fromNode = nodeMap.get(from);
                                    OpenRouterNode toNode = nodeMap.get(to);

                                    if (fromNode != null) fromNode.addEdge(edge1);
                                    if (toNode != null) toNode.addEdge(edge2);
                                }
                            }
                        }

                        default -> {
                            // Ignoriere Relationen und andere Typen
                        }
                    }
                }
            }

            // Aufteilen in Tiles
            Map<OpenRouterGraph.TileMapCoordinate, Map<Long, OpenRouterNode>> tileNodeMap = new HashMap<>();
            for (OpenRouterNode node : nodeMap.values()) {
                OpenRouterGraph.TileMapCoordinate tileCoord = OpenRouterGraph.getTileMapCoordinate(node.getLatitude(), node.getLongitude());
                tileNodeMap.computeIfAbsent(tileCoord, k -> new HashMap<>());
                tileNodeMap.get(tileCoord).put(node.getId(), node);
            }

            return new OpenRouterGraph(tileNodeMap);

        } catch (Exception e) {
            OpenRouterLog.info(TAG, "Error while reading PBF file: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }



    private static double calculateDistance(double[] from, double[] to) {
        final double R = 6371e3; // Erd-Radius in Metern
        double lat1 = Math.toRadians(from[0]);
        double lon1 = Math.toRadians(from[1]);
        double lat2 = Math.toRadians(to[0]);
        double lon2 = Math.toRadians(to[1]);

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlon / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}
