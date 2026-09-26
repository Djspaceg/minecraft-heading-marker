package com.daolan.headingmarker.entity

import com.daolan.headingmarker.Dimensions
import com.daolan.headingmarker.HeadingMarkerMod.Companion.LOGGER
import com.daolan.headingmarker.model.Waypoint
import com.daolan.headingmarker.model.WaypointColor
import java.util.UUID
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.decoration.ArmorStand

/**
 * Owns the invisible armor stands that make waypoints show up on the vanilla locator bar. One
 * stand per online owner's waypoint; nothing here is persisted.
 */
class MarkerEntities(private val server: MinecraftServer) {

    private data class Ref(val owner: UUID, val dimension: String, val key: String)

    private val entityIds = HashMap<Ref, Int>()

    /** Spawns the marker entity for [owner]'s [waypoint]. Returns false if it couldn't. */
    fun spawn(owner: UUID, waypoint: Waypoint): Boolean {
        val level = Dimensions.levelFor(server, waypoint.dimension)
        if (level == null) {
            LOGGER.warn("No level for dimension {}; can't show waypoint {}", waypoint.dimension, waypoint.key)
            return false
        }
        val stand = createStand(level, waypoint)
        if (!level.addFreshEntity(stand)) {
            LOGGER.error("Failed to spawn marker entity for waypoint {}", waypoint.key)
            return false
        }
        stand.customName = Component.literal(NAME_PREFIX + waypoint.key)
        setColorWithCommand(stand, waypoint.color)
        server.playerList.getPlayer(owner)?.let { setViewersWithCommand(stand, it.gameProfile.name) }
        entityIds[Ref(owner, waypoint.dimension, waypoint.key)] = stand.id
        return true
    }

    /** Removes the marker entity for [owner]'s [waypoint], if there is one. */
    fun despawn(owner: UUID, waypoint: Waypoint) {
        val id = entityIds.remove(Ref(owner, waypoint.dimension, waypoint.key)) ?: return
        val level = Dimensions.levelFor(server, waypoint.dimension) ?: return
        (level.getEntity(id) as? ArmorStand)?.discard()
    }

    /** Removes every marker entity belonging to [owner]. Returns how many were found. */
    fun despawnAll(owner: UUID): Int {
        var removed = 0
        val refs = entityIds.keys.filter { it.owner == owner }
        for (ref in refs) {
            val id = entityIds.remove(ref) ?: continue
            val level = Dimensions.levelFor(server, ref.dimension) ?: continue
            val stand = level.getEntity(id) as? ArmorStand ?: continue
            stand.discard()
            removed++
        }
        return removed
    }

    /** Discards loaded marker-looking armor stands this session didn't spawn. */
    fun purgeOrphans(): Int {
        val known = entityIds.map { (ref, id) -> "${ref.dimension}:$id" }.toSet()
        var removed = 0
        for (dimension in listOf(Dimensions.OVERWORLD, Dimensions.NETHER, Dimensions.END)) {
            val level = Dimensions.levelFor(server, dimension) ?: continue
            val orphans =
                level.allEntities.filterIsInstance<ArmorStand>().filter {
                    looksLikeMarker(it) && "$dimension:${it.id}" !in known
                }
            for (stand in orphans) {
                LOGGER.info(
                    "Purging orphaned waypoint entity '{}' at ({},{},{}) in {}",
                    stand.customName?.string,
                    stand.blockX,
                    stand.blockY,
                    stand.blockZ,
                    dimension,
                )
                stand.discard()
                removed++
            }
        }
        return removed
    }

    private fun createStand(level: ServerLevel, waypoint: Waypoint): ArmorStand =
        ArmorStand(EntityTypes.ARMOR_STAND, level).apply {
            setPos(waypoint.x, waypoint.y, waypoint.z)
            isInvisible = true
            setPermanentlyInvulnerable(true)
            setNoGravity(true)
            isSilent = true
            // Marker mode drops the hitbox; setMarker() is private, so set the synced flags.
            entityData.set(
                ArmorStand.DATA_CLIENT_FLAGS,
                (ArmorStand.CLIENT_FLAG_NO_BASEPLATE or ArmorStand.CLIENT_FLAG_MARKER).toByte(),
            )
            getAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE)?.baseValue = TRANSMIT_RANGE
        }

    private fun setColorWithCommand(stand: ArmorStand, color: WaypointColor) =
        runQuietly("waypoint modify ${stand.stringUUID} color ${color.vanillaName}")

    private fun setViewersWithCommand(stand: ArmorStand, playerName: String) =
        runQuietly("waypoint modify ${stand.stringUUID} viewers @a[name=$playerName]")

    private fun runQuietly(command: String) {
        try {
            val source = server.createCommandSourceStack().withSuppressedOutput()
            server.commands.performCommand(server.commands.dispatcher.parse(command, source), command)
        } catch (e: Exception) {
            LOGGER.warn("Command '{}' failed: {}", command, e.message)
        }
    }

    companion object {
        /** Custom-name prefix that identifies marker entities; the key follows it. */
        const val NAME_PREFIX = "hm:"
        private const val TRANSMIT_RANGE = 9999.0

        /** Names used by older mod versions and the datapack, e.g. "red waypoint". */
        private val LEGACY_NAMES: Set<String> =
            WaypointColor.entries.flatMap { listOf("${it.id} waypoint", "${it.vanillaName} waypoint") }
                .toSet()

        fun looksLikeMarker(stand: ArmorStand): Boolean {
            val name = stand.customName?.string ?: return false
            return name.startsWith(NAME_PREFIX) || name in LEGACY_NAMES
        }
    }
}
