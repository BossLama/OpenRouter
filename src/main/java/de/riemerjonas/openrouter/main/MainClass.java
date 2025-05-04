package de.riemerjonas.openrouter.main;

import de.riemerjonas.openrouter.core.*;
import de.riemerjonas.openrouter.core.iface.OpenRouterRoutingProfile;
import de.riemerjonas.openrouter.core.profiles.RoutingProfileFast;
import de.riemerjonas.openrouter.core.profiles.RoutingProfileShort;
import de.riemerjonas.openrouter.debug.ORTimeDebug;
import de.riemerjonas.openrouter.graph.OpenRouterGraph;
import de.riemerjonas.openrouter.graph.algorithm.ORGraphRouteAStarAlgorithm;
import de.riemerjonas.openrouter.graph.core.OpenRouterGraphBuilder;

import java.io.File;

public class MainClass
{

    private static final String TAG = "MainClass";

    public static void main(String[] args) throws InterruptedException {
        OpenRouterLog.setLogLevel(OpenRouterLog.LOG_LEVEL.DEBUG);

        File inputFile = new File("C:/Users/Jonas Riemer/Downloads/maps/test.osm.pbf");
        File outputFile = new File("C:/Users/Jonas Riemer/Downloads/graphs/albania-latest.osm.pbf.graph");
        File gpxFile = new File("C:/Users/Jonas Riemer/Downloads/routes/test.gpx");

        // Building the graph from the PBF file
        ORTimeDebug graphBuildTime = new ORTimeDebug("GraphBuild");
        OpenRouterGraph graph = OpenRouterGraphBuilder.buildFromPbf(inputFile);
        graphBuildTime.printResult();

        // Finding a node by ID
        ORTimeDebug nodeFindTime = new ORTimeDebug("FindNodeByID");
        OpenRouterNode node = graph.getNodeByID(0);
        if(node == null) OpenRouterLog.i(TAG, "Node not found");
        else OpenRouterLog.i(TAG, "Node found with ID " + node.getDeltaId() + " and coordinates " + node.getLatitude() + ", " + node.getLongitude());
        nodeFindTime.printResult();

        // Finding a node by delta ID
        ORTimeDebug nodeFindDeltaTime = new ORTimeDebug("FindNodeByDeltaID");
        OpenRouterNode nodeDelta = graph.getNodeByDeltaID(0);
        if(nodeDelta == null) OpenRouterLog.i(TAG, "Node not found");
        else OpenRouterLog.i(TAG, "Node found with ID " + nodeDelta.getDeltaId() + " and coordinates " + nodeDelta.getLatitude() + ", " + nodeDelta.getLongitude());
        nodeFindDeltaTime.printResult();

        // Finding close nodes to a coordinate
        OpenRouterPoint point = new OpenRouterPoint(48.298758,11.345519);
        ORTimeDebug closeNodesTime = new ORTimeDebug("FindCloseNodes");
        OpenRouterNode nodeClose = graph.findClosestNode(point, 150);
        if(nodeClose != null) OpenRouterLog.i(TAG, "Node found with ID " + nodeClose.getDeltaId() + " and coordinates " + nodeClose.getLatitude() + ", " + nodeClose.getLongitude());
        else OpenRouterLog.i(TAG, "No close nodes found");
        closeNodesTime.printResult();

        // Finding a route between two nodes
        ORTimeDebug routeTime = new ORTimeDebug("FindRoute");
        OpenRouterPoint start = new OpenRouterPoint(48.298758,11.345519);
        OpenRouterPoint end = new OpenRouterPoint(48.140665,11.566028);
        OpenRouterRoutingProfile profile = new RoutingProfileFast();
        OpenRouterRoute route = ORGraphRouteAStarAlgorithm.findRoute(graph, start, end, profile);
        routeTime.printResult();

        OpenRouterGPX.createGPXFile(gpxFile, route);
    }

}
