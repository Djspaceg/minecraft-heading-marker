package com.daolan.headingmarker

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.NameAndId

object HeadingMarkerCommands {

    private val VALID_COLORS: List<String> =
        HeadingMarkerMod.WaypointColor.entries
            .filter { it != HeadingMarkerMod.WaypointColor.WHITE }
            .map { it.colorName }

    @JvmStatic
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registryAccess: CommandBuildContext?,
        environment: Commands.CommandSelection,
    ) {
        val hmCommand =
            Commands.literal("hm")
                .executes { ctx ->
                    sendHelpMessage(ctx.source)
                    1
                }
                .then(
                    Commands.literal("help").executes { ctx ->
                        sendHelpMessage(ctx.source)
                        1
                    }
                )
                .then(Commands.literal("list").executes { ctx -> listWaypoints(ctx.source.player) })
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("color", StringArgumentType.word())
                                .suggests(::suggestActiveWaypoints)
                                .executes { ctx ->
                                    removeWaypoint(
                                        ctx.source.player,
                                        StringArgumentType.getString(ctx, "color"),
                                    )
                                }
                        )
                )
                .then(
                    Commands.literal("clear").executes { ctx ->
                        clearWaypointsInDimension(ctx.source.player)
                    }
                )
                .then(
                    Commands.literal("clearall").executes { ctx ->
                        clearAllWaypoints(ctx.source.player)
                    }
                )
                .then(
                    Commands.literal("share")
                        .then(
                            Commands.argument("player", StringArgumentType.word())
                                .suggests(::suggestOtherPlayers)
                                .then(
                                    Commands.argument("color", StringArgumentType.word())
                                        .suggests(::suggestActiveWaypoints)
                                        .executes { ctx ->
                                            shareWaypoint(
                                                ctx.source.player,
                                                StringArgumentType.getString(ctx, "player"),
                                                StringArgumentType.getString(ctx, "color"),
                                            )
                                        }
                                )
                        )
                )
                .then(
                    Commands.literal("set")
                        // /hm set — player pos, auto color
                        .executes { ctx -> setAtPlayerPos(ctx, null) }
                        // /hm set <color> ...
                        .then(
                            Commands.argument("color", StringArgumentType.word())
                                .suggests(::suggestColors)
                                // /hm set <color> — player pos, specified color
                                .executes { ctx ->
                                    val arg = StringArgumentType.getString(ctx, "color")
                                    if (arg.lowercase() in VALID_COLORS) {
                                        return@executes setAtPlayerPos(ctx, arg)
                                    }
                                    try {
                                        arg.toDouble()
                                        ctx.source.sendFailure(
                                            Component.literal(
                                                "Incomplete coordinates. Usage: /hm set <x> <z> [color]"
                                            )
                                        )
                                    } catch (_: NumberFormatException) {
                                        ctx.source.sendFailure(
                                            Component.literal(
                                                "Unknown color: $arg. Valid colors: ${VALID_COLORS.joinToString(", ")}"
                                            )
                                        )
                                    }
                                    0
                                }
                                // /hm set <color> <x> <z> — 2D with color
                                .then(
                                    Commands.argument("n1", DoubleArgumentType.doubleArg())
                                        .then(
                                            Commands.argument("n2", DoubleArgumentType.doubleArg())
                                                .executes { ctx ->
                                                    setColorXZ(
                                                        ctx,
                                                        StringArgumentType.getString(ctx, "color"),
                                                    )
                                                }
                                                // /hm set <color> <x> <y> <z> — 3D with color
                                                .then(
                                                    Commands.argument(
                                                            "y",
                                                            DoubleArgumentType.doubleArg(),
                                                        )
                                                        .executes { ctx ->
                                                            setColorXYZ(
                                                                ctx,
                                                                StringArgumentType.getString(
                                                                    ctx,
                                                                    "color",
                                                                ),
                                                            )
                                                        }
                                                )
                                        )
                                )
                        )
                        // /hm set <x> <z> ... — coordinates first
                        .then(
                            Commands.argument("n1", DoubleArgumentType.doubleArg())
                                .then(
                                    Commands.argument("n2", DoubleArgumentType.doubleArg())
                                        // /hm set <x> <z> — 2D, auto color
                                        .executes { ctx -> setXZ(ctx, null) }
                                        // /hm set <x> <y> <z> ... — 3D branch (y is always a
                                        // double, no ambiguity)
                                        .then(
                                            Commands.argument("n3", DoubleArgumentType.doubleArg())
                                                // /hm set <x> <y> <z> — 3D, auto color
                                                .executes { ctx -> setXYZ(ctx, null) }
                                                // /hm set <x> <y> <z> <color> — 3D with color
                                                .then(
                                                    Commands.argument(
                                                            "color",
                                                            StringArgumentType.word(),
                                                        )
                                                        .suggests(::suggestColors)
                                                        .executes { ctx ->
                                                            setXYZ(
                                                                ctx,
                                                                StringArgumentType.getString(
                                                                    ctx,
                                                                    "color",
                                                                ),
                                                            )
                                                        }
                                                )
                                        )
                                        // /hm set <x> <z> <color> — 2D with color (word after two
                                        // doubles, unambiguous)
                                        .then(
                                            Commands.argument("color", StringArgumentType.word())
                                                .suggests(::suggestColors)
                                                .executes { ctx ->
                                                    setXZ(
                                                        ctx,
                                                        StringArgumentType.getString(ctx, "color"),
                                                    )
                                                }
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("purge").requires(::isOperator).executes { ctx ->
                        purgeOrphanedEntities(ctx.source)
                    }
                )

        val node = dispatcher.register(hmCommand)
        dispatcher.register(Commands.literal("headingmarker").redirect(node))
    }

    private fun setAtPlayerPos(ctx: CommandContext<CommandSourceStack>, color: String?): Int {
        val player = ctx.source.player ?: return 0
        val colorToUse = color ?: getNextAvailableColor(player)
        return setWaypoint(player, colorToUse, player.x, player.y, player.z)
    }

    /** /hm set <color> <x> <z> */
    private fun setColorXZ(ctx: CommandContext<CommandSourceStack>, color: String): Int {
        val player = ctx.source.player ?: return 0
        val x = DoubleArgumentType.getDouble(ctx, "n1")
        val z = DoubleArgumentType.getDouble(ctx, "n2")
        return setWaypoint(player, color, x, player.y, z)
    }

    /** /hm set <color> <x> <y> <z> */
    private fun setColorXYZ(ctx: CommandContext<CommandSourceStack>, color: String): Int {
        val player = ctx.source.player ?: return 0
        val x = DoubleArgumentType.getDouble(ctx, "n1")
        val y = DoubleArgumentType.getDouble(ctx, "n2")
        val z = DoubleArgumentType.getDouble(ctx, "n3")
        return setWaypoint(player, color, x, y, z)
    }

    /** /hm set <n1:x> <n2:z> [color] — 2D, second arg is z */
    private fun setXZ(ctx: CommandContext<CommandSourceStack>, color: String?): Int {
        val player = ctx.source.player ?: return 0
        val x = DoubleArgumentType.getDouble(ctx, "n1")
        val z = DoubleArgumentType.getDouble(ctx, "n2")
        val colorToUse = color ?: getNextAvailableColor(player)
        return setWaypoint(player, colorToUse, x, player.y, z)
    }

    /** /hm set <n1:x> <n2:y> <n3:z> [color] — 3D */
    private fun setXYZ(ctx: CommandContext<CommandSourceStack>, color: String?): Int {
        val player = ctx.source.player ?: return 0
        val x = DoubleArgumentType.getDouble(ctx, "n1")
        val y = DoubleArgumentType.getDouble(ctx, "n2")
        val z = DoubleArgumentType.getDouble(ctx, "n3")
        val colorToUse = color ?: getNextAvailableColor(player)
        return setWaypoint(player, colorToUse, x, y, z)
    }

    private fun suggestColors(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> = SharedSuggestionProvider.suggest(VALID_COLORS, builder)

    private fun suggestActiveWaypoints(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val player = context.source.player ?: return builder.buildFuture()
        val dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension())
        return SharedSuggestionProvider.suggest(
            HeadingMarkerMod.getWaypoints(player.uuid, dimension).keys,
            builder,
        )
    }

    private fun suggestOtherPlayers(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val selfName = context.source.textName
        val names =
            context.source.server.playerList.players
                .map { it.name.string }
                .filter { it != selfName }
        return SharedSuggestionProvider.suggest(names, builder)
    }

    private fun setWaypoint(
        player: ServerPlayer,
        color: String,
        x: Double,
        y: Double,
        z: Double,
    ): Int {
        val lowerColor = color.lowercase()
        if (lowerColor !in VALID_COLORS) {
            player.sendSystemMessage(
                Component.literal(
                        "Unknown color: $color. Valid colors: ${VALID_COLORS.joinToString(", ")}"
                    )
                    .withStyle(ChatFormatting.RED)
            )
            return 0
        }
        HeadingMarkerMod.createWaypoint(player, lowerColor, x, y, z)
        player.sendSystemMessage(
            Component.literal(
                    "$lowerColor waypoint set at (${x.toInt()}, ${y.toInt()}, ${z.toInt()})"
                )
                .withStyle(ChatFormatting.GREEN)
        )
        return 1
    }

    private fun getNextAvailableColor(player: ServerPlayer): String {
        val dimension = HeadingMarkerMod.getDimensionKey(player.level().dimension())
        val existing = HeadingMarkerMod.getWaypoints(player.uuid, dimension)
        return VALID_COLORS.firstOrNull { it !in existing } ?: VALID_COLORS.first()
    }

    private fun removeWaypoint(player: ServerPlayer?, color: String): Int {
        player ?: return 0
        return if (HeadingMarkerMod.removeWaypoint(player, color)) {
            player.sendSystemMessage(
                Component.literal("$color waypoint removed").withStyle(ChatFormatting.YELLOW)
            )
            1
        } else {
            player.sendSystemMessage(
                Component.literal("No $color waypoint found.").withStyle(ChatFormatting.RED)
            )
            0
        }
    }

    private fun clearWaypointsInDimension(player: ServerPlayer?): Int {
        player ?: return 0
        val count = HeadingMarkerMod.clearWaypointsInDimension(player)
        if (count == 0) {
            player.sendSystemMessage(
                Component.literal("You have no waypoints to clear in this dimension.")
                    .withStyle(ChatFormatting.YELLOW)
            )
        } else {
            player.sendSystemMessage(
                Component.literal("Cleared $count waypoint(s) in this dimension.")
                    .withStyle(ChatFormatting.GREEN)
            )
        }
        return count
    }

    private fun clearAllWaypoints(player: ServerPlayer?): Int {
        player ?: return 0
        val count = HeadingMarkerMod.clearAllWaypoints(player)
        if (count == 0) {
            player.sendSystemMessage(
                Component.literal("You have no waypoints to clear.")
                    .withStyle(ChatFormatting.YELLOW)
            )
        } else {
            player.sendSystemMessage(
                Component.literal("Cleared $count waypoint(s) across all dimensions.")
                    .withStyle(ChatFormatting.GREEN)
            )
        }
        return count
    }

    private fun purgeOrphanedEntities(source: CommandSourceStack): Int {
        val removed = HeadingMarkerMod.purgeOrphanedWaypointEntities(source.server)
        if (removed == 0) {
            source.sendSuccess(
                {
                    Component.literal("No orphaned waypoint entities found.")
                        .withStyle(ChatFormatting.YELLOW)
                },
                false,
            )
        } else {
            source.sendSuccess(
                {
                    Component.literal(
                            "Purged $removed orphaned waypoint entity(ies) across all dimensions."
                        )
                        .withStyle(ChatFormatting.GREEN)
                },
                true,
            )
        }
        return 1
    }

    private fun shareWaypoint(fromPlayer: ServerPlayer?, targetName: String, color: String): Int {
        fromPlayer ?: return 0

        val lowerColor = color.lowercase()
        if (lowerColor !in VALID_COLORS) {
            fromPlayer.sendSystemMessage(
                Component.literal(
                        "Unknown color: $color. Valid colors: ${VALID_COLORS.joinToString(", ")}"
                    )
                    .withStyle(ChatFormatting.RED)
            )
            return 0
        }

        val toPlayer = fromPlayer.level().server!!.playerList.getPlayer(targetName)
        if (toPlayer == null) {
            fromPlayer.sendSystemMessage(
                Component.literal("Player not found or not online: $targetName")
                    .withStyle(ChatFormatting.RED)
            )
            return 0
        }

        if (toPlayer.uuid == fromPlayer.uuid) {
            fromPlayer.sendSystemMessage(
                Component.literal("You cannot share a waypoint with yourself.")
                    .withStyle(ChatFormatting.RED)
            )
            return 0
        }

        return if (HeadingMarkerMod.shareWaypoint(fromPlayer, toPlayer, lowerColor)) {
            fromPlayer.sendSystemMessage(
                Component.literal("Shared $lowerColor waypoint with $targetName")
                    .withStyle(ChatFormatting.GREEN)
            )
            toPlayer.sendSystemMessage(
                Component.literal(
                        "${fromPlayer.name.string} shared their $lowerColor waypoint with you."
                    )
                    .withStyle(ChatFormatting.AQUA)
            )
            1
        } else {
            fromPlayer.sendSystemMessage(
                Component.literal("You have no $lowerColor waypoint in this dimension to share.")
                    .withStyle(ChatFormatting.RED)
            )
            0
        }
    }

    private fun listWaypoints(player: ServerPlayer?): Int {
        player ?: return 0
        val dim = HeadingMarkerMod.getDimensionKey(player.level().dimension())
        val waypoints = HeadingMarkerMod.getWaypoints(player.uuid, dim)
        if (waypoints.isEmpty()) {
            player.sendSystemMessage(
                Component.literal("You have no active waypoints in $dim.")
                    .withStyle(ChatFormatting.YELLOW)
            )
            return 0
        }
        player.sendSystemMessage(
            Component.literal("Active Waypoints in $dim:").withStyle(ChatFormatting.GOLD)
        )
        for ((color, data) in waypoints) {
            player.sendSystemMessage(
                Component.literal(
                        " - $color at (${data.x.toInt()}, ${data.y.toInt()}, ${data.z.toInt()})"
                    )
                    .withStyle(ChatFormatting.GRAY)
            )
        }
        return waypoints.size
    }

    /**
     * MC 26.1 removed hasPermission() from CommandSourceStack, so we check the server's operator
     * list directly via PlayerList.isOp(). Non-player sources (console, command blocks) are treated
     * as operators.
     */
    private fun isOperator(source: CommandSourceStack): Boolean {
        val player = source.player ?: return true
        return (player.level() as ServerLevel)
            .server
            ?.playerList
            ?.isOp(NameAndId(player.uuid, player.gameProfile.name)) ?: false
    }

    private fun sendHelpMessage(source: CommandSourceStack) {
        fun line(text: String, vararg styles: ChatFormatting) =
            source.sendSuccess({ Component.literal(text).withStyle(*styles) }, false)

        fun cmdLine(cmd: String, desc: String) =
            source.sendSuccess(
                {
                    Component.literal("  $cmd")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(" - $desc").withStyle(ChatFormatting.GRAY))
                },
                false,
            )

        line("=== Heading Marker Help ===", ChatFormatting.GOLD, ChatFormatting.BOLD)
        line("")
        line("SET MARKER:", ChatFormatting.AQUA, ChatFormatting.BOLD)
        cmdLine("/hm set [color] [x z | x y z]", "Set a waypoint")
        line("")
        line("MANAGE:", ChatFormatting.AQUA, ChatFormatting.BOLD)
        cmdLine("/hm list", "List active waypoints")
        cmdLine("/hm remove <color>", "Remove a waypoint")
        cmdLine("/hm clear", "Clear waypoints in this dimension")
        cmdLine("/hm clearall", "Clear all waypoints")
        line("")
        line("SHARE:", ChatFormatting.AQUA, ChatFormatting.BOLD)
        cmdLine("/hm share <player> <color>", "Share a waypoint")
        line("")
        line("DISTANCE:", ChatFormatting.AQUA, ChatFormatting.BOLD)
        cmdLine("/trigger hm.distance", "Toggle distance display")
        if (isOperator(source)) {
            line("")
            line("ADMIN:", ChatFormatting.RED, ChatFormatting.BOLD)
            cmdLine("/hm purge", "Purge orphaned waypoint entities (OP only)")
        }
        line("=== =================== ===", ChatFormatting.GOLD)
    }
}
