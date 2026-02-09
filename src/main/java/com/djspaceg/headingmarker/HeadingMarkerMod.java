package com.djspaceg.headingmarker;

import com.djspaceg.headingmarker.storage.WaypointStorage;
import com.djspaceg.headingmarker.waypoint.TrackedWaypoint;
import com.djspaceg.headingmarker.waypoint.Waypoint;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeadingMarkerMod implements ModInitializer {
    public static final String MOD_ID = "headingmarker";
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Map<UUID, Map<String, WaypointData>> playerWaypoints = new HashMap<>();
    private static final Map<UUID, String> lastDistanceText = new HashMap<>(); // Cache to prevent sending redundant action bar updates
    private static int tickCounter = 0;
    private static final int DISTANCE_UPDATE_INTERVAL = 5; // Update every 5 ticks (4 times per second)

    public record WaypointData(String color, double x, double y, double z, TrackedWaypoint trackedWaypoint, int entityId) {}

    public enum WaypointColor {
        RED("red", 0xFF0000, "🔴", Formatting.RED),
        BLUE("blue", 0x5555FF, "🔵", Formatting.BLUE),
        GREEN("green", 0x55FF55, "🟢", Formatting.GREEN),
        YELLOW("yellow", 0xFFFF55, "🟡", Formatting.YELLOW),
        PURPLE("purple", 0xFF55FF, "🟣", Formatting.LIGHT_PURPLE),
        WHITE("white", 0xFFFFFF, "⚪", Formatting.WHITE);

        public final String name;
        public final int colorInt;
        public final String emoji;
        public final Formatting formatting;

        private static final Map<String, WaypointColor> BY_NAME = new HashMap<>();

        static {
            for (WaypointColor c : values()) {
                BY_NAME.put(c.name, c);
            }
        }

        WaypointColor(String name, int colorInt, String emoji, Formatting formatting) {
            this.name = name;
            this.colorInt = colorInt;
            this.emoji = emoji;
            this.formatting = formatting;
        }

        public static WaypointColor fromString(String name) {
            return BY_NAME.getOrDefault(name.toLowerCase(), WHITE);
        }
    }

    public static void createWaypoint(ServerPlayerEntity player, String colorName, double x, double y, double z) {
        UUID playerUuid = player.getUuid();
        WaypointColor color = WaypointColor.fromString(colorName);
        LOGGER.info("Creating waypoint: color={}, pos=({},{},{}), player={}", color.name, x, y, z, player.getName().getString());

        removeWaypointEntity(player, color.name);

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        ArmorStandEntity armorStand = spawnAndConfigureWaypointEntity(world, color, x, y, z);
        if (armorStand == null) {
            LOGGER.error("Aborting waypoint creation due to entity spawn failure.");
            return;
        }

        setWaypointColorWithCommand(world, armorStand, color);

        Vec3i pos = new Vec3i((int) x, (int) y, (int) z);
        Waypoint.Config config = new Waypoint.Config();
        config.color = java.util.Optional.of(color.colorInt);
        TrackedWaypoint trackedWaypoint = TrackedWaypoint.ofPos(playerUuid, config, pos);

        WaypointData data = new WaypointData(color.name, pos.getX(), pos.getY(), pos.getZ(), trackedWaypoint, armorStand.getId());
        playerWaypoints.computeIfAbsent(playerUuid, k -> new HashMap<>()).put(color.name, data);

        LOGGER.info("Successfully created waypoint entity for color {} at ({},{},{})", color.name, x, y, z);
    }

    private static ArmorStandEntity spawnAndConfigureWaypointEntity(ServerWorld world, WaypointColor color, double x, double y, double z) {
        ArmorStandEntity armorStand = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
        armorStand.setPosition(x, y, z);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setNoGravity(true);
        armorStand.setSilent(true);
        armorStand.setCustomName(Text.literal(color.name + " waypoint"));

        try {
            EntityAttributeInstance waypointAttr = armorStand.getAttributeInstance(EntityAttributes.WAYPOINT_TRANSMIT_RANGE);
            if (waypointAttr != null) {
                waypointAttr.setBaseValue(999999.0);
            } else {
                LOGGER.warn("Could not set waypoint transmit range: attribute not found for ArmorStand.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set waypoint attribute", e);
        }

        if (world.spawnEntity(armorStand)) {
            return armorStand;
        } else {
            LOGGER.error("Failed to spawn waypoint entity for color {}", color.name);
            return null;
        }
    }

    private static void setWaypointColorWithCommand(ServerWorld world, ArmorStandEntity armorStand, WaypointColor color) {
        if (world.getServer() == null) {
            LOGGER.warn("Could not set waypoint color for entity {}: server is null", armorStand.getUuid());
            return;
        }
        try {
            String command = String.format("waypoint modify %s color %s", armorStand.getUuidAsString(), color.name);
            var commandSource = world.getServer().getCommandSource().withSilent();
            var dispatcher = world.getServer().getCommandManager().getDispatcher();
            var parseResults = dispatcher.parse(command, commandSource);
            world.getServer().getCommandManager().execute(parseResults, command);
            LOGGER.info("Set waypoint color to {} for entity {}", color.name, armorStand.getUuidAsString());
        } catch (Exception e) {
            LOGGER.error("Failed to execute waypoint color command for entity {}: {}", armorStand.getUuid(), e.getMessage());
        }
    }

    public static boolean removeWaypoint(ServerPlayerEntity player, String color) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);
        if (waypoints != null && waypoints.containsKey(color)) {
            removeWaypointEntity(player, color);
            waypoints.remove(color);
            LOGGER.info("Removed waypoint: color={}", color);
            return true;
        }
        return false;
    }

    private static void removeWaypointEntity(ServerPlayerEntity player, String color) {
        Map<String, WaypointData> waypoints = playerWaypoints.get(player.getUuid());
        if (waypoints != null && waypoints.containsKey(color)) {
            WaypointData data = waypoints.get(color);
            if (data.entityId != -1) {
                ServerWorld world = (ServerWorld) player.getEntityWorld();
                Entity entity = world.getEntityById(data.entityId);
                if (entity instanceof ArmorStandEntity) {
                    entity.discard();
                    LOGGER.info("Removed waypoint entity for color {}", color);
                }
            }
        }
    }

    private static void removeAllPlayerWaypointEntities(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);

        if (waypoints == null || waypoints.isEmpty()) {
            return;
        }

        int removedCount = 0;
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        for (WaypointData data : waypoints.values()) {
            if (data.entityId != -1) {
                Entity entity = world.getEntityById(data.entityId);
                if (entity instanceof ArmorStandEntity) {
                    entity.discard();
                    removedCount++;
                }
            }
        }

        if (removedCount > 0) {
            LOGGER.info("Cleaned up {} waypoint entities for disconnecting player {}",
                    removedCount, player.getName().getString());
        }
    }

    public static Map<String, WaypointData> getWaypoints(UUID playerUuid) {
        return playerWaypoints.getOrDefault(playerUuid, new HashMap<>());
    }

    public static void recreateWaypointEntities(ServerPlayerEntity player) {
        Map<String, WaypointData> waypoints = playerWaypoints.get(player.getUuid());
        if (waypoints == null || waypoints.isEmpty()) return;

        LOGGER.info("Recreating {} waypoint entities for player {}", waypoints.size(), player.getName().getString());

        for (WaypointData data : waypoints.values()) {
            try {
                createWaypoint(player, data.color(), data.x(), data.y(), data.z());
            } catch (Exception e) {
                LOGGER.error("Failed to recreate waypoint entity for color {}: {}", data.color(), e.getMessage());
            }
        }
    }

    private static void displayDistances(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();
        Map<String, WaypointData> waypoints = playerWaypoints.get(playerUuid);

        MutableText fullText = Text.empty();

        if (waypoints != null && !waypoints.isEmpty()) {
            Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());

            fullText = waypoints.keySet().stream()
                .sorted()
                .map(colorName -> {
                    WaypointColor color = WaypointColor.fromString(colorName);
                    WaypointData data = waypoints.get(colorName);
                    int distance = (int) playerPos.distanceTo(new Vec3d(data.x(), data.y(), data.z()));

                    return Text.literal(color.emoji + " ")
                        .append(Text.literal(String.valueOf(distance)).formatted(color.formatting));
                })
                .reduce((text1, text2) -> text1.append("  ").append(text2))
                .orElse(Text.empty());
        }

        String newDistanceText = fullText.getString();

        String oldDistanceText = lastDistanceText.get(playerUuid);
        if (!newDistanceText.equals(oldDistanceText)) {
            lastDistanceText.put(playerUuid, newDistanceText);
            player.sendMessage(fullText, true);
        }
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
				Map<UUID, Map<String, WaypointData>> loadedData = WaypointStorage.loadWaypoints(storageDir);
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
			lastDistanceText.remove(handler.player.getUuid());
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter >= DISTANCE_UPDATE_INTERVAL) {
				tickCounter = 0;
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					displayDistances(player);
				}
			}
		});

		LOGGER.info("Heading Marker Mod initialized.");
	}
}
