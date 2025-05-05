package de.riemerjonas.openrouter.core;

import java.util.List;

public class OpenRouterEdge
{
    private final int fromID;
    private final int toID;
    private final int metaData;

    public OpenRouterEdge(int fromID, int toID, int metaData)
    {
        this.fromID = fromID;
        this.toID = toID;
        this.metaData = metaData;
    }

    public OpenRouterEdge(int fromID, int toID, OpenRouterEdgeMeta metaData)
    {
        this.fromID = fromID;
        this.toID = toID;
        this.metaData = metaData.getPackedData();
    }

    public int getFromID()
    {
        return fromID;
    }

    public int getToID()
    {
        return toID;
    }

    public int getMetaData()
    {
        return metaData;
    }

    public OpenRouterEdgeMeta getMetaDataAsObject()
    {
        return OpenRouterEdgeMeta.fromPackedData(metaData);
    }

    public static byte[] serialize(List<OpenRouterEdge> edges) {
        int size = edges.size();
        byte[] data = new byte[size * 12];

        for (int i = 0; i < size; i++) {
            OpenRouterEdge edge = edges.get(i);
            int offset = i * 12;

            // fromID
            data[offset]     = (byte) (edge.fromID >> 24);
            data[offset + 1] = (byte) (edge.fromID >> 16);
            data[offset + 2] = (byte) (edge.fromID >> 8);
            data[offset + 3] = (byte) edge.fromID;

            // toID
            data[offset + 4] = (byte) (edge.toID >> 24);
            data[offset + 5] = (byte) (edge.toID >> 16);
            data[offset + 6] = (byte) (edge.toID >> 8);
            data[offset + 7] = (byte) edge.toID;

            // metaData
            data[offset + 8]  = (byte) (edge.metaData >> 24);
            data[offset + 9]  = (byte) (edge.metaData >> 16);
            data[offset + 10] = (byte) (edge.metaData >> 8);
            data[offset + 11] = (byte) edge.metaData;
        }

        return data;
    }

}
