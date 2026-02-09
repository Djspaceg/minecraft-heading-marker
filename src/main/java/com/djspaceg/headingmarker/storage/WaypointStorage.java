package com.djspaceg.headingmarker.storage;

import com.djspaceg.headingmarker.HeadingMarkerMod;
import com.djspaceg.headingmarker.HeadingMarkerMod.WaypointData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.djspaceg.headingmarker.waypoint.TrackedWaypoint;
import com.djspaceg.headingmarker.waypoint.Waypoint;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WaypointStorage {

    private static final Gson GSON = new Gson();
    private static final Type WAYPOINT_MAP_TYPE = new TypeToken<Map<UUID, Map<String, WaypointData>>>() {}.getType();

    public static void saveWaypoints(Path storageDir, Map<UUID, Map<String, WaypointData>> playerWaypoints) {
        for (Map.Entry<UUID, Map<String, WaypointData>> entry : playerWaypoints.entrySet()) {
            UUID playerUuid = entry.getKey();
            Map<String, WaypointData> waypoints = entry.getValue();
            Path playerFile = storageDir.resolve(playerUuid.toString() + ".json");

            try (FileWriter writer = new FileWriter(playerFile.toFile())) {
                GSON.toJson(waypoints, writer);
            } catch (IOException e) {
                HeadingMarkerMod.LOGGER.error("Failed to save waypoints for player {}", playerUuid, e);
            }
        }
    }

    public static Map<UUID, Map<String, WaypointData>> loadWaypoints(Path storageDir) {
        Map<UUID, Map<String, WaypointData>> playerWaypoints = new HashMap<>();

        try (var files = Files.list(storageDir)) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                String fileName = path.getFileName().toString();
                String uuidString = fileName.substring(0, fileName.length() - 5); // Remove .json extension

                try {
                    UUID playerUuid = UUID.fromString(uuidString);
                    try (FileReader reader = new FileReader(path.toFile())) {
                        Map<String, WaypointData> waypoints = GSON.fromJson(reader, new TypeToken<Map<String, WaypointData>>() {}.getType());

                        // Re-create tracked waypoints and set entity IDs to -1 as they are not persistent.
                        waypoints.forEach((color, data) -> {
                            TrackedWaypoint wp = TrackedWaypoint.ofPos(playerUuid, new Waypoint.Config(), new net.minecraft.util.math.Vec3i((int)data.x(), (int)data.y(), (int)data.z()));
                            waypoints.put(color, new WaypointData(data.color(), data.x(), data.y(), data.z(), wp, -1));
                        });

                        playerWaypoints.put(playerUuid, waypoints);
                    } catch (IOException e) {
                        HeadingMarkerMod.LOGGER.error("Failed to load waypoints from file: {}", path, e);
                    }
                } catch (IllegalArgumentException e) {
                    HeadingMarkerMod.LOGGER.warn("Invalid UUID in file name: {}", fileName);
                }
            });
        } catch (IOException e) {
            HeadingMarkerMod.LOGGER.error("Failed to list waypoint files in storage directory.", e);
        }

        return playerWaypoints;
    }
}
