package de.riemerjonas.openrouter.core;

public class OpenRouterEdge
{

    private long fromNodeId;
    private long toNodeId;

    public OpenRouterEdge(long fromNodeId, long toNodeId)
    {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    public long getFromNodeId()
    {
        return fromNodeId;
    }

    public void setFromNodeId(long fromNodeId)
    {
        this.fromNodeId = fromNodeId;
    }

    public long getToNodeId()
    {
        return toNodeId;
    }

    public void setToNodeId(long toNodeId)
    {
        this.toNodeId = toNodeId;
    }


}
