package com.djspaceg.headingmarker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class HeadingMarkerCommands {
    
    // Suggestion provider for colors
    private static final SuggestionProvider<ServerCommandSource> COLOR_SUGGESTIONS = (context, builder) -> {
        List<String> colors = Arrays.asList("red", "blue", "green", "yellow", "purple");
        for (String c : colors) {
            builder.suggest(c);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment) {
        
        dispatcher.register(CommandManager.literal("hm")
            // /hm set ...
            .then(CommandManager.literal("set")
                // Branch A: /hm set <color> (Uses Player Pos)
                .then(CommandManager.argument("color", StringArgumentType.word())
                    .suggests(COLOR_SUGGESTIONS)
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        String color = StringArgumentType.getString(context, "color");
                        return setWaypoint(context.getSource().getServer(), player, color, player.getX(), player.getY(), player.getZ());
                    })
                )
                // Branch B: /hm set <x> ...
                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                    .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                        // Case: /hm set <x> <z>
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            double x = DoubleArgumentType.getDouble(context, "x");
                            double z = DoubleArgumentType.getDouble(context, "z");
                            String color = getNextAvailableColor(player);
                            return setWaypoint(context.getSource().getServer(), player, color, x, player.getY(), z);
                        })
                        .then(CommandManager.argument("val3", DoubleArgumentType.doubleArg())
                            // Case: /hm set x y z
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                double x = DoubleArgumentType.getDouble(context, "x");
                                double y_maybe = DoubleArgumentType.getDouble(context, "z");
                                double z_maybe = DoubleArgumentType.getDouble(context, "val3");
                                String color = getNextAvailableColor(player);
                                return setWaypoint(context.getSource().getServer(), player, color, x, y_maybe, z_maybe);
                            })
                            // Case: /hm set x y z <color>
                            .then(CommandManager.argument("color_3d", StringArgumentType.word())
                                .suggests(COLOR_SUGGESTIONS)
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    double x = DoubleArgumentType.getDouble(context, "x");
                                    double y = DoubleArgumentType.getDouble(context, "z");
                                    double z = DoubleArgumentType.getDouble(context, "val3");
                                    String color = StringArgumentType.getString(context, "color_3d");
                                    return setWaypoint(context.getSource().getServer(), player, color, x, y, z);
                                })
                            )
                        )
                        // Case: /hm set x z <color>
                        .then(CommandManager.argument("color_2d", StringArgumentType.word())
                            .suggests(COLOR_SUGGESTIONS)
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                double x = DoubleArgumentType.getDouble(context, "x");
                                double z = DoubleArgumentType.getDouble(context, "z");
                                String color = StringArgumentType.getString(context, "color_2d");
                                return setWaypoint(context.getSource().getServer(), player, color, x, player.getY(), z);
                            })
                        )
                    )
                )
            )
            // /hm remove ...
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("color", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        return CommandSource.suggestMatching(
                                HeadingMarkerState.getServerState(context.getSource().getServer())
                                        .getWaypoints(context.getSource().getPlayer().getUuid()).keySet(),
                                builder);
                    })
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        String color = StringArgumentType.getString(context, "color");
                        return removeWaypoint(context.getSource().getServer(), player, color);
                    })
                )
            )
            // /hm list
            .then(CommandManager.literal("list")
                .executes(context -> {
                    return listWaypoints(context.getSource().getServer(), context.getSource().getPlayerOrThrow());
                })
            )
        );
    }

    private static String getNextAvailableColor(ServerPlayerEntity player) {
        HeadingMarkerState state = HeadingMarkerState.getServerState(player.getEntityWorld().getServer());
        Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());
        List<String> colors = Arrays.asList("red", "blue", "green", "yellow", "purple");
        for (String c : colors) {
            if (!waypoints.containsKey(c)) return c;
        }
        return "red"; // Fallback overwrite red
    }

    private static int setWaypoint(MinecraftServer server, ServerPlayerEntity player, String color, double x, double y, double z) {
        HeadingMarkerState state = HeadingMarkerState.getServerState(server);
        Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());

        // Use player world
        String dim = player.getEntityWorld().getRegistryKey().getValue().toString();
        
        Waypoint wp = new Waypoint(x, y, z, dim);
        waypoints.put(color, wp);
        state.markDirty();

        player.sendMessage(Text.literal(color + " waypoint set to (" + x + ", " + y + ", " + z + ")").formatted(net.minecraft.util.Formatting.GREEN), false);
        
        // SYNC
        HeadingMarkerMod.syncMarkerData(player);
        
        return 1;
    }

    private static int removeWaypoint(MinecraftServer server, ServerPlayerEntity player, String color) {
        HeadingMarkerState state = HeadingMarkerState.getServerState(server);
        Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());

        if (waypoints.remove(color) != null) {
            state.markDirty();
            player.sendMessage(Text.literal(color + " waypoint removed").formatted(net.minecraft.util.Formatting.YELLOW), false);
            
            // SYNC
            HeadingMarkerMod.syncMarkerData(player);
            
            return 1;
        } else {
            player.sendMessage(Text.literal("No waypoint found for color: " + color).formatted(net.minecraft.util.Formatting.RED), false);
            return 0;
        }
    }

    private static int listWaypoints(MinecraftServer server, ServerPlayerEntity player) {
        HeadingMarkerState state = HeadingMarkerState.getServerState(server);
        Map<String, Waypoint> waypoints = state.getWaypoints(player.getUuid());

        if (waypoints.isEmpty()) {
            player.sendMessage(Text.literal("You have no active waypoints.").formatted(net.minecraft.util.Formatting.YELLOW), false);
            return 0;
        }

        player.sendMessage(Text.literal("Active Waypoints:").formatted(net.minecraft.util.Formatting.GOLD), false);
        waypoints.forEach((colorName, wp) -> {
            String msg = String.format(" - %s: %.1f, %.1f, %.1f (%s)", colorName, wp.x, wp.y, wp.z, wp.dimension);
            player.sendMessage(Text.literal(msg).formatted(net.minecraft.util.Formatting.GRAY), false);
        });
        return waypoints.size();
    }
}
