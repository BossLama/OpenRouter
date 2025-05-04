package de.riemerjonas.openrouter.graph;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.core.iface.OpenRouterCoordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class OpenRouterGraph
{
    private static final String TAG = "OpenRouterGraph";
    private static final double TILE_SIZE = 0.01;
    private final long baseId;
    private final HashMap<Short, ArrayList<byte[]>> tileMap;
    private final ArrayList<byte[]> edges;

    public OpenRouterGraph(ArrayList<OpenRouterNode> nodes, ArrayList<byte[]> edges, long baseId)
    {
        this.edges = edges;
        this.tileMap = new HashMap<>();
        this.baseId = baseId;

        for(OpenRouterNode node : nodes)
        {
            short tileCoordinate = getTileCoordinate(node.getLatitude(), node.getLongitude());
            byte[] nodeBytes = node.toByteArray();
            if(!tileMap.containsKey(tileCoordinate))
            {
                tileMap.put(tileCoordinate, new ArrayList<>());
                tileMap.get(tileCoordinate).add(nodeBytes);
            }
            else
            {
                tileMap.get(tileCoordinate).add(nodeBytes);
            }
        }
        OpenRouterLog.i(TAG, "Created graph with " + tileMap.size() + " tiles");
    }

    public OpenRouterGraph(HashMap<Short, ArrayList<byte[]>> tileMap, ArrayList<byte[]> edges, long baseId)
    {
        this.edges = edges;
        this.tileMap = tileMap;
        this.baseId = baseId;
    }

    /**
     * Returns a list of all nodes in the graph.
     * @return a list of all nodes in the graph
     */
    public ArrayList<OpenRouterNode> getAllNodes()
    {
        ArrayList<OpenRouterNode> allNodes = new ArrayList<>();
        for(short tileCoordinate : tileMap.keySet())
        {
            ArrayList<byte[]> nodes = tileMap.get(tileCoordinate);
            for(byte[] nodeBytes : nodes)
            {
                OpenRouterNode node = OpenRouterNode.fromByteArray(nodeBytes);
                allNodes.add(node);
            }
        }
        return allNodes;
    }

    /**
     * Returns a node with the given ID.
     * @param id the ID of the node
     * @return the node with the given ID
     */
    public OpenRouterNode getNodeByID(long id)
    {
        for(short tileCoordinate : tileMap.keySet())
        {
            ArrayList<byte[]> nodes = tileMap.get(tileCoordinate);
            for(byte[] nodeBytes : nodes)
            {
                OpenRouterNode node = OpenRouterNode.fromByteArray(nodeBytes);
                if((node.getDeltaId() + baseId) == id)
                {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Returns all edges in the graph.
     * @return all edges in the graph
     */
    public ArrayList<OpenRouterEdge> getEdges()
    {
        ArrayList<OpenRouterEdge> openRouterEdges = new ArrayList<>();
        for(byte[] edgeBytes : edges)
        {
            OpenRouterEdge edge = OpenRouterEdge.fromByteArray(edgeBytes);
            openRouterEdges.add(edge);
        }
        return openRouterEdges;
    }

    /**
     * Returns a node with the given deltaID
     * @param deltaID the deltaID of the node
     * @return the node with the given deltaID
     */
    public OpenRouterNode getNodeByDeltaID(int deltaID)
    {
        for(short tileCoordinate : tileMap.keySet())
        {
            ArrayList<byte[]> nodes = tileMap.get(tileCoordinate);
            for(byte[] nodeBytes : nodes)
            {
                OpenRouterNode node = OpenRouterNode.fromByteArray(nodeBytes);
                if(node.getDeltaId() == deltaID)
                {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Finds the closest node to the given latitude and longitude.
     * @param latitude the latitude of the node
     * @param longitude the longitude of the node
     * @return
     */
    public OpenRouterNode findClosestNode(double latitude, double longitude, int maxDistance)
    {
        short tileCoordinate = getTileCoordinate(latitude, longitude);
        ArrayList<byte[]> nodes = tileMap.get(tileCoordinate);
        if(nodes == null)
        {
            OpenRouterLog.i(TAG, "No nodes found in tile " + tileCoordinate);
            return null;
        }
        OpenRouterNode closestNode = null;
        double closestDistance = Double.MAX_VALUE;
        for(byte[] nodeBytes : nodes)
        {
            OpenRouterNode node = OpenRouterNode.fromByteArray(nodeBytes);
            double distance = node.distanceMeter(latitude, longitude);
            if(distance < closestDistance && distance < maxDistance)
            {
                closestDistance = distance;
                closestNode = node;
            }
        }
        if(closestNode == null)
        {
            OpenRouterLog.i(TAG, "No closest found in tile " + tileCoordinate);
            return null;
        }
        OpenRouterLog.i(TAG, "Found closest node " + closestNode.getDeltaId() + " with distance " + closestDistance);
        return closestNode;
    }

    /**
     * Finds the closest node to the given coordinate.
     * @param coordinate the coordinate of the node
     * @return the closest node
     */
    public OpenRouterNode findClosestNode(OpenRouterCoordinate coordinate, int maxDistance)
    {
        return findClosestNode(coordinate.getLatitude(), coordinate.getLongitude(), maxDistance);
    }

    /**
     * Converts the given latitude and longitude to tile coordinates.
     * The latitude is represented as a byte and the longitude as a short.
     * @param latitude the latitude of the tile
     * @param longitude the longitude of the tile
     * @return the tile coordinates
     */
    public static short getTileCoordinate(double latitude, double longitude)
    {
        int lat = (int) (latitude / TILE_SIZE);
        int lon = (int) (longitude / TILE_SIZE);
        return (short) ((lat << 8) | (lon & 0xFF));
    }
}
