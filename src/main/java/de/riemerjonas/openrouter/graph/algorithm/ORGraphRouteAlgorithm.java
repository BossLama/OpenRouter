package de.riemerjonas.openrouter.graph.algorithm;

import de.riemerjonas.openrouter.core.OpenRouterGeoPoint;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;

import java.util.*;

public class ORGraphRouteAlgorithm
{
    private static final String TAG = "ORGraphRouteAlgorithm";

    /**
     * Finds a route between two geographical points using Dijkstra's algorithm.
     * @param graph The graph to search
     * @param start The start point
     * @param end The end point
     * @return A list of node IDs representing the route from start to end
     */
    public static List<Long> findRoute(OpenRouterGraph graph, OpenRouterGeoPoint start, OpenRouterGeoPoint end)
    {
        return findRoute(graph, start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
    }

    /**
     * Finds a route between two geographical points using Dijkstra's algorithm.
     * @param graph The graph to search
     * @param startLat The latitude of the start point
     * @param startLon The longitude of the start point
     * @param endLat The latitude of the end point
     * @param endLon The longitude of the end point
     * @return A list of node IDs representing the route from start to end
     */
    public static List<Long> findRoute(OpenRouterGraph graph, double startLat, double startLon, double endLat, double endLon)
    {
        // 1. Find next nodes
        OpenRouterNode startNode = graph.findClosestNode(startLat, startLon, 50);
        OpenRouterNode endNode = graph.findClosestNode(endLat, endLon, 50);

        if (startNode == null || endNode == null) {
            OpenRouterLog.error(TAG, "Start or destination node not found.");
            return Collections.emptyList();
        }

        // 2. Dijkstra
        Map<Long, Long> distances = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();
        PriorityQueue<long[]> queue = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));

        distances.put(startNode.getId(), 0L);
        queue.add(new long[]{startNode.getId(), 0});

        while (!queue.isEmpty())
        {
            long[] current = queue.poll();
            long currentNodeId = current[0];
            int currentDistance = (int) current[1];

            if (currentNodeId == endNode.getId()) break;

            OpenRouterNode currentNode = graph.getNode(currentNodeId);
            if (currentNode == null) continue;

            for (OpenRouterNode.Edge edge : currentNode.getEdges())
            {
                long neighborId = edge.to();
                long weight = edge.weight();
                long newDist = currentDistance + weight;

                if (newDist < distances.getOrDefault(neighborId, Long.MAX_VALUE))
                {
                    distances.put(neighborId, newDist);
                    previous.put(neighborId, currentNodeId);
                    queue.add(new long[]{neighborId, newDist});}
            }
        }

        // 3. Reconstruct path
        List<Long> path = new LinkedList<>();
        Long current = endNode.getId();
        while (current != null && previous.containsKey(current))
        {
            path.add(0, current);
            current = previous.get(current);
        }

        if (!path.isEmpty() && path.get(0) != startNode.getId())
        {
            path.add(0, startNode.getId());
        }

        if (path.isEmpty())
        {
            OpenRouterLog.error(TAG, "No path found from " + startNode.getId() + " to " + endNode.getId());
        }
        else
        {
            OpenRouterLog.info(TAG, "Path found from " + startNode.getId() + " to " + endNode.getId());
            OpenRouterLog.info(TAG, "Path length: " + path.size());
        }

        return path;
    }
}
