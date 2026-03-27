package com.daolan.headingmarker.storage

import com.daolan.headingmarker.HeadingMarkerMod
import com.daolan.headingmarker.HeadingMarkerMod.WaypointData
import com.daolan.headingmarker.waypoint.TrackedWaypoint
import com.daolan.headingmarker.waypoint.Waypoint
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import net.minecraft.core.Vec3i

object WaypointStorage {

    /** Current storage format version. Bump when the schema changes. */
    private const val FORMAT_VERSION = 2

    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

    // --- Storage DTOs (only these touch JSON) ---

    /** Top-level envelope written to each player file. */
    private data class PlayerFile(
        val formatVersion: Int = FORMAT_VERSION,
        val dimensions: Map<String, Map<String, StoredWaypoint>> = emptyMap(),
    )

    /** The only fields that get persisted per waypoint. */
    private data class StoredWaypoint(
        val color: String = "",
        val dimension: String = "",
        val x: Double = 0.0,
        val y: Double = 0.0,
        val z: Double = 0.0,
    )

    // --- Conversion helpers ---

    private fun WaypointData.toStored() = StoredWaypoint(color, dimension, x, y, z)

    private fun StoredWaypoint.toRuntime(playerUuid: UUID): WaypointData {
        val resolvedColor = HeadingMarkerMod.WaypointColor.fromString(color)
        val config = Waypoint.Config().apply { this.color = Optional.of(resolvedColor.colorInt) }
        val wp = TrackedWaypoint.ofPos(playerUuid, config, Vec3i(x.toInt(), y.toInt(), z.toInt()))
        return WaypointData(color, dimension, x, y, z, wp, -1)
    }

    /** Normalize color keys during import (e.g. "light_purple" -> "purple"). */
    private fun migrateColorKey(key: String): String =
        when (key) {
            "light_purple" -> "purple"
            else -> key
        }

    private fun migrateStored(stored: StoredWaypoint): StoredWaypoint {
        val migratedColor = migrateColorKey(stored.color)
        return if (migratedColor != stored.color) stored.copy(color = migratedColor) else stored
    }

    // --- Public API ---

    @JvmStatic
    fun saveWaypoints(
        storageDir: Path,
        playerWaypoints: Map<UUID, Map<String, Map<String, WaypointData>>>,
    ) {
        for ((playerUuid, dimensionWaypoints) in playerWaypoints) {
            val playerFile = storageDir.resolve("$playerUuid.json")
            try {
                val storedDimensions =
                    dimensionWaypoints.mapValues { (_, colorMap) ->
                        colorMap.mapValues { (_, data) -> data.toStored() }
                    }
                val envelope = PlayerFile(FORMAT_VERSION, storedDimensions)
                FileWriter(playerFile.toFile()).use { writer -> GSON.toJson(envelope, writer) }
            } catch (e: IOException) {
                HeadingMarkerMod.LOGGER.error(
                    "Failed to save waypoints for player {}",
                    playerUuid,
                    e,
                )
            }
        }
    }

    @JvmStatic
    fun loadWaypoints(
        storageDir: Path
    ): MutableMap<UUID, MutableMap<String, MutableMap<String, WaypointData>>> {
        val result = HashMap<UUID, MutableMap<String, MutableMap<String, WaypointData>>>()

        try {
            Files.list(storageDir).use { files ->
                files
                    .filter { it.toString().endsWith(".json") }
                    .forEach { path ->
                        val fileName = path.fileName.toString()
                        val uuidString = fileName.removeSuffix(".json")

                        val playerUuid =
                            try {
                                UUID.fromString(uuidString)
                            } catch (_: IllegalArgumentException) {
                                HeadingMarkerMod.LOGGER.warn(
                                    "Invalid UUID in file name: {}",
                                    fileName,
                                )
                                return@forEach
                            }

                        val dimensions =
                            try {
                                FileReader(path.toFile()).use { reader ->
                                    importPlayerFile(reader, playerUuid)
                                }
                            } catch (e: Exception) {
                                HeadingMarkerMod.LOGGER.error(
                                    "Failed to load waypoints from {}: {}",
                                    path,
                                    e.message,
                                )
                                null
                            }

                        if (dimensions != null && dimensions.isNotEmpty()) {
                            result[playerUuid] = dimensions
                        }
                    }
            }
        } catch (e: IOException) {
            HeadingMarkerMod.LOGGER.error("Failed to list waypoint files in storage directory.", e)
        }

        return result
    }

