package com.daolan.headingmarker.waypoint

import net.minecraft.world.entity.Entity

fun interface EntityTickProgress {
    fun getTickProgress(entity: Entity): Float
}
