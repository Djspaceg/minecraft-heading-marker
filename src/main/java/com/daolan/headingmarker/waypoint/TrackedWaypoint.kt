package com.daolan.headingmarker.waypoint

import java.util.UUID
import kotlin.math.atan2
import net.minecraft.core.Vec3i
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

abstract class TrackedWaypoint(
    val owner: UUID,
    override val config: Waypoint.Config,
    protected val pos: Vec3i,
) : Waypoint {

    fun getRelativeYaw(
        world: Level,
        yawProvider: YawProvider,
        tickProgress: EntityTickProgress,
    ): Double {
        val camPos = yawProvider.cameraPos
        val dX = pos.x + 0.5 - camPos.x
        val dZ = pos.z + 0.5 - camPos.z

        val angleRad = atan2(dZ, dX)
        val angleDeg = Math.toDegrees(angleRad) - 90.0

        val camYaw = yawProvider.cameraYaw.toDouble()

        return Mth.wrapDegrees(angleDeg - camYaw)
    }

    interface YawProvider {
        val cameraYaw: Float
        val cameraPos: Vec3
    }

    companion object {
        @JvmStatic
        fun ofPos(owner: UUID, config: Waypoint.Config, pos: Vec3i): TrackedWaypoint {
            return object : TrackedWaypoint(owner, config, pos) {}
        }
    }
}
