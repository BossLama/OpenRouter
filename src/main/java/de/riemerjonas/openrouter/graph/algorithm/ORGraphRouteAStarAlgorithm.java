package de.riemerjonas.openrouter.graph.algorithm;

import de.riemerjonas.openrouter.core.*;
import de.riemerjonas.openrouter.core.iface.OpenRouterCoordinate;
import de.riemerjonas.openrouter.core.iface.OpenRouterRoutingProfile;
import de.riemerjonas.openrouter.debug.ORTimeDebug;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;

import java.util.*;

public class ORGraphRouteAStarAlgorithm {

    private static final String TAG = "ORGraphRouteAStarAlgorithm";

    /*
    public static OpenRouterRoute findRoute(OpenRouterGraph graph,
                                            OpenRouterCoordinate start,
                                            OpenRouterCoordinate end,
                                            OpenRouterRoutingProfile profile)
    {

        OpenRouterBbox bbox = new OpenRouterBbox(
                start.getLatitude() - 1,
                start.getLongitude() - 1,
                end.getLatitude() + 1,
                end.getLongitude() + 1);
        OpenRouterNode nodeStart = graph.findClosestNode(start, 150);
        OpenRouterNode nodeEnd = graph.findClosestNode(end, 150);

        if (nodeStart == null || nodeEnd == null)
        {
            OpenRouterLog.i(TAG, "Start or end node not found");
            return null;
        }

        ORTimeDebug bboxNodeTime = new ORTimeDebug("NodesInBbox");
        bboxNodeTime.start();
        ArrayList<OpenRouterNode> allNodes = graph.getAllNodesInBbox(bbox);
        bboxNodeTime.printResult();
        if (allNodes.isEmpty())
        {
            OpenRouterLog.i(TAG, "No nodes found in bbox");
            return null;
        }

        ORTimeDebug bboxEdgeTime = new ORTimeDebug("EdgesInBbox");
        bboxEdgeTime.start();
        ArrayList<OpenRouterEdge> edges = graph.getEdgesInBbox(bbox);
        bboxEdgeTime.printResult();
        if (edges.isEmpty())
        {
            OpenRouterLog.i(TAG, "No edges found in bbox");
            return null;
        }

        OpenRouterLog.d(TAG, "Found " + allNodes.size() + " nodes and " + edges.size() + " edges in bbox");

        Map<Integer, OpenRouterNode> idToNode = new HashMap<>();
        for (OpenRouterNode node : allNodes)
        {
            idToNode.put(node.getDeltaId(), node);
        }

        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        PriorityQueue<int[]> queue = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        distances.put(nodeStart.getDeltaId(), 0);
        queue.add(new int[] { nodeStart.getDeltaId(), 0 });

        while (!queue.isEmpty())
        {
            int[] current = queue.poll();
            int currentId = current[0];
            int currentDist = current[1];

            if (currentId == nodeEnd.getDeltaId()) break;

            for (OpenRouterEdge edge : edges)
            {
                if (edge.getFromDeltaID() == currentId)
                {
                    int neighborId = edge.getToDeltaID();
                    int newDist = currentDist + profile.getWeight(edge);

                    if (newDist < distances.getOrDefault(neighborId, Integer.MAX_VALUE))
                    {
                        distances.put(neighborId, newDist);
                        previous.put(neighborId, currentId);
                        queue.add(new int[] { neighborId, newDist });
                    }
                }
            }
        }

        // Pfad rekonstruktieren
        ArrayList<OpenRouterNode> path = new ArrayList<>();
        Integer currentId = nodeEnd.getDeltaId();
        while (currentId != null) {
            OpenRouterNode node = idToNode.get(currentId);
            if (node == null) break;
            path.add(node);
            currentId = previous.get(currentId);
        }

        Collections.reverse(path);
        if (path.isEmpty() || path.get(0).getDeltaId() != nodeStart.getDeltaId())
        {
            OpenRouterLog.i(TAG, "No path found from start to end");
            return null;
        }
        else
        {
            OpenRouterLog.d(TAG, "Path found from start to end");
        }

        return new OpenRouterRoute(nodeStart, nodeEnd, path);
    }
     */

