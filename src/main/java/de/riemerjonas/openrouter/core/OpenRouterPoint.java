package de.riemerjonas.openrouter.core;

import de.riemerjonas.openrouter.core.iface.OpenRouterCoordinate;

public class OpenRouterPoint implements OpenRouterCoordinate
{
    private static final double COORDINATE_PRECISION = 1E6;
    private final int latitude;
    private final int longitude;

    public OpenRouterPoint(int latitude, int longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public OpenRouterPoint(double latitude, double longitude)
    {
        this.latitude = (int) (latitude * COORDINATE_PRECISION);
        this.longitude = (int) (longitude * COORDINATE_PRECISION);
    }

    @Override
    public double getLatitude()
    {
        return latitude / COORDINATE_PRECISION;
    }

    @Override
    public double getLongitude()
    {
        return longitude / COORDINATE_PRECISION;
    }

    @Override
    public int getLatitudeInt()
    {
        return latitude;
    }

    @Override
    public int getLongitudeInt()
    {
        return longitude;
    }

}
