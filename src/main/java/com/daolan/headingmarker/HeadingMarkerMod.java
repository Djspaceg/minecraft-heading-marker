package com.daolan.headingmarker;

import com.daolan.headingmarker.storage.WaypointStorage;
import com.daolan.headingmarker.waypoint.TrackedWaypoint;
import com.daolan.headingmarker.waypoint.Waypoint;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;

public class HeadingMarkerMod implements ModInitializer {
    public static final String MOD_ID = "headingmarker";
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Map structure: PlayerUUID -> (DimensionKey -> (Color -> WaypointData))
    // This supports 5 colors × 3 dimensions × per-player = up to 15 markers per player
    private static final Map<UUID, Map<String, Map<String, WaypointData>>> playerWaypoints = new HashMap<>();
    private static final Map<UUID, String> lastDistanceText = new HashMap<>(); // Cache to prevent sending redundant action bar updates
    private static int tickCounter = 0;
    private static final int DISTANCE_UPDATE_INTERVAL = 5; // Update every 5 ticks (4 times per second)

    // Dimension constants for easy reference
    public static final String DIM_OVERWORLD = "overworld";
    public static final String DIM_NETHER = "the_nether";
    public static final String DIM_END = "the_end";

    public record WaypointData(String color, String dimension, double x, double y, double z, TrackedWaypoint trackedWaypoint, int entityId) {}

    /**
     * Get the dimension key string from a World's registry key
     */
    public static String getDimensionKey(ResourceKey<Level> worldKey) {
        return worldKey.identifier().getPath(); // Returns "overworld", "the_nether", or "the_end"
    }

    public enum WaypointColor {
        RED("red", 0xFF0000, "🔴", ChatFormatting.RED),
        BLUE("blue", 0x5555FF, "🔵", ChatFormatting.BLUE),
        GREEN("green", 0x55FF55, "🟢", ChatFormatting.GREEN),
        YELLOW("yellow", 0xFFFF55, "🟡", ChatFormatting.YELLOW),
        PURPLE("light_purple", 0xFF55FF, "🟣", ChatFormatting.LIGHT_PURPLE),
        WHITE("white", 0xFFFFFF, "⚪", ChatFormatting.WHITE);

        public final String name;
        public final int colorInt;
        public final String emoji;
        public final ChatFormatting formatting;

        private static final Map<String, WaypointColor> BY_NAME = new HashMap<>();

        static {
            for (WaypointColor c : values()) {
                BY_NAME.put(c.name, c);
            }
            // Add "purple" as an alias for "light_purple" for backward compatibility
            BY_NAME.put("purple", PURPLE);
        }

        WaypointColor(String name, int colorInt, String emoji, ChatFormatting formatting) {
            this.name = name;
            this.colorInt = colorInt;
            this.emoji = emoji;
            this.formatting = formatting;
        }

        public static WaypointColor fromString(String name) {
            return BY_NAME.getOrDefault(name.toLowerCase(), WHITE);
        }
    }

    public static void createWaypoint(ServerPlayer player, String colorName, double x, double y, double z) {
        UUID playerUuid = player.getUUID();
        WaypointColor color = WaypointColor.fromString(colorName);
        ServerLevel world = player.level();
        String dimension = getDimensionKey(world.dimension());

        LOGGER.info("Creating waypoint: color={}, dimension={}, pos=({},{},{}), player={}",
                color.name, dimension, x, y, z, player.getName().getString());

        removeWaypointEntityInWorld(world, playerUuid, color.name, dimension);

        ArmorStand armorStand = spawnAndConfigureWaypointEntity(world, color, x, y, z);
        if (armorStand == null) {
            LOGGER.error("Aborting waypoint creation due to entity spawn failure.");
            return;
        }

        setWaypointColorWithCommand(world, armorStand, color);
        setWaypointViewersWithCommand(world, armorStand, player.getName().getString());

        Vec3i pos = new Vec3i((int) x, (int) y, (int) z);
        Waypoint.Config config = new Waypoint.Config();
        config.color = java.util.Optional.of(color.colorInt);
        TrackedWaypoint trackedWaypoint = TrackedWaypoint.ofPos(playerUuid, config, pos);

        WaypointData data = new WaypointData(color.name, dimension, pos.getX(), pos.getY(), pos.getZ(), trackedWaypoint, armorStand.getId());
        playerWaypoints
                .computeIfAbsent(playerUuid, k -> new HashMap<>())
                .computeIfAbsent(dimension, k -> new HashMap<>())
                .put(color.name, data);

        LOGGER.info("Successfully created waypoint entity for color {} in {} at ({},{},{})", color.name, dimension, x, y, z);
    }

