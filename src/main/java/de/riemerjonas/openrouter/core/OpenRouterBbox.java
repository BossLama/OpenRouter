package de.riemerjonas.openrouter.core;

import de.riemerjonas.openrouter.core.iface.OpenRouterCoordinate;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;

import java.util.ArrayList;

public class OpenRouterBbox {

    private final double minLat;
    private final double minLon;
    private final double maxLat;
    private final double maxLon;

    public OpenRouterBbox(double minLat, double minLon, double maxLat, double maxLon)
    {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
    }

    public OpenRouterBbox(OpenRouterCoordinate min, OpenRouterCoordinate max)
    {
        this.minLat = Math.min(min.getLatitude(), max.getLatitude());
        this.minLon = Math.min(min.getLongitude(), max.getLongitude());
        this.maxLat = Math.max(min.getLatitude(), max.getLatitude());
        this.maxLon = Math.max(min.getLongitude(), max.getLongitude());
    }

    public double getMinLat()
    {
        return minLat;
    }

    public double getMinLon()
    {
        return minLon;
    }

    public double getMaxLat()
    {
        return maxLat;
    }

    public double getMaxLon()
    {
        return maxLon;
    }

    public boolean contains(OpenRouterCoordinate coordinate)
    {
        return coordinate.getLatitude() >= minLat && coordinate.getLatitude() <= maxLat &&
               coordinate.getLongitude() >= minLon && coordinate.getLongitude() <= maxLon;
    }
}
