package com.daolan.headingmarker.storage

import com.daolan.headingmarker.HeadingMarkerMod
import com.daolan.headingmarker.model.MarkerKeys
import com.daolan.headingmarker.model.Waypoint
import com.daolan.headingmarker.model.WaypointColor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object WaypointStorage {

    /** Current storage format version. Bump when the schema changes. */
    private const val FORMAT_VERSION = 3

    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

    private data class ImportResult(
        val dimensions: MutableMap<String, MutableMap<String, Waypoint>>,
        val migrated: Boolean,
    )

    // --- Storage DTOs (only these touch JSON) ---

    /** Top-level envelope written to each player file. */
    private data class PlayerFile(
        val formatVersion: Int = FORMAT_VERSION,
        val dimensions: Map<String, Map<String, StoredWaypoint>> = emptyMap(),
    )

    /** The only fields that get persisted per waypoint. */
    private data class StoredWaypoint(
        val markerKey: String = "",
        val color: String = "",
        val dimension: String = "",
        val x: Double = 0.0,
        val y: Double = 0.0,
        val z: Double = 0.0,
        val name: String = "",
    )

    // --- Conversion helpers ---

    private fun Waypoint.toStored() = StoredWaypoint(key, color.id, dimension, x, y, z, name)

    private fun StoredWaypoint.toRuntime(markerKey: String): Waypoint =
        Waypoint(markerKey, WaypointColor.fromStored(color), dimension, x, y, z, name)

    /** Normalize color keys during import (e.g. "light_purple" -> "purple"). */
    private fun migrateColorKey(key: String): String =
        when (key) {
            WaypointColor.LEGACY_PURPLE -> WaypointColor.PURPLE.id
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
        playerWaypoints: Map<UUID, Map<String, Map<String, Waypoint>>>,
    ) {
        for ((playerUuid, dimensionWaypoints) in playerWaypoints) {
            savePlayer(storageDir, playerUuid, dimensionWaypoints)
        }
    }

    @JvmStatic
    fun loadWaypoints(
        storageDir: Path
    ): MutableMap<UUID, MutableMap<String, MutableMap<String, Waypoint>>> {
        val result = HashMap<UUID, MutableMap<String, MutableMap<String, Waypoint>>>()

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

                        val importResult =
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

                        if (importResult != null && importResult.dimensions.isNotEmpty()) {
                            result[playerUuid] = importResult.dimensions
                            if (importResult.migrated) {
                                savePlayer(storageDir, playerUuid, importResult.dimensions)
                                HeadingMarkerMod.LOGGER.info(
                                    "Migrated waypoint file {} to marker-key format v{}",
                                    fileName,
                                    FORMAT_VERSION,
                                )
                            }
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
    ): ImportResult? {
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
    ): ImportResult {
        val envelope: PlayerFile =
            try {
                GSON.fromJson(root, PlayerFile::class.java)
            } catch (e: JsonSyntaxException) {
                HeadingMarkerMod.LOGGER.warn(
                    "Failed to parse versioned file for {}: {}",
                    playerUuid,
                    e.message,
                )
                return ImportResult(HashMap(), false)
            }

        val result = HashMap<String, MutableMap<String, Waypoint>>()
        var migrated = envelope.formatVersion < FORMAT_VERSION
        for ((dimension, markerMap) in envelope.dimensions) {
            val rebuilt = HashMap<String, Waypoint>()
            for ((mapKey, stored) in markerMap) {
                val normalizedStored = migrateStored(stored.copy(dimension = dimension))
                val candidate =
                    (normalizedStored.markerKey.ifBlank { mapKey }).trim().ifEmpty { mapKey.trim() }
                val normalizedKey = ensureUniqueMarkerKey(candidate, rebuilt)
                if (normalizedKey != candidate || normalizedStored != stored.copy(dimension = dimension)) {
                    migrated = true
                }
                rebuilt[normalizedKey] = normalizedStored.toRuntime(normalizedKey)
            }
            result[dimension] = rebuilt
        }
        return ImportResult(result, migrated)
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
    ): ImportResult {
        HeadingMarkerMod.LOGGER.info("Importing legacy waypoint file for player {}", playerUuid)
        val result = HashMap<String, MutableMap<String, Waypoint>>()

        for ((dimension, dimElement) in root.entrySet()) {
            if (!dimElement.isJsonObject) continue
            val rebuilt = HashMap<String, Waypoint>()

            for ((colorKey, wpElement) in dimElement.asJsonObject.entrySet()) {
                if (!wpElement.isJsonObject) continue
                val obj = wpElement.asJsonObject

                val stored =
                    StoredWaypoint(
                        markerKey = "",
                        color = obj.get("color")?.asString ?: colorKey,
                        dimension = dimension,
                        x = obj.get("x")?.asDouble ?: 0.0,
                        y = obj.get("y")?.asDouble ?: 0.0,
                        z = obj.get("z")?.asDouble ?: 0.0,
                        name = obj.get("name")?.asString ?: "",
                    )
                val migrated = migrateStored(stored)
                val markerKey = ensureUniqueMarkerKey("", rebuilt)
                rebuilt[markerKey] = migrated.toRuntime(markerKey)
            }

            if (rebuilt.isNotEmpty()) {
                result[dimension] = rebuilt
            }
        }
        return ImportResult(result, true)
    }

    private fun ensureUniqueMarkerKey(candidate: String, existing: Map<String, Waypoint>): String {
        val normalized = candidate.trim()
        return if (MarkerKeys.isUsable(normalized) && normalized !in existing) normalized
        else MarkerKeys.generate { it in existing }
    }

    @JvmStatic
    fun savePlayer(
        storageDir: Path,
        playerUuid: UUID,
        dimensionWaypoints: Map<String, Map<String, Waypoint>>,
    ) {
        val playerFile = storageDir.resolve("$playerUuid.json")
        try {
            val storedDimensions =
                dimensionWaypoints.mapValues { (_, markerMap) ->
                    markerMap.mapValues { (_, data) -> data.toStored() }
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