    public static OpenRouterRoute findRoute(OpenRouterGraph graph,
                                            OpenRouterCoordinate start,
                                            OpenRouterCoordinate end,
                                            OpenRouterRoutingProfile profile)
    {
        // Dynamisch kleinere Bounding Box bestimmen (1.5x Sicherheitspuffer)
        double latBuffer = Math.abs(start.getLatitude() - end.getLatitude()) * 1.5;
        double lonBuffer = Math.abs(start.getLongitude() - end.getLongitude()) * 1.5;
        OpenRouterBbox bbox = new OpenRouterBbox(
                Math.min(start.getLatitude(), end.getLatitude()) - latBuffer,
                Math.min(start.getLongitude(), end.getLongitude()) - lonBuffer,
                Math.max(start.getLatitude(), end.getLatitude()) + latBuffer,
                Math.max(start.getLongitude(), end.getLongitude()) + lonBuffer);

        OpenRouterNode nodeStart = graph.findClosestNode(start, 150);
        OpenRouterNode nodeEnd = graph.findClosestNode(end, 150);

        if (nodeStart == null || nodeEnd == null)
        {
            OpenRouterLog.i(TAG, "Start or end node not found");
            return null;
        }

        ORTimeDebug bboxNodeTime = new ORTimeDebug("NodesInBbox");
        bboxNodeTime.start();
        ArrayList<OpenRouterNode> allNodes = graph.getAllNodesInBbox(bbox);
        bboxNodeTime.printResult();
        if (allNodes.isEmpty())
        {
            OpenRouterLog.i(TAG, "No nodes found in bbox");
            return null;
        }

        ORTimeDebug bboxEdgeTime = new ORTimeDebug("EdgesInBbox");
        bboxEdgeTime.start();
        ArrayList<OpenRouterEdge> edges = graph.getEdgesInBbox(bbox);
        bboxEdgeTime.printResult();
        if (edges.isEmpty())
        {
            OpenRouterLog.i(TAG, "No edges found in bbox");
            return null;
        }

        OpenRouterLog.d(TAG, "Found " + allNodes.size() + " nodes and " + edges.size() + " edges in bbox");

        // Mapping DeltaID -> Node
        Map<Integer, OpenRouterNode> idToNode = new HashMap<>();
        for (OpenRouterNode node : allNodes) {
            idToNode.put(node.getDeltaId(), node);
        }

        // Adjazenzliste: FromDeltaID -> Liste ausgehender Kanten
        Map<Integer, List<OpenRouterEdge>> edgesFromNode = new HashMap<>();
        for (OpenRouterEdge edge : edges) {
            edgesFromNode
                    .computeIfAbsent(edge.getFromDeltaID(), k -> new ArrayList<>())
                    .add(edge);
        }

        // Dijkstra Initialisierung
        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        PriorityQueue<int[]> queue = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        int startId = nodeStart.getDeltaId();
        int endId = nodeEnd.getDeltaId();

        distances.put(startId, 0);
        queue.add(new int[] { startId, 0 });

        while (!queue.isEmpty())
        {
            int[] current = queue.poll();
            int currentId = current[0];
            int currentDist = current[1];

            if (currentId == endId) break;

            for (OpenRouterEdge edge : edgesFromNode.getOrDefault(currentId, Collections.emptyList())) {
                int neighborId = edge.getToDeltaID();
                int newDist = currentDist + profile.getWeight(edge);

                if (newDist < distances.getOrDefault(neighborId, Integer.MAX_VALUE)) {
                    distances.put(neighborId, newDist);
                    previous.put(neighborId, currentId);
                    queue.add(new int[] { neighborId, newDist });
                }
            }
        }

        // Pfad rekonstruieren
        ArrayList<OpenRouterNode> path = new ArrayList<>();
        Integer currentId = endId;
        while (currentId != null) {
            OpenRouterNode node = idToNode.get(currentId);
            if (node == null) break;
            path.add(node);
            currentId = previous.get(currentId);
        }

        Collections.reverse(path);
        if (path.isEmpty() || path.get(0).getDeltaId() != startId)
        {
            OpenRouterLog.i(TAG, "No path found from start to end");
            return null;
        }
        else
        {
            OpenRouterLog.d(TAG, "Path found from start to end");
        }

        return new OpenRouterRoute(nodeStart, nodeEnd, path);
    }

}
