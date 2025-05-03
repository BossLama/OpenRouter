package de.riemerjonas.openrouter.main;

import de.riemerjonas.openrouter.core.OpenRouterBoundingBox;
import de.riemerjonas.openrouter.core.OpenRouterGeoPoint;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.debug.ORTimeTest;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;

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
        OpenRouterGeoPoint startPoint = new OpenRouterGeoPoint(48.300828,11.349852);
        OpenRouterGeoPoint endPoint = new OpenRouterGeoPoint(48.302812,11.350861);

        // Building a graph from a PBF file
        File pbfFile = new File("C:/Users/Jonas Riemer/Downloads/maps/test.osm.pbf");
        if (!pbfFile.exists())
        {
            OpenRouterLog.error(TAG, "PBF file not found: " + pbfFile.getAbsolutePath());
            return;
        }
        OpenRouterLog.info(TAG, "PBF file found: " + pbfFile.getAbsolutePath());
        OpenRouterGraph graph = OpenRouterGraph.buildFromPbf(pbfFile);

        // Save the graph to a file
        ORTimeTest saveTimeTest = new ORTimeTest();
        saveTimeTest.start();
        File outputFile = new File("C:/Users/Jonas Riemer/Downloads/test.graph");
        OpenRouterLog.info(TAG, "Saving graph to file: " + outputFile.getAbsolutePath());
        graph.save(outputFile);
        OpenRouterLog.info(TAG, "Graph saved in " + saveTimeTest.stop() + "ms");

        // Load the graph from a file
        ORTimeTest loadTimeTest = new ORTimeTest();
        loadTimeTest.start();
        OpenRouterLog.info(TAG, "Loading graph from file: " + outputFile.getAbsolutePath());
        OpenRouterGraph loadedGraph = OpenRouterGraph.load(outputFile);
        OpenRouterLog.info(TAG, "Graph loaded in " + loadTimeTest.stop() + "ms");

        // Load the graph from a file with bounding box
        ORTimeTest loadBoundingBoxTimeTest = new ORTimeTest();
        loadBoundingBoxTimeTest.start();
        OpenRouterLog.info(TAG, "Loading graph from file with bounding box: " + outputFile.getAbsolutePath());
        OpenRouterBoundingBox bbox = new OpenRouterBoundingBox(startPoint, endPoint);
        OpenRouterGraph loadedGraphWithBoundingBox = OpenRouterGraph.load(outputFile, bbox);
        OpenRouterLog.info(TAG, "Graph loaded in " + loadBoundingBoxTimeTest.stop() + "ms");


        // Find nearest node to a given coordinate
        ORTimeTest timeTest = new ORTimeTest();
        timeTest.start();
        OpenRouterLog.info(TAG, "Finding nearest node to coordinates");
        OpenRouterNode nearestNode = graph.findClosestNode(startPoint.getLatitude(), startPoint.getLongitude(), 50);
        OpenRouterLog.info(TAG, "Found nearest node in " + timeTest.stop() + "ms");
        OpenRouterLog.info(TAG, "Nearest node found: " + nearestNode.getId());

        // Find a route between two nodes
        ORTimeTest routeTimeTest = new ORTimeTest();
        routeTimeTest.start();
        OpenRouterLog.info(TAG, "Finding route between two nodes");
        List<Long> route = graph.findRoute(
                startPoint.getLatitude(), startPoint.getLongitude(),
                endPoint.getLatitude(), endPoint.getLongitude()
        );
        OpenRouterLog.info(TAG, "Found route in " + routeTimeTest.stop() + "ms");

        // Getting route length
        List<OpenRouterNode> routeNodes = new ArrayList<>();
        double routeLength = 0;
        OpenRouterNode previousNode = null;
        for (Long nodeId : route)
        {
            OpenRouterNode node = graph.getNode(nodeId);
            if (node != null)
            {
                if(previousNode != null) routeLength += node.getDistanceTo(previousNode);
                previousNode = node;
                routeNodes.add(node);
            }
        }
        OpenRouterLog.info(TAG, "Route length: " + routeLength + "m");



    }

}
