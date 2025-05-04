package de.riemerjonas.openrouter.graph.algorithm;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.core.OpenRouterRoute;
import de.riemerjonas.openrouter.core.iface.OpenRouterCoordinate;
import de.riemerjonas.openrouter.core.iface.OpenRouterRoutingProfile;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;

import java.util.*;

public class ORGraphRouteAStarAlgorithm {

    private static final String TAG = "ORGraphRouteAStarAlgorithm";

    public static OpenRouterRoute findRoute(OpenRouterGraph graph,
                                            OpenRouterCoordinate start,
                                            OpenRouterCoordinate end,
                                            OpenRouterRoutingProfile profile)
    {
        OpenRouterNode nodeStart = graph.findClosestNode(start, 150);
        OpenRouterNode nodeEnd = graph.findClosestNode(end, 150);

        if (nodeStart == null || nodeEnd == null)
        {
            OpenRouterLog.i(TAG, "Start or end node not found");
            return null;
        }

        ArrayList<OpenRouterNode> allNodes = graph.getAllNodes();
        ArrayList<OpenRouterEdge> edges = graph.getEdges();

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
}
