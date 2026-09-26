package com.daolan.headingmarker.model

import net.minecraft.ChatFormatting

enum class WaypointColor(
    /** Name players type and the mod stores. */
    val id: String,
    val emoji: String,
    val formatting: ChatFormatting,
) {
    RED("red", "🔴", ChatFormatting.RED),
    BLUE("blue", "🔵", ChatFormatting.BLUE),
    GREEN("green", "🟢", ChatFormatting.GREEN),
    YELLOW("yellow", "🟡", ChatFormatting.YELLOW),
    PURPLE("purple", "🟣", ChatFormatting.LIGHT_PURPLE),
    /** Fallback for stored colors the mod doesn't know. Players can't pick it. */
    WHITE("white", "⚪", ChatFormatting.WHITE);

    /** Name vanilla commands use for this color (e.g. "light_purple" for PURPLE). */
    val vanillaName: String
        get() = formatting.name.lowercase()

    val displayName: String
        get() = id.replaceFirstChar { it.uppercaseChar() }

    val selectable: Boolean
        get() = this != WHITE

    companion object {
        val SELECTABLE: List<WaypointColor> = entries.filter { it.selectable }

        /** Old saves and the vanilla color list call purple "light_purple". */
        const val LEGACY_PURPLE = "light_purple"

        private val BY_NAME: Map<String, WaypointColor> =
            entries.associateBy { it.id } + (LEGACY_PURPLE to PURPLE)

        /** Every word a selector reads as a color, so marker keys must never equal one. */
        val RESERVED_WORDS: Set<String> = BY_NAME.keys

        /** Parses a color name, accepting the legacy "light_purple" alias. */
        fun parse(name: String): WaypointColor? = BY_NAME[name.trim().lowercase()]

        /** Resolves a stored color, falling back to [WHITE] for anything unknown. */
        fun fromStored(name: String?): WaypointColor = name?.let(::parse) ?: WHITE
    }
}
