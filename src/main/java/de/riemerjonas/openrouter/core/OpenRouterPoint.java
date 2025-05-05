package de.riemerjonas.openrouter.core;

import de.riemerjonas.openrouter.core.ifaces.IGeoCoordinate;

public class OpenRouterPoint implements IGeoCoordinate
{
    private final int latitudeE6;
    private final int longitudeE6;

    /**
     * Creates a new OpenRouterPoint with the given latitude and longitude in E6 format.
     * @param latitudeE6 the latitude in E6 format
     * @param longitudeE6 the longitude in E6 format
     */
    public OpenRouterPoint(int latitudeE6, int longitudeE6)
    {
        this.latitudeE6 = latitudeE6;
        this.longitudeE6 = longitudeE6;
    }

    /**
     * Creates a new OpenRouterPoint with the given latitude and longitude in degrees.
     * @param latitude the latitude in degrees
     * @param longitude the longitude in degrees
     */
    public OpenRouterPoint(double latitude, double longitude)
    {
        this.latitudeE6 = (int) (latitude * 1E6);
        this.longitudeE6 = (int) (longitude * 1E6);
    }


    @Override
    public int getLatitudeE6() {
        return latitudeE6;
    }

    @Override
    public int getLongitudeE6() {
        return longitudeE6;
    }

    /**
     * Returns a OpenRouterPoint from bytes.
     * @param bytes the bytes to read from
     * @return the OpenRouterPoint
     */
    public static OpenRouterPoint fromBytes(byte[] bytes)
    {
        int latitude = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        int longitude = ((bytes[4] & 0xFF) << 24) | ((bytes[5] & 0xFF) << 16) | ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
        return new OpenRouterPoint(latitude, longitude);
    }

}
