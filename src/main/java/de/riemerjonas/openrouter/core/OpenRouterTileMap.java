package de.riemerjonas.openrouter.core;

import de.riemerjonas.openrouter.core.ifaces.IGeoCoordinate;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OpenRouterTileMap
{
    private static final String TAG = "OpenRouterTileMap";
    private static final double TILE_FACTOR = 10;

    private final Map<Short, List<OpenRouterNode>> tileMap;

    public OpenRouterTileMap(Map<Short, List<OpenRouterNode>> tileMap)
    {
        this.tileMap = tileMap;
    }

    /**
     * Returns the tile map.
     * @return the tile map
     */
    public Map<Short, List<OpenRouterNode>> getTileMap()
    {
        return tileMap;
    }

    /**
     * Returns the nodes in the given tile.
     * @param tileID is the tile ID
     * @return the nodes in the tile
     */
    public List<OpenRouterNode> getNodesInTile(short tileID)
    {
        return tileMap.getOrDefault(tileID, new ArrayList<>());
    }

    public List<OpenRouterNode> getNodesInViewBox(OpenRouterViewBox viewBox)
    {
        List<OpenRouterNode> nodes = new ArrayList<>();
        List<Short> tileIDs = getTileIDs(viewBox);
        for (short tileID : tileIDs)
        {
            nodes.addAll(getNodesInTile(tileID));
        }
        return nodes;
    }

    /**
     * Returns all nodes in the tile map.
     * @return all nodes in the tile map
     */
    public List<OpenRouterNode> getNodes()
    {
        List<OpenRouterNode> allNodes = new ArrayList<>();
        for (List<OpenRouterNode> nodes : tileMap.values())
        {
            allNodes.addAll(nodes);
        }
        return allNodes;
    }

    public OpenRouterNode getNearestNode(double latitude, double longitude)
    {
        short tileID = getTileID(latitude, longitude);
        List<OpenRouterNode> nodes = getNodesInTile(tileID);
        OpenRouterNode nearestNode = null;
        double minDistance = Double.MAX_VALUE;

        for (OpenRouterNode node : nodes)
        {
            double distance = node.distanceTo(new OpenRouterPoint(latitude, longitude));
            if (distance < minDistance)
            {
                minDistance = distance;
                nearestNode = node;
            }
        }
        return nearestNode;
    }

    /**
     * Returns the node with the given ID.
     * @param id is the node ID
     * @return the node with the given ID
     */
    public OpenRouterNode getNode(int id)
    {
        for (List<OpenRouterNode> nodes : tileMap.values())
        {
            for (OpenRouterNode node : nodes)
            {
                if (node.getId() == id)
                {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Returns the tile ID for the given latitude and longitude.
     * @param lat is the latitude in degrees
     * @param lon is the longitude in degrees
     * @return the tile ID
     */
    public static short getTileID(double lat, double lon)
    {
        int tileX = (int) Math.floor(lon * TILE_FACTOR);
        int tileY = (int) Math.floor(lat * TILE_FACTOR);
        return (short) ((tileX << 16) | (tileY & 0xFFFF));
    }

    /**
     * Returns the tile ID for the given coordinate.
     * @param coordinate is the coordinate
     * @return the tile ID
     */
    public static short getTileID(IGeoCoordinate coordinate)
    {
        return getTileID(coordinate.getLatitude(), coordinate.getLongitude());
    }

    /**
     * Returns all tileIDs in a given view box.
     * @param viewBox is the view box
     * @return the tile IDs
     */
    public static List<Short> getTileIDs(OpenRouterViewBox viewBox)
    {
        int minTileX = (int) Math.floor(viewBox.getMinLongitude() * TILE_FACTOR);
        int minTileY = (int) Math.floor(viewBox.getMinLatitude() * TILE_FACTOR);
        int maxTileX = (int) Math.floor(viewBox.getMaxLongitude() * TILE_FACTOR);
        int maxTileY = (int) Math.floor(viewBox.getMaxLatitude() * TILE_FACTOR);

        List<Short> tileIDs = new ArrayList<>();
        for (int x = minTileX; x <= maxTileX; x++)
        {
            for (int y = minTileY; y <= maxTileY; y++)
            {
                tileIDs.add((short) ((x << 16) | (y & 0xFFFF)));
            }
        }
        return tileIDs;
    }

    public static OpenRouterTileMap create(List<OpenRouterNode> nodes)
    {
        Map<Short, List<OpenRouterNode>> tileMap = new java.util.HashMap<>();
        for (OpenRouterNode node : nodes)
        {
            short tileID = getTileID(node.getCoordinate());
            tileMap.computeIfAbsent(tileID, k -> new ArrayList<>()).add(node);
        }
        return new OpenRouterTileMap(tileMap);
    }

    public static byte[] serializeTileMap(Map<Short, List<OpenRouterNode>> tileMap) throws IOException {
        ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(dataBuffer);

        int tileCount = tileMap.size();
        ByteArrayOutputStream tileData = new ByteArrayOutputStream();
        DataOutputStream tileOut = new DataOutputStream(tileData);

        // --- SAVE TILE INDEX ---
        OpenRouterLog.d(TAG, "Parsing tile index");
        List<IndexEntry> index = new ArrayList<>();

        for (Map.Entry<Short, List<OpenRouterNode>> entry : tileMap.entrySet()) {
            short tileId = entry.getKey();
            List<OpenRouterNode> nodes = entry.getValue();

            int offset = tileOut.size();

            for (OpenRouterNode node : nodes) {
                byte[] nodeBytes = node.toByteArray();
                tileOut.writeShort(nodeBytes.length);
                tileOut.write(nodeBytes);
            }

            int length = tileOut.size() - offset;
            index.add(new IndexEntry(tileId, offset, length));
        }

        // --- WRITE HEADER ---
        OpenRouterLog.d(TAG, "Parsing header");
        dataOut.writeShort(tileCount);
        for (IndexEntry entry : index) {
            dataOut.writeShort(entry.tileId);
            dataOut.writeInt(entry.offset);
            dataOut.writeInt(entry.length);
        }

        // --- WRITE TILE DATA ---
        OpenRouterLog.d(TAG, "Parsing tile data");
        dataOut.write(tileData.toByteArray());

        return dataBuffer.toByteArray();
    }

    public static List<OpenRouterNode> extractTile(byte[] data, short tileId) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        int tileCount = in.readUnsignedShort();

        // Read the index
        OpenRouterLog.d(TAG, "Parsing tile index");
        for (int i = 0; i < tileCount; i++) {
            short id = in.readShort();
            int offset = in.readInt();
            int length = in.readInt();
            if (id == tileId) {
                // Move the stream to the start of the tile data
                OpenRouterLog.d(TAG, "Parsing tile data");
                ByteArrayInputStream tileStream = new ByteArrayInputStream(data, in.available() - data.length + offset, length);
                DataInputStream tileIn = new DataInputStream(tileStream);

                List<OpenRouterNode> nodes = new ArrayList<>();
                while (tileStream.available() > 0) {
                    int len = tileIn.readUnsignedShort();
                    byte[] nodeBytes = new byte[len];
                    tileIn.readFully(nodeBytes);
                    nodes.add(OpenRouterNode.fromByteArray(nodeBytes));
                }
                return nodes;
            }
        }

        return Collections.emptyList(); // Tile nicht gefunden
    }


    public static class IndexEntry {
        short tileId;
        int offset;
        int length;

        IndexEntry(short tileId, int offset, int length) {
            this.tileId = tileId;
            this.offset = offset;
            this.length = length;
        }
    }
}
