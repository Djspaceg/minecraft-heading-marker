package com.daolan.headingmarker

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

/**
 * Maps levels to the dimension ids waypoints are stored under, and back. Vanilla dimensions use
 * their bare path ("overworld", "the_nether", "the_end") as they always have; any other dimension
 * keeps its namespace ("mymod:mining") so two mods' dimensions can't share waypoints.
 */
object Dimensions {
    private const val VANILLA_NAMESPACE = "minecraft"

    fun idOf(level: Level): String = idOf(level.dimension())

    fun idOf(key: ResourceKey<Level>): String {
        val id = key.identifier()
        return if (id.namespace == VANILLA_NAMESPACE) id.path else id.toString()
    }

    fun levelFor(server: MinecraftServer, id: String): ServerLevel? =
        server.allLevels.firstOrNull { idOf(it) == id }

    /**
     * Older versions stored every dimension under its bare path. Returns the current id for such
     * a [legacyId] if exactly one loaded level has that path, otherwise null.
     */
    fun upgradeLegacyId(server: MinecraftServer, legacyId: String): String? {
        if (':' in legacyId || levelFor(server, legacyId) != null) return null
        return server.allLevels
            .filter { it.dimension().identifier().path == legacyId }
            .singleOrNull()
            ?.let(::idOf)
    }
}
