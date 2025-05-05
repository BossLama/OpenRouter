package de.riemerjonas.openrouter.core;

import de.riemerjonas.openrouter.core.ifaces.IGeoCoordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenRouterViewBox
{
    private int minLatitudeE6;
    private int minLongitudeE6;
    private int maxLatitudeE6;
    private int maxLongitudeE6;

    /**
     * Creates a new OpenRouterViewBox with the given coordinates.
     * @param minLatitudeE6 is the minimum latitude in E6 format
     * @param minLongitudeE6 is the minimum longitude in E6 format
     * @param maxLatitudeE6 is the maximum latitude in E6 format
     * @param maxLongitudeE6 is the maximum longitude in E6 format
     */
    public OpenRouterViewBox(int minLatitudeE6, int minLongitudeE6, int maxLatitudeE6, int maxLongitudeE6)
    {
        this.minLatitudeE6 = minLatitudeE6;
        this.minLongitudeE6 = minLongitudeE6;
        this.maxLatitudeE6 = maxLatitudeE6;
        this.maxLongitudeE6 = maxLongitudeE6;
    }

    /**
     * Creates a new OpenRouterViewBox with the given coordinates.
     * The coordinates have no specific order.
     * @param cord1 is the first coordinate
     * @param cord2 is the second coordinate
     */
    public OpenRouterViewBox(IGeoCoordinate cord1, IGeoCoordinate cord2)
    {
        this.minLatitudeE6 = Math.min(cord1.getLatitudeE6(), cord2.getLatitudeE6());
        this.minLongitudeE6 = Math.min(cord1.getLongitudeE6(), cord2.getLongitudeE6());
        this.maxLatitudeE6 = Math.max(cord1.getLatitudeE6(), cord2.getLatitudeE6());
        this.maxLongitudeE6 = Math.max(cord1.getLongitudeE6(), cord2.getLongitudeE6());
    }

    /**
     * Checks if the given coordinates are within the view box.
     * @param latitudeE6 is the latitude in E6 format
     * @param longitudeE6 is the longitude in E6 format
     * @return true if the coordinates are within the view box, false otherwise
     */
    public boolean contains(int latitudeE6, int longitudeE6)
    {
        return latitudeE6 >= minLatitudeE6 && latitudeE6 <= maxLatitudeE6 &&
               longitudeE6 >= minLongitudeE6 && longitudeE6 <= maxLongitudeE6;
    }

    /**
     * Checks if the given coordinate is within the view box.
     * @param coordinate is the coordinate
     * @return true if the coordinate is within the view box, false otherwise
     */
    public boolean contains(IGeoCoordinate coordinate)
    {
        return contains(coordinate.getLatitudeE6(), coordinate.getLongitudeE6());
    }

    /**
     * Checks if the given latitude and longitude are within the view box.
     * @param latitude is the latitude in degrees
     * @param longitude is the longitude in degrees
     * @return true if the coordinates are within the view box, false otherwise
     */
    public boolean contains(double latitude, double longitude)
    {
        return contains((int) (latitude * 1E6), (int) (longitude * 1E6));
    }

    /**
     * Returns the minimum latitude in degrees.
     * @return the minimum latitude in degrees
     */
    public double getMinLatitude()
    {
        return minLatitudeE6 / 1E6;
    }

    /**
     * Returns the minimum longitude in degrees.
     * @return the minimum longitude in degrees
     */
    public double getMinLongitude()
    {
        return minLongitudeE6 / 1E6;
    }

    /**
     * Returns the maximum latitude in degrees.
     * @return the maximum latitude in degrees
     */
    public double getMaxLatitude()
    {
        return maxLatitudeE6 / 1E6;
    }

    /**
     * Returns the maximum longitude in degrees.
     * @return the maximum longitude in degrees
     */
    public double getMaxLongitude()
    {
        return maxLongitudeE6 / 1E6;
    }

    public List<OpenRouterEdge> filterEdges(List<OpenRouterEdge> edges, List<OpenRouterNode> nodes)
    {
        HashMap<Integer, OpenRouterNode> nodeMap = new HashMap<>();
        for (OpenRouterNode node : nodes)
        {
            nodeMap.put(node.getId(), node);
        }

        List<OpenRouterEdge> filteredEdges = new ArrayList<>();
        for (OpenRouterEdge edge : edges)
        {
            OpenRouterNode fromNode = nodeMap.get(edge.getFromID());
            OpenRouterNode toNode = nodeMap.get(edge.getToID());

            if (fromNode != null && toNode != null &&
                contains(fromNode.getCoordinate()) && contains(toNode.getCoordinate()))
            {
                filteredEdges.add(edge);
            }
        }

        return filteredEdges;
    }
}
