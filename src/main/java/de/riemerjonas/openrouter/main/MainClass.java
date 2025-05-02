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
        assert graph != null;
        graph.save(outputFile);
    }

}
