package de.riemerjonas.openrouter.graph;

import de.riemerjonas.openrouter.core.OpenRouterBoundingBox;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.graph.algorithm.ORGraphRouteAlgorithm;
import de.riemerjonas.openrouter.graph.core.ORGraphBuilder;
import de.riemerjonas.openrouter.graph.core.ORGraphFileHandler;

import java.io.File;
import java.io.IOException;
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

    /**
     * Find the closest node to a given coordinate
     * @param latitude Latitude of the coordinate
     * @param longitude Longitude of the coordinate
     * @param maxDistanceMeters Maximum distance in meters
     * @return The closest node within the specified distance, or null if none found
     */
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

    /**
     * Find a route between two coordinates
     * @param startLat The latitude of the start point
     * @param startLon The longitude of the start point
     * @param endLat The latitude of the end point
     * @param endLon The longitude of the end point
     * @return A list of node IDs representing the route
     */
    public List<Long> findRoute(
            double startLat, double startLon,
            double endLat, double endLon
    ) {
        return ORGraphRouteAlgorithm.findRoute(this, startLat, startLon, endLat, endLon);
    }

    /**
     * Save the graph to a file
     * @param file The file to save the graph to
     */
    public void save(File file)
    {
        try
        {
            ORGraphFileHandler.saveGraph(this, file);
        }
        catch (IOException e)
        {
            OpenRouterLog.error(TAG, "Error while saving graph: " + e.getMessage());
        }
    }

    /**
     * Load a graph from a file
     * @param file The file to load the graph from
     * @return The loaded graph
     */
    public static OpenRouterGraph load(File file)
    {
        try
        {
            return ORGraphFileHandler.loadFullGraph(file);
        }
        catch (IOException e)
        {
            OpenRouterLog.error(TAG, "Error while loading graph: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load a graph from a PBF file
     * @param file The PBF file to read
     * @param boundingBox The bounding box to load
     * @return The graph built from the PBF file
     */
    public static OpenRouterGraph load(File file, OpenRouterBoundingBox boundingBox)
    {
        try
        {
            return ORGraphFileHandler.loadGraph
                    (file, boundingBox.getMinLat(), boundingBox.getMaxLat(), boundingBox.getMinLon(), boundingBox.getMaxLon());
        }
        catch (IOException e)
        {
            OpenRouterLog.error(TAG, "Error while loading graph from PBF: " + e.getMessage());
            return null;
        }
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
