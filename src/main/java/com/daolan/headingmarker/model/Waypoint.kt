package com.daolan.headingmarker.model

/**
 * A saved waypoint. Pure data: the marker entity that shows it on the locator bar is tracked
 * separately by [com.daolan.headingmarker.entity.MarkerEntities].
 */
data class Waypoint(
    /** Short id, unique among one player's waypoints in one dimension. See [MarkerKeys]. */
    val key: String,
    val color: WaypointColor,
    /** Dimension id as produced by [com.daolan.headingmarker.Dimensions.idOf]. */
    val dimension: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val name: String = "",
) {
    /** The name if set, otherwise the capitalized color. */
    val label: String
        get() = name.ifBlank { color.displayName }

    /** Same color at the same place; ignores the key and name. */
    fun sameSpotAs(other: Waypoint): Boolean =
        color == other.color &&
            dimension == other.dimension &&
            x == other.x &&
            y == other.y &&
            z == other.z
}

/**
 * Normalizes a typed selector or name: trims it and strips one pair of surrounding double quotes,
 * so `"Home Base"` means the same as `Home Base` in every command, greedy arguments included.
 */
fun cleanInput(text: String): String {
    val trimmed = text.trim()
    val quoted = trimmed.length >= 2 && trimmed.startsWith('"') && trimmed.endsWith('"')
    return if (quoted) trimmed.substring(1, trimmed.length - 1).trim() else trimmed
}
