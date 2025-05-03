package de.riemerjonas.openrouter.core;

public class OpenRouterCompressor
{

    public static int compressCoordinate(double coordinate)
    {
        return (int) Math.round(coordinate * 1E6);
    }

    public static double decompressCoordinate(int coordinate)
    {
        return coordinate / 1E6;
    }

    public static byte compressWeight(long weight)
    {
        return (byte) (weight & 0xFFFF);
    }

    public static long decompressWeight(byte weight)
    {
        return weight & 0xFFFF;
    }

}
