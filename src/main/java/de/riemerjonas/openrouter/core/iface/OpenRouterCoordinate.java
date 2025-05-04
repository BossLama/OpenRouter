package de.riemerjonas.openrouter.core.iface;

public interface OpenRouterCoordinate
{
    /**
     * Returns the latitude of the coordinate as a double.
     * @return the latitude of the coordinate
     */
    double getLatitude();

    /**
     * Returns the longitude of the coordinate as a double.
     * @return the longitude of the coordinate
     */
    double getLongitude();

    /**
     * Returns the latitude of the coordinate as an int.
     * @return the latitude of the coordinate
     */
    int getLatitudeInt();

    /**
     * Returns the longitude of the coordinate as an int.
     * @return the longitude of the coordinate
     */
    int getLongitudeInt();
}
