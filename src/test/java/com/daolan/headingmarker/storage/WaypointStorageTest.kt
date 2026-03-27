package com.daolan.headingmarker.storage

import com.daolan.headingmarker.HeadingMarkerMod
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
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

    private fun <T> withMcEnv(block: () -> T): T? {
        return try {
            block()
        } catch (e: ExceptionInInitializerError) {
            assumeTrue(false, "Skipping - Minecraft environment not available: ${e.message}")
            null
        } catch (e: NoClassDefFoundError) {
            assumeTrue(false, "Skipping - Minecraft environment not available: ${e.message}")
            null
        }
    }

    // ---- Versioned format (v2) ----

    @Test
    fun `load versioned format with sub-block precision`(@TempDir tempDir: Path) =
        withMcEnv {
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

            val red = overworld["red"]!!
            assertEquals(100.5, red.x, 0.001, "X should preserve sub-block precision")
            assertEquals(64.3, red.y, 0.001, "Y should preserve sub-block precision")
            assertEquals(-200.7, red.z, 0.001, "Z should preserve sub-block precision")
            assertEquals("red", red.color)

            val nether = dims["the_nether"]!!
            assertEquals(1, nether.size)
            assertEquals("green", nether["green"]!!.color)
        }!!

    // ---- Legacy format (unversioned) ----

    @Test
    fun `load legacy unversioned format`(@TempDir tempDir: Path) =
        withMcEnv {
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
            val red = result[TEST_UUID]!!["overworld"]!!["red"]!!
            assertEquals(100.0, red.x, 0.001)
            assertEquals("red", red.color)
        }!!

    @Test
    fun `load legacy format with extra unknown fields`(@TempDir tempDir: Path) =
        withMcEnv {
            // Old files had trackedWaypoint, entityId, and Optional fields that should be ignored
            val json =
                """
        {
          "overworld": {
            "blue": {
              "color": "blue",
              "dimension": "overworld",
              "x": 50.0, "y": 70.0, "z": 300.0,
              "entityId": 42,
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
            val blue = result[TEST_UUID]!!["overworld"]!!["blue"]!!
            assertEquals(50.0, blue.x, 0.001)
            assertEquals("blue", blue.color)
            assertEquals(-1, blue.entityId, "entityId should be -1 (runtime only)")
        }!!

    @Test
    fun `legacy format with missing fields uses defaults`(@TempDir tempDir: Path) =
        withMcEnv {
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
            val wp = result[TEST_UUID]!!["overworld"]!!["red"]!!
            assertEquals(42.0, wp.x, 0.001, "X should be parsed")
            assertEquals(0.0, wp.y, 0.001, "Missing Y should default to 0")
            assertEquals(-99.0, wp.z, 0.001, "Z should be parsed")
            assertEquals("red", wp.color, "Missing color should fall back to the map key")
            assertEquals("overworld", wp.dimension, "Dimension should come from the outer key")
            assertEquals(-1, wp.entityId, "entityId should always be -1 on load")
            assertNotNull(wp.trackedWaypoint, "trackedWaypoint should be recreated")
        }!!

    @Test
    fun `legacy format with completely empty waypoint object`(@TempDir tempDir: Path) =
        withMcEnv {
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
            val wp = result[TEST_UUID]!!["the_nether"]!!["blue"]!!
            assertEquals(0.0, wp.x, 0.001, "Missing X should default to 0")
            assertEquals(0.0, wp.y, 0.001, "Missing Y should default to 0")
            assertEquals(0.0, wp.z, 0.001, "Missing Z should default to 0")
            assertEquals("blue", wp.color, "Color should fall back to the map key")
            assertEquals("the_nether", wp.dimension)
        }!!

    // ---- Migration ----

    @Test
    fun `legacy light_purple migrates to purple`(@TempDir tempDir: Path) =
        withMcEnv {
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
            assertFalse(
                overworld.containsKey("light_purple"),
                "light_purple key should be migrated",
            )
            assertTrue(overworld.containsKey("purple"), "Should have purple key after migration")
            assertEquals("purple", overworld["purple"]!!.color)
        }!!

    @Test
    fun `versioned light_purple migrates to purple`(@TempDir tempDir: Path) =
        withMcEnv {
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
            assertFalse(overworld.containsKey("light_purple"))
            assertTrue(overworld.containsKey("purple"))
            assertEquals("purple", overworld["purple"]!!.color)
        }!!

    // ---- Corrupt / edge cases ----

    @Test
    fun `corrupt json file is skipped gracefully`(@TempDir tempDir: Path) =
        withMcEnv {
            Files.writeString(tempDir.resolve("$TEST_UUID.json"), "{ broken json !!!")

            val result = WaypointStorage.loadWaypoints(tempDir)
            assertFalse(result.containsKey(TEST_UUID), "Corrupt file should be skipped")
        }!!

    @Test
    fun `empty json file is skipped gracefully`(@TempDir tempDir: Path) =
        withMcEnv {
            Files.writeString(tempDir.resolve("$TEST_UUID.json"), "")

            val result = WaypointStorage.loadWaypoints(tempDir)
            assertFalse(result.containsKey(TEST_UUID), "Empty file should be skipped")
        }!!

    @Test
    fun `null json is skipped gracefully`(@TempDir tempDir: Path) =
        withMcEnv {
            Files.writeString(tempDir.resolve("$TEST_UUID.json"), "null")

            val result = WaypointStorage.loadWaypoints(tempDir)
            assertFalse(result.containsKey(TEST_UUID), "Null JSON should be skipped")
        }!!

    @Test
    fun `non-uuid filename is skipped`(@TempDir tempDir: Path) =
        withMcEnv {
            Files.writeString(tempDir.resolve("not-a-uuid.json"), """{"overworld":{}}""")

            val result = WaypointStorage.loadWaypoints(tempDir)
            assertTrue(result.isEmpty(), "Non-UUID files should be skipped")
        }!!

    @Test
    fun `empty dimensions object loads as empty`(@TempDir tempDir: Path) =
        withMcEnv {
            val json = """{ "formatVersion": 2, "dimensions": {} }"""
            Files.writeString(tempDir.resolve("$TEST_UUID.json"), json)

            val result = WaypointStorage.loadWaypoints(tempDir)
            assertFalse(result.containsKey(TEST_UUID), "Empty dimensions should not create entry")
        }!!

    // ---- Round-trip ----

    @Test
    fun `save then load preserves all data`(@TempDir tempDir: Path) =
        withMcEnv {
            // Build runtime data
            val waypoints =
                HashMap<
                    UUID,
                    MutableMap<String, MutableMap<String, HeadingMarkerMod.WaypointData>>,
                >()
            val overworld = HashMap<String, HeadingMarkerMod.WaypointData>()
            overworld["red"] =
                HeadingMarkerMod.WaypointData("red", "overworld", 123.456, 64.789, -987.654)
            overworld["blue"] = HeadingMarkerMod.WaypointData("blue", "overworld", 0.0, 0.0, 0.0)
            val nether = HashMap<String, HeadingMarkerMod.WaypointData>()
            nether["green"] =
                HeadingMarkerMod.WaypointData("green", "the_nether", -100.5, 30.0, 200.5)
            val dims =
                mutableMapOf<String, MutableMap<String, HeadingMarkerMod.WaypointData>>(
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

            val loadedRed = loadedDims["overworld"]!!["red"]!!
            assertEquals(123.456, loadedRed.x, 0.001, "X should round-trip with precision")
            assertEquals(64.789, loadedRed.y, 0.001, "Y should round-trip with precision")
            assertEquals(-987.654, loadedRed.z, 0.001, "Z should round-trip with precision")
            assertEquals("red", loadedRed.color)
            assertEquals("overworld", loadedRed.dimension)
            assertEquals(-1, loadedRed.entityId, "entityId should be -1 after load")

            val loadedGreen = loadedDims["the_nether"]!!["green"]!!
            assertEquals(-100.5, loadedGreen.x, 0.001)
            assertEquals("the_nether", loadedGreen.dimension)
        }!!

    @Test
    fun `multiple players save and load independently`(@TempDir tempDir: Path) =
        withMcEnv {
            val waypoints =
                HashMap<
                    UUID,
                    MutableMap<String, MutableMap<String, HeadingMarkerMod.WaypointData>>,
                >()

            val p1 =
                mutableMapOf<String, MutableMap<String, HeadingMarkerMod.WaypointData>>(
                    "overworld" to
                        hashMapOf(
                            "red" to
                                HeadingMarkerMod.WaypointData("red", "overworld", 1.0, 2.0, 3.0)
                        )
                )
            val p2 =
                mutableMapOf<String, MutableMap<String, HeadingMarkerMod.WaypointData>>(
                    "the_end" to
                        hashMapOf(
                            "blue" to
                                HeadingMarkerMod.WaypointData("blue", "the_end", 4.0, 5.0, 6.0)
                        )
                )
            waypoints[TEST_UUID] = p1
            waypoints[TEST_UUID_2] = p2

            WaypointStorage.saveWaypoints(tempDir, waypoints)
            val loaded = WaypointStorage.loadWaypoints(tempDir)

            assertEquals(2, loaded.size, "Should load both players")
            assertEquals("red", loaded[TEST_UUID]!!["overworld"]!!["red"]!!.color)
            assertEquals("blue", loaded[TEST_UUID_2]!!["the_end"]!!["blue"]!!.color)
        }!!
}