    /**
     * Import a single player file, handling both the current versioned format and the legacy
     * unversioned format transparently.
     */
    private fun importPlayerFile(
        reader: FileReader,
        playerUuid: UUID,
    ): MutableMap<String, MutableMap<String, WaypointData>>? {
        // Parse as a generic JSON tree first so we can detect the format
        val jsonElement =
            try {
                com.google.gson.JsonParser.parseReader(reader)
            } catch (e: JsonSyntaxException) {
                HeadingMarkerMod.LOGGER.warn(
                    "Corrupt waypoint file for player {}, skipping: {}",
                    playerUuid,
                    e.message,
                )
                return null
            }

        if (jsonElement == null || !jsonElement.isJsonObject) return null
        val root = jsonElement.asJsonObject

        // Detect format: versioned files have "formatVersion" + "dimensions"
        return if (root.has("formatVersion") && root.has("dimensions")) {
            importVersioned(root, playerUuid)
        } else {
            // Legacy format: top-level keys are dimension names directly
            importLegacy(root, playerUuid)
        }
    }

    /** Import the current versioned format. */
    private fun importVersioned(
        root: com.google.gson.JsonObject,
        playerUuid: UUID,
    ): MutableMap<String, MutableMap<String, WaypointData>> {
        val envelope: PlayerFile =
            try {
                GSON.fromJson(root, PlayerFile::class.java)
            } catch (e: JsonSyntaxException) {
                HeadingMarkerMod.LOGGER.warn(
                    "Failed to parse versioned file for {}: {}",
                    playerUuid,
                    e.message,
                )
                return HashMap()
            }

        val result = HashMap<String, MutableMap<String, WaypointData>>()
        for ((dimension, colorMap) in envelope.dimensions) {
            val rebuilt = HashMap<String, WaypointData>()
            for ((colorKey, stored) in colorMap) {
                val migrated = migrateStored(stored.copy(dimension = dimension))
                val migratedKey = migrateColorKey(colorKey)
                rebuilt[migratedKey] = migrated.toRuntime(playerUuid)
            }
            result[dimension] = rebuilt
        }
        return result
    }

    /**
     * Import the legacy unversioned format where the JSON root is: { "overworld": { "red": {
     * "color":"red", "dimension":"overworld", "x":1.0, ... }, ... }, ... }
     *
     * Gson may encounter unknown fields (trackedWaypoint, entityId, etc.) from older versions. We
     * parse each waypoint manually from the JSON tree to extract only the fields we need, making
     * this resilient to any extra/missing fields.
     */
    private fun importLegacy(
        root: com.google.gson.JsonObject,
        playerUuid: UUID,
    ): MutableMap<String, MutableMap<String, WaypointData>> {
        HeadingMarkerMod.LOGGER.info("Importing legacy waypoint file for player {}", playerUuid)
        val result = HashMap<String, MutableMap<String, WaypointData>>()

        for ((dimension, dimElement) in root.entrySet()) {
            if (!dimElement.isJsonObject) continue
            val rebuilt = HashMap<String, WaypointData>()

            for ((colorKey, wpElement) in dimElement.asJsonObject.entrySet()) {
                if (!wpElement.isJsonObject) continue
                val obj = wpElement.asJsonObject

                val stored =
                    StoredWaypoint(
                        color = obj.get("color")?.asString ?: colorKey,
                        dimension = dimension,
                        x = obj.get("x")?.asDouble ?: 0.0,
                        y = obj.get("y")?.asDouble ?: 0.0,
                        z = obj.get("z")?.asDouble ?: 0.0,
                    )
                val migrated = migrateStored(stored)
                val migratedKey = migrateColorKey(colorKey)
                rebuilt[migratedKey] = migrated.toRuntime(playerUuid)
            }

            if (rebuilt.isNotEmpty()) {
                result[dimension] = rebuilt
            }
        }
        return result
    }
}
