package de.riemerjonas.openrouter.core;

public class OpenRouterEdge
{
    private final int fromDeltaID;
    private final int toDeltaID;
    private final short distance;
    private final short maxSpeed;
    private boolean isBidirectional = false;

    public OpenRouterEdge(int fromDeltaID, int toDeltaID, short distance, short maxSpeed, boolean isBidirectional)
    {
        this.fromDeltaID = fromDeltaID;
        this.toDeltaID = toDeltaID;
        this.distance = distance;
        this.maxSpeed = maxSpeed;
        this.isBidirectional = isBidirectional;
    }

    public int getFromDeltaID()
    {
        return fromDeltaID;
    }

    public int getToDeltaID()
    {
        return toDeltaID;
    }

    public short getDistance()
    {
        return distance;
    }

    public short getMaxSpeed()
    {
        return maxSpeed;
    }

    public boolean isBidirectional()
    {
        return isBidirectional;
    }

    public byte[] toByteArray()
    {
        byte[] byteArray = new byte[13];
        byteArray[0] = (byte) (fromDeltaID >> 24);
        byteArray[1] = (byte) (fromDeltaID >> 16);
        byteArray[2] = (byte) (fromDeltaID >> 8);
        byteArray[3] = (byte) (fromDeltaID);
        byteArray[4] = (byte) (toDeltaID >> 24);
        byteArray[5] = (byte) (toDeltaID >> 16);
        byteArray[6] = (byte) (toDeltaID >> 8);
        byteArray[7] = (byte) (toDeltaID);
        byteArray[8] = (byte) (distance >> 8);
        byteArray[9] = (byte) (distance);
        byteArray[10] = (byte) (maxSpeed >> 8);
        byteArray[11] = (byte) (maxSpeed);
        byteArray[12] = (byte) (isBidirectional ? 1 : 0);
        return byteArray;
    }

    public static OpenRouterEdge fromByteArray(byte[] byteArray)
    {
        int fromDeltaID = ((byteArray[0] & 0xFF) << 24) | ((byteArray[1] & 0xFF) << 16) | ((byteArray[2] & 0xFF) << 8) | (byteArray[3] & 0xFF);
        int toDeltaID = ((byteArray[4] & 0xFF) << 24) | ((byteArray[5] & 0xFF) << 16) | ((byteArray[6] & 0xFF) << 8) | (byteArray[7] & 0xFF);
        short distance = (short) (((byteArray[8] & 0xFF) << 8) | (byteArray[9] & 0xFF));
        short maxSpeed = (short) (((byteArray[10] & 0xFF) << 8) | (byteArray[11] & 0xFF));
        boolean isBidirectional = byteArray[12] == 1;
        return new OpenRouterEdge(fromDeltaID, toDeltaID, distance, maxSpeed, isBidirectional);
    }

}
