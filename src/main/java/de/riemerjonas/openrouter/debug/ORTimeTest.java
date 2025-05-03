package de.riemerjonas.openrouter.debug;

public class ORTimeTest
{
    private long startTime;

    public void start()
    {
        startTime = System.currentTimeMillis();
    }

    public long stop()
    {
        return System.currentTimeMillis() - startTime;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getElapsedTime()
    {
        return System.currentTimeMillis() - startTime;
    }

}
