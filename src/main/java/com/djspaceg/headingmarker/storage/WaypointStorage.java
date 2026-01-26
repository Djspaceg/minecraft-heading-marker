package com.djspaceg.headingmarker.storage;

import com.djspaceg.headingmarker.HeadingMarkerMod;
import com.djspaceg.headingmarker.HeadingMarkerMod.WaypointData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.waypoint.TrackedWaypoint;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WaypointStorage {
    private static final Gson GSON = new Gson();
    private static final String FILE_NAME = "headingmarker_waypoints.json";

    public static void save(MinecraftServer server, Map<UUID, Map<String, WaypointData>> playerWaypoints) {
        File file = getFile(server);
        // Save both waypoints and showDistance toggle
        Map<String, Object> root = new HashMap<>();
        Map<String, Map<String, int[]>> waypointsPart = new HashMap<>();
        for (var entry : playerWaypoints.entrySet()) {
            String uuid = entry.getKey().toString();
            Map<String, int[]> colorMap = new HashMap<>();
            for (var colorEntry : entry.getValue().entrySet()) {
                WaypointData data = colorEntry.getValue();
                colorMap.put(colorEntry.getKey(), new int[]{data.x(), data.y(), data.z()});
            }
            waypointsPart.put(uuid, colorMap);
        }
        root.put("waypoints", waypointsPart);
        // Save showDistance toggles
        Map<String, Boolean> showDistancePart = new HashMap<>();
        for (Map.Entry<UUID, Boolean> entry : HeadingMarkerMod.getPlayerShowDistanceMap().entrySet()) {
            showDistancePart.put(entry.getKey().toString(), entry.getValue());
        }
        root.put("showDistance", showDistancePart);
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            HeadingMarkerMod.LOGGER.error("Failed to save waypoints", e);
        }
    }

    public static Map<UUID, Map<String, WaypointData>> load(MinecraftServer server) {
        File file = getFile(server);
        Map<UUID, Map<String, WaypointData>> result = new HashMap<>();
        if (!file.exists()) return result;
        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> root = GSON.fromJson(reader, new TypeToken<Map<String, Object>>() {
            }.getType());
            if (root != null) {
                // Load waypoints
                Object waypointsObj = root.get("waypoints");
                if (waypointsObj instanceof Map<?, ?> data) {
                    for (Map.Entry<?, ?> entry : data.entrySet()) {
                        UUID uuid = UUID.fromString(entry.getKey().toString());
                        Map<String, WaypointData> colorMap = new HashMap<>();
                        Map<?, ?> colorData = (Map<?, ?>) entry.getValue();
                        for (Map.Entry<?, ?> colorEntry : colorData.entrySet()) {
                            List<?> pos = (List<?>) colorEntry.getValue();
                            int x = ((Number) pos.get(0)).intValue(), y = ((Number) pos.get(1)).intValue(), z = ((Number) pos.get(2)).intValue();
                            TrackedWaypoint wp = TrackedWaypoint.ofPos(uuid, null, new net.minecraft.util.math.Vec3i(x, y, z));
                            colorMap.put(colorEntry.getKey().toString(), new WaypointData(colorEntry.getKey().toString(), x, y, z, wp));
                        }
                        result.put(uuid, colorMap);
                    }
                }
                // Load showDistance toggles
                Object showDistanceObj = root.get("showDistance");
                if (showDistanceObj instanceof Map<?, ?> showDistanceMap) {
                    HeadingMarkerMod.getPlayerShowDistanceMap().clear();
                    for (Map.Entry<?, ?> entry : showDistanceMap.entrySet()) {
                        HeadingMarkerMod.getPlayerShowDistanceMap().put(UUID.fromString(entry.getKey().toString()), Boolean.TRUE.equals(entry.getValue()));
                    }
                }
            }
        } catch (IOException e) {
            HeadingMarkerMod.LOGGER.error("Failed to load waypoints", e);
        }
        return result;
    }

    private static File getFile(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(FILE_NAME).toFile();
    }
}
