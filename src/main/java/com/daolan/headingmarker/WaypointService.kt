package com.daolan.headingmarker

import com.daolan.headingmarker.HeadingMarkerMod.Companion.LOGGER
import com.daolan.headingmarker.entity.MarkerEntities
import com.daolan.headingmarker.model.Waypoint
import com.daolan.headingmarker.model.WaypointColor
import com.daolan.headingmarker.storage.WaypointStorage
import java.nio.file.Path
import java.util.UUID
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

/**
 * Everything Heading Marker does for one running server: keeps waypoint data, marker entities,
 * the distance HUD, and the save files in step. Created when the server starts and dropped when it
 * stops, so nothing leaks from one world into the next.
 */
class WaypointService(private val server: MinecraftServer, private val storageDir: Path) {
    private val registry = WaypointRegistry()
    private val entities = MarkerEntities(server)
    private val hud = DistanceHud()
    private var ticks = 0

    fun load() {
        val loaded = WaypointStorage.loadWaypoints(storageDir)
        registry.replaceAll(loaded)
        LOGGER.info("Loaded waypoints for {} players.", loaded.size)
    }

    /** Runs once the levels exist: moves waypoints saved under pre-namespace dimension ids. */
    fun onLevelsLoaded() {
        for (legacyId in registry.dimensionIds()) {
            val current = Dimensions.upgradeLegacyId(server, legacyId) ?: continue
            LOGGER.info("Moving waypoints from dimension id {} to {}", legacyId, current)
            registry.renameDimension(legacyId, current)
        }
    }

    /** Writes every owner whose waypoints changed since the last save. */
    fun saveChanged() {
        for (owner in registry.takeDirty()) {
            WaypointStorage.savePlayer(storageDir, owner, registry.allDimensions(owner))
        }
    }

    // --- Queries ---

    fun waypoints(owner: UUID, dimension: String): Map<String, Waypoint> =
        registry.waypoints(owner, dimension)

    fun waypointsHere(player: ServerPlayer): Map<String, Waypoint> =
        registry.waypoints(player.uuid, Dimensions.idOf(player.level()))

    /** The selectable color [player] has used least in their current dimension. */
    fun nextColor(player: ServerPlayer): WaypointColor {
        val counts = waypointsHere(player).values.groupingBy { it.color }.eachCount()
        return WaypointColor.SELECTABLE.minBy { counts[it] ?: 0 }
    }

    // --- Changes ---

    /** Creates a waypoint in [player]'s current dimension, or returns null if its marker failed. */
    fun create(player: ServerPlayer, color: WaypointColor, x: Double, y: Double, z: Double): Waypoint? {
        val waypoint = registry.add(player.uuid, Dimensions.idOf(player.level()), color, x, y, z)
        if (!entities.spawn(player.uuid, waypoint)) {
            registry.remove(player.uuid, waypoint.dimension, waypoint.key)
            return null
        }
        LOGGER.info(
            "{} created waypoint {} ({}) in {} at ({}, {}, {})",
            player.name.string,
            waypoint.key,
            color.id,
            waypoint.dimension,
            x,
            y,
            z,
        )
        return waypoint
    }

    /** Removes every waypoint in [player]'s dimension that [selector] matches. */
    fun remove(player: ServerPlayer, selector: String): List<Waypoint> {
        val dimension = Dimensions.idOf(player.level())
        val matched = registry.select(player.uuid, dimension, selector)
        for (waypoint in matched) {
            entities.despawn(player.uuid, waypoint)
            registry.remove(player.uuid, dimension, waypoint.key)
        }
        return matched
    }

    /** Sets (or clears, if blank) the name of every waypoint [selector] matches. */
    fun rename(player: ServerPlayer, selector: String, name: String): List<Waypoint> {
        val dimension = Dimensions.idOf(player.level())
        return registry.select(player.uuid, dimension, selector).mapNotNull {
            registry.rename(player.uuid, dimension, it.key, name)
        }
    }

    fun clearDimension(player: ServerPlayer): Int {
        val removed = registry.clear(player.uuid, Dimensions.idOf(player.level()))
        removed.forEach { entities.despawn(player.uuid, it) }
        return removed.size
    }

    fun clearAll(player: ServerPlayer): Int {
        val removed = registry.clearAll(player.uuid)
        removed.forEach { entities.despawn(player.uuid, it) }
        return removed.size
    }

    /** Gives [to] their own copy of each of [from]'s waypoints that [selector] matches. */
    fun share(from: ServerPlayer, to: ServerPlayer, selector: String): Int {
        val dimension = Dimensions.idOf(from.level())
        var shared = 0
        for (source in registry.select(from.uuid, dimension, selector)) {
            val copy =
                registry.add(to.uuid, dimension, source.color, source.x, source.y, source.z, source.name)
            if (entities.spawn(to.uuid, copy)) shared++
            else registry.remove(to.uuid, dimension, copy.key)
        }
        return shared
    }

    fun purgeOrphans(): Int = entities.purgeOrphans()

    // --- Events ---

    fun onJoin(player: ServerPlayer) {
        for (waypoints in registry.allDimensions(player.uuid).values) {
            for (waypoint in waypoints.values) entities.spawn(player.uuid, waypoint)
        }
    }

    fun onDisconnect(player: ServerPlayer) {
        val removed = entities.despawnAll(player.uuid)
        hud.forget(player.uuid)
        if (removed > 0) {
            LOGGER.info("Removed {} marker entities for {}", removed, player.name.string)
        }
    }

    fun onEntityLoad(entity: Entity, level: ServerLevel) = entities.onEntityLoad(entity, level)

    fun tick() {
        entities.tick()
        if (++ticks < HUD_INTERVAL_TICKS) return
        ticks = 0
        for (player in server.playerList.players) {
            hud.update(player, waypointsHere(player).values)
        }
    }

    private companion object {
        const val HUD_INTERVAL_TICKS = 5
    }
}
