package com.djspaceg.headingmarker;

import com.djspaceg.headingmarker.storage.WaypointStorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;

import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import com.djspaceg.headingmarker.waypoint.TrackedWaypoint;
import com.djspaceg.headingmarker.waypoint.Waypoint;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeadingMarkerMod implements ModInitializer {
    public static final String MOD_ID = "headingmarker";
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    static final Map<UUID, Boolean> playerShowDistance = new HashMap<>();
    private static final Map<UUID, Map<String, WaypointData>> playerWaypoints = new HashMap<>();

    public static Map<UUID, Boolean> getPlayerShowDistanceMap() {
        return playerShowDistance;
    }

    public static boolean getShowDistance(UUID playerUuid) {
        return playerShowDistance.getOrDefault(playerUuid, false);
    }

    public static void setShowDistance(ServerPlayerEntity player, boolean value) {
        playerShowDistance.put(player.getUuid(), value);
        LOGGER.info("Set showDistance={} for player={}", value, player.getName().getString());
    }
    // For non-player contexts (loading from disk)
    public static void setShowDistance(UUID playerUuid, boolean value) {
        playerShowDistance.put(playerUuid, value);
    }

    /**
     * Create and track a waypoint for a player.
     * Server-side storage only. For rendering, use the datapack.
     */
    public static TrackedWaypoint createWaypoint(ServerPlayerEntity player, String color, double x, double y, double z) {
        UUID playerUuid = player.getUuid();
        LOGGER.info("Creating waypoint: color={}, pos=({},{},{}), player={}", color, x, y, z, player.getName().getString());
        Waypoint.Config config = new Waypoint.Config();
        config.color = java.util.Optional.of(getColorInt(color));
        Vec3i pos = new Vec3i((int) x, (int) y, (int) z);
        TrackedWaypoint waypoint = TrackedWaypoint.ofPos(playerUuid, config, pos);
        WaypointData data = new WaypointData(color, pos.getX(), pos.getY(), pos.getZ(), waypoint);
        playerWaypoints.computeIfAbsent(playerUuid, k -> new HashMap<>()).put(color, data);
        LOGGER.info("Waypoint stored server-side");
        return waypoint;
    }

    /**
     * Remove a waypoint for a player.
     */
    public static boolean removeWaypoint(ServerPlayerEntity player, String color) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);
        if (waypoints != null && waypoints.containsKey(color)) {
            waypoints.remove(color);
            LOGGER.info("Removed waypoint: color={}", color);
            return true;
        }
        return false;
    }

    /**
     * Get all waypoints for a player.
     */
    public static Map<String, WaypointData> getWaypoints(UUID playerUuid) {
        return playerWaypoints.getOrDefault(playerUuid, new HashMap<>());
    }

    private static int getColorInt(String color) {
        switch (color.toLowerCase()) {
            case "red":
                return 0xFF0000;
            case "blue":
                return 0x5555FF;
            case "green":
                return 0x55FF55;
            case "yellow":
                return 0xFFFF55;
            case "purple":
                return 0xFF55FF;
            default:
                return 0xFFFFFF;
        }
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Heading Marker Mod 1.0.6 Initializing (Server-Only)...");

        // Register commands
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register);

        // Register custom ColorArgumentType directly
        ArgumentTypeRegistry.registerArgumentType(Identifier.of(MOD_ID, "color"), ColorArgumentType.class, ConstantArgumentSerializer.of(ColorArgumentType::color));
        LOGGER.info("Registered ColorArgumentType for command serialization");

        // Load waypoints on server start
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            playerWaypoints.clear();
            playerWaypoints.putAll(WaypointStorage.load(server));
            LOGGER.info("Loaded waypoints from disk: {} players", playerWaypoints.size());
        });

        // Also run a post-start command repair in case other mods overwrote or replaced our /hm node
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                HeadingMarkerCommands.ensureRegistered(server.getCommandManager().getDispatcher());
                LOGGER.info("Ran post-start command repair for /hm");
            } catch (Throwable t) {
                LOGGER.warn("Failed to repair /hm command tree on server start: {}", t.toString());
            }
        });

        // Save waypoints on server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            WaypointStorage.save(server, playerWaypoints);
            LOGGER.info("Saved waypoints to disk");
        });

        // Run command repair on player join to ensure client receives full tree
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            try {
                HeadingMarkerCommands.ensureRegistered(server.getCommandManager().getDispatcher());
            } catch (Throwable t) {
                LOGGER.warn("Failed to repair /hm command tree on player join: {}", t.toString());
            }
        });
    }

    // Per-player waypoint storage: UUID -> (color -> WaypointData)
    public record WaypointData(String color, int x, int y, int z, TrackedWaypoint waypoint) {
    }
}
