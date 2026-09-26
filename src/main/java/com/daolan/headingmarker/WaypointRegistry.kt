package com.daolan.headingmarker

import com.daolan.headingmarker.model.MarkerKeys
import com.daolan.headingmarker.model.Waypoint
import com.daolan.headingmarker.model.WaypointColor
import java.util.UUID

/**
 * In-memory waypoint data: owner → dimension → key → [Waypoint]. Knows nothing about entities,
 * files, or commands, and remembers which owners changed since the last [takeDirty].
 */
class WaypointRegistry {
    private val byOwner = HashMap<UUID, MutableMap<String, MutableMap<String, Waypoint>>>()
    private val dirty = HashSet<UUID>()

    /** Replaces all data with [data] (e.g. freshly loaded from disk). Nothing is marked dirty. */
    fun replaceAll(data: Map<UUID, Map<String, Map<String, Waypoint>>>) {
        byOwner.clear()
        dirty.clear()
        for ((owner, dimensions) in data) {
            val copy = byOwner.getOrPut(owner) { HashMap() }
            for ((dimension, waypoints) in dimensions) {
                copy[dimension] = HashMap(waypoints)
            }
        }
    }

    fun owners(): Set<UUID> = byOwner.keys.toSet()

    /** A snapshot of [owner]'s waypoints in [dimension], keyed by marker key. */
    fun waypoints(owner: UUID, dimension: String): Map<String, Waypoint> =
        byOwner[owner]?.get(dimension)?.toMap() ?: emptyMap()

    /** A snapshot of all of [owner]'s waypoints: dimension → key → waypoint. */
    fun allDimensions(owner: UUID): Map<String, Map<String, Waypoint>> =
        byOwner[owner]?.mapValues { (_, waypoints) -> waypoints.toMap() } ?: emptyMap()

    /** Adds a waypoint under a freshly generated key and returns it. */
    fun add(
        owner: UUID,
        dimension: String,
        color: WaypointColor,
        x: Double,
        y: Double,
        z: Double,
        name: String = "",
    ): Waypoint {
        val bucket = bucket(owner, dimension)
        val key = MarkerKeys.generate { it in bucket }
        val waypoint = Waypoint(key, color, dimension, x, y, z, name.trim())
        bucket[key] = waypoint
        dirty += owner
        return waypoint
    }

    /**
     * Waypoints [selector] refers to: the one whose key matches exactly, otherwise every
     * waypoint whose color or name matches (case-insensitive).
     */
    fun select(owner: UUID, dimension: String, selector: String): List<Waypoint> {
        val waypoints = byOwner[owner]?.get(dimension) ?: return emptyList()
        return selectFrom(waypoints, selector)
    }

    fun remove(owner: UUID, dimension: String, key: String): Waypoint? {
        val removed = byOwner[owner]?.get(dimension)?.remove(key) ?: return null
        dirty += owner
        return removed
    }

    fun rename(owner: UUID, dimension: String, key: String, name: String): Waypoint? {
        val bucket = byOwner[owner]?.get(dimension) ?: return null
        val existing = bucket[key] ?: return null
        val renamed = existing.copy(name = name.trim())
        bucket[key] = renamed
        dirty += owner
        return renamed
    }

    /** Removes and returns every waypoint [owner] has in [dimension]. */
    fun clear(owner: UUID, dimension: String): List<Waypoint> {
        val bucket = byOwner[owner]?.get(dimension) ?: return emptyList()
        val removed = bucket.values.toList()
        bucket.clear()
        if (removed.isNotEmpty()) dirty += owner
        return removed
    }

    /** Removes and returns every waypoint [owner] has in any dimension. */
    fun clearAll(owner: UUID): List<Waypoint> {
        val dimensions = byOwner[owner] ?: return emptyList()
        val removed = dimensions.values.flatMap { it.values }
        dimensions.clear()
        if (removed.isNotEmpty()) dirty += owner
        return removed
    }

    /** Owners changed since the last call. */
    fun takeDirty(): Set<UUID> {
        val changed = dirty.toSet()
        dirty.clear()
        return changed
    }

    private fun bucket(owner: UUID, dimension: String): MutableMap<String, Waypoint> =
        byOwner.getOrPut(owner) { HashMap() }.getOrPut(dimension) { HashMap() }

    companion object {
        fun selectFrom(waypoints: Map<String, Waypoint>, selector: String): List<Waypoint> {
            val wanted = selector.trim()
            if (wanted.isEmpty()) return emptyList()
            waypoints[wanted]?.let { return listOf(it) }
            val color = WaypointColor.parse(wanted)
            return waypoints.values.filter {
                it.color == color || it.name.equals(wanted, ignoreCase = true)
            }
        }
    }
}
