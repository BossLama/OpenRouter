package de.riemerjonas.openrouter.graph.algorithm;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
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

            for (OpenRouterEdge edge : currentNode.getEdges())
            {
                long neighborId = edge.getToNodeId();
                long weight = edge.getWeight();
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

    public static List<Long> findRouteAStar(OpenRouterGraph graph, double startLat, double startLon, double endLat, double endLon) {
        OpenRouterNode startNode = graph.findClosestNode(startLat, startLon, 50);
        OpenRouterNode endNode = graph.findClosestNode(endLat, endLon, 50);

        if (startNode == null || endNode == null) {
            OpenRouterLog.error(TAG, "Start or destination node not found.");
            return Collections.emptyList();
        }

        // Maps für die Kosten und die vorherigen Knoten
        Map<Long, Long> gCosts = new HashMap<>();
        Map<Long, Long> fCosts = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();

        // PriorityQueue, die nach f-Kosten sortiert ist
        PriorityQueue<long[]> queue = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));

        // Startknoten initialisieren
        gCosts.put(startNode.getId(), 0L);
        fCosts.put(startNode.getId(), haversine(startNode.getLatitude(),
                startNode.getLongitude(), endNode.getLatitude(), endNode.getLongitude()));
        queue.add(new long[]{startNode.getId(), fCosts.get(startNode.getId())});

        while (!queue.isEmpty()) {
            long[] current = queue.poll();
            long currentNodeId = current[0];

            // Wenn der Zielknoten erreicht wurde, den Pfad rekonstruieren
            if (currentNodeId == endNode.getId()) {
                return reconstructPath(previous, endNode.getId());
            }

            OpenRouterNode currentNode = graph.getNode(currentNodeId);
            if (currentNode == null) continue;

            // Für alle benachbarten Knoten
            for (OpenRouterEdge edge : currentNode.getEdges()) {
                long neighborId = edge.getToNodeId();
                long weight = edge.getWeight();

                // g-Kosten für den benachbarten Knoten
                long tentativeGCost = gCosts.get(currentNodeId) + weight;

                if (tentativeGCost < gCosts.getOrDefault(neighborId, Long.MAX_VALUE)) {
                    // Update der g- und f-Kosten
                    gCosts.put(neighborId, tentativeGCost);
                    long heuristic = haversine(graph.getNode(neighborId).getLatitude(),
                            graph.getNode(neighborId).getLongitude(), endNode.getLatitude(), endNode.getLongitude());
                    long fCost = tentativeGCost + heuristic;

                    fCosts.put(neighborId, fCost);
                    previous.put(neighborId, currentNodeId);
                    queue.add(new long[]{neighborId, fCost});
                }
            }
        }

        // Falls kein Pfad gefunden wurde
        OpenRouterLog.error(TAG, "No path found from " + startNode.getId() + " to " + endNode.getId());
        return Collections.emptyList();
    }

    private static List<Long> reconstructPath(Map<Long, Long> previous, long endNodeId) {
        List<Long> path = new LinkedList<>();
        Long current = endNodeId;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        return path;
    }

    // Haversine-Formel zur Berechnung der Luftlinienentfernung zwischen zwei Punkten (in Metern)
    private static long haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Radius der Erde in Metern
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (long) (R * c); // Ergebnis in Metern
    }


}
