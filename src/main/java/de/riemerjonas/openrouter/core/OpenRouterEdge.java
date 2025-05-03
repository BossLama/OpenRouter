package de.riemerjonas.openrouter.core;

public class OpenRouterEdge
{
    private long fromNodeId;
    private long toNodeId;
    private long weight;

    public OpenRouterEdge(long fromNodeId, long toNodeId, short weight)
    {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.weight = weight;
    }

    public long getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(long fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public long getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(long toNodeId) {
        this.toNodeId = toNodeId;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public long[] getAsSlim() {
        return new long[] { fromNodeId, toNodeId, weight };
    }
}
