package de.riemerjonas.openrouter.graph.algorithm;

import de.riemerjonas.openrouter.core.*;
import de.riemerjonas.openrouter.core.ifaces.IGeoCoordinate;
import de.riemerjonas.openrouter.core.ifaces.IRoutingProfile;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;

import java.util.*;

public class ORGraphRouter {

    private static final String TAG = "ORGraphRouter";

    public static List<OpenRouterNode> route(IGeoCoordinate start, IGeoCoordinate end, OpenRouterGraph graph, IRoutingProfile profile)
    {
        return route(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude(), graph, profile);
    }
    public static List<OpenRouterNode> route(double latStart, double lonStart, double latEnd, double lonEnd, OpenRouterGraph graph, IRoutingProfile profile)
    {
        // Getting start and end nodes
        OpenRouterNode startNode = graph.getNearestNode(latStart, lonStart);
        OpenRouterNode endNode = graph.getNearestNode(latEnd, lonEnd);

        // Setting up a ViewBox with margin
        double margin = 0.2;
        double latMin = Math.min(latStart, latEnd) - margin;
        double latMax = Math.max(latStart, latEnd) + margin;
        double lonMin = Math.min(lonStart, lonEnd) - margin;
        double lonMax = Math.max(lonStart, lonEnd) + margin;
        OpenRouterPoint viewBoxPoint1 = new OpenRouterPoint(latMin, lonMin);
        OpenRouterPoint viewBoxPoint2 = new OpenRouterPoint(latMax, lonMax);
        OpenRouterViewBox viewBox = new OpenRouterViewBox(viewBoxPoint1, viewBoxPoint2);

        if (startNode == null || endNode == null)
        {
            OpenRouterLog.e(TAG, "Start or end node is null");
            return null;
        }
        else
        {
            OpenRouterLog.d(TAG, "Start node: " + startNode.getId() + " (" + startNode.getLatitude() + ", " + startNode.getLongitude() + ")");
            OpenRouterLog.d(TAG, "End node: " + endNode.getId() + " (" + endNode.getLatitude() + ", " + endNode.getLongitude() + ")");
        }


        // Prepare Dijkstra's algorithm
        List<OpenRouterNode> nodes = graph.getTileMap().getNodesInViewBox(viewBox);
        List<OpenRouterEdge> edges = viewBox.filterEdges(graph.getEdges(), nodes);

        OpenRouterLog.d(TAG, "Use " + nodes.size() + " nodes and " + edges.size() + " edges for routing.");

        Map<Integer, OpenRouterNode> nodeById = new HashMap<>();
        for (OpenRouterNode node : nodes)
        {
            nodeById.put(node.getId(), node);
        }

        Map<Integer, List<OpenRouterEdge>> edgesByFrom = new HashMap<>();
        for (OpenRouterEdge edge : edges)
        {
            edgesByFrom.computeIfAbsent(edge.getFromID(), k -> new ArrayList<>()).add(edge);
        }

        Map<Integer, Integer> previous = new HashMap<>(); // id → Vorgänger-ID
        Map<Integer, Double> distance = new HashMap<>();
        PriorityQueue<NodeEntry> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.priority));

        // Check for start and end nodes
        if (edgesByFrom.get(startNode.getId()) == null)
        {
            OpenRouterLog.e(TAG, "Start node has no outgoing edges!");
        }
        if (edgesByFrom.get(endNode.getId()) == null)
        {
            OpenRouterLog.e(TAG, "End node has no incoming edges?");
        }


        // Initialisierung
        distance.put(startNode.getId(), 0.0);
        queue.add(new NodeEntry(startNode.getId(), 0.0));

        while (!queue.isEmpty())
        {
            NodeEntry current = queue.poll();
            int currentId = current.nodeId;

            if (currentId == endNode.getId()) break;

            List<OpenRouterEdge> neighbors = edgesByFrom.getOrDefault(currentId, Collections.emptyList());
            for (OpenRouterEdge edge : neighbors)
            {
                int neighborId = edge.getToID();
                double cost = profile.getWeight(edge);

                double newDist = distance.get(currentId) + cost;
                if (newDist < distance.getOrDefault(neighborId, Double.POSITIVE_INFINITY))
                {
                    distance.put(neighborId, newDist);
                    previous.put(neighborId, currentId);
                    queue.add(new NodeEntry(neighborId, newDist));
                }
            }
        }

        // Weg rückverfolgen
        LinkedList<OpenRouterNode> path = new LinkedList<>();
        Integer current = endNode.getId();
        while (current != null && nodeById.containsKey(current))
        {
            path.addFirst(nodeById.get(current));
            current = previous.get(current);
        }

        return path.isEmpty() || !path.getFirst().equals(startNode) ? null : path;
    }

    // Hilfsklasse
    private static class NodeEntry {
        int nodeId;
        double priority;

        NodeEntry(int nodeId, double priority) {
            this.nodeId = nodeId;
            this.priority = priority;
        }
    }
}
