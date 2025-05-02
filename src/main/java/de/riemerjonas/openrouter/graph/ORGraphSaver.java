package de.riemerjonas.openrouter.graph;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.OpenRouterNode;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class ORGraphSaver {

    public static void save(OpenRouterGraph graph) {
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
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream("graph.orbin"))))) {

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
}