    /**
     * Create a waypoint in a specific world/dimension without requiring the player to be in that world.
     * Used for recreating waypoints across all dimensions when a player joins.
     */
    private static void createWaypointInWorld(ServerLevel world, UUID playerUuid, String colorName, double x, double y, double z) {
        WaypointColor color = WaypointColor.fromString(colorName);
        String dimension = getDimensionKey(world.dimension());

        removeWaypointEntityInWorld(world, playerUuid, color.name, dimension);

        ArmorStand armorStand = spawnAndConfigureWaypointEntity(world, color, x, y, z);
        if (armorStand == null) {
            LOGGER.error("Aborting waypoint creation due to entity spawn failure in {}", dimension);
            return;
        }

        setWaypointColorWithCommand(world, armorStand, color);

        // Restrict the waypoint entity to be visible only to its owner player
        net.minecraft.server.MinecraftServer server = world.getServer();
        if (server != null) {
            ServerPlayer owner = server.getPlayerList().getPlayer(playerUuid);
            if (owner != null) {
                setWaypointViewersWithCommand(world, armorStand, owner.getName().getString());
            }
        }

        Vec3i pos = new Vec3i((int) x, (int) y, (int) z);
        Waypoint.Config config = new Waypoint.Config();
        config.color = java.util.Optional.of(color.colorInt);
        TrackedWaypoint trackedWaypoint = TrackedWaypoint.ofPos(playerUuid, config, pos);

        WaypointData data = new WaypointData(color.name, dimension, pos.getX(), pos.getY(), pos.getZ(), trackedWaypoint, armorStand.getId());
        playerWaypoints
                .computeIfAbsent(playerUuid, k -> new HashMap<>())
                .computeIfAbsent(dimension, k -> new HashMap<>())
                .put(color.name, data);
    }

    private static ArmorStand spawnAndConfigureWaypointEntity(ServerLevel world, WaypointColor color, double x, double y, double z) {
        ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, world);
        armorStand.setPos(x, y, z);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setNoGravity(true);
        armorStand.setSilent(true);
        armorStand.setCustomName(Component.literal(color.name + " waypoint"));

        try {
            AttributeInstance waypointAttr = armorStand.getAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE);
            if (waypointAttr != null) {
                waypointAttr.setBaseValue(999999.0);
            } else {
                LOGGER.warn("Could not set waypoint transmit range: attribute not found for ArmorStand.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set waypoint attribute", e);
        }

