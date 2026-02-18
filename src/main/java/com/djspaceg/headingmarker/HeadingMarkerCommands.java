package com.djspaceg.headingmarker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HeadingMarkerCommands {

    private static final List<String> VALID_COLORS = Arrays.asList("red", "blue", "green", "yellow", "purple");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> hmCommand = dispatcher.register(
                CommandManager.literal("hm")
                        .requires(source -> true)
                        .executes(context -> {
                            sendHelpMessage(context.getSource().getPlayer());
                            return 1;
                        })
                        .then(CommandManager.literal("help")
                                .requires(source -> true)
                                .executes(context -> {
                                    sendHelpMessage(context.getSource().getPlayer());
                                    return 1;
                                })
                        )
                        .then(CommandManager.literal("list")
                                .requires(source -> true)
                                .executes(context -> listWaypoints(context.getSource().getPlayer()))
                        )
                        .then(CommandManager.literal("remove")
                                .requires(source -> true)
                                .then(CommandManager.argument("color", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            String dimension = HeadingMarkerMod.getDimensionKey(player.getEntityWorld().getRegistryKey());
                                            return CommandSource.suggestMatching(HeadingMarkerMod.getWaypoints(player.getUuid(), dimension).keySet(), builder);
                                        })
                                        .executes(context -> removeWaypoint(context.getSource().getPlayer(), StringArgumentType.getString(context, "color")))
                                )
                        )
                        .then(CommandManager.literal("set")
                                .requires(source -> true)
                                // /hm set - use player position, default color
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    String color = getDefaultColor(player);
                                    return setWaypoint(player, color, player.getX(), player.getY(), player.getZ());
                                })
                                // Branch 1: First argument is a color
                                .then(CommandManager.argument("arg1", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            // Suggest colors for first argument
                                            return CommandSource.suggestMatching(VALID_COLORS, builder);
                                        })
                                        .executes(context -> {
                                            // /hm set <color> or /hm set <x>
                                            String arg1 = StringArgumentType.getString(context, "arg1");
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            
                                            // If arg1 is a valid color, use it with player position
                                            if (VALID_COLORS.contains(arg1.toLowerCase())) {
                                                return setWaypoint(player, arg1, player.getX(), player.getY(), player.getZ());
                                            }
                                            
                                            // Otherwise, try to parse as X coordinate
                                            try {
                                                Double.parseDouble(arg1);
                                                player.sendMessage(Text.literal("Need Z coordinate. Usage: /hm set <x> <z> [color]").formatted(Formatting.RED), false);
                                                return 0;
                                            } catch (NumberFormatException e) {
                                                player.sendMessage(Text.literal("Unknown color or invalid coordinate: " + arg1).formatted(Formatting.RED), false);
                                                return 0;
                                            }
                                        })
                                        // Second argument
                                        .then(CommandManager.argument("arg2", StringArgumentType.word())
                                                .executes(context -> {
                                                    // /hm set <arg1> <arg2>
                                                    return handleTwoArgs(context);
                                                })
                                                // Third argument
                                                .then(CommandManager.argument("arg3", StringArgumentType.word())
                                                        .executes(context -> {
                                                            // /hm set <arg1> <arg2> <arg3>
                                                            return handleThreeArgs(context);
                                                        })
                                                        // Fourth argument
                                                        .then(CommandManager.argument("arg4", StringArgumentType.word())
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

        dispatcher.register(CommandManager.literal("headingmarker")
                .requires(source -> true)
                .redirect(hmCommand));
    }

    private static int setWaypoint(ServerPlayerEntity player, String color, double x, double y, double z) {
        // Validate color since we're using StringArgumentType instead of ColorArgumentType
        String lowerColor = color.toLowerCase();
        if (!VALID_COLORS.contains(lowerColor)) {
            player.sendMessage(Text.literal("Unknown color: " + color + ". Valid colors: " + String.join(", ", VALID_COLORS)).formatted(Formatting.RED), false);
            return 0;
        }

        HeadingMarkerMod.createWaypoint(player, lowerColor, x, y, z);
        player.sendMessage(Text.literal(lowerColor + " waypoint set to (" + (int)x + ", " + (int)y + ", " + (int)z + ")").formatted(Formatting.GREEN), false);
        return 1;
    }

    /**
     * Get the first available color that doesn't have a waypoint, or default to "red"
     */
    private static String getDefaultColor(ServerPlayerEntity player) {
        String dimension = HeadingMarkerMod.getDimensionKey(player.getEntityWorld().getRegistryKey());
        Map<String, HeadingMarkerMod.WaypointData> existingWaypoints = HeadingMarkerMod.getWaypoints(player.getUuid(), dimension);
        
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
    private static int handleTwoArgs(com.mojang.brigadier.context.CommandContext<ServerCommandSource> context) {
        String arg1 = StringArgumentType.getString(context, "arg1");
        String arg2 = StringArgumentType.getString(context, "arg2");
        ServerPlayerEntity player = context.getSource().getPlayer();

        // Check if arg1 is a color
        if (VALID_COLORS.contains(arg1.toLowerCase())) {
            // /hm set <color> <x>
            player.sendMessage(Text.literal("Need Z coordinate. Usage: /hm set " + arg1 + " <x> <z> [<y>]").formatted(Formatting.RED), false);
            return 0;
        }

        // Try to parse as /hm set <x> <z>
        try {
            double x = Double.parseDouble(arg1);
            double z = Double.parseDouble(arg2);
            String color = getDefaultColor(player);
            return setWaypoint(player, color, x, player.getY(), z);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.literal("Invalid coordinates. Usage: /hm set <x> <z> [color]").formatted(Formatting.RED), false);
            return 0;
        }
    }

    /**
     * Parse three arguments: could be "color x z", "x z color", or "x y z"
     * Handles: /hm set <color> <x> <z>, /hm set <x> <z> <color>, /hm set <x> <y> <z>
     */
    private static int handleThreeArgs(com.mojang.brigadier.context.CommandContext<ServerCommandSource> context) {
        String arg1 = StringArgumentType.getString(context, "arg1");
        String arg2 = StringArgumentType.getString(context, "arg2");
        String arg3 = StringArgumentType.getString(context, "arg3");
        ServerPlayerEntity player = context.getSource().getPlayer();

        // Check if arg1 is a color: /hm set <color> <x> <z>
        if (VALID_COLORS.contains(arg1.toLowerCase())) {
            try {
                double x = Double.parseDouble(arg2);
                double z = Double.parseDouble(arg3);
                return setWaypoint(player, arg1, x, player.getY(), z);
            } catch (NumberFormatException e) {
                player.sendMessage(Text.literal("Invalid coordinates after color. Usage: /hm set " + arg1 + " <x> <z>").formatted(Formatting.RED), false);
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
                player.sendMessage(Text.literal("Invalid coordinates before color. Usage: /hm set <x> <z> " + arg3).formatted(Formatting.RED), false);
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
            player.sendMessage(Text.literal("Invalid arguments. Usage: /hm set <color> <x> <z> or /hm set <x> <y> <z> or /hm set <x> <z> <color>").formatted(Formatting.RED), false);
            return 0;
        }
    }

    /**
     * Parse four arguments: "color x y z" or "x y z color"
     * Handles: /hm set <color> <x> <y> <z>, /hm set <x> <y> <z> <color>
     */
    private static int handleFourArgs(com.mojang.brigadier.context.CommandContext<ServerCommandSource> context) {
        String arg1 = StringArgumentType.getString(context, "arg1");
        String arg2 = StringArgumentType.getString(context, "arg2");
        String arg3 = StringArgumentType.getString(context, "arg3");
        String arg4 = StringArgumentType.getString(context, "arg4");
        ServerPlayerEntity player = context.getSource().getPlayer();

        // Check if arg1 is a color: /hm set <color> <x> <y> <z>
        if (VALID_COLORS.contains(arg1.toLowerCase())) {
            try {
                double x = Double.parseDouble(arg2);
                double y = Double.parseDouble(arg3);
                double z = Double.parseDouble(arg4);
                return setWaypoint(player, arg1, x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage(Text.literal("Invalid coordinates after color. Usage: /hm set " + arg1 + " <x> <y> <z>").formatted(Formatting.RED), false);
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
                player.sendMessage(Text.literal("Invalid coordinates before color. Usage: /hm set <x> <y> <z> " + arg4).formatted(Formatting.RED), false);
                return 0;
            }
        }

        player.sendMessage(Text.literal("Invalid arguments. Usage: /hm set <color> <x> <y> <z> or /hm set <x> <y> <z> <color>").formatted(Formatting.RED), false);
        return 0;
    }

    private static int removeWaypoint(ServerPlayerEntity player, String color) {
        if (HeadingMarkerMod.removeWaypoint(player, color)) {
            player.sendMessage(Text.literal(color + " waypoint removed").formatted(Formatting.YELLOW), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("No waypoint found for color: " + color).formatted(Formatting.RED), false);
            return 0;
        }
    }

    private static int listWaypoints(ServerPlayerEntity player) {
        String dimension = HeadingMarkerMod.getDimensionKey(player.getEntityWorld().getRegistryKey());
        Map<String, HeadingMarkerMod.WaypointData> waypoints = HeadingMarkerMod.getWaypoints(player.getUuid(), dimension);

        if (waypoints.isEmpty()) {
            player.sendMessage(Text.literal("You have no active waypoints in " + dimension + ".").formatted(Formatting.YELLOW), false);
            return 0;
        }

        player.sendMessage(Text.literal("Active Waypoints in " + dimension + ":").formatted(Formatting.GOLD), false);
        waypoints.forEach((color, data) -> {
            String coords = String.format("(%d, %d, %d)", (int)data.x(), (int)data.y(), (int)data.z());
            player.sendMessage(Text.literal(" - " + color + " waypoint at " + coords).formatted(Formatting.GRAY), false);
        });
        return waypoints.size();
    }

    private static void sendHelpMessage(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("========================================").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("Heading Marker - Command Help").formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal("========================================").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("SET MARKER:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /hm set [color] [x y z | x z]  ").formatted(Formatting.YELLOW).append(Text.literal("- Set marker at position").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal("  Examples: /hm set, /hm set red, /hm set 100 200, /hm set red 100 64 200").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("REMOVE MARKER:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /hm remove <color>  ").formatted(Formatting.YELLOW).append(Text.literal("- Remove marker by color").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("LIST MARKERS:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /hm list  ").formatted(Formatting.YELLOW).append(Text.literal("- List all your markers in this dimension").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("DISTANCE DISPLAY:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /trigger hm.distance  ").formatted(Formatting.YELLOW).append(Text.literal("- Toggle distance display on actionbar").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("========================================").formatted(Formatting.GOLD), false);
    }
}
