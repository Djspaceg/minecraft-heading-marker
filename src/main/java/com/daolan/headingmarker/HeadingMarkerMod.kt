package com.daolan.headingmarker

import com.daolan.headingmarker.storage.WaypointStorage
import com.daolan.headingmarker.waypoint.TrackedWaypoint
import com.daolan.headingmarker.waypoint.Waypoint
import java.io.IOException
import java.nio.file.Files
import java.util.*
import kotlin.random.Random
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.slf4j.LoggerFactory

class HeadingMarkerMod : ModInitializer {

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register)

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            LOGGER.info("Loading waypoints from storage...")
            val storageDir = FabricLoader.getInstance().gameDir.resolve("waypoints")
            try {
                Files.createDirectories(storageDir)
                val loadedData = WaypointStorage.loadWaypoints(storageDir)
                playerWaypoints.putAll(loadedData)
                LOGGER.info("Loaded data for {} players.", loadedData.size)
            } catch (e: IOException) {
                LOGGER.error("Failed to create waypoint storage directory.", e)
            }
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { _ ->
            LOGGER.info("Saving all waypoints to storage...")
            val storageDir = FabricLoader.getInstance().gameDir.resolve("waypoints")
            WaypointStorage.saveWaypoints(storageDir, playerWaypoints)
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            recreateWaypointEntities(handler.player)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            removeAllPlayerWaypointEntities(handler.player)
            lastDistanceText.remove(handler.player.uuid)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            tickCounter++
            if (tickCounter >= DISTANCE_UPDATE_INTERVAL) {
                tickCounter = 0
                for (player in server.playerList.players) {
                    displayDistances(player)
                }
            }
        }

        LOGGER.info("Heading Marker Mod initialized.")
    }

    enum class WaypointColor(
        @JvmField val colorName: String,
        @JvmField val colorInt: Int,
        @JvmField val emoji: String,
        @JvmField val formatting: ChatFormatting,
    ) {
        RED("red", 0xFF0000, "🔴", ChatFormatting.RED),
        BLUE("blue", 0x5555FF, "🔵", ChatFormatting.BLUE),
        GREEN("green", 0x55FF55, "🟢", ChatFormatting.GREEN),
        YELLOW("yellow", 0xFFFF55, "🟡", ChatFormatting.YELLOW),
        PURPLE("purple", 0xFF55FF, "🟣", ChatFormatting.LIGHT_PURPLE),
        WHITE("white", 0xFFFFFF, "⚪", ChatFormatting.WHITE);

        /** The Minecraft color name used in vanilla commands (e.g. "light_purple" for PURPLE). */
        val mcColorName: String
            get() = formatting.name.lowercase()

        companion object {
            private val BY_NAME: MutableMap<String, WaypointColor> =
                buildMap {
                        for (c in WaypointColor.entries) {
                            put(c.colorName, c)
                        }
                        // Backward compatibility: accept "light_purple" as alias for "purple"
                        put("light_purple", PURPLE)
                    }
                    .toMutableMap()

            @JvmStatic
            fun fromString(name: String): WaypointColor =
                BY_NAME.getOrDefault(name.lowercase(), WHITE)
        }
    }

    data class WaypointData(
        @JvmField val markerKey: String,
        @JvmField val color: String,
        @JvmField val dimension: String,
        @JvmField val x: Double,
        @JvmField val y: Double,
        @JvmField val z: Double,
        @JvmField val name: String = "",
        @Transient @JvmField val trackedWaypoint: TrackedWaypoint? = null,
        @Transient @JvmField val entityId: Int = -1,
    )

    companion object {
        const val MOD_ID = "headingmarker"
        @JvmField val LOGGER = LoggerFactory.getLogger(MOD_ID)!!

        const val DIM_OVERWORLD = "overworld"
        const val DIM_NETHER = "the_nether"
        const val DIM_END = "the_end"

        private val playerWaypoints =
            HashMap<UUID, MutableMap<String, MutableMap<String, WaypointData>>>()
        private val lastDistanceText = HashMap<UUID, String>()
        private const val DISTANCE_UPDATE_INTERVAL = 5
        private const val MARKER_KEY_LENGTH = 8
        private const val MARKER_KEY_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"
        private val MARKER_KEY_REGEX = Regex("^[a-z0-9]{1,8}$")
        private var tickCounter = 0

        @JvmStatic
        fun getDimensionKey(worldKey: ResourceKey<Level>): String = worldKey.identifier().path

        @JvmStatic
        fun createWaypoint(
            player: ServerPlayer,
            colorName: String,
            x: Double,
            y: Double,
            z: Double,
            name: String = "",
        ): String {
            val playerUuid = player.uuid
            val color = WaypointColor.fromString(colorName)
            val world = player.level()
            val dimension = getDimensionKey(world.dimension())
            val markerKey =
                generateMarkerKey(
                    playerWaypoints.getOrPut(playerUuid) { HashMap() }.getOrPut(dimension) {
                        HashMap()
                    }
                )

            LOGGER.info(
                "Creating waypoint: key={}, color={}, dimension={}, pos=({},{},{}), player={}",
                markerKey,
                color.colorName,
                dimension,
                x,
                y,
                z,
                player.name.string,
            )

            val armorStand = spawnAndConfigureWaypointEntity(world, color, x, y, z)
            if (armorStand == null) {
                LOGGER.error("Aborting waypoint creation due to entity spawn failure.")
                return ""
            }
            armorStand.customName = Component.literal("hm:$markerKey")

            setWaypointColorWithCommand(world, armorStand, color)
            setWaypointViewersWithCommand(world, armorStand, player.gameProfile.name)

            val pos = Vec3i(x.toInt(), y.toInt(), z.toInt())
            val config = Waypoint.Config().apply { this.color = Optional.of(color.colorInt) }
            val trackedWaypoint = TrackedWaypoint.ofPos(playerUuid, config, pos)

            val data =
                WaypointData(
                    markerKey,
                    color.colorName,
                    dimension,
                    x,
                    y,
                    z,
                    name,
                    trackedWaypoint,
                    armorStand.id,
                )
            playerWaypoints
                .getOrPut(playerUuid) { HashMap() }
                .getOrPut(dimension) { HashMap() }[markerKey] = data

            LOGGER.info(
                "Successfully created waypoint {} ({}) in {} at ({},{},{})",
                markerKey,
                color.colorName,
                dimension,
                x,
                y,
                z,
            )
            return markerKey
        }

        private fun createWaypointInWorld(
            world: ServerLevel,
            playerUuid: UUID,
            markerKey: String,
            colorName: String,
            x: Double,
            y: Double,
            z: Double,
            name: String = "",
        ) {
            val color = WaypointColor.fromString(colorName)
            val dimension = getDimensionKey(world.dimension())

            val armorStand = spawnAndConfigureWaypointEntity(world, color, x, y, z)
            if (armorStand == null) {
                LOGGER.error(
                    "Aborting waypoint creation due to entity spawn failure in {}",
                    dimension,
                )
                return
            }
            armorStand.customName = Component.literal("hm:$markerKey")

            setWaypointColorWithCommand(world, armorStand, color)

            world.server.playerList.getPlayer(playerUuid)?.let { owner ->
                setWaypointViewersWithCommand(world, armorStand, owner.gameProfile.name)
            }

            val pos = Vec3i(x.toInt(), y.toInt(), z.toInt())
            val config = Waypoint.Config().apply { this.color = Optional.of(color.colorInt) }
            val trackedWaypoint = TrackedWaypoint.ofPos(playerUuid, config, pos)

            val data =
                WaypointData(
                    markerKey,
                    color.colorName,
                    dimension,
                    x,
                    y,
                    z,
                    name,
                    trackedWaypoint,
                    armorStand.id,
                )
            playerWaypoints
                .getOrPut(playerUuid) { HashMap() }
                .getOrPut(dimension) { HashMap() }[markerKey] = data
        }

        private fun spawnAndConfigureWaypointEntity(
            world: ServerLevel,
            color: WaypointColor,
            x: Double,
            y: Double,
            z: Double,
        ): ArmorStand? {
            val armorStand =
                ArmorStand(EntityTypes.ARMOR_STAND, world).apply {
                    setPos(x, y, z)
                    isInvisible = true
                    isInvulnerable = true
                    setNoGravity(true)
                    isSilent = true
                    customName = Component.literal("${color.colorName} waypoint")
                }

            // Set marker mode (no hitbox) and remove base plate.
            // setMarker()/setNoBasePlate() are private in MC 26.1, so we set the
            // client flags byte directly via the synced entity data accessor.
            try {
                val flagsField = ArmorStand::class.java.getDeclaredField("DATA_CLIENT_FLAGS")
                flagsField.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val dataKey =
                    flagsField.get(null) as net.minecraft.network.syncher.EntityDataAccessor<Byte>
                // Bit flags: 0x01 = small, 0x04 = arms, 0x08 = no base plate, 0x10 = marker
                val flags: Byte = (0x08 or 0x10).toByte() // NoBasePlate + Marker
                armorStand.entityData.set(dataKey, flags)
            } catch (e: Exception) {
                LOGGER.warn(
                    "Could not set marker/no-base-plate flags via reflection: {}",
                    e.message,
                )
            }

            try {
                val waypointAttr = armorStand.getAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE)
                if (waypointAttr != null) {
                    waypointAttr.baseValue = 9999.0
                } else {
                    LOGGER.warn(
                        "Could not set waypoint transmit range: attribute not found for ArmorStand."
                    )
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to set waypoint attribute", e)
            }

            return if (world.addFreshEntity(armorStand)) armorStand
            else {
                LOGGER.error("Failed to spawn waypoint entity for color {}", color.colorName)
                null
            }
        }

        private fun setWaypointColorWithCommand(
            world: ServerLevel,
            armorStand: ArmorStand,
            color: WaypointColor,
        ) {
            val server = world.server
            try {
                val command = "waypoint modify ${armorStand.stringUUID} color ${color.mcColorName}"
                val commandSource = server.createCommandSourceStack().withSuppressedOutput()
                val dispatcher = server.commands.dispatcher
                val parseResults = dispatcher.parse(command, commandSource)
                server.commands.performCommand(parseResults, command)
                LOGGER.info(
                    "Set waypoint color to {} for entity {}",
                    color.colorName,
                    armorStand.stringUUID,
                )
            } catch (e: Exception) {
                LOGGER.error(
                    "Failed to execute waypoint color command for entity {}: {}",
                    armorStand.uuid,
                    e.message,
                )
            }
        }

        private fun setWaypointViewersWithCommand(
            world: ServerLevel,
            armorStand: ArmorStand,
            playerName: String,
        ) {
            val server = world.server
            try {
                val command =
                    "waypoint modify ${armorStand.stringUUID} viewers @a[name=$playerName]"
                val commandSource = server.createCommandSourceStack().withSuppressedOutput()
                val dispatcher = server.commands.dispatcher
                val parseResults = dispatcher.parse(command, commandSource)
                server.commands.performCommand(parseResults, command)
                LOGGER.info(
                    "Restricted waypoint {} visibility to player {}",
                    armorStand.stringUUID,
                    playerName,
                )
            } catch (e: Exception) {
                LOGGER.warn(
                    "Could not restrict waypoint viewers for entity {} (feature may not be available): {}",
                    armorStand.uuid,
                    e.message,
                )
            }
        }

        @JvmStatic
        fun removeWaypoint(player: ServerPlayer, selector: String): Int {
            val playerUuid = player.uuid
            val dimension = getDimensionKey(player.level().dimension())

            val waypoints = playerWaypoints[playerUuid]?.get(dimension) ?: return 0
            val matched = findWaypointMatches(waypoints, selector)
            if (matched.isEmpty()) return 0

            for ((markerKey, _) in matched) {
                removeWaypointEntity(player.level(), playerUuid, markerKey, dimension)
                waypoints.remove(markerKey)
            }
            LOGGER.info(
                "Removed {} waypoint(s): selector='{}', dimension={}",
                matched.size,
                selector,
                dimension,
            )
            return matched.size
        }

        /**
         * Renames all waypoints matching [selector] for [player] in their current dimension.
         * Returns the number of updated waypoints.
         */
        @JvmStatic
        fun renameWaypoint(player: ServerPlayer, selector: String, newName: String): Int {
            val playerUuid = player.uuid
            val dimension = getDimensionKey(player.level().dimension())

            val waypoints = playerWaypoints[playerUuid]?.get(dimension) ?: return 0
            val matched = findWaypointMatches(waypoints, selector)
            if (matched.isEmpty()) return 0
            val trimmed = newName.trim()
            for ((markerKey, existing) in matched) {
                waypoints[markerKey] = existing.copy(name = trimmed)
            }
            LOGGER.info(
                "Renamed {} waypoint(s) for player {} in {} using selector '{}' to '{}'",
                matched.size,
                player.name.string,
                dimension,
                selector,
                trimmed,
            )
            return matched.size
        }

        private fun removeWaypointEntity(
            world: ServerLevel,
            playerUuid: UUID,
            markerKey: String,
            dimension: String,
        ) {
            val data = playerWaypoints[playerUuid]?.get(dimension)?.get(markerKey) ?: return
            if (data.entityId != -1) {
                val entity = world.getEntity(data.entityId)
                if (entity is ArmorStand) {
                    entity.discard()
                    LOGGER.info(
                        "Removed waypoint entity for key {} (color {}) in {}",
                        markerKey,
                        data.color,
                        dimension,
                    )
                }
            }
        }

        private fun findWaypointMatches(
            waypoints: Map<String, WaypointData>,
            selector: String,
        ): Map<String, WaypointData> {
            val trimmed = selector.trim()
            if (trimmed.isEmpty()) return emptyMap()

            val byKey = waypoints[trimmed]
            if (byKey != null) return mapOf(trimmed to byKey)

            val lower = trimmed.lowercase()
            return waypoints.filterValues { data ->
                data.color.equals(lower, ignoreCase = true) || data.name.equals(trimmed, ignoreCase = true)
            }
        }

        private fun removeAllPlayerWaypointEntities(player: ServerPlayer) {
            val playerUuid = player.uuid
            val dimensionWaypoints = playerWaypoints[playerUuid] ?: return
            if (dimensionWaypoints.isEmpty()) return

            var removedCount = 0
            val server = player.level().server

            for ((dimension, waypoints) in dimensionWaypoints) {
                val world = getWorldForDimension(server, dimension) ?: continue
                for (data in waypoints.values) {
                    if (data.entityId != -1) {
                        val entity = world.getEntity(data.entityId)
                        if (entity is ArmorStand) {
                            entity.discard()
                            removedCount++
                        }
                    }
                }
            }

            if (removedCount > 0) {
                LOGGER.info(
                    "Cleaned up {} waypoint entities for disconnecting player {}",
                    removedCount,
                    player.name.string,
                )
            }
        }

        private fun getWorldForDimension(
            server: MinecraftServer?,
            dimension: String,
        ): ServerLevel? {
            if (server == null) return null
            return when (dimension) {
                DIM_OVERWORLD -> server.getLevel(Level.OVERWORLD)
                DIM_NETHER -> server.getLevel(Level.NETHER)
                DIM_END -> server.getLevel(Level.END)
                else -> null
            }
        }

        @JvmStatic
        fun getWaypoints(playerUuid: UUID, dimension: String): Map<String, WaypointData> =
            playerWaypoints[playerUuid]?.get(dimension) ?: emptyMap()

        @JvmStatic
        fun getAllWaypoints(playerUuid: UUID): Map<String, Map<String, WaypointData>> =
            playerWaypoints.getOrDefault(playerUuid, emptyMap())

        @JvmStatic
        fun clearWaypointsInDimension(player: ServerPlayer): Int {
            val playerUuid = player.uuid
            val world = player.level()
            val dimension = getDimensionKey(world.dimension())

            val waypoints = playerWaypoints[playerUuid]?.get(dimension)
            if (waypoints.isNullOrEmpty()) return 0

            val count = waypoints.size
            for (data in waypoints.values) {
                if (data.entityId != -1) {
                    (world.getEntity(data.entityId) as? ArmorStand)?.discard()
                }
            }
            waypoints.clear()

            LOGGER.info(
                "Cleared {} waypoints in {} for player {}",
                count,
                dimension,
                player.name.string,
            )
            return count
        }

        @JvmStatic
        fun clearAllWaypoints(player: ServerPlayer): Int {
            val playerUuid = player.uuid
            val dimensionWaypoints = playerWaypoints[playerUuid]
            if (dimensionWaypoints.isNullOrEmpty()) return 0

            var count = 0
            val server = player.level().server

            for ((dimension, waypoints) in dimensionWaypoints) {
                val world = getWorldForDimension(server, dimension)
                if (world == null) {
                    count += waypoints.size
                    waypoints.clear()
                    continue
                }
                for (data in waypoints.values) {
                    if (data.entityId != -1) {
                        (world.getEntity(data.entityId) as? ArmorStand)?.discard()
                    }
                }
                count += waypoints.size
                waypoints.clear()
            }

            LOGGER.info(
                "Cleared all {} waypoints across all dimensions for player {}",
                count,
                player.name.string,
            )
            return count
        }

        @JvmStatic
        fun shareWaypoint(
            fromPlayer: ServerPlayer,
            toPlayer: ServerPlayer,
            selector: String,
        ): Int {
            val fromUuid = fromPlayer.uuid
            val dimension = getDimensionKey(fromPlayer.level().dimension())

            val sourceData = findWaypointMatches(getWaypoints(fromUuid, dimension), selector)
            if (sourceData.isEmpty()) return 0
            val world = getWorldForDimension(fromPlayer.level().server, dimension) ?: return 0
            var shared = 0

            for ((_, data) in sourceData) {
                val targetKey =
                    generateMarkerKey(
                        playerWaypoints
                            .getOrPut(toPlayer.uuid) { HashMap() }
                            .getOrPut(dimension) { HashMap() }
                    )
                createWaypointInWorld(
                    world,
                    toPlayer.uuid,
                    targetKey,
                    data.color,
                    data.x,
                    data.y,
                    data.z,
                    data.name,
                )
                shared++
            }

            LOGGER.info(
                "Shared {} waypoint(s) from {} to {} in {} using selector '{}'",
                shared,
                fromPlayer.name.string,
                toPlayer.name.string,
                dimension,
                selector,
            )
            return shared
        }

        @JvmStatic
        fun recreateWaypointEntities(player: ServerPlayer) {
            val playerUuid = player.uuid
            val dimensionWaypoints = playerWaypoints[playerUuid]
            if (dimensionWaypoints.isNullOrEmpty()) return

            var recreatedCount = 0
            val server = player.level().server

            for ((dimension, waypoints) in dimensionWaypoints) {
                val world = getWorldForDimension(server, dimension)
                if (world == null) {
                    LOGGER.warn(
                        "Could not get world for dimension {} to recreate waypoints",
                        dimension,
                    )
                    continue
                }

                LOGGER.info(
                    "Recreating {} waypoint entities for player {} in {}",
                    waypoints.size,
                    player.name.string,
                    dimension,
                )

                val waypointSnapshot = ArrayList(waypoints.entries)
                for ((existingKey, data) in waypointSnapshot) {
                    try {
                        val key =
                            if (
                                isValidMarkerKey(existingKey) &&
                                    existingKey !in reservedSelectorWords()
                            ) existingKey
                            else generateMarkerKey(waypoints)
                        if (key != existingKey) {
                            waypoints.remove(existingKey)
                        }
                        createWaypointInWorld(
                            world,
                            playerUuid,
                            key,
                            data.color,
                            data.x,
                            data.y,
                            data.z,
                            data.name,
                        )
                        recreatedCount++
                    } catch (e: Exception) {
                        LOGGER.error(
                            "Failed to recreate waypoint entity for key {} (color {}) in {}: {}",
                            existingKey,
                            data.color,
                            dimension,
                            e.message,
                        )
                    }
                }
            }

            if (recreatedCount > 0) {
                LOGGER.info(
                    "Successfully recreated {} waypoint entities across all dimensions for player {}",
                    recreatedCount,
                    player.name.string,
                )
            }
        }

        private fun displayDistances(player: ServerPlayer) {
            val playerUuid = player.uuid
            val dimension = getDimensionKey(player.level().dimension())
            val waypoints = getWaypoints(playerUuid, dimension)

            val fullText: MutableComponent =
                if (waypoints.isNotEmpty()) {
                    val playerPos = Vec3(player.x, player.y, player.z)
                    waypoints.entries
                        .sortedBy { it.key }
                        .map { (_, data) ->
                            val color = WaypointColor.fromString(data.color)
                            val distance =
                                playerPos.distanceTo(Vec3(data.x, data.y, data.z)).toInt()
                            val label =
                                if (data.name.isNotBlank()) data.name.trim().take(12)
                                else color.colorName.replaceFirstChar { it.uppercaseChar() }
                            Component.literal("${color.emoji} $label ")
                                .append(Component.literal("$distance").withStyle(color.formatting))
                        }
                        .reduce { text1, text2 -> text1.append("  ").append(text2) }
                } else {
                    Component.empty()
                }

            val newDistanceText = fullText.string
            val oldDistanceText = lastDistanceText[playerUuid]
            if (newDistanceText != oldDistanceText) {
                lastDistanceText[playerUuid] = newDistanceText
                player.sendSystemMessage(fullText, true)
            }
        }

        @JvmStatic
        fun purgeOrphanedWaypointEntities(server: MinecraftServer): Int {
            // Track known entities with dimension qualifier to avoid cross-dimension ID collisions
            val knownEntities = HashSet<String>()
            for (dimMap in playerWaypoints.values) {
                for ((dim, colorMap) in dimMap) {
                    for (data in colorMap.values) {
                        if (data.entityId != -1) knownEntities.add("$dim:${data.entityId}")
                    }
                }
            }

            val waypointNames = buildSet {
                for (color in WaypointColor.entries) {
                    add("${color.colorName} waypoint")
                    // Include legacy MC color name variants (e.g. "light_purple waypoint")
                    if (color.mcColorName != color.colorName) {
                        add("${color.mcColorName} waypoint")
                    }
                }
            }

            var removed = 0
            for (dimKey in arrayOf(DIM_OVERWORLD, DIM_NETHER, DIM_END)) {
                val world = getWorldForDimension(server, dimKey) ?: continue

                val orphans = mutableListOf<ArmorStand>()
                for (entity in world.allEntities) {
                    if (entity !is ArmorStand) continue
                    val customName = entity.customName?.string ?: continue
                    if (!customName.startsWith("hm:") && customName !in waypointNames) continue
                    if ("$dimKey:${entity.id}" in knownEntities) continue
                    orphans.add(entity)
                }

                for (stand in orphans) {
                    LOGGER.info(
                        "Purging orphaned waypoint entity '{}' (id={}) at ({},{},{}) in {}",
                        stand.customName?.string,
                        stand.id,
                        stand.x.toInt(),
                        stand.y.toInt(),
                        stand.z.toInt(),
                        dimKey,
                    )
                    stand.discard()
                    removed++
                }
            }

            LOGGER.info("Purged {} orphaned waypoint entity(ies) across all dimensions.", removed)
            return removed
        }

        private fun generateMarkerKey(existing: Map<String, WaypointData>): String {
            repeat(1024) {
                val candidate =
                    buildString(MARKER_KEY_LENGTH) {
                        repeat(MARKER_KEY_LENGTH) {
                            append(MARKER_KEY_ALPHABET[Random.nextInt(MARKER_KEY_ALPHABET.length)])
                        }
                    }
                if (!existing.containsKey(candidate) && candidate !in reservedSelectorWords()) {
                    return candidate
                }
            }
            throw IllegalStateException("Unable to generate a unique $MARKER_KEY_LENGTH-character marker key.")
        }

        private fun isValidMarkerKey(key: String): Boolean = MARKER_KEY_REGEX.matches(key)

        private fun reservedSelectorWords(): Set<String> =
            WaypointColor.entries.map { it.colorName }.toSet() + "light_purple"
    }
}
