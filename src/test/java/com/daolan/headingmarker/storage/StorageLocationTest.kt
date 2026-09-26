package com.daolan.headingmarker.storage

import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class StorageLocationTest {
    private val uuidFile = "12345678-1234-1234-1234-123456789abc.json"

    @Test
    fun `first use copies legacy files and keeps the legacy folder`(@TempDir root: Path) {
        val legacy = Files.createDirectories(root.resolve("waypoints"))
        Files.writeString(legacy.resolve(uuidFile), "{}")
        Files.writeString(legacy.resolve("notes.txt"), "ignored")
        val world = root.resolve("saves/World A/headingmarker")

        assertEquals(world, StorageLocation.prepare(world, legacy))

        assertEquals("{}", Files.readString(world.resolve(uuidFile)))
        assertFalse(Files.exists(world.resolve("notes.txt")))
        assertTrue(Files.exists(legacy.resolve(uuidFile)), "legacy files may belong to other worlds")
    }

    @Test
    fun `an existing world folder is never refilled from legacy files`(@TempDir root: Path) {
        val legacy = Files.createDirectories(root.resolve("waypoints"))
        Files.writeString(legacy.resolve(uuidFile), "{\"legacy\": true}")
        val world = Files.createDirectories(root.resolve("world/headingmarker"))

        StorageLocation.prepare(world, legacy)

        assertFalse(Files.exists(world.resolve(uuidFile)), "cleared waypoints must stay cleared")
    }

    @Test
    fun `works without a legacy folder`(@TempDir root: Path) {
        val world = root.resolve("world/headingmarker")
        StorageLocation.prepare(world, root.resolve("waypoints"))
        assertTrue(Files.isDirectory(world))
    }
}
