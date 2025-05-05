package de.riemerjonas.openrouter.core;

import de.riemerjonas.openrouter.core.ifaces.IGeoCoordinate;

/**
 * Represents a node in the OpenRouter system.
 * Each node has a unique ID and a coordinate represented by an OpenRouterPoint.
 */
public class OpenRouterNode implements IGeoCoordinate
{
    private final int id;
    private final OpenRouterPoint coordinate;

    /**
     * Creates a new OpenRouterNode with the given ID and coordinate.
     * @param id the ID of the node
     * @param coordinate the coordinate of the node
     */
    public OpenRouterNode(int id, OpenRouterPoint coordinate)
    {
        this.id = id;
        this.coordinate = coordinate;
    }

    /**
     * Creates a new OpenRouterNode with the given ID and coordinate.
     * @param id the ID of the node
     * @param latitude the latitude of node in degrees
     * @param longitude the longitude of node in degrees
     */
    public OpenRouterNode(int id, double latitude, double longitude)
    {
        this.id = id;
        this.coordinate = new OpenRouterPoint(latitude, longitude);
    }

    /**
     * Returns the ID of this node.
     * @return the ID of this node
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns the coordinate of this node.
     * @return the coordinate of this node
     */
    public OpenRouterPoint getCoordinate()
    {
        return coordinate;
    }

    /**
     * Returns the latitude of this node in degrees.
     * @return the latitude of this node in degrees
     */
    public double distanceTo(OpenRouterNode other)
    {
        return coordinate.distanceTo(other.coordinate);
    }

    @Override
    public int getLatitudeE6() {
        return coordinate.getLatitudeE6();
    }

    @Override
    public int getLongitudeE6() {
        return coordinate.getLongitudeE6();
    }

    /**
     * Returns the node as a byte array.
     * @return the node as a byte array
     */
    public byte[] toByteArray()
    {
        byte[] bytes = new byte[12];

        bytes[0] = (byte) (id >> 24);
        bytes[1] = (byte) (id >> 16);
        bytes[2] = (byte) (id >> 8);
        bytes[3] = (byte) id;

        int lat = coordinate.getLatitudeE6();
        bytes[4] = (byte) (lat >> 24);
        bytes[5] = (byte) (lat >> 16);
        bytes[6] = (byte) (lat >> 8);
        bytes[7] = (byte) lat;

        int lon = coordinate.getLongitudeE6();
        bytes[8]  = (byte) (lon >> 24);
        bytes[9]  = (byte) (lon >> 16);
        bytes[10] = (byte) (lon >> 8);
        bytes[11] = (byte) lon;

        return bytes;
    }


    /**
     * Creates a new OpenRouterNode from a byte array.
     * @param bytes the byte array
     * @return the OpenRouterNode
     */
    public static OpenRouterNode fromByteArray(byte[] bytes)
    {
        int id = ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);

        int latitudeE6 = ((bytes[4] & 0xFF) << 24) |
                ((bytes[5] & 0xFF) << 16) |
                ((bytes[6] & 0xFF) << 8) |
                (bytes[7] & 0xFF);

        int longitudeE6 = ((bytes[8] & 0xFF) << 24) |
                ((bytes[9] & 0xFF) << 16) |
                ((bytes[10] & 0xFF) << 8) |
                (bytes[11] & 0xFF);

        return new OpenRouterNode(id, new OpenRouterPoint(latitudeE6, longitudeE6));
    }

}
