package com.daolan.headingmarker

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap

/**
 * Vanilla must be bootstrapped before anything touches registries (Level.OVERWORLD, Commands,
 * ...). If a test class touches one first, the registries fail to initialize for every later test
 * in the same JVM, so each such class calls [bootstrap] from @BeforeAll.
 */
object MinecraftTestEnv {
    fun bootstrap() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }
}
