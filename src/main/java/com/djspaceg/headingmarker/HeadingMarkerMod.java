package com.djspaceg.headingmarker;

import com.djspaceg.headingmarker.storage.WaypointStorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.waypoint.TrackedWaypoint;
import net.minecraft.world.waypoint.Waypoint;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeadingMarkerMod implements ModInitializer {
    // Per-player showDistance toggle: UUID -> Boolean

    public static final String MOD_ID = "headingmarker";
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    // Custom payload for syncing waypoints
    public static final Identifier WAYPOINT_SYNC_ID = Identifier.of(MOD_ID, "waypoint_sync");
    static final Map<UUID, Boolean> playerShowDistance = new HashMap<>();
    private static final Map<UUID, Map<String, WaypointData>> playerWaypoints = new HashMap<>();

    public static Map<UUID, Boolean> getPlayerShowDistanceMap() {
        return playerShowDistance;
    }

    public static boolean getShowDistance(UUID playerUuid) {
        return playerShowDistance.getOrDefault(playerUuid, false);
    }

    public static void setShowDistance(UUID playerUuid, boolean value) {
        playerShowDistance.put(playerUuid, value);
    }

    /**
     * Create and track a waypoint for a player.
     * Uses vanilla TrackedWaypoint system and WaypointS2CPacket.
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
        WaypointSyncPayload payload = new WaypointSyncPayload(color, pos.getX(), pos.getY(), pos.getZ(), getColorInt(color), false);
        ServerPlayNetworking.send(player, payload);
        LOGGER.info("Sent custom waypoint sync to client");
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
            WaypointSyncPayload payload = new WaypointSyncPayload(color, 0, 0, 0, 0, true);
            ServerPlayNetworking.send(player, payload);
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
        LOGGER.info("Heading Marker Mod 1.0.5 Initializing (Custom Networking + Vanilla Waypoint Math)...");

        // Register custom payload
        PayloadTypeRegistry.playS2C().register(WaypointSyncPayload.ID, WaypointSyncPayload.CODEC);

        // Register commands
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register);

        // Attempt to register custom ColorArgumentType for command serialization so clients can receive it
        try {
            Class<?> argumentTypesClass = Class.forName("net.minecraft.command.argument.ArgumentTypes");
            Class<?> serializerIface = Class.forName("net.minecraft.command.argument.serialize.ArgumentSerializer");

            java.lang.reflect.Method registerMethod = argumentTypesClass.getMethod("register", net.minecraft.util.Identifier.class, Class.class, serializerIface);

            Object serializerProxy = java.lang.reflect.Proxy.newProxyInstance(
                    serializerIface.getClassLoader(),
                    new Class[]{serializerIface},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        // Methods may be named "write", "read", "toJson" depending on mapping
                        if (name.equals("write")) {
                            // (arg, buf) -> no-op
                            return null;
                        } else if (name.equals("read")) {
                            // (buf) -> new ColorArgumentType()
                            return ColorArgumentType.color();
                        } else if (name.equals("toJson") || name.equals("toJsonObject")) {
                            // (arg, json) -> no-op
                            return null;
                        }
                        return null;
                    }
            );

            registerMethod.invoke(null, Identifier.of(MOD_ID, "color"), ColorArgumentType.class, serializerProxy);
            LOGGER.info("Registered ColorArgumentType for command serialization");
        } catch (Throwable t) {
            LOGGER.warn("Could not register ColorArgumentType serializer; falling back to runtime-validated strings: {}", t.toString());
        }

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

        // Re-send waypoints to player on join; also run a final per-join command repair to ensure client receives full tree
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            try {
                HeadingMarkerCommands.ensureRegistered(server.getCommandManager().getDispatcher());
            } catch (Throwable t) {
                LOGGER.warn("Failed to repair /hm command tree on player join: {}", t.toString());
            }

            UUID uuid = handler.player.getUuid();
            Map<String, WaypointData> waypoints = playerWaypoints.get(uuid);
            if (waypoints != null) {
                for (WaypointData data : waypoints.values()) {
                    WaypointSyncPayload payload = new WaypointSyncPayload(data.color, data.x, data.y, data.z, getColorInt(data.color), false);
                    ServerPlayNetworking.send(handler.player, payload);
                }
            }
        });
    }

    public record WaypointSyncPayload(String color, int x, int y, int z, int colorInt,
                                      boolean remove) implements CustomPayload {
        public static final CustomPayload.Id<WaypointSyncPayload> ID = new CustomPayload.Id<>(WAYPOINT_SYNC_ID);
        public static final PacketCodec<RegistryByteBuf, WaypointSyncPayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    buf.writeString(value.color);
                    buf.writeInt(value.x);
                    buf.writeInt(value.y);
                    buf.writeInt(value.z);
                    buf.writeInt(value.colorInt);
                    buf.writeBoolean(value.remove);
                },
                buf -> new WaypointSyncPayload(
                        buf.readString(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readBoolean()
                )
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    // Per-player waypoint storage: UUID -> (color -> WaypointData)
        public record WaypointData(String color, int x, int y, int z, TrackedWaypoint waypoint) {
    }
}
