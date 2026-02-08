package com.djspaceg.headingmarker;

import com.djspaceg.headingmarker.storage.WaypointStorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.djspaceg.headingmarker.waypoint.TrackedWaypoint;
import com.djspaceg.headingmarker.waypoint.Waypoint;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class HeadingMarkerMod implements ModInitializer {
    public static final String MOD_ID = "headingmarker";
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final Map<UUID, Map<String, WaypointData>> playerWaypoints = new HashMap<>();
    private static int tickCounter = 0;
    private static final int DISTANCE_UPDATE_INTERVAL = 5; // Update every 5 ticks (4 times per second)

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
     * Checks all worlds/dimensions since waypoint might be in a different dimension.
     */
    private static void removeWaypointEntity(ServerPlayerEntity player, String color) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);
        if (waypoints != null && waypoints.containsKey(color)) {
            WaypointData data = waypoints.get(color);
            if (data.entityId != -1) {
                // Check all worlds since waypoint might be in a different dimension
                for (ServerWorld world : player.getServer().getWorlds()) {
                    Entity entity = world.getEntityById(data.entityId);
                    if (entity instanceof ArmorStandEntity) {
                        entity.discard();
                        LOGGER.info("Removed waypoint entity for color {} in dimension {}", color, world.getRegistryKey().getValue());
                        break;
                    }
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

    private static String getEmojiForColor(String color) {
        switch (color.toLowerCase()) {
            case "red":
                return "🔴";
            case "blue":
                return "🔵";
            case "green":
                return "🟢";
            case "yellow":
                return "🟡";
            case "purple":
                return "🟣";
            default:
                return "⚪";
        }
    }

    private static Formatting getFormattingForColor(String color) {
        switch (color.toLowerCase()) {
            case "red":
                return Formatting.RED;
            case "blue":
                return Formatting.BLUE;
            case "green":
                return Formatting.GREEN;
            case "yellow":
                return Formatting.YELLOW;
            case "purple":
                return Formatting.LIGHT_PURPLE;
            default:
                return Formatting.WHITE;
        }
    }

    /**
     * Display distances to waypoints on the player's actionbar.
     */
    private static void displayDistances(ServerPlayerEntity player) {
        // Get player's current position
        Vec3d playerPos = player.getPos();
        
        // Get waypoints for this player
        Map<String, WaypointData> waypoints = playerWaypoints.get(player.getUuid());
        
        if (waypoints == null || waypoints.isEmpty()) {
            // No waypoints - clear actionbar
            player.sendMessage(Text.literal(""), true); // true = actionbar
            return;
        }
        
        // Build actionbar text with distances
        MutableText actionbarText = Text.literal("");
        boolean first = true;
        
        for (Map.Entry<String, WaypointData> entry : waypoints.entrySet()) {
            WaypointData waypoint = entry.getValue();
            
            // Calculate distance in meters (blocks)
            double dx = playerPos.x - waypoint.x();
            double dy = playerPos.y - waypoint.y();
            double dz = playerPos.z - waypoint.z();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            int distanceMeters = (int) Math.round(distance);
            
            // Add spacing between waypoints
            if (!first) {
                actionbarText.append(Text.literal("  "));
            }
            first = false;
            
            // Add colored emoji and distance
            String emoji = getEmojiForColor(waypoint.color());
            actionbarText.append(Text.literal(emoji + " " + distanceMeters + "m")
                .formatted(getFormattingForColor(waypoint.color())));
        }
        
        // Send to actionbar (true = actionbar, not chat)
        player.sendMessage(actionbarText, true);
    }

    /**
     * Clean up orphaned waypoint armor stand entities on server start.
     * This prevents duplicate waypoints after restart.
     */
    private static void cleanupOrphanedWaypointEntities(net.minecraft.server.MinecraftServer server) {
        int cleanedCount = 0;
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof ArmorStandEntity armorStand) {
                    // Check if this is a waypoint entity (has custom name ending with "waypoint")
                    if (armorStand.hasCustomName() && 
                        armorStand.getCustomName().getString().endsWith(" waypoint")) {
                        armorStand.discard();
                        cleanedCount++;
                    }
                }
            }
        }
        if (cleanedCount > 0) {
            LOGGER.info("Cleaned up {} orphaned waypoint entities from previous session", cleanedCount);
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

        // Setup scoreboard objectives for distance display on server start
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();
            
            // Create trigger objective for toggling distance display
            ScoreboardObjective triggerObj = scoreboard.getObjective("hm.distance");
            if (triggerObj == null) {
                scoreboard.addObjective("hm.distance", 
                    ScoreboardCriterion.TRIGGER, 
                    Text.literal("Toggle Distance Display"),
                    ScoreboardCriterion.RenderType.INTEGER,
                    false, null);
                LOGGER.info("Created hm.distance trigger objective");
            }
            
            // Create state objective for tracking distance display state
            ScoreboardObjective stateObj = scoreboard.getObjective("hm.show_distance");
            if (stateObj == null) {
                scoreboard.addObjective("hm.show_distance",
                    ScoreboardCriterion.DUMMY,
                    Text.literal("Show Distance State"),
                    ScoreboardCriterion.RenderType.INTEGER,
                    false, null);
                LOGGER.info("Created hm.show_distance state objective");
            }
        });

        // Load waypoints on server start and cleanup old waypoint entities
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Clean up any orphaned waypoint entities from previous sessions
            cleanupOrphanedWaypointEntities(server);
            
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
            
            // Enable trigger for this player
            Scoreboard scoreboard = server.getScoreboard();
            ScoreboardObjective triggerObj = scoreboard.getObjective("hm.distance");
            if (triggerObj != null) {
                ScoreAccess scoreAccess = scoreboard.getPlayerScore(handler.player, triggerObj);
                scoreAccess.unlock(); // Enable the trigger for this player
            }
        });

        // Server tick handler for distance display and trigger detection
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();
            ScoreboardObjective triggerObj = scoreboard.getObjective("hm.distance");
            ScoreboardObjective stateObj = scoreboard.getObjective("hm.show_distance");
            
            if (triggerObj == null || stateObj == null) {
                return; // Objectives not created yet
            }
            
            // Increment tick counter for distance update throttling
            tickCounter++;
            boolean shouldUpdateDistances = (tickCounter % DISTANCE_UPDATE_INTERVAL == 0);
            
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Check if player triggered hm.distance
                ScoreAccess triggerScore = scoreboard.getPlayerScore(player, triggerObj);
                
                if (triggerScore.getScore() > 0) {
                    // Player triggered the command - toggle their state
                    ScoreAccess stateScore = scoreboard.getPlayerScore(player, stateObj);
                    
                    int currentState = stateScore.getScore();
                    int newState = (currentState == 0) ? 1 : 0; // Toggle 0->1 or 1->0
                    stateScore.setScore(newState);
                    
                    // Send confirmation message
                    if (newState == 1) {
                        player.sendMessage(Text.literal("Distance display enabled")
                            .formatted(Formatting.GREEN), false);
                    } else {
                        player.sendMessage(Text.literal("Distance display disabled")
                            .formatted(Formatting.YELLOW), false);
                        // Clear any existing distance text from the actionbar
                        player.sendMessage(Text.empty(), true);
                        // Clear any existing distance text from the actionbar
                        player.sendMessage(Text.empty(), true);
                    }
                    
                    // Reset trigger (ready for next use)
                    triggerScore.setScore(0);
                    
                    // Re-enable trigger for this player
                    triggerScore.unlock();
                }
                
                // Update distance display for players who have it enabled (throttled)
                if (shouldUpdateDistances) {
                    ScoreAccess stateScore = scoreboard.getPlayerScore(player, stateObj);
                    if (stateScore.getScore() == 1) {
                        // Distance display is enabled for this player
                        displayDistances(player);
                    }
                }
            }
        });
    }

    // Per-player waypoint storage: UUID -> (color -> WaypointData)
    public record WaypointData(String color, int x, int y, int z, TrackedWaypoint waypoint, int entityId) {
    }
}
