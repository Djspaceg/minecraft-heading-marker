package com.djspaceg.headingmarker;

import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Text;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class HeadingMarkerMod implements ModInitializer {
    public static final String MOD_ID = "headingmarker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Heading Marker Mod 1.0.3 (1.21.11 Edition) Initializing...");

        // Register commands
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register);

        // HUD Tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            HeadingMarkerState state = HeadingMarkerState.getServerState(server);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());
                if (waypoints.isEmpty())
                    continue;

                String status = waypoints.entrySet().stream()
                        .filter(entry -> entry.getValue().active && isSameDimension(player, entry.getValue()))
                        .map(entry -> formatWaypointStatus(player, entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("   "));

                if (!status.isEmpty()) {
                    player.sendMessage(Text.literal(status), true);
                }
            }
        });
    }

    private boolean isSameDimension(ServerPlayerEntity player, Waypoint wp) {
        // Attempting to get world from command source as Entity.getWorld() might be
        // unavailable or renamed
        // Actually, for tick loop, command source stack might be transient?
        // But ServerPlayerEntity always has world.
        // Let's try casting to Entity if ServerPlayerEntity is problematic
        // Or assume player.getWorld() works now that imports are clean
        // I will try player.getWorld() because org.w3c.dom.Text import might have
        // blocked visibility of clean imports? No.
        // I'll try player.getCommandSource().getWorld() as a fallback
        return player.getCommandSource().getWorld().getRegistryKey().getValue().toString().equals(wp.dimension);
    }

    private String formatWaypointStatus(ServerPlayerEntity player, String color, Waypoint wp) {
        double distSq = player.squaredDistanceTo(wp.x, player.getY(), wp.z); // use 2D distance for ease
        double dist = Math.sqrt(distSq);

        String colorCode = getColorCode(color);
        return String.format("%s%s: %.0fm§r", colorCode, capitalize(color), dist);
    }

    private String getColorCode(String color) {
        switch (color.toLowerCase()) {
            case "red":
                return "§c";
            case "blue":
                return "§9";
            case "green":
                return "§a";
            case "yellow":
                return "§e";
            case "purple":
                return "§5";
            default:
                return "§f";
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
