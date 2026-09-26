package com.daolan.headingmarker.storage

import com.daolan.headingmarker.HeadingMarkerMod.Companion.LOGGER
import com.daolan.headingmarker.model.MarkerKeys
import com.daolan.headingmarker.model.Waypoint
import com.daolan.headingmarker.model.WaypointColor
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.UUID

/**
 * Reads and writes one `<player-uuid>.json` file per player.
 *
 * Current format (v3):
 * ```
 * { "formatVersion": 3,
 *   "dimensions": { "overworld": { "<key>": { "markerKey": "<key>", "color": "red",
 *                                              "dimension": "overworld", "x": 1.5, "y": 64.0,
 *                                              "z": -3.0, "name": "Home" } } } }
 * ```
 *
 * Older files are still read: v2 (same envelope, keyed by color) and the unversioned legacy
 * format, whose root maps dimension → color → waypoint. Anything that needed fixing on the way in
 * (old version, color alias, unusable key) is written back in the current format right away.
 *
 * Reading is field-by-field and tolerant: a malformed waypoint is skipped rather than losing the
 * whole file, and a file that isn't valid JSON is moved aside instead of being overwritten later.
 */
object WaypointStorage {

    /** Current storage format version. Bump when the schema changes. */
    private const val FORMAT_VERSION = 3
    private const val EXTENSION = ".json"

    private val GSON = GsonBuilder().setPrettyPrinting().create()

    private class Parsed(
        val dimensions: Map<String, Map<String, Waypoint>>,
        val needsRewrite: Boolean,
    )

    // --- Write-side DTOs (the only types Gson reflects over) ---

    private data class PlayerFile(
        val formatVersion: Int,
        val dimensions: Map<String, Map<String, StoredWaypoint>>,
    )

    private data class StoredWaypoint(
        val markerKey: String,
        val color: String,
        val dimension: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val name: String,
    )

    // --- Public API ---

    @JvmStatic
    fun loadWaypoints(storageDir: Path): Map<UUID, Map<String, Map<String, Waypoint>>> {
        val result = HashMap<UUID, Map<String, Map<String, Waypoint>>>()
        val files =
            try {
                Files.list(storageDir).use { paths ->
                    paths.filter { it.fileName.toString().endsWith(EXTENSION) }.toList()
                }
            } catch (e: IOException) {
                LOGGER.error("Failed to list waypoint files in {}", storageDir, e)
                return result
            }

        for (file in files.sorted()) {
            val fileName = file.fileName.toString()
            val owner =
                try {
                    UUID.fromString(fileName.removeSuffix(EXTENSION))
                } catch (_: IllegalArgumentException) {
                    LOGGER.warn("Invalid UUID in file name: {}", fileName)
                    continue
                }
            val parsed = readFile(file, owner) ?: continue
            if (parsed.dimensions.isEmpty()) continue
            result[owner] = parsed.dimensions
            if (parsed.needsRewrite) {
                savePlayer(storageDir, owner, parsed.dimensions)
                LOGGER.info("Migrated waypoint file {} to format v{}", fileName, FORMAT_VERSION)
            }
        }
        return result
    }

    @JvmStatic
    fun saveWaypoints(storageDir: Path, waypoints: Map<UUID, Map<String, Map<String, Waypoint>>>) {
        for ((owner, dimensions) in waypoints) savePlayer(storageDir, owner, dimensions)
    }

    /**
     * Writes [owner]'s file atomically: a crash mid-write leaves the previous file intact. Returns
     * false (after logging) if it couldn't; one player's failure never stops the others.
     */
    @JvmStatic
    fun savePlayer(
        storageDir: Path,
        owner: UUID,
        dimensions: Map<String, Map<String, Waypoint>>,
    ): Boolean {
        val target = storageDir.resolve("$owner$EXTENSION")
        val temp = storageDir.resolve("$owner$EXTENSION.tmp")
        return try {
            val envelope =
                PlayerFile(
                    FORMAT_VERSION,
                    dimensions.mapValues { (_, waypoints) ->
                        waypoints.mapValues { (_, waypoint) -> waypoint.toStored() }
                    },
                )
            Files.newBufferedWriter(temp, UTF_8).use { GSON.toJson(envelope, it) }
            moveReplacing(temp, target)
            true
        } catch (e: Exception) {
            LOGGER.error("Failed to save waypoints for player {}", owner, e)
            runCatching { Files.deleteIfExists(temp) }
            false
        }
    }

