package de.riemerjonas.openrouter.graph.core;

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
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
        {

            // Schreibe die Anzahl der Tiles
            out.writeInt(graph.getTileNodeMap().size());

            // Durchlaufe die Tiles und speichere die Knoten und Kanten
            for (Map.Entry<OpenRouterGraph.TileMapCoordinate, Map<Long, OpenRouterNode>> entry : graph.getTileNodeMap().entrySet())
            {
                OpenRouterGraph.TileMapCoordinate tileCoord = entry.getKey();
                Map<Long, OpenRouterNode> nodeMap = entry.getValue();

                // Speichere Tile-Koordinaten (x, y)
                out.writeShort(tileCoord.x());
                out.writeShort(tileCoord.y());

                // Speichere Anzahl der Knoten in der Kachel
                out.writeInt(nodeMap.size());

                // Speichere die Knoten und Kanten
                for (OpenRouterNode node : nodeMap.values())
                {
                    // Schreibe Knoten-ID, Latitude, Longitude
                    out.writeLong(node.getId());
                    out.writeDouble(node.getLatitude());
                    out.writeDouble(node.getLongitude());

                    // Schreibe Kanten des Knotens
                    List<long[]> edges = node.getEdgesAsList();
                    out.writeInt(edges.size()); // Anzahl der Kanten
                    for (long[] edge : edges) {
                        // Schreibe von Node ID, zu Node ID, Gewicht
                        out.writeLong(edge[0]);
                        out.writeLong(edge[1]);
                        out.writeLong(edge[2]);
                    }
                }
            }
        }
    }

    // Methode zum Laden des gesamten Graphen ohne BoundingBox
    public static OpenRouterGraph loadFullGraph(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            // Lies die Anzahl der Tiles
            int numTiles = in.readInt();
            Map<OpenRouterGraph.TileMapCoordinate, Map<Long, OpenRouterNode>> tileNodeMap = new HashMap<>();

            // Lade alle Tiles und ihre Knoten
            for (int i = 0; i < numTiles; i++) {
                short x = in.readShort();
                short y = in.readShort();
                OpenRouterGraph.TileMapCoordinate tileCoord = new OpenRouterGraph.TileMapCoordinate(x, y);

                int numNodes = in.readInt();
                Map<Long, OpenRouterNode> nodeMap = new HashMap<>();

                // Lade alle Knoten und ihre Kanten für dieses Tile
                for (int j = 0; j < numNodes; j++) {
                    long id = in.readLong();
                    double latitude = in.readDouble();
                    double longitude = in.readDouble();

                    // Lade Kanten für den Knoten
                    int numEdges = in.readInt();
                    List<long[]> edges = new ArrayList<>();
                    for (int k = 0; k < numEdges; k++) {
                        long fromNodeId = in.readLong();
                        long toNodeId = in.readLong();
                        long weight = in.readLong();
                        edges.add(new long[]{fromNodeId, toNodeId, weight});
                    }

                    OpenRouterNode node = new OpenRouterNode(id, latitude, longitude);
                    for (long[] edge : edges) {
                        node.addEdge(edge);
                    }
                    nodeMap.put(id, node);
                }

                tileNodeMap.put(tileCoord, nodeMap);
            }

            // Rückgabe des vollständigen Graphen
            return new OpenRouterGraph(tileNodeMap);
        }
    }



    public static OpenRouterGraph loadGraph(File file, double minLat, double maxLat, double minLon, double maxLon) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            // Lies die Anzahl der Tiles
            int numTiles = in.readInt();
            Map<OpenRouterGraph.TileMapCoordinate, Map<Long, OpenRouterNode>> tileNodeMap = new HashMap<>();

            // Lade nur die Tiles, die innerhalb der BoundingBox liegen
            for (int i = 0; i < numTiles; i++) {
                short x = in.readShort();
                short y = in.readShort();
                OpenRouterGraph.TileMapCoordinate tileCoord = new OpenRouterGraph.TileMapCoordinate(x, y);

                // Überprüfe, ob diese Kachel innerhalb der BoundingBox liegt
                if (isTileInBoundingBox(tileCoord, minLat, maxLat, minLon, maxLon)) {
                    int numNodes = in.readInt();
                    Map<Long, OpenRouterNode> nodeMap = new HashMap<>();

                    // Lade alle Knoten in dieser Kachel
                    for (int j = 0; j < numNodes; j++) {
                        long id = in.readLong();
                        double latitude = in.readDouble();
                        double longitude = in.readDouble();

                        // Lade Kanten für den Knoten
                        int numEdges = in.readInt();
                        List<long[]> edges = new ArrayList<>();
                        for (int k = 0; k < numEdges; k++) {
                            long fromNodeId = in.readLong();
                            long toNodeId = in.readLong();
                            long weight = in.readLong();
                            edges.add(new long[]{fromNodeId, toNodeId, weight});
                        }

                        OpenRouterNode node = new OpenRouterNode(id, latitude, longitude);
                        for (long[] edge : edges) {;
                            node.addEdge(edge);
                        }
                        nodeMap.put(id, node);
                    }

                    tileNodeMap.put(tileCoord, nodeMap);
                } else {
                    // Überspringe die Knoten und Kanten der Kacheln außerhalb der BoundingBox
                    int numNodes = in.readInt();
                    for (int j = 0; j < numNodes; j++) {
                        in.readLong(); // ID
                        in.readDouble(); // Latitude
                        in.readDouble(); // Longitude
                        int numEdges = in.readInt();
                        for (int k = 0; k < numEdges; k++) {
                            in.readLong(); // fromNodeId
                            in.readLong(); // toNodeId
                            in.readLong(); // weight
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
