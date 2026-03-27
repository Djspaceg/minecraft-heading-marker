package com.daolan.headingmarker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HeadingMarkerCommands {

    private static final List<String> VALID_COLORS = Arrays.stream(HeadingMarkerMod.WaypointColor.values())
            .map(c -> c.name)
            .collect(Collectors.toList());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        var hmCommand = Commands.literal("hm")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if (player != null) sendHelpMessage(player);
                    return 1;
                })
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player != null) sendHelpMessage(player);
                            return 1;
                        })
                )
                .then(Commands.literal("list")
                        .executes(ctx -> listWaypoints(ctx.getSource().getPlayer()))
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("color", StringArgumentType.word())
                                .suggests(HeadingMarkerCommands::suggestActiveWaypoints)
                                .executes(ctx -> removeWaypoint(ctx.getSource().getPlayer(), StringArgumentType.getString(ctx, "color")))
                        )
                )
                .then(Commands.literal("clear")
                        .executes(ctx -> clearWaypointsInDimension(ctx.getSource().getPlayer()))
                )
                .then(Commands.literal("clearall")
                        .executes(ctx -> clearAllWaypoints(ctx.getSource().getPlayer()))
                )
                .then(Commands.literal("share")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(HeadingMarkerCommands::suggestOtherPlayers)
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .suggests(HeadingMarkerCommands::suggestActiveWaypoints)
                                        .executes(ctx -> shareWaypoint(
                                                ctx.getSource().getPlayer(),
                                                StringArgumentType.getString(ctx, "player"),
                                                StringArgumentType.getString(ctx, "color")))
                                )
                        )
                )
                .then(Commands.literal("set")
                        // /hm set
                        .executes(ctx -> setAtPlayerPos(ctx, null))
                        // /hm set <color> ...
                        .then(Commands.argument("color", StringArgumentType.word())
                                .suggests(HeadingMarkerCommands::suggestColors)
                                .executes(ctx -> {
                                    String arg = StringArgumentType.getString(ctx, "color");
                                    if (VALID_COLORS.contains(arg.toLowerCase())) {
                                        return setAtPlayerPos(ctx, arg);
                                    }
                                    // Try parsing as double for X coord if not a color
                                    try {
                                        Double.parseDouble(arg);
                                        ctx.getSource().sendFailure(Component.literal("Incomplete coordinates. Usage: /hm set <x> <z> [color]"));
                                    } catch (NumberFormatException e) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown color: " + arg));
                                    }
                                    return 0;
                                })
                                // /hm set <color> <x> <z>
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> setAtPos(ctx, StringArgumentType.getString(ctx, "color"), true))
                                                // /hm set <color> <x> <y> <z>
                                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> setAtPos(ctx, StringArgumentType.getString(ctx, "color"), false))
                                                )
                                        )
                                )
                        )
                        // /hm set <x> <z> ...
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> setAtPos(ctx, null, true))
                                        // /hm set <x> <z> <color>
                                        .then(Commands.argument("color", StringArgumentType.word())
                                                .suggests(HeadingMarkerCommands::suggestColors)
                                                .executes(ctx -> setAtPos(ctx, StringArgumentType.getString(ctx, "color"), true))
                                        )
                                        // /hm set <x> <y> <z> ...
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> setAtPos(ctx, null, false))
                                                // /hm set <x> <y> <z> <color>
                                                .then(Commands.argument("color", StringArgumentType.word())
                                                        .suggests(HeadingMarkerCommands::suggestColors)
                                                        .executes(ctx -> setAtPos(ctx, StringArgumentType.getString(ctx, "color"), false))
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("purgeorphans")
                        .requires(source -> {
                            ServerPlayer player = source.getPlayer();
                            // Allow non-player command sources (console/command blocks) to run this command.
                            if (player == null) return true;
                            // For player sources, check operator status via the server's PlayerList.
                            // PlayerList.isOp expects a NameAndId instance in this MC version.
                            return ((ServerLevel) player.level()).getServer().getPlayerList().isOp(
                                    new NameAndId(player.getUUID(), player.getName().getString())
                            );
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) {
                                ctx.getSource().sendFailure(Component.literal("Only players can purge orphans."));
                                return 0;
                            }
                            int purged = HeadingMarkerMod.purgeOrphanedArmorStands(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("Purged " + purged + " orphaned markers.").withStyle(ChatFormatting.GREEN), true);
                            return purged;
                        })
                );

        var node = dispatcher.register(hmCommand);
        dispatcher.register(Commands.literal("headingmarker").redirect(node));
    }

    private static int setAtPlayerPos(CommandContext<CommandSourceStack> ctx, String color) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        String colorToUse = color != null ? color : getNextAvailableColor(player);
        return setWaypoint(player, colorToUse, player.getX(), player.getY(), player.getZ());
    }

    private static int setAtPos(CommandContext<CommandSourceStack> ctx, String color, boolean usePlayerY) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        double x = DoubleArgumentType.getDouble(ctx, "x");
        double z = DoubleArgumentType.getDouble(ctx, "z");
        double y = usePlayerY ? player.getY() : DoubleArgumentType.getDouble(ctx, "y");
        
        String colorToUse = color != null ? color : getNextAvailableColor(player);
        return setWaypoint(player, colorToUse, x, y, z);
    }

    private static CompletableFuture<Suggestions> suggestColors(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(VALID_COLORS, builder);
    }

    private static CompletableFuture<Suggestions> suggestActiveWaypoints(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return builder.buildFuture();
        
        String dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension());
        return SharedSuggestionProvider.suggest(HeadingMarkerMod.getWaypoints(player.getUUID(), dimension).keySet(), builder);
    }

    private static CompletableFuture<Suggestions> suggestOtherPlayers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String selfName = context.getSource().getTextName();
        return SharedSuggestionProvider.suggest(
                context.getSource().getServer().getPlayerList().getPlayers().stream()
                        .map(p -> p.getName().getString())
                        .filter(name -> !name.equals(selfName))
                        .collect(Collectors.toList()),
                builder);
    }

    private static int setWaypoint(ServerPlayer player, String color, double x, double y, double z) {
        HeadingMarkerMod.createWaypoint(player, color, x, y, z);
        player.sendSystemMessage(Component.literal(color + " waypoint set at (" + (int)x + ", " + (int)y + ", " + (int)z + ")")
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static String getNextAvailableColor(ServerPlayer player) {
        String dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension());
        Map<String, HeadingMarkerMod.WaypointData> existing = HeadingMarkerMod.getWaypoints(player.getUUID(), dimension);
        for (String color : VALID_COLORS) {
            if (!existing.containsKey(color)) return color;
        }
        return VALID_COLORS.get(0);
    }

    private static int removeWaypoint(ServerPlayer player, String color) {
        if (player == null) return 0;
        if (HeadingMarkerMod.removeWaypoint(player, color)) {
            player.sendSystemMessage(Component.literal(color + " waypoint removed").withStyle(ChatFormatting.YELLOW));
            return 1;
        } else {
            player.sendSystemMessage(Component.literal("No " + color + " waypoint found.").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int clearWaypointsInDimension(ServerPlayer player) {
        if (player == null) return 0;
        int count = HeadingMarkerMod.clearWaypointsInDimension(player);
        player.sendSystemMessage(Component.literal("Cleared " + count + " waypoints in this dimension.").withStyle(ChatFormatting.GREEN));
        return count;
    }

    private static int clearAllWaypoints(ServerPlayer player) {
        if (player == null) return 0;
        int count = HeadingMarkerMod.clearAllWaypoints(player);
        player.sendSystemMessage(Component.literal("Cleared " + count + " waypoints across all dimensions.").withStyle(ChatFormatting.GREEN));
        return count;
    }

    private static int shareWaypoint(ServerPlayer fromPlayer, String targetName, String color) {
        if (fromPlayer == null) return 0;
        ServerPlayer toPlayer = ((ServerLevel) fromPlayer.level()).getServer().getPlayerList().getPlayer(targetName);
        if (toPlayer == null) {
            fromPlayer.sendSystemMessage(Component.literal("Player " + targetName + " not found.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (HeadingMarkerMod.shareWaypoint(fromPlayer, toPlayer, color)) {
            fromPlayer.sendSystemMessage(Component.literal("Shared " + color + " waypoint with " + targetName).withStyle(ChatFormatting.GREEN));
            toPlayer.sendSystemMessage(Component.literal(fromPlayer.getName().getString() + " shared their " + color + " waypoint with you.")
                    .withStyle(ChatFormatting.AQUA));
            return 1;
        } else {
            fromPlayer.sendSystemMessage(Component.literal("You don't have a " + color + " waypoint to share here.").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int listWaypoints(ServerPlayer player) {
        if (player == null) return 0;
        String dim = HeadingMarkerMod.getDimensionKey(player.level().dimension());
        Map<String, HeadingMarkerMod.WaypointData> waypoints = HeadingMarkerMod.getWaypoints(player.getUUID(), dim);
        if (waypoints.isEmpty()) {
            player.sendSystemMessage(Component.literal("No active waypoints in " + dim).withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        player.sendSystemMessage(Component.literal("Active Waypoints in " + dim + ":").withStyle(ChatFormatting.GOLD));
        waypoints.forEach((color, data) -> {
            player.sendSystemMessage(Component.literal(" - " + color + " at (" + (int)data.x() + ", " + (int)data.y() + ", " + (int)data.z() + ")")
                    .withStyle(ChatFormatting.GRAY));
        });
        return waypoints.size();
    }

    private static void sendHelpMessage(ServerPlayer player) {
        if (player == null) return;
        player.sendSystemMessage(Component.literal("=== Heading Marker Help ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("/hm set [color] [pos] ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Set a waypoint").withStyle(ChatFormatting.GRAY)));
        player.sendSystemMessage(Component.literal("/hm list ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- List active waypoints").withStyle(ChatFormatting.GRAY)));
        player.sendSystemMessage(Component.literal("/hm remove <color> ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Remove a waypoint").withStyle(ChatFormatting.GRAY)));
        player.sendSystemMessage(Component.literal("/hm share <player> <color> ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Share a waypoint").withStyle(ChatFormatting.GRAY)));
        player.sendSystemMessage(Component.literal("/hm clear[all] ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Clear waypoints").withStyle(ChatFormatting.GRAY)));
        player.sendSystemMessage(Component.literal("/trigger hm.distance ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Toggle distance display").withStyle(ChatFormatting.GRAY)));
    }
}
