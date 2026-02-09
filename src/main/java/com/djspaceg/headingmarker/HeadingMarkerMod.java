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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class HeadingMarkerMod implements ModInitializer {
    public static final String MOD_ID = "headingmarker";
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final Map<UUID, Map<String, WaypointData>> playerWaypoints = new HashMap<>();
    private static final Map<UUID, String> lastDistanceText = new HashMap<>(); // Cache for change detection
    private static int tickCounter = 0;
    private static final int DISTANCE_UPDATE_INTERVAL = 5; // Update every 5 ticks (4 times per second)

    /**
     * Create and track a waypoint for a player using vanilla waypoint system.
     * Creates an invisible armor stand entity with waypoint_transmit_range attribute.
     */
    public static void createWaypoint(ServerPlayerEntity player, String color, double x, double y, double z) {
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
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        ArmorStandEntity armorStand = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
        armorStand.setPosition(x, y, z);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setNoGravity(true);
        armorStand.setSilent(true);
        // Note: setMarker() is private in this version, using reflection or accepting without marker flag
        armorStand.setCustomName(net.minecraft.text.Text.literal(color + " waypoint"));
        
        // Note: Entity persistence and NBT tagging removed as APIs changed in 1.21.11
        // Waypoints will be recreated on player join from storage

        // Set waypoint transmit range attribute
        // This makes vanilla clients render the waypoint in the Locator Bar
        try {
            EntityAttributeInstance waypointAttr = armorStand.getAttributeInstance(EntityAttributes.WAYPOINT_TRANSMIT_RANGE);
            if (waypointAttr != null) {
                waypointAttr.setBaseValue(999999.0);
                LOGGER.info("Set waypoint transmit range to 999999 for {} waypoint", color);
            } else {
                LOGGER.warn("Armor stand doesn't have waypoint_transmit_range attribute");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set waypoint attribute: {}", e.getMessage(), e);
        }
        
        // Spawn the entity
        world.spawnEntity(armorStand);
        
        // Store reference to entity for later removal
        WaypointData data = new WaypointData(color, pos.getX(), pos.getY(), pos.getZ(), waypoint, armorStand.getId());
        playerWaypoints.computeIfAbsent(playerUuid, k -> new HashMap<>()).put(color, data);
        
        LOGGER.info("Created waypoint entity at ({},{},{})", x, y, z);
    }
    
    /**
     * Remove waypoint entity from the world.
     * Note: Each dimension tracks waypoints separately (red in overworld ≠ red in nether).
     */
    private static void removeWaypointEntity(ServerPlayerEntity player, String color) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);
        if (waypoints != null && waypoints.containsKey(color)) {
            WaypointData data = waypoints.get(color);
            if (data.entityId != -1) {
                // Only check player's current world since dimensions track separately
                ServerWorld world = (ServerWorld) player.getEntityWorld();
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
     * Remove all waypoint entities for a player across all worlds.
     * Called when player disconnects to clean up their markers.
     */
    private static void removeAllPlayerWaypointEntities(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);
        
        if (waypoints == null || waypoints.isEmpty()) {
            return;
        }
        
        int removedCount = 0;
        
        // Method 1: Remove by entity ID (fastest if loaded)
        for (Map.Entry<String, WaypointData> entry : waypoints.entrySet()) {
            WaypointData data = entry.getValue();
            if (data.entityId != -1) {
                ServerWorld world = (ServerWorld) player.getEntityWorld();
                Entity entity = world.getEntityById(data.entityId);
                if (entity instanceof ArmorStandEntity) {
                    entity.discard();
                    removedCount++;
                }
            }
        }
        
        // Method 2: Scan for any remaining entities with player's UUID in NBT
        // This catches entities that might not have been tracked by ID
        // Note: NBT-based identification removed as writeNbt() API changed
        // Relying on tracked entity IDs only
        /*
        for (ServerWorld world : player.server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof ArmorStandEntity armorStand) {
                    var nbt = armorStand.writeNbt(new net.minecraft.nbt.NbtCompound());
                    if (nbt.contains("HeadingMarkerOwner")) {
                        String ownerUuid = nbt.getString("HeadingMarkerOwner");
                        if (playerUuid.toString().equals(ownerUuid)) {
                            armorStand.discard();
                            removedCount++;
                        }
                    }
                }
            }
        }
        */

        if (removedCount > 0) {
            LOGGER.info("Cleaned up {} waypoint entities for disconnecting player {}", 
                removedCount, player.getName().getString());
        }
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
        return switch (color.toLowerCase()) {
            case "red" -> 0xFF0000;
            case "blue" -> 0x5555FF;
            case "green" -> 0x55FF55;
            case "yellow" -> 0xFFFF55;
            case "purple" -> 0xFF55FF;
            default -> 0xFFFFFF;
        };
    }

    private static String getEmojiForColor(String color) {
        return switch (color.toLowerCase()) {
            case "red" -> "🔴";
            case "blue" -> "🔵";
            case "green" -> "🟢";
            case "yellow" -> "🟡";
            case "purple" -> "🟣";
            default -> "⚪";
        };
    }

    private static Formatting getFormattingForColor(String color) {
        return switch (color.toLowerCase()) {
            case "red" -> Formatting.RED;
            case "blue" -> Formatting.BLUE;
            case "green" -> Formatting.GREEN;
            case "yellow" -> Formatting.YELLOW;
            case "purple" -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };
    }

    /**
     * Display distances to waypoints on the player's actionbar.
     * Only sends update if the text has changed since last update.
     */
    private static void displayDistances(ServerPlayerEntity player) {
        // Get player's current position
        Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());

        // Get waypoints for this player
        Map<String, WaypointData> waypoints = playerWaypoints.get(player.getUuid());
        
        String newDistanceText;
        if (waypoints == null || waypoints.isEmpty()) {
            newDistanceText = ""; // Empty string for no waypoints
        } else {
            // Build actionbar text with distances
            StringBuilder textBuilder = new StringBuilder();
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
                    textBuilder.append("  ");
                }
                first = false;
                
                // Add colored emoji and distance
                String emoji = getEmojiForColor(waypoint.color());
                textBuilder.append(emoji).append(" ").append(distanceMeters).append("m");
            }
            
            newDistanceText = textBuilder.toString();
        }
        
        // Only send update if text has changed
        UUID playerUuid = player.getUuid();
        String lastText = lastDistanceText.get(playerUuid);
        
        if (!newDistanceText.equals(lastText)) {
            // Text changed - send update
            if (newDistanceText.isEmpty()) {
                // No waypoints - clear actionbar
                player.sendMessage(Text.literal(""), true);
            } else {
                // Build formatted text from the string
                MutableText actionbarText = Text.literal("");
                boolean first = true;
                
                for (Map.Entry<String, WaypointData> entry : waypoints.entrySet()) {
                    WaypointData waypoint = entry.getValue();
                    
                    // Calculate distance again for formatting
                    double dx = playerPos.x - waypoint.x();
                    double dy = playerPos.y - waypoint.y();
                    double dz = playerPos.z - waypoint.z();
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    int distanceMeters = (int) Math.round(distance);
                    
                    if (!first) {
                        actionbarText.append(Text.literal("  "));
                    }
                    first = false;
                    
                    String emoji = getEmojiForColor(waypoint.color());
                    actionbarText.append(Text.literal(emoji + " " + distanceMeters + "m")
                        .formatted(getFormattingForColor(waypoint.color())));
                }
                
                player.sendMessage(actionbarText, true);
            }
            
            // Update cache
            lastDistanceText.put(playerUuid, newDistanceText);
        }
    }

    /**
     * Clean up orphaned waypoint armor stand entities on server start.
     * Note: NBT-based cleanup removed as writeNbt() API changed in 1.21.11.
     * Entities will be cleaned up through tracked IDs and player disconnect handlers.
     */
    private static void cleanupOrphanedWaypointEntities(net.minecraft.server.MinecraftServer server) {
        // Method removed - relying on disconnect handlers and tracked entity IDs
        LOGGER.info("Waypoint cleanup handled through disconnect handlers");
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Heading Marker Mod 1.0.6 Initializing (Server-Only)...");

        // Log runtime context so you can see exactly which instance is running.
        try {
            ModContainer container = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null);
            String version = (container != null) ? container.getMetadata().getVersion().getFriendlyString() : "unknown";
            LOGGER.info("HeadingMarker runtime: user.dir={}, modVersion={}", System.getProperty("user.dir"), version);
            if (container != null) {
                LOGGER.info("HeadingMarker origin paths: {}", container.getOrigin().getPaths());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to log runtime context", e);
        }

        // Write a marker file so you can confirm the mod actually loaded.
        try {
            Path marker = Paths.get("headingmarker.installed");
            if (!Files.exists(marker)) {
                Files.writeString(marker, "headingmarker:installed\n");
            }
            LOGGER.info("HeadingMarkerMod install marker written at {}", marker.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.warn("Failed to write install marker file", e);
        }

        // Register commands
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register);

        // Register custom ColorArgumentType directly
        ArgumentTypeRegistry.registerArgumentType(Identifier.of(MOD_ID, "color"), ColorArgumentType.class, ConstantArgumentSerializer.of(ColorArgumentType::color));
        LOGGER.info("Registered ColorArgumentType for command serialization");

        // Setup scoreboard objectives for distance display on server start
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();
            
            // Create trigger objective for toggling distance display
            ScoreboardObjective triggerObj = scoreboard.getNullableObjective("hm.distance");
            if (triggerObj == null) {
                scoreboard.addObjective("hm.distance", 
                    ScoreboardCriterion.TRIGGER, 
                    Text.literal("Toggle Distance Display"),
                    ScoreboardCriterion.RenderType.INTEGER,
                    false, null);
                LOGGER.info("Created hm.distance trigger objective");
            }
            
            // Create state objective for tracking distance display state
            ScoreboardObjective stateObj = scoreboard.getNullableObjective("hm.show_distance");
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

        // Clean up waypoint entities when player disconnects
        // This also fires automatically on server stop/restart
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            removeAllPlayerWaypointEntities(player);
            
            // Clear distance display cache for this player
            lastDistanceText.remove(player.getUuid());
            
            LOGGER.info("Cleaned up waypoint entities for disconnecting player: {}", 
                player.getName().getString());
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
            ScoreboardObjective triggerObj = scoreboard.getNullableObjective("hm.distance");
            if (triggerObj != null) {
                ScoreAccess scoreAccess = scoreboard.getOrCreateScore(handler.player, triggerObj);
                scoreAccess.unlock(); // Enable the trigger for this player
            }
        });

        // Server tick handler for distance display and trigger detection
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();
            ScoreboardObjective triggerObj = scoreboard.getNullableObjective("hm.distance");
            ScoreboardObjective stateObj = scoreboard.getNullableObjective("hm.show_distance");

            if (triggerObj == null || stateObj == null) {
                return; // Objectives not created yet
            }
            
            // Increment tick counter for distance update throttling
            tickCounter++;
            boolean shouldUpdateDistances = (tickCounter % DISTANCE_UPDATE_INTERVAL == 0);
            
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Check if player triggered hm.distance
                ScoreAccess triggerScore = scoreboard.getOrCreateScore(player, triggerObj);

                if (triggerScore.getScore() > 0) {
                    // Player triggered the command - toggle their state
                    ScoreAccess stateScore = scoreboard.getOrCreateScore(player, stateObj);

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
                        // Clear the cache for this player
                        lastDistanceText.remove(player.getUuid());
                    }
                    
                    // Reset trigger (ready for next use)
                    triggerScore.setScore(0);
                    
                    // Re-enable trigger for this player
                    triggerScore.unlock();
                }
                
                // Update distance display for players who have it enabled (throttled)
                if (shouldUpdateDistances) {
                    ScoreAccess stateScore = scoreboard.getOrCreateScore(player, stateObj);
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
