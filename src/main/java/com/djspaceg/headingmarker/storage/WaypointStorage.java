package com.djspaceg.headingmarker.storage;

import com.djspaceg.headingmarker.HeadingMarkerMod;
import com.djspaceg.headingmarker.HeadingMarkerMod.WaypointData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.djspaceg.headingmarker.waypoint.TrackedWaypoint;
import com.djspaceg.headingmarker.waypoint.Waypoint;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WaypointStorage {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Optional.class, new OptionalTypeAdapter<>())
            .create();

    /**
     * Custom TypeAdapter for Optional to avoid Java module reflection issues
     */
    private static class OptionalTypeAdapter<T> extends TypeAdapter<Optional<T>> {
        @Override
        public void write(JsonWriter out, Optional<T> value) throws IOException {
            if (value.isEmpty()) {
                out.nullValue();
            } else {
                out.jsonValue(String.valueOf(value.get()));
            }
        }

        @Override
        public Optional<T> read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return Optional.empty();
            }
            @SuppressWarnings("unchecked")
            T value = (T) Integer.valueOf(in.nextInt());
            return Optional.of(value);
        }
    }

    /**
     * Save waypoints to storage. Structure: PlayerUUID -> Dimension -> Color -> WaypointData
     */
    public static void saveWaypoints(Path storageDir, Map<UUID, Map<String, Map<String, WaypointData>>> playerWaypoints) {
        for (Map.Entry<UUID, Map<String, Map<String, WaypointData>>> entry : playerWaypoints.entrySet()) {
            UUID playerUuid = entry.getKey();
            Map<String, Map<String, WaypointData>> dimensionWaypoints = entry.getValue();
            Path playerFile = storageDir.resolve(playerUuid.toString() + ".json");

            try (FileWriter writer = new FileWriter(playerFile.toFile())) {
                GSON.toJson(dimensionWaypoints, writer);
            } catch (IOException e) {
                HeadingMarkerMod.LOGGER.error("Failed to save waypoints for player {}", playerUuid, e);
            }
        }
    }

    /**
     * Load waypoints from storage. Structure: PlayerUUID -> Dimension -> Color -> WaypointData
     */
    public static Map<UUID, Map<String, Map<String, WaypointData>>> loadWaypoints(Path storageDir) {
        Map<UUID, Map<String, Map<String, WaypointData>>> playerWaypoints = new HashMap<>();

        try (var files = Files.list(storageDir)) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                String fileName = path.getFileName().toString();
                String uuidString = fileName.substring(0, fileName.length() - 5); // Remove .json extension

                try {
                    UUID playerUuid = UUID.fromString(uuidString);
                    try (FileReader reader = new FileReader(path.toFile())) {
                        Map<String, Map<String, WaypointData>> dimensionWaypoints = GSON.fromJson(
                                reader,
                                new TypeToken<Map<String, Map<String, WaypointData>>>() {}.getType()
                        );

                        if (dimensionWaypoints == null) {
                            dimensionWaypoints = new HashMap<>();
                        }

                        // Re-create tracked waypoints and set entity IDs to -1 as they are not persistent.
                        for (Map.Entry<String, Map<String, WaypointData>> dimEntry : dimensionWaypoints.entrySet()) {
                            String dimension = dimEntry.getKey();
                            Map<String, WaypointData> waypoints = dimEntry.getValue();

                            Map<String, WaypointData> recreatedWaypoints = new HashMap<>();
                            for (Map.Entry<String, WaypointData> wpEntry : waypoints.entrySet()) {
                                String colorKey = wpEntry.getKey();
                                WaypointData data = wpEntry.getValue();

                                // Migrate old "purple" key to "light_purple" to match new color names
                                String migratedKey = colorKey;
                                String migratedColor = data.color();
                                if ("purple".equals(colorKey)) {
                                    migratedKey = "light_purple";
                                    migratedColor = "light_purple";
                                    HeadingMarkerMod.LOGGER.info("Migrating waypoint key from 'purple' to 'light_purple' for player {} in {}", 
                                            playerUuid, dimension);
                                }

                                TrackedWaypoint wp = TrackedWaypoint.ofPos(
                                        playerUuid,
                                        new Waypoint.Config(),
                                        new net.minecraft.util.math.Vec3i((int)data.x(), (int)data.y(), (int)data.z())
                                );
                                recreatedWaypoints.put(migratedKey, new WaypointData(
                                        migratedColor,
                                        dimension,
                                        data.x(),
                                        data.y(),
                                        data.z(),
                                        wp,
                                        -1
                                ));
                            }
                            dimensionWaypoints.put(dimension, recreatedWaypoints);
                        }

                        playerWaypoints.put(playerUuid, dimensionWaypoints);
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
