package de.riemerjonas.openrouter.graph;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.core.OpenRouterTileMap;
import de.riemerjonas.openrouter.core.ifaces.IGeoCoordinate;

import java.util.List;

public class OpenRouterGraph
{
    private final OpenRouterTileMap tileMap;
    private final List<OpenRouterEdge> edges;

    /**
     * Creates a new OpenRouterGraph with the given tile map.
     * @param tileMap the tile map
     */
    public OpenRouterGraph(OpenRouterTileMap tileMap, List<OpenRouterEdge> edges)
    {
        this.tileMap = tileMap;
        this.edges = edges;
    }

    /**
     * Creates a new OpenRouterGraph with the given nodes.
     * @param nodes the nodes
     */
    public OpenRouterGraph(List<OpenRouterNode> nodes, List<OpenRouterEdge> edges)
    {
        this.edges = edges;
        this.tileMap = OpenRouterTileMap.create(nodes);
    }

    /**
     * Returns the tile map.
     * @return the tile map
     */
    public OpenRouterTileMap getTileMap()
    {
        return tileMap;
    }

    /**
     * Returns the edges.
     * @return the edges
     */
    public List<OpenRouterEdge> getEdges()
    {
        return edges;
    }

    /**
     * Returns all nodes in the graph.
     * @return all nodes in the graph
     */
    public List<OpenRouterNode> getNodes()
    {
        return tileMap.getNodes();
    }

    /**
     * Returns the nearest node to the given latitude and longitude.
     * @param latitude is the latitude in degrees
     * @param longitude is the longitude in degrees
     * @return the nearest node
     */
    public OpenRouterNode getNearestNode(double latitude, double longitude)
    {
        return tileMap.getNearestNode(latitude, longitude);
    }

    /**
     * Returns the nearest node to the given coordinate.
     * @param coordinate is the coordinate
     * @return the nearest node
     */
    public OpenRouterNode getNearestNode(IGeoCoordinate coordinate)
    {
        return tileMap.getNearestNode(coordinate.getLatitude(), coordinate.getLongitude());
    }
}
