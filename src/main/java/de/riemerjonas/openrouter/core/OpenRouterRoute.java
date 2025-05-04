package de.riemerjonas.openrouter.core;

import java.util.ArrayList;

public class OpenRouterRoute
{
    private final OpenRouterNode startNode;
    private final OpenRouterNode endNode;
    private final ArrayList<OpenRouterNode> routeNodes;

    public OpenRouterRoute(OpenRouterNode startNode, OpenRouterNode endNode, ArrayList<OpenRouterNode> routeNodes)
    {
        this.startNode = startNode;
        this.endNode = endNode;
        this.routeNodes = routeNodes;
    }

    public OpenRouterNode getStartNode()
    {
        return startNode;
    }

    public OpenRouterNode getEndNode()
    {
        return endNode;
    }

    public ArrayList<OpenRouterNode> getRouteNodes()
    {
        return routeNodes;
    }

    public int getRouteNodeCount()
    {
        return routeNodes.size();
    }

    public double getTotalDistance()
    {
        double totalDistance = 0;
        OpenRouterNode lastNode = startNode;
        for (OpenRouterNode node : routeNodes)
        {
            totalDistance += node.distanceMeter(lastNode);
            lastNode = node;
        }
        return totalDistance;
    }
}
