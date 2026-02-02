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
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.entity.Entity;
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
     * Create and track a waypoint for a player using vanilla waypoint system.
     * Creates an invisible armor stand entity with waypoint_transmit_range attribute.
     */
    public static TrackedWaypoint createWaypoint(ServerPlayerEntity player, String color, double x, double y, double z) {
        UUID playerUuid = player.getUuid();
        LOGGER.info("Creating waypoint: color={}, pos=({},{},{}), player={}", color, x, y, z, player.getName().getString());
        
        // Store waypoint data for persistence
        Waypoint.Config config = new Waypoint.Config();
        config.color = java.util.Optional.of(getColorInt(color));
        Vec3i pos = new Vec3i((int) x, (int) y, (int) z);
        TrackedWaypoint waypoint = TrackedWaypoint.ofPos(playerUuid, config, pos);
        
        // Remove existing waypoint entity if present
        removeWaypointEntity(player, color);
        
        // Create armor stand entity for vanilla waypoint rendering
        ServerWorld world = player.getServerWorld();
        ArmorStandEntity armorStand = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
        armorStand.setPosition(x, y, z);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setNoGravity(true);
        armorStand.setSilent(true);
        armorStand.setMarker(true);
        armorStand.setCustomName(net.minecraft.text.Text.literal(color + " waypoint"));
        
        // Try to set waypoint_transmit_range attribute
        // This makes vanilla clients render the waypoint in the Locator Bar
        try {
            // Look for vanilla waypoint transmission range attribute
            Identifier waypointRangeId = Identifier.of("minecraft", "waypoint_transmission_range");
            RegistryKey<net.minecraft.entity.attribute.EntityAttribute> attributeKey = 
                RegistryKey.of(RegistryKeys.ATTRIBUTE, waypointRangeId);
            
            // Try to get the attribute from registry
            var registry = world.getRegistryManager().getOrThrow(RegistryKeys.ATTRIBUTE);
            var attribute = registry.get(waypointRangeId);
            
            if (attribute != null) {
                EntityAttributeInstance instance = armorStand.getAttributes().getCustomInstance(attribute);
                if (instance != null) {
                    instance.setBaseValue(999999.0);
                    LOGGER.info("Set waypoint_transmission_range attribute");
                }
            } else {
                LOGGER.warn("waypoint_transmission_range attribute not found in registry");
            }
        } catch (Exception e) {
            LOGGER.warn("Could not set waypoint attribute: {}", e.getMessage());
        }
        
        // Spawn the entity
        world.spawnEntity(armorStand);
        
        // Store reference to entity for later removal
        WaypointData data = new WaypointData(color, pos.getX(), pos.getY(), pos.getZ(), waypoint, armorStand.getId());
        playerWaypoints.computeIfAbsent(playerUuid, k -> new HashMap<>()).put(color, data);
        
        LOGGER.info("Created waypoint entity at ({},{},{})", x, y, z);
        return waypoint;
    }
    
    /**
     * Remove waypoint entity from the world.
     */
    private static void removeWaypointEntity(ServerPlayerEntity player, String color) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);
        if (waypoints != null && waypoints.containsKey(color)) {
            WaypointData data = waypoints.get(color);
            if (data.entityId != -1) {
                ServerWorld world = player.getServerWorld();
                Entity entity = world.getEntityById(data.entityId);
                if (entity instanceof ArmorStandEntity) {
                    entity.discard();
                    LOGGER.info("Removed waypoint entity for color {}", color);
                }
            }
        }
    }

    /**
     * Remove a waypoint for a player.
     */
    public static boolean removeWaypoint(ServerPlayerEntity player, String color) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);
        if (waypoints != null && waypoints.containsKey(color)) {
            // Remove the entity first
            removeWaypointEntity(player, color);
            // Remove from storage
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
    
    /**
     * Recreate waypoint entities for a player (called on join).
     */
    public static void recreateWaypointEntities(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);
        if (waypoints == null || waypoints.isEmpty()) return;
        
        LOGGER.info("Recreating {} waypoint entities for player {}", waypoints.size(), player.getName().getString());
        
        for (Map.Entry<String, WaypointData> entry : waypoints.entrySet()) {
            String color = entry.getKey();
            WaypointData data = entry.getValue();
            
            // Recreate the waypoint entity
            try {
                createWaypoint(player, color, data.x(), data.y(), data.z());
            } catch (Exception e) {
                LOGGER.error("Failed to recreate waypoint entity for color {}: {}", color, e.getMessage());
            }
        }
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

        // Run command repair and recreate waypoint entities on player join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            try {
                HeadingMarkerCommands.ensureRegistered(server.getCommandManager().getDispatcher());
            } catch (Throwable t) {
                LOGGER.warn("Failed to repair /hm command tree on player join: {}", t.toString());
            }
            
            // Recreate waypoint entities for this player
            recreateWaypointEntities(handler.player);
        });
    }

    // Per-player waypoint storage: UUID -> (color -> WaypointData)
    public record WaypointData(String color, int x, int y, int z, TrackedWaypoint waypoint, int entityId) {
    }
}
