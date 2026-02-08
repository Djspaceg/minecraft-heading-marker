package com.djspaceg.headingmarker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HeadingMarkerCommands {

    public static final List<String> EXPECTED_SUBCOMMANDS = Arrays.asList("help", "list", "remove", "set");

    // Helpers to build individual subcommand nodes so we can merge them into an existing /hm node safely
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> helpNode() {
        return CommandManager.literal("help").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            sendHelpMessage(player);
            return 1;
        });
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> listNode() {
        return CommandManager.literal("list").executes(context -> {
            return listWaypoints(context.getSource().getServer(), context.getSource().getPlayerOrThrow());
        });
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> removeNode() {
        return CommandManager.literal("remove")
                .then(CommandManager.argument("color", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            ServerPlayerEntity maybePlayer = context.getSource().getPlayer();
                            if (maybePlayer == null) return builder.buildFuture();
                            return CommandSource.suggestMatching(
                                    HeadingMarkerMod.getWaypoints(maybePlayer.getUuid()).keySet(),
                                    builder);
                        })
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String color = StringArgumentType.getString(context, "color");
                            return removeWaypoint(context.getSource().getServer(), player, color);
                        })
                );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> setNode() {
        return CommandManager.literal("set")
                .then(CommandManager.argument("color", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(ColorArgumentType.getColors(), builder))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String color = StringArgumentType.getString(context, "color");
                            if (!ColorArgumentType.getColors().contains(color.toLowerCase())) {
                                player.sendMessage(Text.literal("Unknown color: " + color).formatted(Formatting.RED), false);
                                return 0;
                            }
                            return setWaypoint(context.getSource().getServer(), player, color, player.getX(), player.getY(), player.getZ());
                        })
                )
                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                        .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    double x = DoubleArgumentType.getDouble(context, "x");
                                    double z = DoubleArgumentType.getDouble(context, "z");
                                    String color = getNextAvailableColor(player);
                                    return setWaypoint(context.getSource().getServer(), player, color, x, player.getY(), z);
                                })
                                .then(CommandManager.argument("val3", DoubleArgumentType.doubleArg())
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            double x = DoubleArgumentType.getDouble(context, "x");
                                            double y_maybe = DoubleArgumentType.getDouble(context, "z");
                                            double z_maybe = DoubleArgumentType.getDouble(context, "val3");
                                            String color = getNextAvailableColor(player);
                                            return setWaypoint(context.getSource().getServer(), player, color, x, y_maybe, z_maybe);
                                        })
                                        .then(CommandManager.argument("color_3d", StringArgumentType.word())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(ColorArgumentType.getColors(), builder))
                                                .executes(context -> {
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                    double y = DoubleArgumentType.getDouble(context, "z");
                                                    double z = DoubleArgumentType.getDouble(context, "val3");
                                                    String color = StringArgumentType.getString(context, "color_3d");
                                                    if (!ColorArgumentType.getColors().contains(color.toLowerCase())) {
                                                        player.sendMessage(Text.literal("Unknown color: " + color).formatted(Formatting.RED), false);
                                                        return 0;
                                                    }
                                                    return setWaypoint(context.getSource().getServer(), player, color, x, y, z);
                                                })
                                        )
                                )
                                .then(CommandManager.argument("color_2d", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(ColorArgumentType.getColors(), builder))
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            double x = DoubleArgumentType.getDouble(context, "x");
                                            double z = DoubleArgumentType.getDouble(context, "z");
                                            String color = StringArgumentType.getString(context, "color_2d");
                                            if (!ColorArgumentType.getColors().contains(color.toLowerCase())) {
                                                player.sendMessage(Text.literal("Unknown color: " + color).formatted(Formatting.RED), false);
                                                return 0;
                                            }
                                            return setWaypoint(context.getSource().getServer(), player, color, x, player.getY(), z);
                                        })
                                )
                        )
                );
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        HeadingMarkerMod.LOGGER.info("/hm register callback called (env={})", environment);
        ensureRegistered(dispatcher);
    }

    // Ensure the dispatcher contains the full /hm tree; merges missing nodes if present or registers a fresh tree
    public static void ensureRegistered(CommandDispatcher<ServerCommandSource> dispatcher) {
        com.mojang.brigadier.tree.CommandNode<ServerCommandSource> root = dispatcher.getRoot();
        java.util.Optional<com.mojang.brigadier.tree.CommandNode<ServerCommandSource>> existing = root.getChildren().stream().filter(n -> "hm".equals(n.getName())).findFirst();
        if (existing.isPresent()) {
            HeadingMarkerMod.LOGGER.info("/hm already present, need to replace with full command tree");
            // Remove existing /hm command - we need to re-register with executor
            root.getChildren().removeIf(n -> "hm".equals(n.getName()));
            HeadingMarkerMod.LOGGER.info("Removed existing /hm command, will re-register");
            // Fall through to register the complete tree below
        }

        // Register complete /hm command tree with default executor
        dispatcher.register(CommandManager.literal("hm")
                .executes(context -> {
                    // Default: show help when /hm is called without subcommand
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    sendHelpMessage(player);
                    return 1;
                })
                .then(helpNode())
                .then(listNode())
                .then(removeNode())
                .then(setNode())
        );

        // Post-registration sanity check: verify required literal children are present
        List<String> expected = Arrays.asList("help", "list", "remove", "set");
        java.util.Set<String> present = dispatcher.getRoot().getChildren().stream()
                .filter(n -> "hm".equals(n.getName()))
                .flatMap(n -> n.getChildren().stream())
                .map(n -> n.getName())
                .collect(java.util.stream.Collectors.toSet());
        for (String req : expected) {
            if (!present.contains(req)) {
                HeadingMarkerMod.LOGGER.error("/hm registration missing expected child: {} (present: {})", req, present);
                throw new IllegalStateException("/hm registration missing expected child: " + req);
            }
        }
    }

    private static String getNextAvailableColor(ServerPlayerEntity player) {
        Map<String, ?> waypoints = HeadingMarkerMod.getWaypoints(player.getUuid());
        List<String> colors = Arrays.asList("red", "blue", "green", "yellow", "purple");
        for (String c : colors) {
            if (!waypoints.containsKey(c)) return c;
        }
        return "red"; // Fallback overwrite red
    }

    private static int setWaypoint(MinecraftServer server, ServerPlayerEntity player, String color, double x, double y, double z) {
        // Create vanilla TrackedWaypoint
        HeadingMarkerMod.createWaypoint(player, color, x, y, z);

        MutableText message = MutableText.of(new net.minecraft.text.PlainTextContent.Literal(color + " waypoint set to (" + x + ", " + y + ", " + z + ")"));
        player.sendMessage(message.formatted(Formatting.GREEN), false);

        return 1;
    }

    private static int removeWaypoint(MinecraftServer server, ServerPlayerEntity player, String color) {
        if (HeadingMarkerMod.removeWaypoint(player, color)) {
            MutableText message = MutableText.of(new net.minecraft.text.PlainTextContent.Literal(color + " waypoint removed"));
            player.sendMessage(message.formatted(Formatting.YELLOW), false);
            return 1;
        } else {
            MutableText message = MutableText.of(new net.minecraft.text.PlainTextContent.Literal("No waypoint found for color: " + color));
            player.sendMessage(message.formatted(Formatting.RED), false);
            return 0;
        }
    }

    private static int listWaypoints(MinecraftServer server, ServerPlayerEntity player) {
        Map<String, ?> waypoints = HeadingMarkerMod.getWaypoints(player.getUuid());

        if (waypoints.isEmpty()) {
            player.sendMessage(Text.literal("You have no active waypoints.").formatted(net.minecraft.util.Formatting.YELLOW), false);
            return 0;
        }

        player.sendMessage(Text.literal("Active Waypoints:").formatted(net.minecraft.util.Formatting.GOLD), false);
        waypoints.forEach((colorName, wp) -> {
            player.sendMessage(Text.literal(" - " + colorName + " waypoint").formatted(net.minecraft.util.Formatting.GRAY), false);
        });
        return waypoints.size();
    }

    private static void sendHelpMessage(ServerPlayerEntity player) {
        // Header
        player.sendMessage(Text.literal("========================================").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("Heading Marker - Command Help").formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal("========================================").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal(""), false);

        // Set marker commands
        player.sendMessage(Text.literal("SET MARKER:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /hm set <color> [x] [y] [z]  ").formatted(Formatting.YELLOW).append(Text.literal("- Set marker at position (defaults to player if no coords)").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal("• /hm set <x> <z>  ").formatted(Formatting.YELLOW).append(Text.literal("- 2D marker at X,Z (Y=player)").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal("• /hm set <x> <z> <color>  ").formatted(Formatting.YELLOW).append(Text.literal("- 2D marker with color").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal("• /hm set <x> <y> <z>  ").formatted(Formatting.YELLOW).append(Text.literal("- 3D marker at X,Y,Z").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal("• /hm set <x> <y> <z> <color>  ").formatted(Formatting.YELLOW).append(Text.literal("- 3D marker with color").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);

        // Remove marker
        player.sendMessage(Text.literal("REMOVE MARKER:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /hm remove <color>  ").formatted(Formatting.YELLOW).append(Text.literal("- Remove marker by color").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);

        // Distance display toggle
        player.sendMessage(Text.literal("DISTANCE DISPLAY:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /trigger hm.distance  ").formatted(Formatting.YELLOW).append(Text.literal("- Toggle distance display on actionbar").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);

        // Color reference
        player.sendMessage(Text.literal("COLORS:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• red = ").formatted(Formatting.GRAY).append(Text.literal("🔴 Red").formatted(Formatting.RED)), false);
        player.sendMessage(Text.literal("• blue = ").formatted(Formatting.GRAY).append(Text.literal("🔵 Blue").formatted(Formatting.BLUE)), false);
        player.sendMessage(Text.literal("• green = ").formatted(Formatting.GRAY).append(Text.literal("🟢 Green").formatted(Formatting.GREEN)), false);
        player.sendMessage(Text.literal("• yellow = ").formatted(Formatting.GRAY).append(Text.literal("🟡 Yellow").formatted(Formatting.YELLOW)), false);
        player.sendMessage(Text.literal("• purple = ").formatted(Formatting.GRAY).append(Text.literal("🟣 Purple").formatted(Formatting.LIGHT_PURPLE)), false);
        player.sendMessage(Text.literal(""), false);

        // Quick examples
        player.sendMessage(Text.literal("QUICK EXAMPLES:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /hm set red 1000 64 -500  ").formatted(Formatting.GREEN).append(Text.literal("- Mark home base").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal("• /hm set blue 1000 64 -500  ").formatted(Formatting.GREEN).append(Text.literal("- Mark mine location").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal("• /hm set purple 1000 64 -500  ").formatted(Formatting.GREEN).append(Text.literal("- Mark nether portal").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);

        // Footer
        player.sendMessage(Text.literal("========================================").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("Tip: ").formatted(Formatting.GOLD).append(Text.literal("Use /hm set <color> [x] [y] [z] to mark locations! Click tab for suggestions.").formatted(Formatting.YELLOW)), false);
        player.sendMessage(Text.literal("========================================").formatted(Formatting.GOLD), false);
    }
}
