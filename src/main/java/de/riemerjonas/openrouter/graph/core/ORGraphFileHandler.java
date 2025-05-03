package de.riemerjonas.openrouter.graph.core;

import de.riemerjonas.openrouter.core.OpenRouterCompressor;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.riemerjonas.openrouter.graph.OpenRouterGraph.TILE_SIZE;

public class ORGraphFileHandler {

    public static void saveGraph(OpenRouterGraph graph, File file) throws IOException
    {
        // A List of all nodes in the graph
        HashMap<Long, Integer> nodeIdToIndex = new HashMap<>();
        List<OpenRouterNode> allNodes = graph.getAllNodes();
        for(OpenRouterNode node : allNodes)
        {
            nodeIdToIndex.put(node.getId(), nodeIdToIndex.size());
        }


        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
        {

            // Write the number of tiles
            out.writeInt(graph.getTileNodeMap().size());

            // Write the nodeIdToIndex map
            out.writeInt(nodeIdToIndex.size());
            for (Map.Entry<Long, Integer> entry : nodeIdToIndex.entrySet())
            {
                out.writeLong(entry.getKey());
                out.writeInt(entry.getValue());
            }

            // Write the tile coordinates and nodes
            for (Map.Entry<OpenRouterGraph.TileMapCoordinate, Map<Long, OpenRouterNode>> entry : graph.getTileNodeMap().entrySet())
            {
                OpenRouterGraph.TileMapCoordinate tileCoord = entry.getKey();
                Map<Long, OpenRouterNode> nodeMap = entry.getValue();

                // Speichere die Kachel-Koordinaten
                out.writeShort(tileCoord.x());
                out.writeShort(tileCoord.y());

                // Speichere Anzahl der Knoten in der Kachel
                out.writeInt(nodeMap.size());

                // Speichere die Knoten und Kanten
                for (OpenRouterNode node : nodeMap.values())
                {

                    // Schreibe Knoten-ID, Latitude, Longitude
                    out.writeInt(nodeIdToIndex.get(node.getId()));
                    out.writeInt(node.getLatitudeAsInt());
                    out.writeInt(node.getLongitudeAsInt());

                    // Schreibe Kanten des Knotens
                    List<OpenRouterNode.Edge> edges = node.getEdges();
                    out.writeInt(edges.size()); // Anzahl der Kanten
                    for (OpenRouterNode.Edge edge : edges)
                    {
                        out.writeInt(nodeIdToIndex.get(edge.to()));
                        out.writeByte(OpenRouterCompressor.compressWeight(edge.weight()));
                    }
                }
            }
        }
    }

