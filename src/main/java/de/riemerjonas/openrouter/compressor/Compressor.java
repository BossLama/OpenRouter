package de.riemerjonas.openrouter.compressor;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.OpenRouterNode;

public class Compressor
{

    public static byte[] compressOpenRouterNode(OpenRouterNode node)
    {
        long id = node.getId();
        int latitude = node.getLatitude();
        int longitude = node.getLongitude();

        if (fitsInBits(id, 16) && fitsInBits(latitude, 16) && fitsInBits(longitude, 16))
        {
            byte[] result = new byte[8];
            writeLong(result, 0, id);
            writeShort(result, 6, latitude);
            writeShort(result, 8, longitude);
            return result;
        }

        if (fitsInBits(id, 21) && fitsInBits(latitude, 21) && fitsInBits(longitude, 21))
        {
            long packed = ((long) id << 42) | ((long) latitude << 21) | (long) longitude;
            return new byte[] {
                    (byte)(packed >>> 56),
                    (byte)(packed >>> 48),
                    (byte)(packed >>> 40),
                    (byte)(packed >>> 32),
                    (byte)(packed >>> 24),
                    (byte)(packed >>> 16),
                    (byte)(packed >>> 8),
                    (byte)(packed)
            };
        }

        byte[] result = new byte[20];
        writeLong(result, 0, id);
        writeInt(result, 8, latitude);
        writeInt(result, 12, longitude);
        return result;
    }

    public static byte[] compressEdge(OpenRouterEdge edge) {
        long fromId = edge.getFromNodeId();
        long toId = edge.getToNodeId();

        if (fitsInBits(fromId, 16) && fitsInBits(toId, 16)) {
            byte[] result = new byte[4];
            writeShort(result, 0, (int) fromId);
            writeShort(result, 2, (int) toId);
            return result;
        }

        if (fitsInBits(fromId, 32) && fitsInBits(toId, 32)) {
            byte[] result = new byte[8];
            writeInt(result, 0, (int) fromId);
            writeInt(result, 4, (int) toId);
            return result;
        }

        byte[] result = new byte[16];
        writeLong(result, 0, fromId);
        writeLong(result, 8, toId);
        return result;
    }

    private static boolean fitsInBits(long value, int bits)
    {
        long max = (1L << bits) - 1;
        return value >= 0 && value <= max;
    }

    private static void writeShort(byte[] array, int offset, int value)
    {
        array[offset]     = (byte)(value >>> 8);
        array[offset + 1] = (byte)(value);
    }

    private static void writeInt(byte[] array, int offset, int value)
    {
        array[offset]     = (byte)(value >>> 24);
        array[offset + 1] = (byte)(value >>> 16);
        array[offset + 2] = (byte)(value >>> 8);
        array[offset + 3] = (byte)(value);
    }

    private static void writeLong(byte[] array, int offset, long value)
    {
        array[offset]     = (byte)(value >>> 56);
        array[offset + 1] = (byte)(value >>> 48);
        array[offset + 2] = (byte)(value >>> 40);
        array[offset + 3] = (byte)(value >>> 32);
        array[offset + 4] = (byte)(value >>> 24);
        array[offset + 5] = (byte)(value >>> 16);
        array[offset + 6] = (byte)(value >>> 8);
        array[offset + 7] = (byte)(value);
    }

}
