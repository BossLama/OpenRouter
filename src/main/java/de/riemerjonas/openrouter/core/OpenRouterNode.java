package de.riemerjonas.openrouter.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenRouterNode
{
    private long id;
    private int latitude;
    private int longitude;
    private List<Edge> edges;

    public OpenRouterNode(long id, int latitude, int longitude)
    {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.edges = new ArrayList<>();
    }

    public void addEdge(Edge edge)
    {
        edges.add(edge);
    }

    public List<Edge> getEdges()
    {
        return edges;
    }


    public long getId() {
        return id;
    }


    public double getLatitude() {
        return OpenRouterCompressor.decompressCoordinate(latitude);
    }


    public double getLongitude() {
        return OpenRouterCompressor.decompressCoordinate(longitude);
    }

    public int getLatitudeAsInt() {
        return latitude;
    }

    public int getLongitudeAsInt() {
        return longitude;
    }

    public double getDistanceTo(OpenRouterGeoPoint geoPoint)
    {
        double latDiff = Math.toRadians(geoPoint.getLatitude() - getLatitude());
        double lonDiff = Math.toRadians(geoPoint.getLongitude() - getLongitude());
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(getLatitude())) * Math.cos(Math.toRadians(geoPoint.getLatitude())) *
                Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return c * 6371; // Radius of the Earth in kilometers
    }

    public double getDistanceTo(OpenRouterNode node)
    {
        return getDistanceTo(new OpenRouterGeoPoint(node.getLatitude(), node.getLongitude()));
    }

    public double getDistanceTo(double latitude, double longitude)
    {
        return getDistanceTo(new OpenRouterGeoPoint(latitude, longitude));
    }

    public static record Edge(long to, short weight) {}

}
