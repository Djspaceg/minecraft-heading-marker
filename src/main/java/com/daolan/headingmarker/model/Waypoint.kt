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

    /** Same place, color, and name; ignores the key. */
    fun sameSpotAs(other: Waypoint): Boolean =
        color == other.color &&
            dimension == other.dimension &&
            x == other.x &&
            y == other.y &&
            z == other.z &&
            name == other.name
}
