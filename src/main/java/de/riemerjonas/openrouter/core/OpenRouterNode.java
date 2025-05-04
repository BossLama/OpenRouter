package de.riemerjonas.openrouter.core;

import de.riemerjonas.openrouter.core.iface.OpenRouterCoordinate;

public class OpenRouterNode implements OpenRouterCoordinate
{
    public static final double COORDINATE_PRECISION = 1E6;

    private final int deltaId;
    private final int latitude;
    private final int longitude;

    /**
     * Creates a new OpenRouterNode with the given deltaID, latitude and longitude.
     * @param deltaID the deltaID of the node
     * @param latitude the latitude of the node as a double
     * @param longitude the longitude of the node as a double
     */
    public OpenRouterNode(int deltaID, double latitude, double longitude)
    {
        this.deltaId = deltaID;
        this.latitude = (int) (latitude * COORDINATE_PRECISION);
        this.longitude = (int) (longitude * COORDINATE_PRECISION);
    }

    /**
     * Creates a new OpenRouterNode with the given deltaID, latitude and longitude.
     * @param deltaID the deltaID of the node
     * @param latitude the latitude of the node as an int
     * @param longitude the longitude of the node as an int
     */
    public OpenRouterNode(int deltaID, int latitude, int longitude)
    {
        this.deltaId = deltaID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the deltaID of the node.
     * @return the deltaID of the node
     */
    public int getDeltaId()
    {
        return deltaId;
    }

    /**
     * Returns the latitude of the node as a double.
     * @return the latitude of the node
     */
    public double getLatitude()
    {
        return latitude / COORDINATE_PRECISION;
    }

    /**
     * Returns the longitude of the node as a double.
     * @return the longitude of the node
     */
    public double getLongitude()
    {
        return longitude / COORDINATE_PRECISION;
    }

    /**
     * Returns the latitude of the node as an int.
     * @return the latitude of the node
     */
    public int getLatitudeInt()
    {
        return latitude;
    }

    /**
     * Returns the longitude of the node as an int.
     * @return the longitude of the node
     */
    public int getLongitudeInt()
    {
        return longitude;
    }


    /**
     * Returns the distance to the given latitude and longitude in meters.
     * @param latitude the latitude of the point
     * @param longitude the longitude of the point
     * @return the distance to the point in meters
     */
    public double distanceMeter(double latitude, double longitude)
    {
        double startLatitude = getLatitude();
        double startLongitude = getLongitude();
        double dLat = Math.toRadians(latitude - startLatitude);
        double dLon = Math.toRadians(longitude - startLongitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startLatitude)) * Math.cos(Math.toRadians(latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double radius = 6371e3;
        return radius * c;
    }

    /**
     * Returns the distance to the given OpenRouterNode in meters.
     * @param node the OpenRouterNode to compare to
     * @return the distance to the node in meters
     */
    public double distanceMeter(OpenRouterCoordinate node)
    {
        return distanceMeter(node.getLatitude(), node.getLongitude());
    }

    /**
     * Returns the object as a byte array.
     * @return the byte array representation of the object
     */
    public byte[] toByteArray()
    {
        byte[] bytes = new byte[12];
        int index = 0;

        // deltaId
        bytes[index++] = (byte) ((deltaId >> 24) & 0xFF);
        bytes[index++] = (byte) ((deltaId >> 16) & 0xFF);
        bytes[index++] = (byte) ((deltaId >> 8) & 0xFF);
        bytes[index++] = (byte) (deltaId & 0xFF);

        // latitude
        bytes[index++] = (byte) ((latitude >> 24) & 0xFF);
        bytes[index++] = (byte) ((latitude >> 16) & 0xFF);
        bytes[index++] = (byte) ((latitude >> 8) & 0xFF);
        bytes[index++] = (byte) (latitude & 0xFF);

        // longitude
        bytes[index++] = (byte) ((longitude >> 24) & 0xFF);
        bytes[index++] = (byte) ((longitude >> 16) & 0xFF);
        bytes[index++] = (byte) ((longitude >> 8) & 0xFF);
        bytes[index] = (byte) (longitude & 0xFF);

        return bytes;
    }

    /**
     * Returns the coordinate as a byte array.
     * @return the byte array representation of the coordinate
     */
    public byte[] toCoordinateByteArray()
    {
        byte[] bytes = new byte[8];
        int index = 0;

        // latitude
        bytes[index++] = (byte) ((latitude >> 24) & 0xFF);
        bytes[index++] = (byte) ((latitude >> 16) & 0xFF);
        bytes[index++] = (byte) ((latitude >> 8) & 0xFF);
        bytes[index++] = (byte) (latitude & 0xFF);

        // longitude
        bytes[index++] = (byte) ((longitude >> 24) & 0xFF);
        bytes[index++] = (byte) ((longitude >> 16) & 0xFF);
        bytes[index++] = (byte) ((longitude >> 8) & 0xFF);
        bytes[index] = (byte) (longitude & 0xFF);

        return bytes;
    }

    /**
     * Creates a new OpenRouterNode from the given byte array.
     * @param bytes the byte array representation of the object
     * @return the OpenRouterNode object
     */
    public static OpenRouterNode fromByteArray(byte[] bytes)
    {
        if (bytes.length != 12)
        {
            throw new IllegalArgumentException("Byte array must be 12 bytes long");
        }

        int deltaId = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        int latitude = ((bytes[4] & 0xFF) << 24) | ((bytes[5] & 0xFF) << 16) | ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
        int longitude = ((bytes[8] & 0xFF) << 24) | ((bytes[9] & 0xFF) << 16) | ((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF);

        return new OpenRouterNode(deltaId, latitude, longitude);
    }

    /**
     * Creates a new OpenRouterNode from the given byte array.
     * @param bytes the byte array representation of the object
     * @return the OpenRouterNode object
     */
    public static OpenRouterNode fromCoordinateByteArray(int deltaID, byte[] bytes)
    {
        if (bytes.length != 8)
        {
            throw new IllegalArgumentException("Byte array must be 8 bytes long");
        }

        int latitude = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        int longitude = ((bytes[4] & 0xFF) << 24) | ((bytes[5] & 0xFF) << 16) | ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);

        return new OpenRouterNode(deltaID, latitude, longitude);
    }

}
