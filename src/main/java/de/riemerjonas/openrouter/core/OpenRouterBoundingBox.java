package de.riemerjonas.openrouter.core;

public class OpenRouterBoundingBox {
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;

    public OpenRouterBoundingBox(double minLat, double maxLat, double minLon, double maxLon) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    public OpenRouterBoundingBox(OpenRouterGeoPoint min, OpenRouterGeoPoint max) {
        // Sort the points to get the bounding box
        this.minLat = Math.min(min.getLatitude(), max.getLatitude());
        this.maxLat = Math.max(min.getLatitude(), max.getLatitude());
        this.minLon = Math.min(min.getLongitude(), max.getLongitude());
        this.maxLon = Math.max(min.getLongitude(), max.getLongitude());
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLon() {
        return maxLon;
    }
}
