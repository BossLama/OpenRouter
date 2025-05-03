package de.riemerjonas.openrouter.graph;

import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.graph.algorithm.ORGraphRouteAlgorithm;
import de.riemerjonas.openrouter.graph.core.ORGraphBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenRouterGraph
{
    private static final String TAG = "OpenRouterGraph";
    public static final double TILE_SIZE = 0.01;
    private Map<TileMapCoordinate, Map<Long, OpenRouterNode>> tileNodeMap;


    public OpenRouterGraph(Map<TileMapCoordinate, Map<Long, OpenRouterNode>> tileNodeMap)
    {
        this.tileNodeMap = tileNodeMap;
    }

    /**
     * Get the tilemap
     * @return The tilemap
     */
    public Map<TileMapCoordinate, Map<Long, OpenRouterNode>> getTileNodeMap()
    {
        return tileNodeMap;
    }

    /**
     * Get a node by its ID
     * @param id The ID of the node
     * @return The node with the given ID, or null if not found
     */
    public OpenRouterNode getNode(long id)
    {
        for (Map<Long, OpenRouterNode> tile : tileNodeMap.values())
        {
            if (tile.containsKey(id))
            {
                return tile.get(id);
            }
        }
        return null;
    }

    /**
     * Get all nodes in the graph
     * @return A list of all nodes in the graph
     */
    public ArrayList<OpenRouterNode> getAllNodes()
    {
        ArrayList<OpenRouterNode> allNodes = new ArrayList<>();
        for (Map<Long, OpenRouterNode> tile : tileNodeMap.values())
        {
            allNodes.addAll(tile.values());
        }
        return allNodes;
    }

    public OpenRouterNode findClosestNode(double latitude, double longitude, double maxDistanceMeters)
    {
        TileMapCoordinate centerTile = OpenRouterGraph.getTileMapCoordinate(latitude, longitude);

        OpenRouterNode closestNode = null;
        double minDistance = Double.MAX_VALUE;

        // Nachbarn durchsuchen (3x3 Grid: center + angrenzende Tiles)
        for (short dx = -1; dx <= 1; dx++) {
            for (short dy = -1; dy <= 1; dy++) {
                TileMapCoordinate tile = new TileMapCoordinate(
                        (short) (centerTile.x() + dx),
                        (short) (centerTile.y() + dy)
                );

                Map<Long, OpenRouterNode> nodesInTile = tileNodeMap.get(tile);
                if (nodesInTile == null) continue;

                for (OpenRouterNode node : nodesInTile.values()) {
                    double distance = node.getDistanceTo(latitude, longitude);
                    if (distance < minDistance && distance <= maxDistanceMeters) {
                        minDistance = distance;
                        closestNode = node;
                    }
                }
            }
        }
        return closestNode;
    }

    public List<Long> findRoute(
            double startLat, double startLon,
            double endLat, double endLon
    ) {
        return ORGraphRouteAlgorithm.findRoute(this, startLat, startLon, endLat, endLon);
    }

    /**
     * Build a graph from a PBF file
     * @param file The PBF file to read
     * @return The graph built from the PBF file
     */
    public static OpenRouterGraph buildFromPbf(File file)
    {
        return ORGraphBuilder.buildFromPbf(file);
    }

    /**
     * A record to represent a tile map coordinate
     * @param x Index of the tile in the x direction
     * @param y Index of the tile in the y direction
     */
    public static record TileMapCoordinate(short x, short y){}

    /**
     * Get the tile map coordinate for a given latitude and longitude
     * @param latitude Latitude of the coordinate
     * @param longitude Longitude of the coordinate
     * @return The tile map coordinate
     */
    public static TileMapCoordinate getTileMapCoordinate(double latitude, double longitude)
    {
        short x = (short) ((longitude + 180) / TILE_SIZE);
        short y = (short) ((latitude + 90) / TILE_SIZE);
        return new TileMapCoordinate(x, y);
    }





}
