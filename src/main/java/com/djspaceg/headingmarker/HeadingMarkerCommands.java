package com.djspaceg.headingmarker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HeadingMarkerCommands {

    private static final List<String> VALID_COLORS = Arrays.asList("red", "blue", "green", "yellow", "purple");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        LiteralCommandNode<CommandSourceStack> hmCommand = dispatcher.register(
                Commands.literal("hm")
                        .requires(source -> true)
                        .executes(context -> {
                            sendHelpMessage(context.getSource());
                            return 1;
                        })
                        .then(Commands.literal("help")
                                .requires(source -> true)
                                .executes(context -> {
                                    sendHelpMessage(context.getSource());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("list")
                                .requires(source -> true)
                                .executes(context -> listWaypoints(context.getSource().getPlayer()))
                        )
                        .then(Commands.literal("remove")
                                .requires(source -> true)
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            String dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension());
                                            return SharedSuggestionProvider.suggest(HeadingMarkerMod.getWaypoints(player.getUUID(), dimension).keySet(), builder);
                                        })
                                        .executes(context -> removeWaypoint(context.getSource().getPlayer(), StringArgumentType.getString(context, "color")))
                                )
                        )
                        .then(Commands.literal("clear")
                                .requires(source -> true)
                                .executes(context -> clearWaypointsInDimension(context.getSource().getPlayer()))
                        )
                        .then(Commands.literal("clearall")
                                .requires(source -> true)
                                .executes(context -> clearAllWaypoints(context.getSource().getPlayer()))
                        )
                        .then(Commands.literal("purge")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> purgeOrphanedEntities(context.getSource()))
                        )
                        .then(Commands.literal("share")
                                .requires(source -> true)
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            // Suggest online player names
                                            return SharedSuggestionProvider.suggest(
                                                    context.getSource().getServer().getPlayerList().getPlayers().stream()
                                                            .map(p -> p.getName().getString())
                                                            .filter(name -> !name.equals(context.getSource().getTextName()))
                                                            .toList(),
                                                    builder);
                                        })
                                        .then(Commands.argument("color", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    ServerPlayer player = context.getSource().getPlayer();
                                                    String dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension());
                                                    return SharedSuggestionProvider.suggest(HeadingMarkerMod.getWaypoints(player.getUUID(), dimension).keySet(), builder);
                                                })
                                                .executes(context -> shareWaypoint(
                                                        context.getSource().getPlayer(),
                                                        StringArgumentType.getString(context, "player"),
                                                        StringArgumentType.getString(context, "color")))
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .requires(source -> true)
                                // /hm set - use player position, default color
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    String color = getDefaultColor(player);
                                    return setWaypoint(player, color, player.getX(), player.getY(), player.getZ());
                                })
                                // Branch 1: First argument is a color
                                .then(Commands.argument("arg1", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            // Suggest colors for first argument
                                            return SharedSuggestionProvider.suggest(VALID_COLORS, builder);
                                        })
                                        .executes(context -> {
                                            // /hm set <color> or /hm set <x>
                                            String arg1 = StringArgumentType.getString(context, "arg1");
                                            ServerPlayer player = context.getSource().getPlayer();
                                            
                                            // If arg1 is a valid color, use it with player position
                                            if (VALID_COLORS.contains(arg1.toLowerCase())) {
                                                return setWaypoint(player, arg1, player.getX(), player.getY(), player.getZ());
                                            }
                                            
                                            // Otherwise, try to parse as X coordinate
                                            try {
                                                Double.parseDouble(arg1);
                                                player.sendSystemMessage(Component.literal("Need Z coordinate. Usage: /hm set <x> <z> [color]").withStyle(ChatFormatting.RED));
                                                return 0;
                                            } catch (NumberFormatException e) {
                                                player.sendSystemMessage(Component.literal("Unknown color or invalid coordinate: " + arg1).withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                        })
                                        // Second argument
                                        .then(Commands.argument("arg2", StringArgumentType.word())
                                                .executes(context -> {
                                                    // /hm set <arg1> <arg2>
                                                    return handleTwoArgs(context);
                                                })
                                                // Third argument
                                                .then(Commands.argument("arg3", StringArgumentType.word())
                                                        .executes(context -> {
                                                            // /hm set <arg1> <arg2> <arg3>
                                                            return handleThreeArgs(context);
                                                        })
                                                        // Fourth argument
                                                        .then(Commands.argument("arg4", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    // /hm set <arg1> <arg2> <arg3> <arg4>
                                                                    return handleFourArgs(context);
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(Commands.literal("headingmarker")
                .requires(source -> true)
                .redirect(hmCommand));
    }

    private static int setWaypoint(ServerPlayer player, String color, double x, double y, double z) {
        // Validate color since we're using StringArgumentType instead of ColorArgumentType
        String lowerColor = color.toLowerCase();
        if (!VALID_COLORS.contains(lowerColor)) {
            player.sendSystemMessage(Component.literal("Unknown color: " + color + ". Valid colors: " + String.join(", ", VALID_COLORS)).withStyle(ChatFormatting.RED));
            return 0;
        }

        HeadingMarkerMod.createWaypoint(player, lowerColor, x, y, z);
        player.sendSystemMessage(Component.literal(lowerColor + " waypoint set to (" + (int)x + ", " + (int)y + ", " + (int)z + ")").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    /**
     * Get the first available color that doesn't have a waypoint, or default to "red"
     */
    private static String getDefaultColor(ServerPlayer player) {
        String dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension());
        Map<String, HeadingMarkerMod.WaypointData> existingWaypoints = HeadingMarkerMod.getWaypoints(player.getUUID(), dimension);
        
        for (String color : VALID_COLORS) {
            if (!existingWaypoints.containsKey(color)) {
                return color;
            }
        }
        
        // All colors taken, default to red
        return "red";
    }

    /**
     * Parse two arguments: could be "color x", "x z", or "x y"
     * Handles: /hm set <color> <x>, /hm set <x> <z>
     */
    private static int handleTwoArgs(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        String arg1 = StringArgumentType.getString(context, "arg1");
        String arg2 = StringArgumentType.getString(context, "arg2");
        ServerPlayer player = context.getSource().getPlayer();

        // Check if arg1 is a color
        if (VALID_COLORS.contains(arg1.toLowerCase())) {
            // /hm set <color> <x>
            player.sendSystemMessage(Component.literal("Need Z coordinate. Usage: /hm set " + arg1 + " <x> <z> [<y>]").withStyle(ChatFormatting.RED));
            return 0;
        }

        // Try to parse as /hm set <x> <z>
        try {
            double x = Double.parseDouble(arg1);
            double z = Double.parseDouble(arg2);
            String color = getDefaultColor(player);
            return setWaypoint(player, color, x, player.getY(), z);
        } catch (NumberFormatException e) {
            player.sendSystemMessage(Component.literal("Invalid coordinates. Usage: /hm set <x> <z> [color]").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    /**
     * Parse three arguments: could be "color x z", "x z color", or "x y z"
     * Handles: /hm set <color> <x> <z>, /hm set <x> <z> <color>, /hm set <x> <y> <z>
     */
    private static int handleThreeArgs(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        String arg1 = StringArgumentType.getString(context, "arg1");
        String arg2 = StringArgumentType.getString(context, "arg2");
        String arg3 = StringArgumentType.getString(context, "arg3");
        ServerPlayer player = context.getSource().getPlayer();

        // Check if arg1 is a color: /hm set <color> <x> <z>
        if (VALID_COLORS.contains(arg1.toLowerCase())) {
            try {
                double x = Double.parseDouble(arg2);
                double z = Double.parseDouble(arg3);
                return setWaypoint(player, arg1, x, player.getY(), z);
            } catch (NumberFormatException e) {
                player.sendSystemMessage(Component.literal("Invalid coordinates after color. Usage: /hm set " + arg1 + " <x> <z>").withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        // Check if arg3 is a color: /hm set <x> <z> <color>
        if (VALID_COLORS.contains(arg3.toLowerCase())) {
            try {
                double x = Double.parseDouble(arg1);
                double z = Double.parseDouble(arg2);
                return setWaypoint(player, arg3, x, player.getY(), z);
            } catch (NumberFormatException e) {
                player.sendSystemMessage(Component.literal("Invalid coordinates before color. Usage: /hm set <x> <z> " + arg3).withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        // Try to parse as /hm set <x> <y> <z>
        try {
            double x = Double.parseDouble(arg1);
            double y = Double.parseDouble(arg2);
            double z = Double.parseDouble(arg3);
            String color = getDefaultColor(player);
            return setWaypoint(player, color, x, y, z);
        } catch (NumberFormatException e) {
            player.sendSystemMessage(Component.literal("Invalid arguments. Usage: /hm set <color> <x> <z> or /hm set <x> <y> <z> or /hm set <x> <z> <color>").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    /**
     * Parse four arguments: "color x y z" or "x y z color"
     * Handles: /hm set <color> <x> <y> <z>, /hm set <x> <y> <z> <color>
     */
    private static int handleFourArgs(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        String arg1 = StringArgumentType.getString(context, "arg1");
        String arg2 = StringArgumentType.getString(context, "arg2");
        String arg3 = StringArgumentType.getString(context, "arg3");
        String arg4 = StringArgumentType.getString(context, "arg4");
        ServerPlayer player = context.getSource().getPlayer();

        // Check if arg1 is a color: /hm set <color> <x> <y> <z>
        if (VALID_COLORS.contains(arg1.toLowerCase())) {
            try {
                double x = Double.parseDouble(arg2);
                double y = Double.parseDouble(arg3);
                double z = Double.parseDouble(arg4);
                return setWaypoint(player, arg1, x, y, z);
            } catch (NumberFormatException e) {
                player.sendSystemMessage(Component.literal("Invalid coordinates after color. Usage: /hm set " + arg1 + " <x> <y> <z>").withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        // Check if arg4 is a color: /hm set <x> <y> <z> <color>
        if (VALID_COLORS.contains(arg4.toLowerCase())) {
            try {
                double x = Double.parseDouble(arg1);
                double y = Double.parseDouble(arg2);
                double z = Double.parseDouble(arg3);
                return setWaypoint(player, arg4, x, y, z);
            } catch (NumberFormatException e) {
                player.sendSystemMessage(Component.literal("Invalid coordinates before color. Usage: /hm set <x> <y> <z> " + arg4).withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        player.sendSystemMessage(Component.literal("Invalid arguments. Usage: /hm set <color> <x> <y> <z> or /hm set <x> <y> <z> <color>").withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int removeWaypoint(ServerPlayer player, String color) {
        if (HeadingMarkerMod.removeWaypoint(player, color)) {
            player.sendSystemMessage(Component.literal(color + " waypoint removed").withStyle(ChatFormatting.YELLOW));
            return 1;
        } else {
            player.sendSystemMessage(Component.literal("No waypoint found for color: " + color).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int clearWaypointsInDimension(ServerPlayer player) {
        int removed = HeadingMarkerMod.clearWaypointsInDimension(player);
        if (removed == 0) {
            player.sendSystemMessage(Component.literal("You have no waypoints to clear in this dimension.").withStyle(ChatFormatting.YELLOW));
        } else {
            player.sendSystemMessage(Component.literal("Cleared " + removed + " waypoint(s) in this dimension.").withStyle(ChatFormatting.GREEN));
        }
        return removed;
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

    private static int clearAllWaypoints(ServerPlayer player) {
        int removed = HeadingMarkerMod.clearAllWaypoints(player);
        if (removed == 0) {
            player.sendSystemMessage(Component.literal("You have no waypoints to clear.").withStyle(ChatFormatting.YELLOW));
        } else {
            player.sendSystemMessage(Component.literal("Cleared " + removed + " waypoint(s) across all dimensions.").withStyle(ChatFormatting.GREEN));
        }
        return removed;
    }

    private static int shareWaypoint(ServerPlayer fromPlayer, String targetPlayerName, String colorName) {
        String lowerColor = colorName.toLowerCase();
        if (!VALID_COLORS.contains(lowerColor)) {
            fromPlayer.sendSystemMessage(Component.literal("Unknown color: " + colorName + ". Valid colors: " + String.join(", ", VALID_COLORS)).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Look up the target player on the server
        net.minecraft.server.MinecraftServer server = fromPlayer.level().getServer();
        ServerPlayer toPlayer = server.getPlayerList().getPlayer(targetPlayerName);
        if (toPlayer == null) {
            fromPlayer.sendSystemMessage(Component.literal("Player not found or not online: " + targetPlayerName).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (toPlayer.getUUID().equals(fromPlayer.getUUID())) {
            fromPlayer.sendSystemMessage(Component.literal("You cannot share a waypoint with yourself.").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (HeadingMarkerMod.shareWaypoint(fromPlayer, toPlayer, lowerColor)) {
            fromPlayer.sendSystemMessage(Component.literal("Shared your " + lowerColor + " waypoint with " + targetPlayerName + ".").withStyle(ChatFormatting.GREEN));
            toPlayer.sendSystemMessage(Component.literal(fromPlayer.getName().getString() + " shared their " + lowerColor + " waypoint with you.").withStyle(ChatFormatting.AQUA));
            return 1;
        } else {
            fromPlayer.sendSystemMessage(Component.literal("You have no " + lowerColor + " waypoint in this dimension to share.").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int listWaypoints(ServerPlayer player) {
        String dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension());
        Map<String, HeadingMarkerMod.WaypointData> waypoints = HeadingMarkerMod.getWaypoints(player.getUUID(), dimension);

        if (waypoints.isEmpty()) {
            player.sendSystemMessage(Component.literal("You have no active waypoints in " + dimension + ".").withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        player.sendSystemMessage(Component.literal("Active Waypoints in " + dimension + ":").withStyle(ChatFormatting.GOLD));
        waypoints.forEach((color, data) -> {
            String coords = String.format("(%d, %d, %d)", (int)data.x(), (int)data.y(), (int)data.z());
            player.sendSystemMessage(Component.literal(" - " + color + " waypoint at " + coords).withStyle(ChatFormatting.GRAY));
        });
        return waypoints.size();
    }

    private static void sendHelpMessage(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("========================================").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Heading Marker - Command Help").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("========================================").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("SET MARKER:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("• /hm set [color] [x y z | x z]  ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Set marker at position").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("  Examples: /hm set, /hm set red, /hm set 100 200, /hm set red 100 64 200").withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("REMOVE MARKER:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("• /hm remove <color>  ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Remove marker by color").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("• /hm clear  ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Remove all your markers in this dimension").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("• /hm clearall  ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Remove all your markers across every dimension").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("LIST MARKERS:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("• /hm list  ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- List all your markers in this dimension").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("SHARE MARKER:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("• /hm share <player> <color>  ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Share one of your markers with another player").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("DISTANCE DISPLAY:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("• /trigger hm.distance  ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Toggle distance display on actionbar").withStyle(ChatFormatting.GRAY)), false);
        if (source.hasPermission(2)) {
            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.literal("ADMIN:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            source.sendSuccess(() -> Component.literal("• /hm purge  ").withStyle(ChatFormatting.YELLOW).append(Component.literal("- Purge untracked waypoint entities from all dimensions (OP only)").withStyle(ChatFormatting.GRAY)), false);
        }
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("========================================").withStyle(ChatFormatting.GOLD), false);
    }
}
