package com.daolan.headingmarker

import com.daolan.headingmarker.model.WaypointColor
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
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.NameAndId

object HeadingMarkerCommands {

    private val VALID_COLORS: List<String> = WaypointColor.SELECTABLE.map { it.id }

    private const val INCOMPLETE_COORDS =
        "Incomplete coordinates. Usage: /hm set [color] <x> [y] <z>, e.g. /hm set red 100 200"

    private fun unknownColorMessage(color: String) =
        "Unknown color: $color. Valid colors: ${VALID_COLORS.joinToString(", ")}"

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
                            Commands.argument("selector", StringArgumentType.greedyString())
                                .suggests(::suggestActiveWaypoints)
                                .executes { ctx ->
                                    removeWaypoint(
                                        ctx.source.player,
                                        StringArgumentType.getString(ctx, "selector"),
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
                    Commands.literal("rename")
                        .then(
                            Commands.argument("selector", StringArgumentType.string())
                                .suggests(::suggestActiveWaypoints)
                                // /hm rename <selector> — clear the name
                                .executes { ctx ->
                                    renameWaypoint(
                                        ctx.source.player,
                                        StringArgumentType.getString(ctx, "selector"),
                                        "",
                                    )
                                }
                                // /hm rename <selector> <name> — set a name
                                .then(
                                    Commands.argument("name", StringArgumentType.greedyString())
                                        .executes { ctx ->
                                            renameWaypoint(
                                                ctx.source.player,
                                                StringArgumentType.getString(ctx, "selector"),
                                                StringArgumentType.getString(ctx, "name"),
                                            )
                                        }
                                )
                        )
                )
                .then(
                    Commands.literal("share")
                        .then(
                            Commands.argument("player", StringArgumentType.word())
                                .suggests(::suggestOtherPlayers)
                                .then(
                                    Commands.argument("selector", StringArgumentType.greedyString())
                                        .suggests(::suggestActiveWaypoints)
                                        .executes { ctx ->
                                            shareWaypoint(
                                                ctx.source.player,
                                                StringArgumentType.getString(ctx, "player"),
                                                StringArgumentType.getString(ctx, "selector"),
                                            )
                                        }
                                )
                        )
                )
                .then(
                    Commands.literal("set")
                        // /hm set — player pos, auto color
                        .executes { ctx -> setAtPlayerPos(ctx, null) }
                        // /hm set <x> <z> ... — coordinates first.
                        // ORDER MATTERS: this branch must be registered before the <color>
                        // branch. A word argument also accepts numbers ("100"), so for inputs
                        // like "/hm set 100 200" both branches parse the whole line, and
                        // Brigadier keeps the first-registered one on a tie. With <color>
                        // first, "100 200" became color="100" + an incomplete command.
                        .then(
                            Commands.argument("n1", DoubleArgumentType.doubleArg())
                                // /hm set <x> — not enough coordinates
                                .executes { ctx ->
                                    ctx.source.sendFailure(Component.literal(INCOMPLETE_COORDS))
                                    0
                                }
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
                                    // Numeric-looking words the double parser rejects (e.g. "1e5")
                                    // still land here.
                                    val message =
                                        if (arg.toDoubleOrNull() != null) INCOMPLETE_COORDS
                                        else unknownColorMessage(arg)
                                    ctx.source.sendFailure(Component.literal(message))
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
                                                            "n3",
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
                )
                .then(
                    Commands.literal("purge").requires(::isOperator).executes { ctx ->
                        purgeOrphanedEntities(ctx.source)
                    }
                )

        val node = dispatcher.register(hmCommand)
        dispatcher.register(Commands.literal("headingmarker").redirect(node))
    }

    private fun service() = HeadingMarkerMod.service()

    private fun setAtPlayerPos(ctx: CommandContext<CommandSourceStack>, color: String?): Int {
        val player = ctx.source.player ?: return 0
        return setWaypoint(player, color, player.x, player.y, player.z)
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
        return setWaypoint(player, color, x, player.y, z)
    }

    /** /hm set <n1:x> <n2:y> <n3:z> [color] — 3D */
    private fun setXYZ(ctx: CommandContext<CommandSourceStack>, color: String?): Int {
        val player = ctx.source.player ?: return 0
        val x = DoubleArgumentType.getDouble(ctx, "n1")
        val y = DoubleArgumentType.getDouble(ctx, "n2")
        val z = DoubleArgumentType.getDouble(ctx, "n3")
        return setWaypoint(player, color, x, y, z)
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
        val suggestions = linkedSetOf<String>()
        for (waypoint in service().waypointsHere(player).values) {
            suggestions.add(waypoint.key)
            suggestions.add(waypoint.color.id)
            if (waypoint.name.isNotBlank()) suggestions.add(waypoint.name)
        }
        return SharedSuggestionProvider.suggest(suggestions, builder)
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

    /** Creates a waypoint; a null [colorName] picks the player's least-used color. */
    private fun setWaypoint(
        player: ServerPlayer,
        colorName: String?,
        x: Double,
        y: Double,
        z: Double,
    ): Int {
        val color =
            if (colorName == null) service().nextColor(player)
            else WaypointColor.parse(colorName)?.takeIf { it.selectable }
        if (color == null) {
            player.sendSystemMessage(
                Component.literal(unknownColorMessage(colorName!!)).withStyle(ChatFormatting.RED)
            )
            return 0
        }
        val waypoint = service().create(player, color, x, y, z)
        if (waypoint == null) {
            player.sendSystemMessage(
                Component.literal("Failed to create waypoint. Check server logs.")
                    .withStyle(ChatFormatting.RED)
            )
            return 0
        }
        player.sendSystemMessage(
            Component.literal(
                    "${color.id} waypoint set at (${x.toInt()}, ${y.toInt()}, ${z.toInt()}) [key: ${waypoint.key}]"
                )
                .withStyle(ChatFormatting.GREEN)
        )
        return 1
    }

    private fun removeWaypoint(player: ServerPlayer?, selector: String): Int {
        player ?: return 0
        val removed = service().remove(player, selector).size
        return if (removed > 0) {
            player.sendSystemMessage(
                Component.literal("Removed $removed waypoint(s) matching \"$selector\".")
                    .withStyle(ChatFormatting.YELLOW)
            )
            removed
        } else {
            player.sendSystemMessage(
                Component.literal("No waypoint found matching \"$selector\" in this dimension.")
                    .withStyle(ChatFormatting.RED)
            )
            0
        }
    }

    private fun clearWaypointsInDimension(player: ServerPlayer?): Int {
        player ?: return 0
        val count = service().clearDimension(player)
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
        val count = service().clearAll(player)
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

    private fun renameWaypoint(player: ServerPlayer?, selector: String, newName: String): Int {
        player ?: return 0
        val trimmed = newName.trim()
        val renamed = service().rename(player, selector, trimmed).size
        return if (renamed > 0) {
            if (trimmed.isEmpty()) {
                player.sendSystemMessage(
                    Component.literal("Cleared name on $renamed waypoint(s) matching \"$selector\".")
                        .withStyle(ChatFormatting.GREEN)
                )
            } else {
                player.sendSystemMessage(
                    Component.literal(
                            "Renamed $renamed waypoint(s) matching \"$selector\" to \"$trimmed\"."
                        )
                        .withStyle(ChatFormatting.GREEN)
                )
            }
            renamed
        } else {
            player.sendSystemMessage(
                Component.literal("No waypoint found matching \"$selector\" in this dimension.")
                    .withStyle(ChatFormatting.RED)
            )
            0
        }
    }

    private fun purgeOrphanedEntities(source: CommandSourceStack): Int {
        val removed = service().purgeOrphans()
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

    private fun shareWaypoint(
        fromPlayer: ServerPlayer?,
        targetName: String,
        selector: String,
    ): Int {
        fromPlayer ?: return 0

        val toPlayer = fromPlayer.level().server.playerList.getPlayer(targetName)
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

        val shared = service().share(fromPlayer, toPlayer, selector)
        return if (shared > 0) {
            fromPlayer.sendSystemMessage(
                Component.literal("Shared $shared waypoint(s) matching \"$selector\" with $targetName")
                    .withStyle(ChatFormatting.GREEN)
            )
            val dimension = Dimensions.idOf(fromPlayer.level())
            toPlayer.sendSystemMessage(
                Component.literal(
                        "${fromPlayer.name.string} shared $shared waypoint(s) with you in $dimension."
                    )
                    .withStyle(ChatFormatting.AQUA)
            )
            shared
        } else {
            fromPlayer.sendSystemMessage(
                Component.literal("You have no waypoint matching \"$selector\" in this dimension.")
                    .withStyle(ChatFormatting.RED)
            )
            0
        }
    }

    private fun listWaypoints(player: ServerPlayer?): Int {
        player ?: return 0
        val dim = Dimensions.idOf(player.level())
        val waypoints = service().waypointsHere(player)
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
        for (waypoint in waypoints.values.sortedBy { it.key }) {
            val nameDisplay = if (waypoint.name.isNotBlank()) " \"${waypoint.name}\"" else ""
            player.sendSystemMessage(
                Component.literal(
                        " - ${waypoint.color.id}$nameDisplay at (${waypoint.x.toInt()}, ${waypoint.y.toInt()}, ${waypoint.z.toInt()}) [key: ${waypoint.key}]"
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
        return player.level()
            .server
            .playerList
            .isOp(NameAndId(player.uuid, player.gameProfile.name))
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

        line("=== Heading Marker (/hm) ===", ChatFormatting.GOLD, ChatFormatting.BOLD)
        cmdLine("/hm set [color] [x z | x y z]", "Place waypoint (your position if no coords)")
        cmdLine("/hm list", "List waypoints and keys in this dimension")
        cmdLine("/hm rename <selector> [name]", "Set a label, or clear it if no name")
        cmdLine("/hm remove <selector>", "Remove matching waypoints")
        cmdLine("/hm share <player> <selector>", "Give an online player copies")
        cmdLine("/hm clear", "Remove all waypoints in this dimension")
        cmdLine("/hm clearall", "Remove waypoints in every dimension")
        if (isOperator(source)) {
            cmdLine("/hm purge", "Remove orphaned waypoint entities (OP only)")
        }
        line(
            "<selector> is a key, color, or name and matches every fitting waypoint in this " +
                "dimension. Quote multi-word names for rename, e.g. \"Home Base\".",
            ChatFormatting.GRAY,
        )
        line("Distances to your waypoints show on the actionbar automatically.", ChatFormatting.GRAY)
    }
}