        if (world.addFreshEntity(armorStand)) {
            return armorStand;
        } else {
            LOGGER.error("Failed to spawn waypoint entity for color {}", color.name);
            return null;
        }
    }

    private static void setWaypointColorWithCommand(ServerLevel world, ArmorStand armorStand, WaypointColor color) {
        if (world.getServer() == null) {
            LOGGER.warn("Could not set waypoint color for entity {}: server is null", armorStand.getUUID());
            return;
        }
        try {
            String command = String.format("waypoint modify %s color %s", armorStand.getStringUUID(), color.name);
            var commandSource = world.getServer().createCommandSourceStack().withSuppressedOutput();
            var dispatcher = world.getServer().getCommands().getDispatcher();
            var parseResults = dispatcher.parse(command, commandSource);
            world.getServer().getCommands().performCommand(parseResults, command);
            LOGGER.info("Set waypoint color to {} for entity {}", color.name, armorStand.getStringUUID());
        } catch (Exception e) {
            LOGGER.error("Failed to execute waypoint color command for entity {}: {}", armorStand.getUUID(), e.getMessage());
        }
    }

    /**
     * Restrict waypoint visibility to a single player using the vanilla waypoint viewers command.
     * This prevents other players from seeing the waypoint on their locator bar.
     * Wrapped in try/catch so the mod continues to work if the command is unavailable.
     */
    private static void setWaypointViewersWithCommand(ServerLevel world, ArmorStand armorStand, String playerName) {
        if (world.getServer() == null) {
            LOGGER.warn("Could not restrict waypoint viewers for entity {}: server is null", armorStand.getUUID());
            return;
        }
        try {
            String command = String.format("waypoint modify %s viewers @a[name=%s]",
                    armorStand.getStringUUID(), playerName);
            var commandSource = world.getServer().createCommandSourceStack().withSuppressedOutput();
            var dispatcher = world.getServer().getCommands().getDispatcher();
            var parseResults = dispatcher.parse(command, commandSource);
            world.getServer().getCommands().performCommand(parseResults, command);
            LOGGER.info("Restricted waypoint {} visibility to player {}", armorStand.getStringUUID(), playerName);
        } catch (Exception e) {
            LOGGER.warn("Could not restrict waypoint viewers for entity {} (feature may not be available): {}",
                    armorStand.getUUID(), e.getMessage());
        }
    }

    public static boolean removeWaypoint(ServerPlayer player, String color) {
        UUID playerUuid = player.getUUID();
        String dimension = getDimensionKey(player.level().dimension());

        Map<String, Map<String, WaypointData>> dimensionWaypoints = playerWaypoints.get(playerUuid);
        if (dimensionWaypoints != null) {
            Map<String, WaypointData> waypoints = dimensionWaypoints.get(dimension);
            if (waypoints != null && waypoints.containsKey(color)) {
                removeWaypointEntity(player, color, dimension);
                waypoints.remove(color);
                LOGGER.info("Removed waypoint: color={}, dimension={}", color, dimension);
                return true;
            }
        }
        return false;
    }

    private static void removeWaypointEntity(ServerPlayer player, String color, String dimension) {
        ServerLevel world = (ServerLevel) player.level();
        removeWaypointEntityInWorld(world, player.getUUID(), color, dimension);
    }

    /**
     * Remove a waypoint entity from a specific world/dimension.
     * Used for removing waypoints when we need to specify the exact world.
     */
    private static void removeWaypointEntityInWorld(ServerLevel world, UUID playerUuid, String color, String dimension) {
        Map<String, Map<String, WaypointData>> dimensionWaypoints = playerWaypoints.get(playerUuid);
        if (dimensionWaypoints != null) {
            Map<String, WaypointData> waypoints = dimensionWaypoints.get(dimension);
            if (waypoints != null && waypoints.containsKey(color)) {
                WaypointData data = waypoints.get(color);
                if (data.entityId() != -1) {
                    Entity entity = world.getEntity(data.entityId());
                    if (entity instanceof ArmorStand) {
                        entity.discard();
                        LOGGER.info("Removed waypoint entity for color {} in {}", color, dimension);
                    }
                }
            }
        }
    }

    private static void removeAllPlayerWaypointEntities(ServerPlayer player) {
        UUID playerUuid = player.getUUID();
        Map<String, Map<String, WaypointData>> dimensionWaypoints = playerWaypoints.get(playerUuid);

        if (dimensionWaypoints == null || dimensionWaypoints.isEmpty()) {
            return;
        }

        int removedCount = 0;

        // Remove entities from all dimensions the player has waypoints in
        for (Map.Entry<String, Map<String, WaypointData>> dimEntry : dimensionWaypoints.entrySet()) {
            String dimension = dimEntry.getKey();
            Map<String, WaypointData> waypoints = dimEntry.getValue();

            // Get the corresponding world for this dimension
            ServerLevel world = getWorldForDimension(((ServerLevel) player.level()).getServer(), dimension);
            if (world == null) continue;

            for (WaypointData data : waypoints.values()) {
                if (data.entityId() != -1) {
                    Entity entity = world.getEntity(data.entityId());
                    if (entity instanceof ArmorStand) {
                        entity.discard();
                        removedCount++;
                    }
                }
            }
        }

        if (removedCount > 0) {
            LOGGER.info("Cleaned up {} waypoint entities for disconnecting player {}",
                    removedCount, player.getName().getString());
        }
    }

    /**
     * Get the ServerLevel for a given dimension key string
     */
    private static ServerLevel getWorldForDimension(net.minecraft.server.MinecraftServer server, String dimension) {
        if (server == null) return null;
        return switch (dimension) {
            case DIM_OVERWORLD -> server.getLevel(Level.OVERWORLD);
            case DIM_NETHER -> server.getLevel(Level.NETHER);
            case DIM_END -> server.getLevel(Level.END);
            default -> null;
        };
    }

    /**
     * Get waypoints for a player in their current dimension
     */
    public static Map<String, WaypointData> getWaypoints(UUID playerUuid, String dimension) {
        Map<String, Map<String, WaypointData>> dimensionWaypoints = playerWaypoints.getOrDefault(playerUuid, new HashMap<>());
        return dimensionWaypoints.getOrDefault(dimension, new HashMap<>());
    }

    /**
     * Get waypoints for a player in their current dimension (convenience method)
     */
    public static Map<String, WaypointData> getWaypoints(UUID playerUuid) {
        // Returns empty map - callers should use getWaypoints(UUID, String) with dimension
        return new HashMap<>();
    }

    /**
     * Get all waypoints for a player across all dimensions
     */
    public static Map<String, Map<String, WaypointData>> getAllWaypoints(UUID playerUuid) {
        return playerWaypoints.getOrDefault(playerUuid, new HashMap<>());
    }

    /**
     * Remove all waypoints for a player in their current dimension.
     * Returns the number of waypoints that were removed.
     */
    public static int clearWaypointsInDimension(ServerPlayer player) {
        UUID playerUuid = player.getUUID();
        ServerLevel world = (ServerLevel) player.level();
        String dimension = getDimensionKey(world.dimension());

        Map<String, Map<String, WaypointData>> dimensionWaypoints = playerWaypoints.get(playerUuid);
        if (dimensionWaypoints == null) return 0;

        Map<String, WaypointData> waypoints = dimensionWaypoints.get(dimension);
        if (waypoints == null || waypoints.isEmpty()) return 0;

        int count = waypoints.size();
        for (WaypointData data : waypoints.values()) {
            if (data.entityId() != -1) {
                Entity entity = world.getEntity(data.entityId());
                if (entity instanceof ArmorStand) {
                    entity.discard();
                }
            }
        }
        waypoints.clear();

        LOGGER.info("Cleared {} waypoints in {} for player {}", count, dimension, player.getName().getString());
        return count;
    }

    /**
     * Remove all waypoints for a player across all dimensions.
     * Returns the total number of waypoints that were removed.
     */
    public static int clearAllWaypoints(ServerPlayer player) {
        UUID playerUuid = player.getUUID();
        Map<String, Map<String, WaypointData>> dimensionWaypoints = playerWaypoints.get(playerUuid);
        if (dimensionWaypoints == null || dimensionWaypoints.isEmpty()) return 0;

        int count = 0;
        net.minecraft.server.MinecraftServer server = ((ServerLevel) player.level()).getServer();

        for (Map.Entry<String, Map<String, WaypointData>> dimEntry : dimensionWaypoints.entrySet()) {
            String dimension = dimEntry.getKey();
            Map<String, WaypointData> waypoints = dimEntry.getValue();

            ServerLevel world = getWorldForDimension(server, dimension);
            if (world == null) {
                count += waypoints.size();
                waypoints.clear();
                continue;
            }

            for (WaypointData data : waypoints.values()) {
                if (data.entityId() != -1) {
                    Entity entity = world.getEntity(data.entityId());
                    if (entity instanceof ArmorStand) {
                        entity.discard();
                    }
                }
            }
            count += waypoints.size();
            waypoints.clear();
        }

        LOGGER.info("Cleared all {} waypoints across all dimensions for player {}", count, player.getName().getString());
        return count;
    }

    /**
     * Share a waypoint with another player.
     * Copies the specified color waypoint from {@code fromPlayer}'s current dimension to
     * {@code toPlayer}'s waypoint storage, overwriting any existing waypoint of the same color.
     * Returns true if the waypoint was successfully shared.
     */
    public static boolean shareWaypoint(ServerPlayer fromPlayer, ServerPlayer toPlayer, String colorName) {
        UUID fromUuid = fromPlayer.getUUID();
        WaypointColor color = WaypointColor.fromString(colorName);
        String dimension = getDimensionKey(fromPlayer.level().dimension());

        Map<String, WaypointData> fromWaypoints = getWaypoints(fromUuid, dimension);
        WaypointData sourceData = fromWaypoints.get(color.name);

        if (sourceData == null) {
            return false;
        }

        ServerLevel world = getWorldForDimension(fromPlayer.level().getServer(), dimension);
        if (world == null) {
            return false;
        }

        createWaypointInWorld(world, toPlayer.getUUID(), color.name, sourceData.x(), sourceData.y(), sourceData.z());

        LOGGER.info("Shared {} waypoint from {} to {} at ({},{},{}) in {}",
                color.name, fromPlayer.getName().getString(), toPlayer.getName().getString(),
                (int) sourceData.x(), (int) sourceData.y(), (int) sourceData.z(), dimension);
        return true;
    }

    public static void recreateWaypointEntities(ServerPlayer player) {
        UUID playerUuid = player.getUUID();
        Map<String, Map<String, WaypointData>> dimensionWaypoints = playerWaypoints.get(playerUuid);

        if (dimensionWaypoints == null || dimensionWaypoints.isEmpty()) {
            return;
        }

        int recreatedCount = 0;
        net.minecraft.server.MinecraftServer server = ((ServerLevel) player.level()).getServer();

        // Recreate waypoint entities in all dimensions where the player has waypoints
        for (Map.Entry<String, Map<String, WaypointData>> dimEntry : dimensionWaypoints.entrySet()) {
            String dimension = dimEntry.getKey();
            Map<String, WaypointData> waypoints = dimEntry.getValue();

            // Get the corresponding world for this dimension
            ServerLevel world = getWorldForDimension(server, dimension);
            if (world == null) {
                LOGGER.warn("Could not get world for dimension {} to recreate waypoints", dimension);
                continue;
            }

            LOGGER.info("Recreating {} waypoint entities for player {} in {}",
                    waypoints.size(), player.getName().getString(), dimension);

            // Create a snapshot of the waypoints to avoid ConcurrentModificationException
            // since createWaypointInWorld modifies the same map structure
            List<WaypointData> waypointSnapshot = new ArrayList<>(waypoints.values());
            
            for (WaypointData data : waypointSnapshot) {
                try {
                    // Migrate old "purple" entries to "light_purple" during recreation
                    String colorName = data.color();
                    if ("purple".equals(colorName)) {
                        // Remove old "purple" entry before creating with "light_purple"
                        waypoints.remove("purple");
                        LOGGER.info("Migrating waypoint from 'purple' to 'light_purple' for player {} in {}", 
                                player.getName().getString(), dimension);
                    }
                    
                    createWaypointInWorld(world, playerUuid, colorName, data.x(), data.y(), data.z());
                    recreatedCount++;
                } catch (Exception e) {
                    LOGGER.error("Failed to recreate waypoint entity for color {} in {}: {}", 
                            data.color(), dimension, e.getMessage());
                }
            }
        }

        if (recreatedCount > 0) {
            LOGGER.info("Successfully recreated {} waypoint entities across all dimensions for player {}",
                    recreatedCount, player.getName().getString());
        }
    }

    private static void displayDistances(ServerPlayer player) {
        UUID playerUuid = player.getUUID();
        String dimension = getDimensionKey(player.level().dimension());
        Map<String, WaypointData> waypoints = getWaypoints(playerUuid, dimension);

        MutableComponent fullText = Component.empty();

        if (!waypoints.isEmpty()) {
            Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());

            fullText = waypoints.keySet().stream()
                .sorted()
                .map(colorName -> {
                    WaypointColor color = WaypointColor.fromString(colorName);
                    WaypointData data = waypoints.get(colorName);
                    int distance = (int) playerPos.distanceTo(new Vec3(data.x(), data.y(), data.z()));

                    return Component.literal(color.emoji + " ")
                        .append(Component.literal(String.valueOf(distance)).withStyle(color.formatting));
                })
                .reduce((text1, text2) -> text1.append("  ").append(text2))
                .orElse(Component.empty());
        }

        String newDistanceText = fullText.getString();

        String oldDistanceText = lastDistanceText.get(playerUuid);
        if (!newDistanceText.equals(oldDistanceText)) {
            lastDistanceText.put(playerUuid, newDistanceText);
            player.sendSystemMessage(fullText, true);
        }
    }

    /**
     * Purge orphaned waypoint entities — armor stands that match the naming pattern used by
     * this mod (e.g. "red waypoint") but are no longer tracked in the internal registry.
     * This is an OP-only recovery tool for clearing disassociated entities that accumulate
     * after crashes, reloads, or other abnormal shutdowns.
     *
     * @param server the MinecraftServer instance
     * @return the number of orphaned entities removed
     */
    public static int purgeOrphanedWaypointEntities(net.minecraft.server.MinecraftServer server) {
        // Collect all entity IDs that are currently tracked in our registry
        Set<Integer> knownEntityIds = new HashSet<>();
        for (Map<String, Map<String, WaypointData>> dimMap : playerWaypoints.values()) {
            for (Map<String, WaypointData> colorMap : dimMap.values()) {
                for (WaypointData data : colorMap.values()) {
                    if (data.entityId() != -1) {
                        knownEntityIds.add(data.entityId());
                    }
                }
            }
        }

        // Build the set of custom-name strings this mod uses for waypoint entities
        Set<String> waypointNames = new HashSet<>();
        for (WaypointColor color : WaypointColor.values()) {
            waypointNames.add(color.name + " waypoint");
        }

        int removed = 0;
        for (String dimKey : new String[]{DIM_OVERWORLD, DIM_NETHER, DIM_END}) {
            ServerLevel world = getWorldForDimension(server, dimKey);
            if (world == null) continue;

            // Collect candidates first to avoid modifying the entity list while iterating
            List<ArmorStand> orphans = new ArrayList<>();
            for (Entity entity : world.getAllEntities()) {
                if (!(entity instanceof ArmorStand stand)) continue;
                Component customName = stand.getCustomName();
                if (customName == null) continue;
                if (!waypointNames.contains(customName.getString())) continue;
                if (knownEntityIds.contains(stand.getId())) continue;
                orphans.add(stand);
            }

            for (ArmorStand stand : orphans) {
                LOGGER.info("Purging orphaned waypoint entity '{}' (id={}) at ({},{},{}) in {}",
                        stand.getCustomName().getString(), stand.getId(),
                        (int) stand.getX(), (int) stand.getY(), (int) stand.getZ(), dimKey);
                stand.discard();
                removed++;
            }
        }

        LOGGER.info("Purged {} orphaned waypoint entity(ies) across all dimensions.", removed);
        return removed;
    }

	@Override
	public void onInitialize() {
		// Note: Removed ArgumentTypeRegistry.registerArgumentType for ColorArgumentType
		// to make this mod truly server-only. Using StringArgumentType instead.
		// This allows vanilla clients to connect without the mod installed.

		CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			LOGGER.info("Loading waypoints from storage...");
			Path storageDir = FabricLoader.getInstance().getGameDir().resolve("waypoints");
			try {
				Files.createDirectories(storageDir);
				Map<UUID, Map<String, Map<String, WaypointData>>> loadedData = WaypointStorage.loadWaypoints(storageDir);
				playerWaypoints.putAll(loadedData);
				LOGGER.info("Loaded data for {} players.", loadedData.size());
			} catch (IOException e) {
				LOGGER.error("Failed to create waypoint storage directory.", e);
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Saving all waypoints to storage...");
			Path storageDir = FabricLoader.getInstance().getGameDir().resolve("waypoints");
			WaypointStorage.saveWaypoints(storageDir, playerWaypoints);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> recreateWaypointEntities(handler.player));

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			removeAllPlayerWaypointEntities(handler.player);
			lastDistanceText.remove(handler.player.getUUID());
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter >= DISTANCE_UPDATE_INTERVAL) {
				tickCounter = 0;
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					displayDistances(player);
				}
			}
		});

		LOGGER.info("Heading Marker Mod initialized.");
	}

    /**
     * Purge all orphaned marker armor stands (created by this mod but not in the registry) from all worlds.
     * Only OPs should call this. Returns the number of purged entities.
     */
    public static int purgeOrphanedArmorStands(ServerPlayer executor) {
        net.minecraft.server.MinecraftServer server = executor.level().getServer();
        int purged = 0;
        // Build a set of all known marker entity IDs
        java.util.HashSet<Integer> knownEntityIds = new java.util.HashSet<>();
        for (Map<String, Map<String, WaypointData>> dimMap : playerWaypoints.values()) {
            for (Map<String, WaypointData> colorMap : dimMap.values()) {
                for (WaypointData data : colorMap.values()) {
                    if (data.entityId() != -1) {
                        knownEntityIds.add(data.entityId());
                    }
                }
            }
        }
        // Iterate all loaded worlds
        for (ServerLevel world : server.getAllLevels()) {
            for (ArmorStand armorStand : world.getEntities(EntityType.ARMOR_STAND, e -> true)) {
                // Check for marker: custom name ends with "waypoint" (case-insensitive)
                boolean isMarker = false;
                if (armorStand.hasCustomName()) {
                    String name = Objects.requireNonNull(armorStand.getCustomName()).getString().toLowerCase();
                    if (name.endsWith("waypoint")) {
                        isMarker = true;
                    }
                }
                // Optionally, check for the attribute as well (defensive)
                if (!isMarker && armorStand.getAttributes().hasAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE)) {
                    isMarker = true;
                }
                if (isMarker && !knownEntityIds.contains(armorStand.getId())) {
                    armorStand.discard();
                    purged++;
                }
            }
        }
        LOGGER.info("Purged {} orphaned marker armor stands (not in registry) by {}", purged, executor.getName().getString());
        return purged;
    }
}
