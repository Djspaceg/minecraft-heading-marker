package com.daolan.headingmarker

import com.daolan.headingmarker.model.MarkerKeys
import com.daolan.headingmarker.model.WaypointColor
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class WaypointRegistryTest {
    private val alice = UUID.fromString("00000000-0000-0000-0000-00000000000a")
    private val bob = UUID.fromString("00000000-0000-0000-0000-00000000000b")
    private val registry = WaypointRegistry()

    @Test
    fun `add generates a usable key and trims the name`() {
        val waypoint = registry.add(alice, "overworld", WaypointColor.RED, 1.0, 2.0, 3.0, "  Home ")
        assertTrue(MarkerKeys.isUsable(waypoint.key), "bad key ${waypoint.key}")
        assertEquals("Home", waypoint.name)
        assertEquals(mapOf(waypoint.key to waypoint), registry.waypoints(alice, "overworld"))
    }

    @Test
    fun `waypoints are isolated by owner and dimension`() {
        registry.add(alice, "overworld", WaypointColor.RED, 0.0, 0.0, 0.0)
        registry.add(alice, "the_nether", WaypointColor.BLUE, 0.0, 0.0, 0.0)
        registry.add(bob, "overworld", WaypointColor.GREEN, 0.0, 0.0, 0.0)

        assertEquals(listOf(WaypointColor.RED), registry.waypoints(alice, "overworld").values.map { it.color })
        assertEquals(listOf(WaypointColor.BLUE), registry.waypoints(alice, "the_nether").values.map { it.color })
        assertEquals(listOf(WaypointColor.GREEN), registry.waypoints(bob, "overworld").values.map { it.color })
        assertTrue(registry.waypoints(bob, "the_end").isEmpty())
    }

    @Test
    fun `selector prefers an exact key, else matches every color or name`() {
        val red = registry.add(alice, "overworld", WaypointColor.RED, 0.0, 0.0, 0.0)
        val red2 = registry.add(alice, "overworld", WaypointColor.RED, 1.0, 0.0, 0.0)
        val camp = registry.add(alice, "overworld", WaypointColor.BLUE, 2.0, 0.0, 0.0, "Camp")

        assertEquals(listOf(red), registry.select(alice, "overworld", red.key))
        assertEquals(setOf(red, red2), registry.select(alice, "overworld", "RED").toSet())
        assertEquals(listOf(camp), registry.select(alice, "overworld", "camp"))
        assertEquals(listOf(camp), registry.select(alice, "overworld", " Camp "))
        assertTrue(registry.select(alice, "overworld", "").isEmpty())
        assertTrue(registry.select(alice, "overworld", "nothing").isEmpty())
    }

    @Test
    fun `light_purple selects purple waypoints`() {
        val purple = registry.add(alice, "overworld", WaypointColor.PURPLE, 0.0, 0.0, 0.0)
        assertEquals(listOf(purple), registry.select(alice, "overworld", "light_purple"))
    }

    @Test
    fun `rename, remove, and clear only touch the matching data`() {
        val a = registry.add(alice, "overworld", WaypointColor.RED, 0.0, 0.0, 0.0)
        registry.add(alice, "the_end", WaypointColor.RED, 0.0, 0.0, 0.0)

        assertEquals("Spawn", registry.rename(alice, "overworld", a.key, " Spawn ")?.name)
        assertNull(registry.rename(alice, "overworld", "missing", "x"))
        assertEquals(a.copy(name = "Spawn"), registry.remove(alice, "overworld", a.key))
        assertNull(registry.remove(alice, "overworld", a.key))

        registry.add(alice, "overworld", WaypointColor.BLUE, 0.0, 0.0, 0.0)
        assertEquals(1, registry.clear(alice, "overworld").size)
        assertEquals(1, registry.waypoints(alice, "the_end").size)
        assertEquals(1, registry.clearAll(alice).size)
        assertTrue(registry.allDimensions(alice).values.all { it.isEmpty() })
    }

    @Test
    fun `dirty owners are reported once per change`() {
        registry.replaceAll(mapOf(bob to mapOf("overworld" to emptyMap())))
        assertTrue(registry.takeDirty().isEmpty(), "loading shouldn't mark anything dirty")

        val a = registry.add(alice, "overworld", WaypointColor.RED, 0.0, 0.0, 0.0)
        assertEquals(setOf(alice), registry.takeDirty())
        assertTrue(registry.takeDirty().isEmpty())

        registry.remove(alice, "overworld", "missing")
        registry.clear(bob, "overworld")
        assertTrue(registry.takeDirty().isEmpty(), "no-op changes shouldn't mark owners dirty")

        registry.rename(alice, "overworld", a.key, "x")
        assertEquals(setOf(alice), registry.takeDirty())
    }

    @Test
    fun `renameDimension moves and merges waypoints for every owner`() {
        val legacy = registry.add(alice, "mining", WaypointColor.RED, 1.0, 2.0, 3.0)
        val existing = registry.add(alice, "mymod:mining", WaypointColor.BLUE, 0.0, 0.0, 0.0)
        registry.add(bob, "mining", WaypointColor.GREEN, 0.0, 0.0, 0.0)
        registry.takeDirty()

        registry.renameDimension("mining", "mymod:mining")

        val moved = registry.waypoints(alice, "mymod:mining")
        assertEquals(setOf(legacy.key, existing.key), moved.keys)
        assertEquals("mymod:mining", moved.getValue(legacy.key).dimension)
        assertTrue(registry.waypoints(alice, "mining").isEmpty())
        assertEquals(1, registry.waypoints(bob, "mymod:mining").size)
        assertEquals(setOf(alice, bob), registry.takeDirty())
        assertEquals(setOf("mymod:mining"), registry.dimensionIds())
    }

    @Test
    fun `snapshots don't change when the registry does`() {
        registry.add(alice, "overworld", WaypointColor.RED, 0.0, 0.0, 0.0)
        val snapshot = registry.waypoints(alice, "overworld")
        registry.clear(alice, "overworld")
        assertEquals(1, snapshot.size)
    }

    @Test
    fun `marker keys never collide with color words`() {
        for (word in WaypointColor.RESERVED_WORDS) assertFalse(MarkerKeys.isUsable(word), word)
        assertTrue(MarkerKeys.isUsable("a1b2c3d4"))
        assertFalse(MarkerKeys.isUsable("A1B2C3D4"))
        assertFalse(MarkerKeys.isUsable("toolong123"))
    }
}
