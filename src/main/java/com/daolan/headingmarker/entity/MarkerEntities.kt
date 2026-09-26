package com.daolan.headingmarker.entity

import com.daolan.headingmarker.Dimensions
import com.daolan.headingmarker.HeadingMarkerMod.Companion.LOGGER
import com.daolan.headingmarker.model.Waypoint
import com.daolan.headingmarker.model.WaypointColor
import java.lang.reflect.Field
import java.util.Optional
import java.util.UUID
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.scores.TeamColor

/**
 * Owns the invisible armor stands that put waypoints on the vanilla locator bar: exactly one live
 * stand per waypoint of each online owner.
 *
 * Stands are held by reference, not looked up by id. A stand in an unloaded chunk still transmits
 * (vanilla registers it with the waypoint manager when it is created), but Level.getEntity can't
 * see it, so id lookups silently missed every distant marker.
 *
 * Stands are disposable. Whenever the bound stand goes away for any reason other than [despawn]
 * (killed, or written out with its chunk by a world save), a fresh one replaces it. Any
 * marker-looking stand that loads and isn't the bound one is a leftover from an earlier session
 * or an unloaded copy, and is discarded.
 */
class MarkerEntities(private val server: MinecraftServer) {

    private data class Ref(val owner: UUID, val dimension: String, val key: String)

    private class Binding(val ref: Ref, val waypoint: Waypoint, var stand: ArmorStand)

    private val byRef = HashMap<Ref, Binding>()
    private val byEntityId = HashMap<UUID, Binding>()
    private val orphans = ArrayList<Entity>()
    private var ticks = 0

    /** Shows [owner]'s [waypoint], replacing any stand it already had. */
    fun spawn(owner: UUID, waypoint: Waypoint): Boolean {
        val ref = Ref(owner, waypoint.dimension, waypoint.key)
        byRef[ref]?.let { discard(unbind(it).stand) }

        val level = Dimensions.levelFor(server, waypoint.dimension)
        if (level == null) {
            LOGGER.warn("No level for dimension {}; can't show waypoint {}", waypoint.dimension, waypoint.key)
            return false
        }
        val stand = createStand(level, waypoint)
        // Bind before adding: adding fires ENTITY_LOAD, which must see the stand as ours.
        val binding = Binding(ref, waypoint, stand).also(::bind)
        if (!level.addFreshEntity(stand)) {
            unbind(binding)
            LOGGER.error("Failed to spawn marker entity for waypoint {}", waypoint.key)
            return false
        }
        return true
    }

    /** Removes the stand for [owner]'s [waypoint], wherever it is. */
    fun despawn(owner: UUID, waypoint: Waypoint) {
        val binding = byRef[Ref(owner, waypoint.dimension, waypoint.key)] ?: return
        discard(unbind(binding).stand)
    }

    /** Removes every stand belonging to [owner]. Returns how many there were. */
    fun despawnAll(owner: UUID): Int {
        val owned = byRef.values.filter { it.ref.owner == owner }
        owned.forEach { discard(unbind(it).stand) }
        return owned.size
    }

    /** Fabric ENTITY_LOAD: keep our stands, queue any other marker-looking stand for removal. */
    fun onEntityLoad(entity: Entity, level: ServerLevel) {
        if (entity !is ArmorStand || !looksLikeMarker(entity)) return
        val binding = byEntityId[entity.uuid]
        when {
            binding == null || binding.ref.dimension != Dimensions.idOf(level) -> orphans += entity
            binding.stand === entity -> Unit
            // Our stand was written out with its chunk and read back before tick() noticed.
            binding.stand.isRemoved -> binding.stand = entity
            else -> orphans += entity
        }
    }

    fun tick() {
        // Discard outside the load callback; removing an entity while it's being added is unsafe.
        if (orphans.isNotEmpty()) {
            for (orphan in orphans) {
                if (orphan.isRemoved) continue
                LOGGER.info(
                    "Removing leftover marker entity '{}' at ({}, {}, {})",
                    orphan.customName?.string,
                    orphan.blockX,
                    orphan.blockY,
                    orphan.blockZ,
                )
                orphan.discard()
            }
            orphans.clear()
        }
        if (++ticks >= RESPAWN_CHECK_TICKS) {
            ticks = 0
            val lost = byRef.values.filter { it.stand.isRemoved }
            for (binding in lost) {
                LOGGER.debug(
                    "Marker entity for {} went away ({}); replacing it",
                    binding.ref.key,
                    binding.stand.removalReason,
                )
                spawn(binding.ref.owner, binding.waypoint)
            }
        }
    }

    /** Discards loaded marker-looking armor stands that aren't bound to a waypoint. */
    fun purgeOrphans(): Int {
        var removed = 0
        for (level in server.allLevels) {
            val strays =
                level.allEntities.filterIsInstance<ArmorStand>().filter {
                    looksLikeMarker(it) && byEntityId[it.uuid]?.stand !== it
                }
            for (stand in strays) {
                LOGGER.info(
                    "Purging orphaned marker entity '{}' at ({}, {}, {}) in {}",
                    stand.customName?.string,
                    stand.blockX,
                    stand.blockY,
                    stand.blockZ,
                    Dimensions.idOf(level),
                )
                stand.discard()
                removed++
            }
        }
        return removed
    }

    private fun bind(binding: Binding) {
        byRef[binding.ref] = binding
        byEntityId[binding.stand.uuid] = binding
    }

    private fun unbind(binding: Binding): Binding {
        byRef.remove(binding.ref)
        byEntityId.remove(binding.stand.uuid)
        return binding
    }

    private fun discard(stand: ArmorStand) {
        if (!stand.isRemoved) stand.discard()
    }

    private fun createStand(level: ServerLevel, waypoint: Waypoint): ArmorStand =
        ArmorStand(EntityTypes.ARMOR_STAND, level).apply {
            setPos(waypoint.x, waypoint.y, waypoint.z)
            customName = Component.literal(NAME_PREFIX + waypoint.key)
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
            // Same color `/waypoint modify <entity> color <name>` would set. Setting it directly
            // also works for stands in unloaded chunks, which that command can't select.
            waypointIcon().color = Optional.ofNullable(TeamColor.byName(waypoint.color.vanillaName)?.rgb())
            markTicked(this)
        }

    /**
     * LivingEntity.makeWaypointConnectionWith refuses to connect until the entity has ticked once,
     * and entities in unloaded chunks never tick, so a distant marker never reached the locator
     * bar. Clearing the flag lets vanilla connect it as soon as it spawns. Must run after the other
     * setup: refreshDimensions() may reposition an entity that is past its first tick.
     */
    private fun markTicked(stand: ArmorStand) {
        try {
            FIRST_TICK?.setBoolean(stand, false)
        } catch (e: ReflectiveOperationException) {
            LOGGER.warn("Could not mark marker entity as ticked: {}", e.message)
        }
    }

    companion object {
        /** Custom-name prefix that identifies marker entities; the key follows it. */
        const val NAME_PREFIX = "hm:"
        private const val TRANSMIT_RANGE = 9999.0
        private const val RESPAWN_CHECK_TICKS = 10

        private val FIRST_TICK: Field? =
            try {
                Entity::class.java.getDeclaredField("firstTick").apply { isAccessible = true }
            } catch (e: ReflectiveOperationException) {
                LOGGER.warn("Entity.firstTick not found; distant markers won't show: {}", e.message)
                null
            }

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
