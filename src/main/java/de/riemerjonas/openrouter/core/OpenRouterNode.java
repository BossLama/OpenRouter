package de.riemerjonas.openrouter.core;

public class OpenRouterNode
{

    private long id;
    private int latitude;
    private int longitude;

    public OpenRouterNode(long id, int latitude, int longitude)
    {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public OpenRouterNode(long id, double latitude, double longitude)
    {
        this.id = id;
        this.latitude = (int) (latitude * 1E6);
        this.longitude = (int) (longitude * 1E6);
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getLatitude()
    {
        return latitude;
    }

    public double getLatitudeDouble()
    {
        return latitude / 1E6;
    }

    public void setLatitude(int latitude)
    {
        this.latitude = latitude;
    }

    public int getLongitude()
    {
        return longitude;
    }

    public double getLongitudeDouble()
    {
        return longitude / 1E6;
    }

    public void setLongitude(int longitude)
    {
        this.longitude = longitude;
    }
}
