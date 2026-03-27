package com.daolan.headingmarker.storage

import com.daolan.headingmarker.HeadingMarkerMod
import com.daolan.headingmarker.HeadingMarkerMod.WaypointData
import com.daolan.headingmarker.waypoint.TrackedWaypoint
import com.daolan.headingmarker.waypoint.Waypoint
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import net.minecraft.core.Vec3i

object WaypointStorage {

    private val GSON =
        GsonBuilder().registerTypeAdapter(Optional::class.java, OptionalTypeAdapter<Any>()).create()

    /** Save waypoints to storage. Structure: PlayerUUID -> Dimension -> Color -> WaypointData */
    @JvmStatic
    fun saveWaypoints(
        storageDir: Path,
        playerWaypoints: Map<UUID, Map<String, Map<String, WaypointData>>>,
    ) {
        for ((playerUuid, dimensionWaypoints) in playerWaypoints) {
            val playerFile = storageDir.resolve("$playerUuid.json")
            try {
                FileWriter(playerFile.toFile()).use { writer ->
                    GSON.toJson(dimensionWaypoints, writer)
                }
            } catch (e: IOException) {
                HeadingMarkerMod.LOGGER.error(
                    "Failed to save waypoints for player {}",
                    playerUuid,
                    e,
                )
            }
        }
    }

    /** Load waypoints from storage. Structure: PlayerUUID -> Dimension -> Color -> WaypointData */
    @JvmStatic
    fun loadWaypoints(
        storageDir: Path
    ): MutableMap<UUID, MutableMap<String, MutableMap<String, WaypointData>>> {
        val playerWaypoints = HashMap<UUID, MutableMap<String, MutableMap<String, WaypointData>>>()

        try {
            Files.list(storageDir).use { files ->
                files
                    .filter { it.toString().endsWith(".json") }
                    .forEach { path ->
                        val fileName = path.fileName.toString()
                        val uuidString = fileName.removeSuffix(".json")

                        try {
                            val playerUuid = UUID.fromString(uuidString)
                            try {
                                FileReader(path.toFile()).use { reader ->
                                    val type =
                                        object :
                                                TypeToken<
                                                    Map<String, Map<String, WaypointData>>
                                                >() {}
                                            .type
                                    var dimensionWaypoints:
                                        MutableMap<String, MutableMap<String, WaypointData>>? =
                                        GSON.fromJson(reader, type)

                                    if (dimensionWaypoints == null) {
                                        dimensionWaypoints = HashMap()
                                    }

                                    // Re-create tracked waypoints and set entity IDs to -1 as they
                                    // are not persistent.
                                    val rebuiltDimensions =
                                        HashMap<String, MutableMap<String, WaypointData>>()
                                    for ((dimension, waypoints) in dimensionWaypoints!!) {
                                        val recreatedWaypoints = HashMap<String, WaypointData>()
                                        for ((colorKey, data) in waypoints) {
                                            // Migrate old "light_purple" key to "purple"
                                            val migratedKey =
                                                if (colorKey == "light_purple") "purple"
                                                else colorKey
                                            val migratedColor =
                                                if (data.color == "light_purple") "purple"
                                                else data.color

                                            if (colorKey == "light_purple") {
                                                HeadingMarkerMod.LOGGER.info(
                                                    "Migrating waypoint key from 'light_purple' to 'purple' for player {} in {}",
                                                    playerUuid,
                                                    dimension,
                                                )
                                            }

                                            val resolvedColor =
                                                HeadingMarkerMod.WaypointColor.fromString(
                                                    migratedColor
                                                )
                                            val wp =
                                                TrackedWaypoint.ofPos(
                                                    playerUuid,
                                                    Waypoint.Config().apply {
                                                        color = Optional.of(resolvedColor.colorInt)
                                                    },
                                                    Vec3i(
                                                        data.x.toInt(),
                                                        data.y.toInt(),
                                                        data.z.toInt(),
                                                    ),
                                                )
                                            recreatedWaypoints[migratedKey] =
                                                WaypointData(
                                                    migratedColor,
                                                    dimension,
                                                    data.x,
                                                    data.y,
                                                    data.z,
                                                    wp,
                                                    -1,
                                                )
                                        }
                                        rebuiltDimensions[dimension] = recreatedWaypoints
                                    }

                                    playerWaypoints[playerUuid] = rebuiltDimensions
                                }
                            } catch (e: IOException) {
                                HeadingMarkerMod.LOGGER.error(
                                    "Failed to load waypoints from file: {}",
                                    path,
                                    e,
                                )
                            }
                        } catch (e: IllegalArgumentException) {
                            HeadingMarkerMod.LOGGER.warn("Invalid UUID in file name: {}", fileName)
                        }
                    }
            }
        } catch (e: IOException) {
            HeadingMarkerMod.LOGGER.error("Failed to list waypoint files in storage directory.", e)
        }

        return playerWaypoints
    }

    /** Custom TypeAdapter for Optional to avoid Java module reflection issues */
    private class OptionalTypeAdapter<T : Any> : TypeAdapter<Optional<T>>() {
        override fun write(out: JsonWriter, value: Optional<T>?) {
            if (value == null || value.isEmpty) {
                out.nullValue()
            } else {
                out.jsonValue(value.get().toString())
            }
        }

        override fun read(`in`: JsonReader): Optional<T> {
            if (`in`.peek() == JsonToken.NULL) {
                `in`.nextNull()
                return Optional.empty<T>()
            }
            @Suppress("UNCHECKED_CAST") val value = Integer.valueOf(`in`.nextInt()) as T
            return Optional.of<T>(value)
        }
    }
}
