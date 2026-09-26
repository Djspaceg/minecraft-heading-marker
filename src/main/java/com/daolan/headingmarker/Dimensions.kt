package com.daolan.headingmarker

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

/** Maps levels to the dimension ids waypoints are stored under, and back. */
object Dimensions {
    const val OVERWORLD = "overworld"
    const val NETHER = "the_nether"
    const val END = "the_end"

    fun idOf(level: Level): String = idOf(level.dimension())

    fun idOf(key: ResourceKey<Level>): String = key.identifier().path

    fun levelFor(server: MinecraftServer, id: String): ServerLevel? =
        when (id) {
            OVERWORLD -> server.getLevel(Level.OVERWORLD)
            NETHER -> server.getLevel(Level.NETHER)
            END -> server.getLevel(Level.END)
            else -> null
        }
}