    // --- Reading ---

    private fun readFile(file: Path, owner: UUID): Parsed? {
        val root: JsonElement =
            try {
                Files.newBufferedReader(file, UTF_8).use(JsonParser::parseReader)
            } catch (e: JsonParseException) {
                val backup = setAside(file)
                LOGGER.warn(
                    "Corrupt waypoint file for player {} ({}); moved it to {}",
                    owner,
                    e.message,
                    backup?.fileName ?: "<could not move>",
                )
                return null
            } catch (e: IOException) {
                LOGGER.error("Failed to read waypoint file {}", file, e)
                return null
            }
        if (!root.isJsonObject) return null
        return parse(root.asJsonObject, owner)
    }

    private fun parse(root: JsonObject, owner: UUID): Parsed {
        val versioned = root.has("formatVersion") && root.get("dimensions")?.isJsonObject == true
        val version = if (versioned) root.intOrNull("formatVersion") ?: 0 else 0
        val dimensionsJson = if (versioned) root.getAsJsonObject("dimensions") else root
        if (!versioned) LOGGER.info("Importing legacy waypoint file for player {}", owner)

        var needsRewrite = version < FORMAT_VERSION
        val dimensions = HashMap<String, Map<String, Waypoint>>()
        for ((dimension, dimensionJson) in dimensionsJson.entrySet()) {
            if (!dimensionJson.isJsonObject) continue
            val bucket = HashMap<String, Waypoint>()
            for ((mapKey, waypointJson) in dimensionJson.asJsonObject.entrySet()) {
                if (!waypointJson.isJsonObject) {
                    LOGGER.warn("Skipping malformed waypoint '{}' for player {}", mapKey, owner)
                    needsRewrite = true
                    continue
                }
                val json = waypointJson.asJsonObject

                // Legacy files are keyed by color, and so were v2 files, which is why a
                // color word as the key is replaced below.
                val storedColor = json.stringOrNull("color") ?: if (versioned) null else mapKey
                val storedKey =
                    if (versioned) json.stringOrNull("markerKey")?.trim()?.ifEmpty { null } ?: mapKey.trim()
                    else ""
                val key =
                    if (MarkerKeys.isUsable(storedKey) && storedKey !in bucket) storedKey
                    else MarkerKeys.generate { it in bucket }
                if (key != storedKey || storedColor == WaypointColor.LEGACY_PURPLE) needsRewrite = true

                bucket[key] =
                    Waypoint(
                        key = key,
                        color = WaypointColor.fromStored(storedColor),
                        dimension = dimension,
                        x = json.doubleOrNull("x") ?: 0.0,
                        y = json.doubleOrNull("y") ?: 0.0,
                        z = json.doubleOrNull("z") ?: 0.0,
                        name = json.stringOrNull("name")?.trim() ?: "",
                    )
            }
            if (bucket.isNotEmpty()) dimensions[dimension] = bucket
        }
        return Parsed(dimensions, needsRewrite)
    }

    private fun JsonObject.primitiveOrNull(member: String) =
        get(member)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive

    private fun JsonObject.stringOrNull(member: String): String? =
        primitiveOrNull(member)?.takeIf { it.isString }?.asString

    private fun JsonObject.doubleOrNull(member: String): Double? =
        primitiveOrNull(member)?.takeIf { it.isNumber }?.asDouble?.takeIf { it.isFinite() }

    private fun JsonObject.intOrNull(member: String): Int? =
        primitiveOrNull(member)?.takeIf { it.isNumber }?.asInt

    // --- Files ---

    private fun Waypoint.toStored() = StoredWaypoint(key, color.id, dimension, x, y, z, name)

    private fun moveReplacing(from: Path, to: Path) {
        try {
            Files.move(from, to, ATOMIC_MOVE, REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(from, to, REPLACE_EXISTING)
        }
    }

    /** Renames an unreadable file so the next save doesn't overwrite what's left of it. */
    private fun setAside(file: Path): Path? {
        val backup = file.resolveSibling("${file.fileName}.corrupt-${System.currentTimeMillis()}")
        return try {
            Files.move(file, backup)
            backup
        } catch (e: IOException) {
            LOGGER.error("Could not move corrupt waypoint file {} aside", file, e)
            null
        }
    }
}
