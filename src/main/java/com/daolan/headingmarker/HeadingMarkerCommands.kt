package com.daolan.headingmarker

import com.daolan.headingmarker.model.Waypoint
import com.daolan.headingmarker.model.WaypointColor
import com.daolan.headingmarker.model.cleanInput
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.level.Level

/** The /hm command tree. Parses input, calls [WaypointService], and formats the feedback. */
object HeadingMarkerCommands {

    private val COLOR_NAMES: List<String> = WaypointColor.SELECTABLE.map { it.id }

    private const val INCOMPLETE_COORDS =
        "Incomplete coordinates. Usage: /hm set [color] <x> [y] <z>, e.g. /hm set red 100 200"

    /** Same bar as vanilla admin commands like /kill: permission level 2. */
    private val IS_GAMEMASTER = Commands.hasPermission<CommandSourceStack>(Commands.LEVEL_GAMEMASTERS)

    private fun unknownColorMessage(color: String) =
        "Unknown color: $color. Valid colors: ${COLOR_NAMES.joinToString(", ")}"

    @JvmStatic
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registryAccess: CommandBuildContext?,
        environment: Commands.CommandSelection,
    ) {
        val hmCommand =
            Commands.literal("hm")
                .executes { help(it.source) }
                .then(Commands.literal("help").executes { help(it.source) })
                .then(Commands.literal("list").executes { list(it.player()) })
                .then(
                    Commands.literal("remove")
                        .then(
                            selectorArgument(StringArgumentType.greedyString()).executes {
                                remove(it.player(), it.selector())
                            }
                        )
                )
                .then(Commands.literal("clear").executes { clear(it.player()) })
                .then(Commands.literal("clearall").executes { clearAll(it.player()) })
                .then(
                    Commands.literal("rename")
                        .then(
                            // A single (optionally quoted) word, so a name can follow it.
                            selectorArgument(StringArgumentType.string())
                                // /hm rename <selector> — clear the name
                                .executes { rename(it.player(), it.selector(), "") }
                                // /hm rename <selector> <name> — set a name
                                .then(
                                    Commands.argument("name", StringArgumentType.greedyString())
                                        .executes {
                                            rename(
                                                it.player(),
                                                it.selector(),
                                                StringArgumentType.getString(it, "name"),
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
                                    selectorArgument(StringArgumentType.greedyString()).executes {
                                        share(
                                            it.player(),
                                            StringArgumentType.getString(it, "player"),
                                            it.selector(),
                                        )
                                    }
                                )
                        )
                )
                .then(setCommand())
                .then(
                    Commands.literal("purge").requires(IS_GAMEMASTER).executes { purge(it.source) }
                )

        val node = dispatcher.register(hmCommand)
        // A redirect alone is an incomplete command, so the bare alias needs its own executes.
        dispatcher.register(
            Commands.literal("headingmarker").executes { help(it.source) }.redirect(node)
        )
    }

    private fun setCommand() =
        Commands.literal("set")
            // /hm set — player pos, auto color
            .executes { set(it, null, Shape.HERE) }
            // /hm set <x> <z> ... — coordinates first.
            // ORDER MATTERS: this branch must be registered before the <color> branch. A word
            // argument also accepts numbers ("100"), so for inputs like "/hm set 100 200" both
            // branches parse the whole line, and Brigadier keeps the first-registered one on a
            // tie. With <color> first, "100 200" became color="100" + coordinates.
            .then(
                Commands.argument("n1", DoubleArgumentType.doubleArg())
                    // /hm set <x> — not enough coordinates
                    .executes(::incompleteCoordinates)
                    .then(
                        Commands.argument("n2", DoubleArgumentType.doubleArg())
                            // /hm set <x> <z> — 2D, auto color
                            .executes { set(it, null, Shape.XZ) }
                            .then(
                                Commands.argument("n3", DoubleArgumentType.doubleArg())
                                    // /hm set <x> <y> <z> — 3D, auto color
                                    .executes { set(it, null, Shape.XYZ) }
                                    // /hm set <x> <y> <z> <color>
                                    .then(colorArgument().executes { set(it, it.color(), Shape.XYZ) })
                            )
                            // /hm set <x> <z> <color>
                            .then(colorArgument().executes { set(it, it.color(), Shape.XZ) })
                    )
            )
            // /hm set <color> ...
            .then(
                colorArgument()
                    // /hm set <color> — player pos
                    .executes {
                        // Numeric-looking words the double parser rejects (e.g. "1e5") land here.
                        if (it.color().toDoubleOrNull() != null) incompleteCoordinates(it)
                        else set(it, it.color(), Shape.HERE)
                    }
                    .then(
                        Commands.argument("n1", DoubleArgumentType.doubleArg())
                            // /hm set <color> <x>
                            .executes(::incompleteCoordinates)
                            .then(
                                Commands.argument("n2", DoubleArgumentType.doubleArg())
                                    // /hm set <color> <x> <z>
                                    .executes { set(it, it.color(), Shape.XZ) }
                                    // /hm set <color> <x> <y> <z>
                                    .then(
                                        Commands.argument("n3", DoubleArgumentType.doubleArg())
                                            .executes { set(it, it.color(), Shape.XYZ) }
                                    )
                            )
                    )
            )

    // --- Arguments ---

    private fun colorArgument() =
        Commands.argument("color", StringArgumentType.word()).suggests { _, builder ->
            SharedSuggestionProvider.suggest(COLOR_NAMES, builder)
        }

    private fun selectorArgument(
        type: StringArgumentType
    ): RequiredArgumentBuilder<CommandSourceStack, String> {
        // A greedy argument takes the rest of the line verbatim; a string argument needs quotes
        // around anything but plain word characters, or the suggestion won't parse.
        val quote = type.type != StringArgumentType.StringType.GREEDY_PHRASE
        return Commands.argument("selector", type).suggests { ctx, builder ->
            suggestSelectors(ctx, builder, quote)
        }
    }

    private fun CommandContext<CommandSourceStack>.player(): ServerPlayer =
        source.playerOrException

    private fun CommandContext<CommandSourceStack>.selector(): String =
        StringArgumentType.getString(this, "selector")

    private fun CommandContext<CommandSourceStack>.color(): String =
        StringArgumentType.getString(this, "color")

    private fun suggestSelectors(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder,
        quote: Boolean,
    ): CompletableFuture<Suggestions> {
        val player = context.source.player ?: return builder.buildFuture()
        val suggestions = linkedSetOf<String>()
        for (waypoint in HeadingMarkerMod.service().waypointsHere(player).values) {
            suggestions.add(waypoint.key)
            suggestions.add(waypoint.color.id)
            if (waypoint.name.isNotBlank()) suggestions.add(waypoint.name)
        }
        val shown = if (quote) suggestions.map(StringArgumentType::escapeIfRequired) else suggestions
        return SharedSuggestionProvider.suggest(shown, builder)
    }

    private fun suggestOtherPlayers(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val selfName = context.source.textName
        val names =
            context.source.server.playerList.players
                .map { it.gameProfile.name }
                .filter { it != selfName }
        return SharedSuggestionProvider.suggest(names, builder)
    }

    // --- Actions ---

    private enum class Shape {
        HERE,
        XZ,
        XYZ,
    }

    private fun incompleteCoordinates(ctx: CommandContext<CommandSourceStack>): Int {
        ctx.source.sendFailure(Component.literal(INCOMPLETE_COORDS))
        return 0
    }

    /** Creates a waypoint; a null [colorName] picks the player's least-used color. */
    private fun set(ctx: CommandContext<CommandSourceStack>, colorName: String?, shape: Shape): Int {
        val player = ctx.player()
        fun arg(name: String) = DoubleArgumentType.getDouble(ctx, name)
        val (x, y, z) =
            when (shape) {
                Shape.HERE -> Triple(player.x, player.y, player.z)
                Shape.XZ -> Triple(arg("n1"), player.y, arg("n2"))
                Shape.XYZ -> Triple(arg("n1"), arg("n2"), arg("n3"))
            }

        val service = HeadingMarkerMod.service()
        val color =
            if (colorName == null) service.nextColor(player)
            else WaypointColor.parse(colorName)?.takeIf { it.selectable }
        if (color == null) {
            player.tell(unknownColorMessage(colorName!!), ChatFormatting.RED)
            return 0
        }
        if (!Level.isInSpawnableBounds(BlockPos.containing(x, y, z))) {
            player.tell("Those coordinates are outside the world.", ChatFormatting.RED)
            return 0
        }
        val waypoint = service.create(player, color, x, y, z)
        if (waypoint == null) {
            player.tell("Failed to create waypoint. Check server logs.", ChatFormatting.RED)
            return 0
        }
        player.tell(
            "${color.id} waypoint set at (${blockCoords(waypoint)}) [key: ${waypoint.key}]",
            ChatFormatting.GREEN,
        )
        return 1
    }

    private fun remove(player: ServerPlayer, selector: String): Int {
        val removed = HeadingMarkerMod.service().remove(player, selector).size
        if (removed == 0) return noMatch(player, selector)
        player.tell("Removed $removed waypoint(s) matching \"$selector\".", ChatFormatting.YELLOW)
        return removed
    }

    private fun clear(player: ServerPlayer): Int {
        val count = HeadingMarkerMod.service().clearDimension(player)
        if (count == 0) {
            player.tell("You have no waypoints to clear in this dimension.", ChatFormatting.YELLOW)
        } else {
            player.tell("Cleared $count waypoint(s) in this dimension.", ChatFormatting.GREEN)
        }
        return count
    }

    private fun clearAll(player: ServerPlayer): Int {
        val count = HeadingMarkerMod.service().clearAll(player)
        if (count == 0) {
            player.tell("You have no waypoints to clear.", ChatFormatting.YELLOW)
        } else {
            player.tell("Cleared $count waypoint(s) across all dimensions.", ChatFormatting.GREEN)
        }
        return count
    }

    private fun rename(player: ServerPlayer, selector: String, newName: String): Int {
        val name = cleanInput(newName)
        val renamed = HeadingMarkerMod.service().rename(player, selector, name).size
        if (renamed == 0) return noMatch(player, selector)
        if (name.isEmpty()) {
            player.tell("Cleared name on $renamed waypoint(s) matching \"$selector\".", ChatFormatting.GREEN)
        } else {
            player.tell(
                "Renamed $renamed waypoint(s) matching \"$selector\" to \"$name\".",
                ChatFormatting.GREEN,
            )
        }
        return renamed
    }

    private fun share(from: ServerPlayer, targetName: String, selector: String): Int {
        val to = from.level().server.playerList.getPlayer(targetName)
        if (to == null) {
            from.tell("Player not found or not online: $targetName", ChatFormatting.RED)
            return 0
        }
        if (to.uuid == from.uuid) {
            from.tell("You cannot share a waypoint with yourself.", ChatFormatting.RED)
            return 0
        }

        val result = HeadingMarkerMod.service().share(from, to, selector)
        val targetLabel = to.gameProfile.name
        if (result.matched == 0) return noMatch(from, selector)
        if (result.shared == 0) {
            from.tell("$targetLabel already has those waypoint(s).", ChatFormatting.YELLOW)
            return 0
        }
        val skipped =
            if (result.alreadyHad > 0) " (${result.alreadyHad} they already had were skipped)" else ""
        from.tell(
            "Shared ${result.shared} waypoint(s) matching \"$selector\" with $targetLabel$skipped.",
            ChatFormatting.GREEN,
        )
        to.tell(
            "${from.gameProfile.name} shared ${result.shared} waypoint(s) with you in " +
                "${Dimensions.idOf(from.level())}.",
            ChatFormatting.AQUA,
        )
        return result.shared
    }

    private fun purge(source: CommandSourceStack): Int {
        val removed = HeadingMarkerMod.service().purgeOrphans()
        if (removed == 0) {
            source.sendSuccess(
                { Component.literal("No orphaned waypoint entities found.").withStyle(ChatFormatting.YELLOW) },
                false,
            )
        } else {
            source.sendSuccess(
                {
                    Component.literal("Purged $removed orphaned waypoint entity(ies) across all dimensions.")
                        .withStyle(ChatFormatting.GREEN)
                },
                true,
            )
        }
        return removed
    }

    private fun list(player: ServerPlayer): Int {
        val dimension = Dimensions.idOf(player.level())
        val waypoints = HeadingMarkerMod.service().waypointsHere(player)
        if (waypoints.isEmpty()) {
            player.tell("You have no active waypoints in $dimension.", ChatFormatting.YELLOW)
            return 0
        }
        player.tell("Active Waypoints in $dimension:", ChatFormatting.GOLD)
        for (waypoint in waypoints.values.sortedBy { it.key }) {
            val name = if (waypoint.name.isNotBlank()) " \"${waypoint.name}\"" else ""
            player.tell(
                " - ${waypoint.color.id}$name at (${blockCoords(waypoint)}) [key: ${waypoint.key}]",
                ChatFormatting.GRAY,
            )
        }
        return waypoints.size
    }

    private fun help(source: CommandSourceStack): Int {
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
        if (IS_GAMEMASTER.test(source)) {
            cmdLine("/hm purge", "Remove orphaned waypoint entities (OP only)")
        }
        line(
            "<selector> is a key, color, or name and matches every fitting waypoint in this " +
                "dimension. Quote multi-word names, e.g. \"Home Base\".",
            ChatFormatting.GRAY,
        )
        line("Distances to your waypoints show on the actionbar automatically.", ChatFormatting.GRAY)
        return 1
    }

    // --- Formatting ---

    private fun noMatch(player: ServerPlayer, selector: String): Int {
        player.tell("No waypoint found matching \"$selector\" in this dimension.", ChatFormatting.RED)
        return 0
    }

    /** Block coordinates as F3 shows them: floored, so -0.5 is block -1, not 0. */
    private fun blockCoords(waypoint: Waypoint): String =
        "${Mth.floor(waypoint.x)}, ${Mth.floor(waypoint.y)}, ${Mth.floor(waypoint.z)}"

    private fun ServerPlayer.tell(text: String, color: ChatFormatting) =
        sendSystemMessage(Component.literal(text).withStyle(color))
}
