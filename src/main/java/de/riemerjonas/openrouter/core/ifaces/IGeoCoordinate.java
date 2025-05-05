package de.riemerjonas.openrouter.core.ifaces;

/**
 * Interface representing a geographical coordinate with latitude and longitude.
 * The coordinates are represented in E6 format (degrees multiplied by 1E6).
 */
public interface IGeoCoordinate
{
    /**
     * Returns the latitude of the coordinate in E6 format.
     * @return the latitude in E6 format
     */
    int getLatitudeE6();

    /**
     * Returns the longitude of the coordinate in E6 format.
     * @return the longitude in E6 format
     */
    int getLongitudeE6();

    /**
     * Returns the latitude of this coordinate in degrees.
     * @return the latitude in degrees
     */
    default double getLatitude()
    {
        return getLatitudeE6() / 1E6;
    }

    /**
     * Returns the latitude of this coordinate in degrees.
     * @return the latitude in degrees
     */
    default double getLongitude()
    {
        return getLongitudeE6() / 1E6;
    }

    /**
     * Calculates the distance to another coordinate in meters.
     * @param other the other coordinate
     * @return the distance in meters
     */
    default double distanceTo(IGeoCoordinate other)
    {
        return distanceTo(other.getLatitude(), other.getLongitude());
    }

    /**
     * Calculates the distance to another coordinate in meters.
     * @param latitude the latitude of the other coordinate
     * @param longitude the longitude of the other coordinate
     * @return the distance in meters
     */
    default double distanceTo(double latitude, double longitude)
    {
        double diffLat = Math.toRadians(latitude - getLatitude());
        double diffLon = Math.toRadians(longitude - getLongitude());
        double a = Math.sin(diffLat / 2) * Math.sin(diffLat / 2) +
                Math.cos(Math.toRadians(getLatitude())) * Math.cos(Math.toRadians(latitude)) *
                        Math.sin(diffLon / 2) * Math.sin(diffLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double radius = 6371000;
        return radius * c;
    }

    /**
     * Converts the coordinate to a byte array.
     * The byte array is in the format: [lat1, lat2, lat3, lat4, lon1, lon2, lon3, lon4]
     * where lat1 to lat4 are the bytes of the latitude and lon1 to lon4 are the bytes of the longitude.
     * @return the byte array representation of the coordinate
     */
    default byte[] asByteArray()
    {
        byte[] bytes = new byte[8];
        int lat = getLatitudeE6();
        int lon = getLongitudeE6();
        bytes[0] = (byte) (lat >> 24);
        bytes[1] = (byte) (lat >> 16);
        bytes[2] = (byte) (lat >> 8);
        bytes[3] = (byte) lat;
        bytes[4] = (byte) (lon >> 24);
        bytes[5] = (byte) (lon >> 16);
        bytes[6] = (byte) (lon >> 8);
        bytes[7] = (byte) lon;
        return bytes;
    }

}
