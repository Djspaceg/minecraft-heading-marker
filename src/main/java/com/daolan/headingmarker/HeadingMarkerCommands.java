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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
                    sendHelpMessage(ctx.getSource());
                    return 1;
                })
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            sendHelpMessage(ctx.getSource());
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
                                    try {
                                        Double.parseDouble(arg);
                                        ctx.getSource().sendFailure(Component.literal("Incomplete coordinates. Usage: /hm set <x> <z> [color]"));
                                    } catch (NumberFormatException e) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown color: " + arg + ". Valid colors: " + String.join(", ", VALID_COLORS)));
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
                .then(Commands.literal("purge")
                        .requires(HeadingMarkerCommands::isOperator)
                        .executes(ctx -> purgeOrphanedEntities(ctx.getSource()))
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
        String lowerColor = color.toLowerCase();
        if (!VALID_COLORS.contains(lowerColor)) {
            player.sendSystemMessage(Component.literal("Unknown color: " + color + ". Valid colors: " + String.join(", ", VALID_COLORS)).withStyle(ChatFormatting.RED));
            return 0;
        }
        HeadingMarkerMod.createWaypoint(player, lowerColor, x, y, z);
        player.sendSystemMessage(Component.literal(lowerColor + " waypoint set at (" + (int) x + ", " + (int) y + ", " + (int) z + ")")
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static String getNextAvailableColor(ServerPlayer player) {
        String dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension());
        Map<String, HeadingMarkerMod.WaypointData> existing = HeadingMarkerMod.getWaypoints(player.getUUID(), dimension);
        for (String color : VALID_COLORS) {
            if (!existing.containsKey(color)) return color;
        }
        return VALID_COLORS.getFirst();
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
        if (count == 0) {
            player.sendSystemMessage(Component.literal("You have no waypoints to clear in this dimension.").withStyle(ChatFormatting.YELLOW));
        } else {
            player.sendSystemMessage(Component.literal("Cleared " + count + " waypoint(s) in this dimension.").withStyle(ChatFormatting.GREEN));
        }
        return count;
    }

    private static int clearAllWaypoints(ServerPlayer player) {
        if (player == null) return 0;
        int count = HeadingMarkerMod.clearAllWaypoints(player);
        if (count == 0) {
            player.sendSystemMessage(Component.literal("You have no waypoints to clear.").withStyle(ChatFormatting.YELLOW));
        } else {
            player.sendSystemMessage(Component.literal("Cleared " + count + " waypoint(s) across all dimensions.").withStyle(ChatFormatting.GREEN));
        }
        return count;
    }

    private static int purgeOrphanedEntities(CommandSourceStack source) {
        int removed = HeadingMarkerMod.purgeOrphanedWaypointEntities(source.getServer());
        if (removed == 0) {
            source.sendSuccess(() -> Component.literal("No orphaned waypoint entities found.").withStyle(ChatFormatting.YELLOW), false);
        } else {
            source.sendSuccess(() -> Component.literal("Purged " + removed + " orphaned waypoint entity(ies) across all dimensions.").withStyle(ChatFormatting.GREEN), true);
        }
        return 1;
    }

    private static int shareWaypoint(ServerPlayer fromPlayer, String targetName, String color) {
        if (fromPlayer == null) return 0;

        String lowerColor = color.toLowerCase();
        if (!VALID_COLORS.contains(lowerColor)) {
            fromPlayer.sendSystemMessage(Component.literal("Unknown color: " + color + ". Valid colors: " + String.join(", ", VALID_COLORS)).withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerPlayer toPlayer = fromPlayer.level().getServer().getPlayerList().getPlayer(targetName);
        if (toPlayer == null) {
            fromPlayer.sendSystemMessage(Component.literal("Player not found or not online: " + targetName).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (toPlayer.getUUID().equals(fromPlayer.getUUID())) {
            fromPlayer.sendSystemMessage(Component.literal("You cannot share a waypoint with yourself.").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (HeadingMarkerMod.shareWaypoint(fromPlayer, toPlayer, lowerColor)) {
            fromPlayer.sendSystemMessage(Component.literal("Shared " + lowerColor + " waypoint with " + targetName).withStyle(ChatFormatting.GREEN));
            toPlayer.sendSystemMessage(Component.literal(fromPlayer.getName().getString() + " shared their " + lowerColor + " waypoint with you.")
                    .withStyle(ChatFormatting.AQUA));
            return 1;
        } else {
            fromPlayer.sendSystemMessage(Component.literal("You have no " + lowerColor + " waypoint in this dimension to share.").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int listWaypoints(ServerPlayer player) {
        if (player == null) return 0;
        String dim = HeadingMarkerMod.getDimensionKey(player.level().dimension());
        Map<String, HeadingMarkerMod.WaypointData> waypoints = HeadingMarkerMod.getWaypoints(player.getUUID(), dim);
        if (waypoints.isEmpty()) {
            player.sendSystemMessage(Component.literal("You have no active waypoints in " + dim + ".").withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        player.sendSystemMessage(Component.literal("Active Waypoints in " + dim + ":").withStyle(ChatFormatting.GOLD));
        waypoints.forEach((color, data) -> {
            String coords = String.format("(%d, %d, %d)", (int) data.x(), (int) data.y(), (int) data.z());
            player.sendSystemMessage(Component.literal(" - " + color + " at " + coords).withStyle(ChatFormatting.GRAY));
        });
        return waypoints.size();
    }

    /**
     * Check if the command source has operator-level permissions.
     * MC 26.1 removed hasPermission() from CommandSourceStack, so we check the
     * server's operator list directly via PlayerList.isOp(). Non-player sources
     * (console, command blocks) are treated as operators.
     */
    private static boolean isOperator(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return true;
        return ((ServerLevel) player.level()).getServer().getPlayerList().isOp(
                new NameAndId(player.getUUID(), player.getName().getString())
        );
    }

    private static void sendHelpMessage(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== Heading Marker Help ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("SET MARKER:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("  /hm set [color] [x z | x y z]").withStyle(ChatFormatting.YELLOW).append(Component.literal(" - Set a waypoint").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("MANAGE:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("  /hm list").withStyle(ChatFormatting.YELLOW).append(Component.literal(" - List active waypoints").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("  /hm remove <color>").withStyle(ChatFormatting.YELLOW).append(Component.literal(" - Remove a waypoint").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("  /hm clear").withStyle(ChatFormatting.YELLOW).append(Component.literal(" - Clear waypoints in this dimension").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("  /hm clearall").withStyle(ChatFormatting.YELLOW).append(Component.literal(" - Clear all waypoints").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("SHARE:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("  /hm share <player> <color>").withStyle(ChatFormatting.YELLOW).append(Component.literal(" - Share a waypoint").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("DISTANCE:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("  /trigger hm.distance").withStyle(ChatFormatting.YELLOW).append(Component.literal(" - Toggle distance display").withStyle(ChatFormatting.GRAY)), false);
        if (isOperator(source)) {
            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.literal("ADMIN:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            source.sendSuccess(() -> Component.literal("  /hm purge").withStyle(ChatFormatting.YELLOW).append(Component.literal(" - Purge orphaned waypoint entities (OP only)").withStyle(ChatFormatting.GRAY)), false);
        }
        source.sendSuccess(() -> Component.literal("=== =================== ===").withStyle(ChatFormatting.GOLD), false);
    }
}
