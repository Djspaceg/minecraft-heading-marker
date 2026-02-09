package com.djspaceg.headingmarker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HeadingMarkerCommands {

    private static final List<String> VALID_COLORS = Arrays.asList("red", "blue", "green", "yellow", "purple");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> hmCommand = dispatcher.register(
                CommandManager.literal("hm")
                        .executes(context -> {
                            sendHelpMessage(context.getSource().getPlayer());
                            return 1;
                        })
                        .then(CommandManager.literal("help")
                                .executes(context -> {
                                    sendHelpMessage(context.getSource().getPlayer());
                                    return 1;
                                })
                        )
                        .then(CommandManager.literal("list")
                                .executes(context -> listWaypoints(context.getSource().getPlayer()))
                        )
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("color", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(HeadingMarkerMod.getWaypoints(context.getSource().getPlayer().getUuid()).keySet(), builder))
                                        .executes(context -> removeWaypoint(context.getSource().getPlayer(), StringArgumentType.getString(context, "color")))
                                )
                        )
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("color", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(VALID_COLORS, builder))
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            return setWaypoint(player, StringArgumentType.getString(context, "color"), player.getX(), player.getY(), player.getZ());
                                        })
                                        .then(CommandManager.argument("pos", Vec3ArgumentType.vec3())
                                                .executes(context -> {
                                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                                    Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
                                                    return setWaypoint(player, StringArgumentType.getString(context, "color"), pos.x, pos.y, pos.z);
                                                })
                                        )
                                )
                        )
        );

        dispatcher.register(CommandManager.literal("headingmarker").redirect(hmCommand));
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
        Map<String, HeadingMarkerMod.WaypointData> waypoints = HeadingMarkerMod.getWaypoints(player.getUuid());

        if (waypoints.isEmpty()) {
            player.sendMessage(Text.literal("You have no active waypoints.").formatted(Formatting.YELLOW), false);
            return 0;
        }

        player.sendMessage(Text.literal("Active Waypoints:").formatted(Formatting.GOLD), false);
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
        player.sendMessage(Text.literal("• /hm set <color> [pos]  ").formatted(Formatting.YELLOW).append(Text.literal("- Set marker (defaults to player if no pos)").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("REMOVE MARKER:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /hm remove <color>  ").formatted(Formatting.YELLOW).append(Text.literal("- Remove marker by color").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("DISTANCE DISPLAY:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        player.sendMessage(Text.literal("• /trigger hm.distance  ").formatted(Formatting.YELLOW).append(Text.literal("- Toggle distance display on actionbar").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("COLORS:").formatted(Formatting.AQUA, Formatting.BOLD), false);
        List<Text> colorLines = Arrays.stream(HeadingMarkerMod.WaypointColor.values()).map(c ->
                Text.literal("• " + c.name + " = ").formatted(Formatting.GRAY).append(Text.literal(c.emoji + " " + c.name.substring(0, 1).toUpperCase() + c.name.substring(1)).formatted(c.formatting))
        ).collect(Collectors.toList());
        colorLines.forEach(line -> player.sendMessage(line, false));
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("========================================").formatted(Formatting.GOLD), false);
    }
}
