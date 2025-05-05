package de.riemerjonas.openrouter.core;

import java.io.File;
import java.util.List;

public class OpenRouterGPX {

    public static void create(File file, List<OpenRouterNode> nodes, String name) {
        // Write the GPX file
        StringBuilder gpx = new StringBuilder();
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpx.append("<gpx version=\"1.1\" creator=\"OpenRouter\">\n");
        gpx.append("<trk>\n");
        gpx.append("<name>").append(name).append("</name>\n");
        gpx.append("<trkseg>\n");
        for (OpenRouterNode node : nodes) {
            gpx.append("<trkpt lat=\"").append(node.getLatitude()).append("\" lon=\"").append(node.getLongitude()).append("\">\n");
            gpx.append("</trkpt>\n");
        }
        gpx.append("</trkseg>\n");
        gpx.append("</trk>\n");
        gpx.append("</gpx>\n");

        try {
            java.nio.file.Files.write(file.toPath(), gpx.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
