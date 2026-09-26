package com.daolan.headingmarker

import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class DimensionsTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() = MinecraftTestEnv.bootstrap()
    }

    private fun key(id: String) = ResourceKey.create(Registries.DIMENSION, Identifier.parse(id))

    @Test
    fun `vanilla dimensions keep their bare ids`() {
        assertEquals("overworld", Dimensions.idOf(Level.OVERWORLD))
        assertEquals("the_nether", Dimensions.idOf(Level.NETHER))
        assertEquals("the_end", Dimensions.idOf(Level.END))
    }

    @Test
    fun `modded dimensions keep their namespace`() {
        assertEquals("mymod:mining", Dimensions.idOf(key("mymod:mining")))
        // Used to collide with the vanilla overworld's waypoints.
        assertEquals("othermod:overworld", Dimensions.idOf(key("othermod:overworld")))
    }
}
