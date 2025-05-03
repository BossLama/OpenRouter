package de.riemerjonas.openrouter.main;

import de.riemerjonas.openrouter.core.OpenRouterBoundingBox;
import de.riemerjonas.openrouter.core.OpenRouterGeoPoint;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.debug.ORTimeTest;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;
import de.riemerjonas.openrouter.graph.core.ORGraphBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainClass
{
    private static final String TAG = "MainClass";

    public static void main(String[] args)
    {
        OpenRouterLog.setLogLevel(OpenRouterLog.LogLevel.INFO);
        OpenRouterLog.info(TAG, "Running OpenRouter");

        // Settings
        File inputFile = new File("C:/Users/Jonas Riemer/Downloads/maps/test.osm.pbf");
        File outputFile = new File("C:/Users/Jonas Riemer/Downloads/graphs/test.graph");
        OpenRouterGeoPoint startPoint = new OpenRouterGeoPoint(48.302898,11.352990);
        OpenRouterGeoPoint endPoint = new OpenRouterGeoPoint(48.303253,11.355378);

        // Building a graph from a PBF file
        /*
        ORTimeTest buildTimeTest = new ORTimeTest();
        buildTimeTest.start();
        OpenRouterLog.info(TAG, "Building graph from PBF file: " + inputFile.getAbsolutePath());
        OpenRouterGraph graph = ORGraphBuilder.buildFromPbf(inputFile);
        OpenRouterLog.info(TAG, "Graph built in " + buildTimeTest.stop() + "ms");

        // Save the graph to a file
        ORTimeTest saveTimeTest = new ORTimeTest();
        saveTimeTest.start();
        OpenRouterLog.info(TAG, "Saving graph to file: " + outputFile.getAbsolutePath());
        graph.save(outputFile);
        */

        long usedHeapSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        OpenRouterLog.info(TAG, "Used heap size: " + usedHeapSize / (1024 * 1024) + "MB");

        // Load the graph from a file
        ORTimeTest loadTimeTest = new ORTimeTest();
        loadTimeTest.start();
        OpenRouterLog.info(TAG, "Loading graph from file: " + outputFile.getAbsolutePath());
        OpenRouterGraph loadedGraph = OpenRouterGraph.load(outputFile);
        OpenRouterLog.info(TAG, "Graph loaded in " + loadTimeTest.stop() + "ms");
        OpenRouterLog.info(TAG, "Graph loaded: " + loadedGraph.getAllNodes().size() + " nodes");
        long usedHeapSizeAfterLoad = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        OpenRouterLog.info(TAG, "Used heap size after load: " + usedHeapSizeAfterLoad / (1024 * 1024) + "MB");
        OpenRouterLog.info(TAG, "Difference in heap size: " + (usedHeapSizeAfterLoad - usedHeapSize) / (1024 * 1024) + "MB");
        usedHeapSize = usedHeapSizeAfterLoad;
        OpenRouterLog.info(TAG, "-------------------------");

        //System.exit(0);

        // Load the graph from a file with bounding box
        ORTimeTest loadBboxTimeTest = new ORTimeTest();
        loadBboxTimeTest.start();
        OpenRouterLog.info(TAG, "Loading graph from file with bounding box");
        OpenRouterBoundingBox boundingBox = new OpenRouterBoundingBox(startPoint, endPoint);
        OpenRouterGraph loadedGraphWithBbox = OpenRouterGraph.load(outputFile, boundingBox);
        OpenRouterLog.info(TAG, "Graph loaded in " + loadBboxTimeTest.stop() + "ms");
        OpenRouterLog.info(TAG, "Graph loaded with bounding box: " + loadedGraphWithBbox.getAllNodes().size() + " nodes");
        long usedHeapSizeAfterLoadBbox = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        OpenRouterLog.info(TAG, "Used heap size after load with bounding box: " + usedHeapSizeAfterLoadBbox / (1024 * 1024) + "MB");
        OpenRouterLog.info(TAG, "Difference in heap size: " + (usedHeapSizeAfterLoadBbox - usedHeapSize) / (1024 * 1024) + "MB");
        usedHeapSize = usedHeapSizeAfterLoadBbox;
        OpenRouterLog.info(TAG, "-------------------------");

        // Find nearest node to a given coordinate
        ORTimeTest timeTest = new ORTimeTest();
        timeTest.start();
        OpenRouterLog.info(TAG, "Finding nearest node to coordinates");
        OpenRouterNode nearestNode = loadedGraph.findClosestNode(startPoint.getLatitude(), startPoint.getLongitude(), 50);
        OpenRouterLog.info(TAG, "Found nearest node in " + timeTest.stop() + "ms");
        OpenRouterLog.info(TAG, "Nearest node found: " + nearestNode.getId());
        OpenRouterLog.info(TAG, "-------------------------");
    }

}
