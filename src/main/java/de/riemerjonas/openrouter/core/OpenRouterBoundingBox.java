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
        this.minLat = min.getLatitude();
        this.maxLat = max.getLatitude();
        this.minLon = min.getLongitude();
        this.maxLon = max.getLongitude();
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
