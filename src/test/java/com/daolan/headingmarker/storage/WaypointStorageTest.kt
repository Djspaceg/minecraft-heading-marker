package com.daolan.headingmarker.storage

import com.daolan.headingmarker.model.Waypoint
import com.daolan.headingmarker.model.WaypointColor
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Tests for WaypointStorage covering versioned format, legacy format import, corrupt file handling,
 * migration, and round-trip integrity.
 */
class WaypointStorageTest {

    companion object {
        private val TEST_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")
        private val TEST_UUID_2 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
    }

    private fun wp(
        key: String,
        color: String,
        dimension: String,
        x: Double,
        y: Double,
        z: Double,
        name: String = "",
    ) = Waypoint(key, WaypointColor.fromStored(color), dimension, x, y, z, name)

    private fun waypointByColor(
        dimensionWaypoints: Map<String, Waypoint>,
        color: String,
    ): Waypoint =
        dimensionWaypoints.values.firstOrNull { it.color.id == color }
            ?: error("No waypoint found for color '$color'")

    // ---- Versioned format (v2) ----

    @Test
    fun `load versioned format with sub-block precision`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "formatVersion": 2,
      "dimensions": {
        "overworld": {
          "red": { "color": "red", "dimension": "overworld", "x": 100.5, "y": 64.3, "z": -200.7 },
          "blue": { "color": "blue", "dimension": "overworld", "x": 50.0, "y": 70.0, "z": 300.0 }
        },
        "the_nether": {
          "green": { "color": "green", "dimension": "the_nether", "x": 10.0, "y": 40.0, "z": -50.0 }
        }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)

        assertTrue(result.containsKey(TEST_UUID))
        val dims = result[TEST_UUID]!!
        assertEquals(2, dims.size, "Should have 2 dimensions")

        val overworld = dims["overworld"]!!
        assertEquals(2, overworld.size)

        val red = waypointByColor(overworld, "red")
        assertEquals(100.5, red.x, 0.001, "X should preserve sub-block precision")
        assertEquals(64.3, red.y, 0.001, "Y should preserve sub-block precision")
        assertEquals(-200.7, red.z, 0.001, "Z should preserve sub-block precision")
        assertEquals("red", red.color.id)

