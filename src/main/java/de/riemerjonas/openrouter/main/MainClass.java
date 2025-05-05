package de.riemerjonas.openrouter.main;

import de.riemerjonas.openrouter.core.OpenRouterGPX;
import de.riemerjonas.openrouter.core.OpenRouterLog;
import de.riemerjonas.openrouter.core.OpenRouterNode;
import de.riemerjonas.openrouter.core.OpenRouterPoint;
import de.riemerjonas.openrouter.core.ifaces.IRoutingProfile;
import de.riemerjonas.openrouter.core.profiles.RoutingProfileFast;
import de.riemerjonas.openrouter.core.profiles.RoutingProfileShort;
import de.riemerjonas.openrouter.debug.ORTimeDebug;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;
import de.riemerjonas.openrouter.graph.algorithm.ORGraphRouter;
import de.riemerjonas.openrouter.graph.core.ORGraphHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainClass
{

    private static final String TAG = "MainClass";

    public static void main(String[] args) throws InterruptedException {
        OpenRouterLog.setLogLevel(OpenRouterLog.LOG_LEVEL.DEBUG);

        File inputFile = new File("C:/Users/Jonas Riemer/Downloads/maps/bayern.osm.pbf");
        File outputFile = new File("C:/Users/Jonas Riemer/Downloads/graphs/bayern.graph");


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
        OpenRouterPoint point = new OpenRouterPoint(48.301890,11.351371);
        OpenRouterNode nearestNode = loadedGraph.getNearestNode(point);
        if(nearestNode == null) OpenRouterLog.e(TAG, "Point is null");
        else OpenRouterLog.d(TAG, "Node: " + nearestNode.getLatitude() + ", " + nearestNode.getLongitude());
        nearestNodeTime.printResult();

        // ========== Routing ========== //
        OpenRouterPoint destination = new OpenRouterPoint(49.489835,11.082304);
        ArrayList<IRoutingProfile> profiles = new ArrayList<>();
        profiles.add(new RoutingProfileFast());
        profiles.add(new RoutingProfileShort());

        for (IRoutingProfile profile : profiles) {
            ORTimeDebug routeTime = new ORTimeDebug("Routing");
            routeTime.start();
            OpenRouterLog.d(TAG, "Using profile: " + profile.getName());
            List<OpenRouterNode> routeProfile = ORGraphRouter.route(point, destination, loadedGraph, profile);
            routeTime.printResult();
            if(routeProfile != null)
            {
                String baseName = "route_" + profile.getName() + ".gpx";
                String filePath = "C:/Users/Jonas Riemer/Downloads/routes/" + baseName;
                OpenRouterGPX.create(new File(filePath), routeProfile, profile.getName());
                OpenRouterLog.d(TAG, "Route created: " + filePath);
            }
        }


    }

}
