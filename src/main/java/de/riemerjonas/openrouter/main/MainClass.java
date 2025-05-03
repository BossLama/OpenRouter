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
        OpenRouterGeoPoint endPoint = new OpenRouterGeoPoint(48.371707,11.514032);

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

        // Load the graph from a file
        ORTimeTest loadTimeTest = new ORTimeTest();
        loadTimeTest.start();
        OpenRouterLog.info(TAG, "Loading graph from file: " + outputFile.getAbsolutePath());
        OpenRouterGraph loadedGraph = OpenRouterGraph.load(outputFile);
        OpenRouterLog.info(TAG, "Graph loaded in " + loadTimeTest.stop() + "ms");
        OpenRouterLog.info(TAG, "Graph loaded: " + loadedGraph.getAllNodes().size() + " nodes");

        //System.exit(0);

        // Load the graph from a file with bounding box
        ORTimeTest loadBboxTimeTest = new ORTimeTest();
        loadBboxTimeTest.start();
        OpenRouterLog.info(TAG, "Loading graph from file with bounding box");
        OpenRouterBoundingBox boundingBox = new OpenRouterBoundingBox(startPoint, endPoint);
        OpenRouterGraph loadedGraphWithBbox = OpenRouterGraph.load(outputFile, boundingBox);
        OpenRouterLog.info(TAG, "Graph loaded in " + loadBboxTimeTest.stop() + "ms");
        OpenRouterLog.info(TAG, "Graph loaded with bounding box: " + loadedGraphWithBbox.getAllNodes().size() + " nodes");


        // Find nearest node to a given coordinate
        ORTimeTest timeTest = new ORTimeTest();
        timeTest.start();
        OpenRouterLog.info(TAG, "Finding nearest node to coordinates");
        OpenRouterNode nearestNode = loadedGraph.findClosestNode(startPoint.getLatitude(), startPoint.getLongitude(), 50);
        OpenRouterLog.info(TAG, "Found nearest node in " + timeTest.stop() + "ms");
        OpenRouterLog.info(TAG, "Nearest node found: " + nearestNode.getId());

        // Find a route between two nodes with bounding box
        ORTimeTest routeTimeTest = new ORTimeTest();
        routeTimeTest.start();
        OpenRouterLog.info(TAG, "Finding route between two nodes");
        List<Long> route = loadedGraphWithBbox.findRoute(
                startPoint.getLatitude(), startPoint.getLongitude(),
                endPoint.getLatitude(), endPoint.getLongitude()
        );
        OpenRouterLog.info(TAG, "Found route in " + routeTimeTest.stop() + "ms");

        // Find a route between two nodes without bounding box
        ORTimeTest routeTimeTest2 = new ORTimeTest();
        routeTimeTest2.start();
        OpenRouterLog.info(TAG, "Finding route between two nodes without bounding box");
        List<Long> route2 = loadedGraph.findRoute(
                startPoint.getLatitude(), startPoint.getLongitude(),
                endPoint.getLatitude(), endPoint.getLongitude()
        );
        OpenRouterLog.info(TAG, "Found route in " + routeTimeTest2.stop() + "ms");
        OpenRouterLog.info(TAG, "Route found: " + route.size() + " nodes");
    }

}
