package de.riemerjonas.openrouter.graph;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.OpenRouterNode;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ORGraphSaver {

    public static void save(OpenRouterGraph graph, File output) {
        List<OpenRouterNode> allNodes = graph.getTileMap()
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();

        // Map von Node-ID zu Index (z. B. für Edges oder Tiles)
        Map<Long, Integer> nodeIdToIndex = new HashMap<>(allNodes.size());
        for (int i = 0; i < allNodes.size(); i++) {
            nodeIdToIndex.put(allNodes.get(i).getId(), i);
        }

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(output))))) {

            // --- 1. Schreibe Node-IDs (einmalig) ---
            out.writeInt(allNodes.size());
            for (OpenRouterNode node : allNodes) {
                out.writeLong(node.getId());
            }

            // --- 2. Schreibe Node-Daten (nur lat/lon) ---
            for (OpenRouterNode node : allNodes) {
                out.writeInt(node.getLatitude());
                out.writeInt(node.getLongitude());
            }

            // --- 3. Tile-Zuordnung ---
            Map<OpenRouterGraph.TileCoordinates, List<OpenRouterNode>> tileMap = graph.getTileMap();
            out.writeInt(tileMap.size());

            for (Map.Entry<OpenRouterGraph.TileCoordinates, List<OpenRouterNode>> entry : tileMap.entrySet()) {
                OpenRouterGraph.TileCoordinates tile = entry.getKey();
                List<OpenRouterNode> tileNodes = entry.getValue();

                out.writeShort(tile.x());
                out.writeShort(tile.y());
                out.writeInt(tileNodes.size());

                for (OpenRouterNode node : tileNodes) {
                    int index = nodeIdToIndex.get(node.getId());
                    out.writeInt(index);
                }
            }

            // --- 4. Kanten speichern (nur Indices) ---
            List<OpenRouterEdge> edges = graph.getEdges();
            out.writeInt(edges.size());

            for (OpenRouterEdge edge : edges) {
                int fromIndex = nodeIdToIndex.get(edge.getFromNodeId());
                int toIndex = nodeIdToIndex.get(edge.getToNodeId());
                out.writeInt(fromIndex);
                out.writeInt(toIndex);
            }

        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Speichern des Graphen", e);
        }
    }

    public static OpenRouterGraph load(File input) {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new GZIPInputStream(new FileInputStream(input))))) {

            // --- 1. Lese Node-IDs ---
            int nodeCount = in.readInt();
            long[] nodeIds = new long[nodeCount];
            for (int i = 0; i < nodeCount; i++) {
                nodeIds[i] = in.readLong();
            }

            // --- 2. Lese Node-Daten (lat/lon) ---
            OpenRouterNode[] nodes = new OpenRouterNode[nodeCount];
            for (int i = 0; i < nodeCount; i++) {
                int lat = in.readInt();
                int lon = in.readInt();
                nodes[i] = new OpenRouterNode(nodeIds[i], lat, lon);
            }

            // --- 3. Lese Tile-Zuordnung ---
            int tileCount = in.readInt();
            HashMap<OpenRouterGraph.TileCoordinates, List<OpenRouterNode>> tileMap = new HashMap<>();

            for (int i = 0; i < tileCount; i++) {
                short x = in.readShort();
                short y = in.readShort();
                int tileNodeCount = in.readInt();

                List<OpenRouterNode> tileNodes = new ArrayList<>(tileNodeCount);
                for (int j = 0; j < tileNodeCount; j++) {
                    int nodeIndex = in.readInt();
                    tileNodes.add(nodes[nodeIndex]);
                }

                tileMap.put(new OpenRouterGraph.TileCoordinates(x, y), tileNodes);
            }

            // --- 4. Lese Kanten ---
            int edgeCount = in.readInt();
            List<OpenRouterEdge> edges = new ArrayList<>(edgeCount);

            for (int i = 0; i < edgeCount; i++) {
                int fromIndex = in.readInt();
                int toIndex = in.readInt();
                long fromId = nodes[fromIndex].getId();
                long toId = nodes[toIndex].getId();
                edges.add(new OpenRouterEdge(fromId, toId));
            }

            return new OpenRouterGraph(edges, tileMap);

        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden des Graphen", e);
        }
    }

}
