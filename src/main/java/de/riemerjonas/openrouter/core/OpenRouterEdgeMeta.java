package de.riemerjonas.openrouter.core;

/**
 * Represents the metadata of an edge in the OpenRouter graph.
 * This class is used to store the maximum speed, distance, and additional time for an edge.
 */
public class OpenRouterEdgeMeta
{
    private final int packedData;

    /**
     * Creates a new OpenRouterEdgeMeta with the given maximum speed, distance, and additional time.
     *
     * @param maxSpeedMs      the maximum speed in meters per second
     * @param distanceMeter   the distance in meters
     * @param additionalTime  the additional time in seconds
     */
    public OpenRouterEdgeMeta(short maxSpeedMs, short distanceMeter, short additionalTime)
    {
        this.packedData = (maxSpeedMs & 0xFFFF) | ((distanceMeter & 0xFFFF) << 16) | ((additionalTime & 0xFFFF) << 32);
    }

    /**
     * Returns the maximum speed in meters per second.
     * @return the maximum speed in meters per second
     */
    public short getMaxSpeedMs()
    {
        return (short) (packedData & 0xFFFF);
    }

    /**
     * Returns the distance in meters.
     * @return the distance in meters
     */
    public short getDistanceMeter()
    {
        return (short) ((packedData >> 16) & 0xFFFF);
    }

    /**
     * Returns the additional time in seconds.
     * @return the additional time in seconds
     */
    public short getAdditionalTime()
    {
        return (short) ((packedData >> 32) & 0xFFFF);
    }

    /**
     * Returns the packed data as an integer.
     * @return the packed data
     */
    public int getPackedData()
    {
        return packedData;
    }

    /**
     * Returns a OpenRouterEdgeMeta from integer.
     * @param packedData the packed data to read from
     * @return the OpenRouterEdgeMeta
     */
    public static OpenRouterEdgeMeta fromPackedData(int packedData)
    {
        return new OpenRouterEdgeMeta((short) (packedData & 0xFFFF), (short) ((packedData >> 16) & 0xFFFF), (short) ((packedData >> 32) & 0xFFFF));
    }


}
