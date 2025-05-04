package de.riemerjonas.openrouter.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OpenRouterGPX
{

    private static final String TAG = "OpenRouterGPX";

    public static void createGPXFile(File file, OpenRouterRoute route) {
        ArrayList<OpenRouterNode> nodes = route.getRouteNodes();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<gpx version=\"1.1\" creator=\"OpenRouter\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
            writer.write("  <trk>\n");
            writer.write("    <name>Generated Route</name>\n");
            writer.write("    <trkseg>\n");

            for (OpenRouterNode node : nodes) {
                writer.write("      <trkpt lat=\"" + node.getLatitude() + "\" lon=\"" + node.getLongitude() + "\"></trkpt>\n");
            }

            writer.write("    </trkseg>\n");
            writer.write("  </trk>\n");
            writer.write("</gpx>\n");
        } catch (IOException e)
        {
            OpenRouterLog.e(TAG, "Error writing GPX file: " + e.getMessage(), e);
        }
    }

}
