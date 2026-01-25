package com.djspaceg.headingmarker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class HeadingMarkerState {
    private static HeadingMarkerState instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Map<UUID, Map<String, Waypoint>> playerWaypoints = new HashMap<>();
    private static File lastSaveFile;

    public static HeadingMarkerState getServerState(MinecraftServer server) {
        if (instance == null) {
            instance = load(server);
        }
        return instance;
    }

    public void markDirty() {
        if (lastSaveFile != null) {
            save(lastSaveFile);
        }
    }

    private static HeadingMarkerState load(MinecraftServer server) {
        File savePath = server.getSavePath(WorldSavePath.ROOT).resolve("data").toFile();
        if (!savePath.exists()) {
            savePath.mkdirs();
        }
        File file = new File(savePath, "headingmarker_v2.json");
        lastSaveFile = file;

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<UUID, Map<String, Waypoint>>>() {
                }.getType();
                Map<UUID, Map<String, Waypoint>> data = GSON.fromJson(reader, type);
                HeadingMarkerState state = new HeadingMarkerState();
                if (data != null) {
                    state.playerWaypoints = data;
                }
                return state;
            } catch (IOException e) {
                HeadingMarkerMod.LOGGER.error("Failed to load heading markers", e);
            }
        }
        return new HeadingMarkerState();
    }

    private void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(playerWaypoints, writer);
        } catch (IOException e) {
            HeadingMarkerMod.LOGGER.error("Failed to save heading markers", e);
        }
    }

    public Map<String, Waypoint> getWaypoints(UUID playerId) {
        return playerWaypoints.computeIfAbsent(playerId, k -> new HashMap<>());
    }
}
