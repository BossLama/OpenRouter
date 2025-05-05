package de.riemerjonas.openrouter.main;

import de.riemerjonas.openrouter.core.OpenRouterGPX;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.core.OpenRouterPoint;
import de.riemerjonas.openrouter.debug.ORTimeDebug;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;
import de.riemerjonas.openrouter.graph.algorithm.ORGraphRouter;
import de.riemerjonas.openrouter.graph.core.ORGraphHandler;

import java.io.File;
import java.util.List;

public class MainClass
{

    private static final String TAG = "MainClass";

    public static void main(String[] args) throws InterruptedException {
        OpenRouterLog.setLogLevel(OpenRouterLog.LOG_LEVEL.DEBUG);

        File inputFile = new File("C:/Users/Jonas Riemer/Downloads/maps/test.osm.pbf");
        File outputFile = new File("C:/Users/Jonas Riemer/Downloads/graphs/test.graph");
        File gpxFile = new File("C:/Users/Jonas Riemer/Downloads/routes/test.gpx");


        // ========== GraphBuilder ==========
        /*
        ORTimeDebug graphTime = new ORTimeDebug("GraphBuilder");
        graphTime.start();
        OpenRouterGraph graph = ORGraphHandler.buildFromPBF(inputFile);
        graphTime.printResult();

        // ========== GraphSaver ========== //
        ORTimeDebug saveTime = new ORTimeDebug("GraphSaver");
        saveTime.start();
        ORGraphHandler.save(outputFile, graph);
        saveTime.printResult();
        */

        // ========== GraphLoader ========== //
        ORTimeDebug loadTime = new ORTimeDebug("GraphLoader");
        loadTime.start();
        OpenRouterGraph loadedGraph = ORGraphHandler.load(outputFile);
        loadTime.printResult();

        //========== NearestNode ========== //
        ORTimeDebug nearestNodeTime = new ORTimeDebug("NearestNode");
        nearestNodeTime.start();
        OpenRouterPoint point = new OpenRouterPoint(48.300546, 11.348695);
        OpenRouterNode nearestNode = loadedGraph.getNearestNode(point);
        if(nearestNode == null) OpenRouterLog.e(TAG, "Point is null");
        else OpenRouterLog.d(TAG, "Node: " + nearestNode.getLatitude() + ", " + nearestNode.getLongitude());
        nearestNodeTime.printResult();

        // ========== Routing ========== //
        ORTimeDebug routeTime = new ORTimeDebug("Routing");
        routeTime.start();
        OpenRouterPoint destination = new OpenRouterPoint(48.283223,11.366155);
        List<OpenRouterNode> route = ORGraphRouter.route(point, destination, loadedGraph);
        if(route == null || route.size() == 0) OpenRouterLog.e(TAG, "Route is null or empty");
        else OpenRouterLog.d(TAG, "Route: " + route.size() + " nodes");
        routeTime.printResult();

        // ========== GPX ========== //
        ORTimeDebug gpxTime = new ORTimeDebug("GPX");
        gpxTime.start();
        if (route != null && route.size() > 0) {
            OpenRouterGPX.create(gpxFile, route, "Test Route");
            OpenRouterLog.d(TAG, "GPX file created: " + gpxFile.getAbsolutePath());
        } else {
            OpenRouterLog.e(TAG, "Route is null or empty, cannot create GPX file");
        }



    }

}
