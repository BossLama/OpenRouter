package de.riemerjonas.openrouter.core;

import java.util.ArrayList;
import java.util.List;

public class OpenRouterNode
{
    private long id;
    private double latitude;
    private double longitude;
    private List<long[]> edges;

    public OpenRouterNode(long id, double latitude, double longitude)
    {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.edges = new ArrayList<>();
    }

    public void addEdge(OpenRouterEdge edge)
    {
        long[] edgeArray = edge.getAsSlim();
        edges.add(edgeArray);
    }

    public void removeEdge(OpenRouterEdge edge)
    {
        long[] edgeArray = edge.getAsSlim();
        edges.remove(edgeArray);
    }

    public List<long[]> getEdgesAsList()
    {
        return edges;
    }

    public List<OpenRouterEdge> getEdges()
    {
        List<OpenRouterEdge> edgeList = new ArrayList<>();
        for (long[] edge : edges)
        {
            edgeList.add(new OpenRouterEdge(edge[0], edge[1], (short) edge[2]));
        }
        return edgeList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistanceTo(OpenRouterGeoPoint geoPoint)
    {
        double latDiff = Math.toRadians(geoPoint.getLatitude() - latitude);
        double lonDiff = Math.toRadians(geoPoint.getLongitude() - longitude);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(geoPoint.getLatitude())) *
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

}
