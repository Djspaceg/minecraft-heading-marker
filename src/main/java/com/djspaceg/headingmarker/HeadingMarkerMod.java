package com.djspaceg.headingmarker;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class HeadingMarkerMod implements ModInitializer {
    public static final String MOD_ID = "headingmarker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Heading Marker Mod 1.0.5 Initializing...");

        // Register payload type
        PayloadTypeRegistry.playS2C().register(WaypointSyncPayload.ID, WaypointSyncPayload.CODEC);

        // Register commands
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register);

        // Sync when player joins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            syncMarkerData(handler.getPlayer());
        });
    }

    public static void syncMarkerData(ServerPlayerEntity player) {
        if (player == null) return;
        
        HeadingMarkerState state = HeadingMarkerState.getServerState(player.getEntityWorld().getServer());
        Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());

        WaypointSyncPayload payload = new WaypointSyncPayload(waypoints);
        ServerPlayNetworking.send(player, payload);
    }
}
