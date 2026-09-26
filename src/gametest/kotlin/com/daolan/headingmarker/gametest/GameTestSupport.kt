package com.daolan.headingmarker.gametest

import com.daolan.headingmarker.HeadingMarkerMod
import java.util.Locale
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3

internal fun markers(level: ServerLevel): List<ArmorStand> =
    level.allEntities.filterIsInstance<ArmorStand>().filter {
        it.customName?.string?.startsWith("hm:") == true && !it.isRemoved
    }

internal fun markerFor(level: ServerLevel, key: String): ArmorStand? =
    markers(level).firstOrNull { it.customName?.string == "hm:$key" }

internal fun waypoints(player: ServerPlayer) =
    HeadingMarkerMod.service().waypoints(player.uuid, "overworld")

internal fun GameTestHelper.check(condition: Boolean, message: () -> String) =
    assertTrue(condition, message())

/** Runs [body] and always disconnects [players] afterwards so tests don't leak state. */
internal fun withPlayers(vararg players: RecordingPlayer, body: () -> Unit) {
    try {
        body()
    } finally {
        players.forEach(TestPlayers::leave)
    }
}

internal fun GameTestHelper.spot(x: Double, z: Double): Vec3 = absoluteVec(Vec3(x, 2.0, z))

/** Test structures sit millions of blocks out, where Double.toString gives "1.0E7". */
internal fun coords(v: Vec3): String =
    listOf(v.x, v.y, v.z).joinToString(" ") { String.format(Locale.ROOT, "%.2f", it) }

/** Vanilla's private receiver → transmitter → connection table for [level]. */
@Suppress("UNCHECKED_CAST")
internal fun connections(level: ServerLevel): com.google.common.collect.Table<ServerPlayer, Any, Any> {
    val field = level.waypointManager.javaClass.getDeclaredField("connections")
    field.isAccessible = true
    return field.get(level.waypointManager) as com.google.common.collect.Table<ServerPlayer, Any, Any>
}
