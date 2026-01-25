package com.djspaceg.headingmarker;

import java.util.Map;

import org.w3c.dom.Text;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class HeadingMarkerCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            net.minecraft.server.command.CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("hm")
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("color", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("red");
                                    builder.suggest("blue");
                                    builder.suggest("green");
                                    builder.suggest("yellow");
                                    builder.suggest("purple");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    String color = StringArgumentType.getString(context, "color");
                                    return setWaypoint(context.getSource().getServer(), player, color);
                                })))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("color", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    return net.minecraft.command.CommandSource.suggestMatching(
                                            HeadingMarkerState.getServerState(context.getSource().getServer())
                                                    .getWaypoints(context.getSource().getPlayer().getUuid()).keySet(),
                                            builder);
                                })
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    String color = StringArgumentType.getString(context, "color");
                                    return removeWaypoint(context.getSource().getServer(), player, color);
                                })))
                .then(CommandManager.literal("list")
                        .executes(context -> {
                            return listWaypoints(context.getSource().getServer(),
                                    context.getSource().getPlayerOrThrow());
                        })));
    }

    private static int setWaypoint(MinecraftServer server, ServerPlayerEntity player, String color) {
        HeadingMarkerState state = HeadingMarkerState.getServerState(server);
        Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());

        // Use getCommandSource().getWorld() as safest bet for dimension
        Waypoint wp = new Waypoint(player.getX(), player.getY(), player.getZ(),
                player.getCommandSource().getWorld().getRegistryKey().getValue().toString());
        waypoints.put(color, wp);
        state.markDirty();

        player.sendMessage(Text.literal("§aWaypoint §f" + color + "§a set at current location."), false);
        return 1;
    }

    private static int removeWaypoint(MinecraftServer server, ServerPlayerEntity player, String color) {
        HeadingMarkerState state = HeadingMarkerState.getServerState(server);
        Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());

        if (waypoints.remove(color) != null) {
            state.markDirty();
            player.sendMessage(Text.literal("§eWaypoint §f" + color + "§e removed."), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("§cNo waypoint found for color §f" + color), false);
            return 0;
        }
    }

    private static int listWaypoints(MinecraftServer server, ServerPlayerEntity player) {
        HeadingMarkerState state = HeadingMarkerState.getServerState(server);
        Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());

        if (waypoints.isEmpty()) {
            player.sendMessage(Text.literal("§7No waypoints set."), false);
            return 0;
        }

        player.sendMessage(Text.literal("§6Current Waypoints:"), false);
        waypoints.forEach((color, wp) -> {
            player.sendMessage(Text.literal(
                    " - §b" + color + "§r: " + String.format("%.1f, %.1f, %.1f [%s]", wp.x, wp.y, wp.z, wp.dimension)),
                    false);
        });

        return 1;
    }
}
