package com.daolan.headingmarker.storage

import com.daolan.headingmarker.HeadingMarkerMod.Companion.LOGGER
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource

/** Where waypoint files live: inside the world folder, so each world has its own waypoints. */
object StorageLocation {
    private const val WORLD_FOLDER = "headingmarker"

    /**
     * Older versions kept one shared `waypoints/` folder in the game directory, so every
     * singleplayer world showed the same waypoints.
     */
    private const val LEGACY_FOLDER = "waypoints"

    /** Returns [server]'s waypoint folder, creating it (and importing legacy files) if needed. */
    fun forServer(server: MinecraftServer): Path =
        prepare(
            worldDir = server.getWorldPath(LevelResource.ROOT).resolve(WORLD_FOLDER).normalize(),
            legacyDir = FabricLoader.getInstance().gameDir.resolve(LEGACY_FOLDER),
        )

    /**
     * Creates [worldDir]. The first time, copies any legacy files from [legacyDir] into it. The
     * legacy folder is left alone: it may belong to other worlds that haven't been opened yet.
     */
    fun prepare(worldDir: Path, legacyDir: Path): Path {
        if (Files.isDirectory(worldDir)) return worldDir
        Files.createDirectories(worldDir)
        if (!Files.isDirectory(legacyDir)) return worldDir

        val legacyFiles =
            try {
                Files.list(legacyDir).use { paths ->
                    paths.filter { it.fileName.toString().endsWith(".json") }.toList()
                }
            } catch (e: IOException) {
                LOGGER.error("Could not read legacy waypoint folder {}", legacyDir, e)
                return worldDir
            }
        var copied = 0
        for (file in legacyFiles) {
            try {
                Files.copy(file, worldDir.resolve(file.fileName))
                copied++
            } catch (e: IOException) {
                LOGGER.error("Could not copy legacy waypoint file {}", file, e)
            }
        }
        if (copied > 0) {
            LOGGER.info(
                "Copied {} waypoint file(s) from the old shared folder {} into {}. " +
                    "The old folder is no longer used once every world has been opened.",
                copied,
                legacyDir,
                worldDir,
            )
        }
        return worldDir
    }
}
