package de.riemerjonas.openrouter.debug;

import de.riemerjonas.openrouter.core.OpenRouterLog;

public class ORTimeDebug
{
    private String name;
    private long startTime;
    private long endTime;

    public ORTimeDebug(String name)
    {
        this.name = name;
        this.startTime = 0;
        this.endTime = 0;
        start();
    }

    public void start()
    {
        this.startTime = System.currentTimeMillis();
    }

    public void end()
    {
        this.endTime = System.currentTimeMillis();
    }

    public long getDuration()
    {
        return this.endTime - this.startTime;
    }

    public String getName()
    {
        return this.name;
    }

    public void printResult()
    {
        if(endTime == 0) end();
        String line1 = "| Time for " + name + ": " + getDuration() + "ms |";
        System.out.println("-".repeat(line1.length()));
        System.out.println(line1);
        System.out.println("-".repeat(line1.length()));

    }


}