        val nether = dims["the_nether"]!!
        assertEquals(1, nether.size)
        assertEquals("green", waypointByColor(nether, "green").color.id)
    }

    // ---- Legacy format (unversioned) ----

    @Test
    fun `load legacy unversioned format`(@TempDir tempDir: Path) {
        // This is what older versions wrote: bare dimension map, no envelope
        val json =
            """
    {
      "overworld": {
        "red": { "color": "red", "dimension": "overworld", "x": 100.0, "y": 64.0, "z": -200.0 }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertTrue(result.containsKey(TEST_UUID))
        val red = waypointByColor(result[TEST_UUID]!!["overworld"]!!, "red")
        assertEquals(100.0, red.x, 0.001)
        assertEquals("red", red.color.id)
    }

    @Test
    fun `load legacy format with extra unknown fields`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "overworld": {
        "blue": {
          "color": "blue",
          "dimension": "overworld",
          "x": 50.0, "y": 70.0, "z": 300.0,
          "trackedWaypoint": {
            "owner": "12345678-1234-1234-1234-123456789abc",
            "config": { "color": { "value": 5592575 } },
            "pos": { "x": 50, "y": 70, "z": 300 }
          }
        }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertTrue(result.containsKey(TEST_UUID), "Should load despite extra fields")
        val blue = waypointByColor(result[TEST_UUID]!!["overworld"]!!, "blue")
        assertEquals(50.0, blue.x, 0.001)
        assertEquals("blue", blue.color.id)
    }

    @Test
    fun `legacy format with missing fields uses defaults`(@TempDir tempDir: Path) {
        // Minimal waypoint: only x and z, missing color, dimension, y
        val json =
            """
    {
      "overworld": {
        "red": { "x": 42.0, "z": -99.0 }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertTrue(result.containsKey(TEST_UUID), "Should load despite missing fields")
        val wp = waypointByColor(result[TEST_UUID]!!["overworld"]!!, "red")
        assertEquals(42.0, wp.x, 0.001, "X should be parsed")
        assertEquals(0.0, wp.y, 0.001, "Missing Y should default to 0")
        assertEquals(-99.0, wp.z, 0.001, "Z should be parsed")
        assertEquals("red", wp.color.id, "Missing color should fall back to the map key")
        assertEquals("overworld", wp.dimension, "Dimension should come from the outer key")
    }

    @Test
    fun `legacy format with completely empty waypoint object`(@TempDir tempDir: Path) {
        // Waypoint object exists but has no fields at all
        val json =
            """
    {
      "the_nether": {
        "blue": {}
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertTrue(result.containsKey(TEST_UUID), "Should load despite empty waypoint object")
        val wp = waypointByColor(result[TEST_UUID]!!["the_nether"]!!, "blue")
        assertEquals(0.0, wp.x, 0.001, "Missing X should default to 0")
        assertEquals(0.0, wp.y, 0.001, "Missing Y should default to 0")
        assertEquals(0.0, wp.z, 0.001, "Missing Z should default to 0")
        assertEquals("blue", wp.color.id, "Color should fall back to the map key")
        assertEquals("the_nether", wp.dimension)
    }

    // ---- Migration ----

    @Test
    fun `legacy light_purple migrates to purple`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "overworld": {
        "light_purple": { "color": "light_purple", "dimension": "overworld", "x": 1.0, "y": 2.0, "z": 3.0 }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        val overworld = result[TEST_UUID]!!["overworld"]!!
        assertTrue(overworld.values.none { it.color.id == "light_purple" })
        assertEquals("purple", waypointByColor(overworld, "purple").color.id)
    }

    @Test
    fun `versioned light_purple migrates to purple`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "formatVersion": 2,
      "dimensions": {
        "overworld": {
          "light_purple": { "color": "light_purple", "dimension": "overworld", "x": 5.0, "y": 6.0, "z": 7.0 }
        }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        val overworld = result[TEST_UUID]!!["overworld"]!!
        assertTrue(overworld.values.none { it.color.id == "light_purple" })
        assertEquals("purple", waypointByColor(overworld, "purple").color.id)
    }

    @Test
    fun `long marker keys are normalized to short keys`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "formatVersion": 3,
      "dimensions": {
        "overworld": {
          "mk-very-long-legacy-key-12345": {
            "markerKey": "mk-very-long-legacy-key-12345",
            "color": "red",
            "dimension": "overworld",
            "x": 1.0,
            "y": 2.0,
            "z": 3.0
          }
        }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        val keys = result[TEST_UUID]!!["overworld"]!!.keys
        assertEquals(1, keys.size)
        val key = keys.first()
        assertTrue(key.length <= 8, "Marker keys should be at most 8 characters")
        assertTrue(key.matches(Regex("^[a-z0-9]{1,8}$")), "Marker key should be compact")
    }

    @Test
    fun `legacy markers are migrated once and persisted`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "formatVersion": 2,
      "dimensions": {
        "overworld": {
          "red": {
            "color": "red",
            "dimension": "overworld",
            "x": 10.0,
            "y": 64.0,
            "z": -20.0
          }
        }
      }
    }
    """
                .trimIndent()
        val playerFile = tempDir.resolve("$TEST_UUID.json")
        Files.writeString(playerFile, json)

        val firstLoad = WaypointStorage.loadWaypoints(tempDir)
        val firstKey = firstLoad[TEST_UUID]!!["overworld"]!!.keys.first()
        assertTrue(firstKey.matches(Regex("^[a-z0-9]{1,8}$")))

        val rewritten = Files.readString(playerFile)
        assertTrue(rewritten.contains("\"formatVersion\": 3"))
        assertTrue(rewritten.contains("\"markerKey\": \"$firstKey\""))

        val secondLoad = WaypointStorage.loadWaypoints(tempDir)
        val secondKey = secondLoad[TEST_UUID]!!["overworld"]!!.keys.first()
        assertEquals(firstKey, secondKey, "Migrated key should remain stable on subsequent loads")
    }

    // ---- Corrupt / edge cases ----

    @Test
    fun `corrupt json file is skipped gracefully`(@TempDir tempDir: Path) {
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), "{ broken json !!!")

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertFalse(result.containsKey(TEST_UUID), "Corrupt file should be skipped")
    }

    @Test
    fun `empty json file is skipped gracefully`(@TempDir tempDir: Path) {
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), "")

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertFalse(result.containsKey(TEST_UUID), "Empty file should be skipped")
    }

    @Test
    fun `null json is skipped gracefully`(@TempDir tempDir: Path) {
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), "null")

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertFalse(result.containsKey(TEST_UUID), "Null JSON should be skipped")
    }

    @Test
    fun `non-uuid filename is skipped`(@TempDir tempDir: Path) {
        Files.writeString(tempDir.resolve("not-a-uuid.json"), """{"overworld":{}}""")

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertTrue(result.isEmpty(), "Non-UUID files should be skipped")
    }

    @Test
    fun `empty dimensions object loads as empty`(@TempDir tempDir: Path) {
        val json = """{ "formatVersion": 2, "dimensions": {} }"""
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertFalse(result.containsKey(TEST_UUID), "Empty dimensions should not create entry")
    }

    // ---- Round-trip ----

    @Test
    fun `save then load preserves all data`(@TempDir tempDir: Path) {
        // Build runtime data
        val waypoints =
            HashMap<
                UUID,
                MutableMap<String, MutableMap<String, Waypoint>>,
            >()
        val overworld = HashMap<String, Waypoint>()
        overworld["mk-red"] =
            wp(
                "mk-red",
                "red",
                "overworld",
                123.456,
                64.789,
                -987.654,
            )
        overworld["mk-blue"] =
            wp("mk-blue", "blue", "overworld", 0.0, 0.0, 0.0)
        val nether = HashMap<String, Waypoint>()
        nether["mk-green"] =
            wp(
                "mk-green",
                "green",
                "the_nether",
                -100.5,
                30.0,
                200.5,
            )
        val dims =
            mutableMapOf<String, MutableMap<String, Waypoint>>(
                "overworld" to overworld,
                "the_nether" to nether,
            )
        waypoints[TEST_UUID] = dims

        // Save
        WaypointStorage.saveWaypoints(tempDir, waypoints)

        // Load
        val loaded = WaypointStorage.loadWaypoints(tempDir)

        assertTrue(loaded.containsKey(TEST_UUID))
        val loadedDims = loaded[TEST_UUID]!!
        assertEquals(2, loadedDims.size)

        val loadedRed = waypointByColor(loadedDims["overworld"]!!, "red")
        assertEquals(123.456, loadedRed.x, 0.001, "X should round-trip with precision")
        assertEquals(64.789, loadedRed.y, 0.001, "Y should round-trip with precision")
        assertEquals(-987.654, loadedRed.z, 0.001, "Z should round-trip with precision")
        assertEquals("red", loadedRed.color.id)
        assertEquals("overworld", loadedRed.dimension)

        val loadedGreen = waypointByColor(loadedDims["the_nether"]!!, "green")
        assertEquals(-100.5, loadedGreen.x, 0.001)
        assertEquals("the_nether", loadedGreen.dimension)
    }

    @Test
    fun `multiple players save and load independently`(@TempDir tempDir: Path) {
        val waypoints =
            HashMap<
                UUID,
                MutableMap<String, MutableMap<String, Waypoint>>,
            >()

        val p1 =
            mutableMapOf<String, MutableMap<String, Waypoint>>(
                "overworld" to
                    hashMapOf(
                        "mk-p1-red" to
                            wp(
                                "mk-p1-red",
                                "red",
                                "overworld",
                                1.0,
                                2.0,
                                3.0,
                            )
                    )
            )
        val p2 =
            mutableMapOf<String, MutableMap<String, Waypoint>>(
                "the_end" to
                    hashMapOf(
                        "mk-p2-blue" to
                            wp(
                                "mk-p2-blue",
                                "blue",
                                "the_end",
                                4.0,
                                5.0,
                                6.0,
                            )
                    )
            )
        waypoints[TEST_UUID] = p1
        waypoints[TEST_UUID_2] = p2

        WaypointStorage.saveWaypoints(tempDir, waypoints)
        val loaded = WaypointStorage.loadWaypoints(tempDir)

        assertEquals(2, loaded.size, "Should load both players")
        assertEquals("red", waypointByColor(loaded[TEST_UUID]!!["overworld"]!!, "red").color.id)
        assertEquals(
            "blue",
            waypointByColor(loaded[TEST_UUID_2]!!["the_end"]!!, "blue").color.id,
        )
    }

    // ---- Name field ----

    @Test
    fun `named waypoint round-trips through save and load`(@TempDir tempDir: Path) {
        val waypoints =
            HashMap<
                UUID,
                MutableMap<String, MutableMap<String, Waypoint>>,
            >()
        val overworld = HashMap<String, Waypoint>()
        overworld["mk-red"] =
            wp(
                "mk-red",
                "red",
                "overworld",
                1.0,
                2.0,
                3.0,
                "Home Base",
            )
        overworld["mk-blue"] =
            wp("mk-blue", "blue", "overworld", 4.0, 5.0, 6.0)
        waypoints[TEST_UUID] =
            mutableMapOf<String, MutableMap<String, Waypoint>>(
                "overworld" to overworld
            )

        WaypointStorage.saveWaypoints(tempDir, waypoints)
        val loaded = WaypointStorage.loadWaypoints(tempDir)

        val loadedRed = waypointByColor(loaded[TEST_UUID]!!["overworld"]!!, "red")
        assertEquals("Home Base", loadedRed.name, "Name should survive round-trip")

        val loadedBlue = waypointByColor(loaded[TEST_UUID]!!["overworld"]!!, "blue")
        assertEquals("", loadedBlue.name, "Unnamed waypoint should have empty name")
    }

    @Test
    fun `versioned format with name field`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "formatVersion": 2,
      "dimensions": {
        "overworld": {
          "red": { "color": "red", "dimension": "overworld", "x": 1.0, "y": 2.0, "z": 3.0, "name": "My Spot" }
        }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertEquals("My Spot", waypointByColor(result[TEST_UUID]!!["overworld"]!!, "red").name)
    }

    @Test
    fun `legacy format without name field defaults to empty`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "overworld": {
        "red": { "color": "red", "dimension": "overworld", "x": 1.0, "y": 2.0, "z": 3.0 }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertEquals(
            "",
            waypointByColor(result[TEST_UUID]!!["overworld"]!!, "red").name,
            "Missing name should default to empty",
        )
    }

    @Test
    fun `versioned format without name field defaults to empty`(@TempDir tempDir: Path) {
        val json =
            """
    {
      "formatVersion": 2,
      "dimensions": {
        "overworld": {
          "green": { "color": "green", "dimension": "overworld", "x": 9.0, "y": 8.0, "z": 7.0 }
        }
      }
    }
    """
                .trimIndent()
        Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

        val result = WaypointStorage.loadWaypoints(tempDir)
        assertEquals(
            "",
            waypointByColor(result[TEST_UUID]!!["overworld"]!!, "green").name,
            "Missing name in versioned format should default to empty",
        )
    }
}