    // Methode zum Laden des gesamten Graphen ohne BoundingBox
    public static OpenRouterGraph loadFullGraph(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            // Anzahl der Tiles lesen
            int numTiles = in.readInt();

            // Lese die nodeIdToIndex Map (Rückwärts-Map wird erzeugt)
            int numNodeIds = in.readInt();
            Map<Integer, Long> indexToNodeId = new HashMap<>();
            for (int i = 0; i < numNodeIds; i++) {
                long nodeId = in.readLong();
                int index = in.readInt();
                indexToNodeId.put(index, nodeId);
            }

            Map<OpenRouterGraph.TileMapCoordinate, Map<Long, OpenRouterNode>> tileNodeMap = new HashMap<>();

            // Tiles lesen
            for (int t = 0; t < numTiles; t++) {
                short x = in.readShort();
                short y = in.readShort();
                OpenRouterGraph.TileMapCoordinate tileCoord = new OpenRouterGraph.TileMapCoordinate(x, y);

                int numNodes = in.readInt();
                Map<Long, OpenRouterNode> nodeMap = new HashMap<>();

                // Nodes samt Kanten direkt lesen
                for (int n = 0; n < numNodes; n++) {
                    int nodeIndex = in.readInt();
                    Long nodeId = indexToNodeId.get(nodeIndex);
                    if (nodeId == null) {
                        throw new IOException("Unbekannter nodeIndex: " + nodeIndex);
                    }

                    int lat = in.readInt();
                    int lon = in.readInt();

                    OpenRouterNode node = new OpenRouterNode(nodeId, lat, lon);

                    int edgeCount = in.readInt();
                    for (int e = 0; e < edgeCount; e++) {
                        int toIndex = in.readInt();
                        short weightCompressed = (short) in.readUnsignedByte();

                        Long toId = indexToNodeId.get(toIndex);
                        if (toId == null) {
                            throw new IOException("Unbekannter Edge-Knotenindex: to=" + toIndex);
                        }

                        node.addEdge(new OpenRouterNode.Edge(toId, weightCompressed));
                    }

                    nodeMap.put(nodeId, node);
                }

                tileNodeMap.put(tileCoord, nodeMap);
            }

            return new OpenRouterGraph(tileNodeMap);
        }
    }


    public static OpenRouterGraph loadGraph(File file, double minLat, double maxLat, double minLon, double maxLon) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            // Anzahl der Tiles lesen
            int numTiles = in.readInt();

            // Lese die nodeIdToIndex Map (Rückwärts-Map wird erzeugt)
            int numNodeIds = in.readInt();
            Map<Integer, Long> indexToNodeId = new HashMap<>();
            for (int i = 0; i < numNodeIds; i++) {
                long nodeId = in.readLong();
                int index = in.readInt();
                indexToNodeId.put(index, nodeId);
            }

            Map<OpenRouterGraph.TileMapCoordinate, Map<Long, OpenRouterNode>> tileNodeMap = new HashMap<>();

            // Tiles lesen
            for (int t = 0; t < numTiles; t++) {
                short x = in.readShort();
                short y = in.readShort();
                OpenRouterGraph.TileMapCoordinate tileCoord = new OpenRouterGraph.TileMapCoordinate(x, y);

                int numNodes = in.readInt();

                if (isTileInBoundingBox(tileCoord, minLat, maxLat, minLon, maxLon)) {
                    Map<Long, OpenRouterNode> nodeMap = new HashMap<>();

                    for (int n = 0; n < numNodes; n++) {
                        int nodeIndex = in.readInt();
                        Long nodeId = indexToNodeId.get(nodeIndex);
                        if (nodeId == null) {
                            throw new IOException("Unbekannter nodeIndex: " + nodeIndex);
                        }

                        int lat = in.readInt();
                        int lon = in.readInt();

                        OpenRouterNode node = new OpenRouterNode(nodeId, lat, lon);

                        int edgeCount = in.readInt();
                        for (int e = 0; e < edgeCount; e++) {
                            int toIndex = in.readInt();
                            short weightCompressed = (short) in.readUnsignedByte();

                            Long toId = indexToNodeId.get(toIndex);
                            if (toId == null) {
                                throw new IOException("Unbekannter Edge-Knotenindex: to=" + toIndex);
                            }

                            node.addEdge(new OpenRouterNode.Edge(toId, weightCompressed));
                        }

                        nodeMap.put(nodeId, node);
                    }

                    tileNodeMap.put(tileCoord, nodeMap);
                } else {
                    // Überspringe Knoten und Kanten der nicht relevanten Kachel
                    for (int n = 0; n < numNodes; n++) {
                        in.readInt(); // nodeIndex
                        in.readInt(); // lat (komprimiert)
                        in.readInt(); // lon (komprimiert)

                        int edgeCount = in.readInt();
                        for (int e = 0; e < edgeCount; e++) {
                            in.readInt(); // toIndex
                            in.readUnsignedByte(); // weightCompressed
                        }
                    }
                }
            }

            return new OpenRouterGraph(tileNodeMap);
        }
    }



    private static boolean isTileInBoundingBox(OpenRouterGraph.TileMapCoordinate tileCoord, double minLat, double maxLat, double minLon, double maxLon) {
        double latMin = tileCoord.y() * TILE_SIZE - 90;
        double latMax = (tileCoord.y() + 1) * TILE_SIZE - 90;
        double lonMin = tileCoord.x() * TILE_SIZE - 180;
        double lonMax = (tileCoord.x() + 1) * TILE_SIZE - 180;

        return latMin <= maxLat && latMax >= minLat && lonMin <= maxLon && lonMax >= minLon;
    }

}