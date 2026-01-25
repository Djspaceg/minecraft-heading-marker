package com.djspaceg.headingmarker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.waypoint.TrackedWaypoint;
import net.minecraft.world.waypoint.Waypoint;

public class HeadingMarkerMod implements ModInitializer {
    public static final String MOD_ID = "headingmarker";
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Custom payload for syncing waypoints
    public static final Identifier WAYPOINT_SYNC_ID = Identifier.of(MOD_ID, "waypoint_sync");
    
    public record WaypointSyncPayload(String color, int x, int y, int z, int colorInt, boolean remove) implements CustomPayload {
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
    
    // Per-player waypoint storage: UUID -> (color -> TrackedWaypoint)
    private static final Map<UUID, Map<String, TrackedWaypoint>> playerWaypoints = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Heading Marker Mod 1.0.5 Initializing (Custom Networking + Vanilla Waypoint Math)...");

        // Register custom payload
        PayloadTypeRegistry.playS2C().register(WaypointSyncPayload.ID, WaypointSyncPayload.CODEC);

        // Register commands
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register);
    }
    
    /**
     * Create and track a waypoint for a player.
     * Uses vanilla TrackedWaypoint system and WaypointS2CPacket.
     */
    public static TrackedWaypoint createWaypoint(ServerPlayerEntity player, String color, double x, double y, double z) {
        UUID playerUuid = player.getUuid();
        
        LOGGER.info("Creating waypoint: color={}, pos=({},{},{}), player={}", color, x, y, z, player.getName().getString());
        
        // Create waypoint configuration with custom color
        Waypoint.Config config = new Waypoint.Config();
        config.color = java.util.Optional.of(getColorInt(color));
        
        // Create position-based tracked waypoint
        Vec3i pos = new Vec3i((int)x, (int)y, (int)z);
        
        // Store in our tracking map
        TrackedWaypoint waypoint = TrackedWaypoint.ofPos(playerUuid, config, pos);
        playerWaypoints.computeIfAbsent(playerUuid, k -> new HashMap<>()).put(color, waypoint);
        
        // Send to client via custom networking
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
        Map<String, TrackedWaypoint> waypoints = playerWaypoints.get(playerUuid);
        
        if (waypoints != null && waypoints.containsKey(color)) {
            waypoints.remove(color);
            
            // Send remove packet to client
            WaypointSyncPayload payload = new WaypointSyncPayload(color, 0, 0, 0, 0, true);
            ServerPlayNetworking.send(player, payload);
            
            return true;
        }
        return false;
    }
    
    /**
     * Get all waypoints for a player.
     */
    public static Map<String, TrackedWaypoint> getWaypoints(UUID playerUuid) {
        return playerWaypoints.getOrDefault(playerUuid, new HashMap<>());
    }
    
    private static int getColorInt(String color) {
        switch (color.toLowerCase()) {
            case "red": return 0xFF0000;
            case "blue": return 0x5555FF;
            case "green": return 0x55FF55;
            case "yellow": return 0xFFFF55;
            case "purple": return 0xFF55FF;
            default: return 0xFFFFFF;
        }
    }
}
