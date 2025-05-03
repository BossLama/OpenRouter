package de.riemerjonas.openrouter.main;

import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.debug.ORTimeTest;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;

import java.io.File;
import java.util.List;

public class MainClass
{
    private static final String TAG = "MainClass";

    public static void main(String[] args)
    {
        OpenRouterLog.setLogLevel(OpenRouterLog.LogLevel.INFO);
        OpenRouterLog.info(TAG, "Running OpenRouter");

        // Building a graph from a PBF file
        File pbfFile = new File("C:/Users/Jonas Riemer/Downloads/maps/albania-latest.osm.pbf");
        if (!pbfFile.exists())
        {
            OpenRouterLog.error(TAG, "PBF file not found: " + pbfFile.getAbsolutePath());
            return;
        }
        OpenRouterLog.info(TAG, "PBF file found: " + pbfFile.getAbsolutePath());
        OpenRouterGraph graph = OpenRouterGraph.buildFromPbf(pbfFile);

        // Find nearest node to a given coordinate
        ORTimeTest timeTest = new ORTimeTest();
        timeTest.start();
        OpenRouterLog.info(TAG, "Finding nearest node to coordinates");
        OpenRouterNode nearestNode = graph.findClosestNode(40.701870,19.632272, 50);
        OpenRouterLog.info(TAG, "Found nearest node in " + timeTest.stop() + "ms");
        OpenRouterLog.info(TAG, "Nearest node found: " + nearestNode.getId());

        // Find a route between two nodes
        ORTimeTest routeTimeTest = new ORTimeTest();
        routeTimeTest.start();
        OpenRouterLog.info(TAG, "Finding route between two nodes");
        List<Long> route = graph.findRoute(40.701870, 19.632272, 40.684254,19.625669);
        OpenRouterLog.info(TAG, "Found route in " + routeTimeTest.stop() + "ms");



    }

}
