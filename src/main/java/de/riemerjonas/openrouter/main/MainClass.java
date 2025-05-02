package de.riemerjonas.openrouter.main;

import de.riemerjonas.openrouter.graph.OpenRouterGraph;

import java.io.File;

public class MainClass
{

    public static void main(String[] args)
    {
        File pbfFile = new File("C:/Users/Jonas Riemer/Downloads/maps/test.osm.pbf");
        File outputFile = new File("C:/Users/Jonas Riemer/Downloads/graphs/test.graph");

        OpenRouterGraph graph = OpenRouterGraph.buildFromPbf(pbfFile);
        if(graph != null) graph.save(outputFile);

        long startTime = System.currentTimeMillis();
        OpenRouterGraph graphLoaded = OpenRouterGraph.loadFromFile(outputFile);
        if(graphLoaded != null)
        {
            System.out.println("Graph loaded successfully.");
            System.out.println(graphLoaded.getEdges().size());
            System.out.println(graphLoaded.getTileMap().size());

            System.out.println("Time taken to load graph: " + (System.currentTimeMillis() - startTime) + " ms");

        }
        else
        {
            System.out.println("Failed to load graph.");
            System.out.println("Time taken until failure: " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

}
