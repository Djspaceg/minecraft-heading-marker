package com.daolan.headingmarker.gametest

import com.google.common.collect.Table
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.decoration.ArmorStand

/** Optional diagnostics that log vanilla waypoint behavior instead of asserting it. */
class ProbeGameTests {

    @Suppress("UNCHECKED_CAST")
    private fun connections(level: ServerLevel): Table<ServerPlayer, Any, Any> {
        val field = level.waypointManager.javaClass.getDeclaredField("connections")
        field.isAccessible = true
        return field.get(level.waypointManager) as Table<ServerPlayer, Any, Any>
    }

    private fun report(helper: GameTestHelper, text: String) =
        helper.level.server.sendSystemMessage(Component.literal("PROBE $text"))

    @GameTest(required = false, maxTicks = 40)
    fun probeOtherPlayerConnection(helper: GameTestHelper) {
        val owner = TestPlayers.join(helper, "hm_probe_a")
        val other = TestPlayers.join(helper, "hm_probe_b")
        TestPlayers.run(owner, "hm set red ${coords(helper.spot(5.0, 5.0))}")
        val stand = markerFor(helper.level, waypoints(owner).keys.single())!!
        // Transmitters only open connections after their first tick (LivingEntity.firstTick).
        helper.runAfterDelay(10) {
            withPlayers(owner, other) {
                val table = connections(helper.level)
                report(
                    helper,
                    "tableSize=${table.size()} owner->marker=${table.contains(owner, stand)} " +
                        "other->marker=${table.contains(other, stand)} " +
                        "owner->other=${table.contains(owner, other)}",
                )
            }
            helper.succeed()
        }
    }

    @GameTest(required = false, maxTicks = 40)
    fun probeFarWaypoint(helper: GameTestHelper) {
        val owner = TestPlayers.join(helper, "hm_probe_far")
        TestPlayers.run(owner, "hm set red ${coords(helper.spot(3000.0, 3000.0))}")
        val key = waypoints(owner).keys.single()
        helper.runAfterDelay(10) {
            withPlayers(owner) {
                val transmitting =
                    helper.level.waypointManager.transmitters().any {
                        (it as? ArmorStand)?.customName?.string == "hm:$key"
                    }
                report(
                    helper,
                    "far visible=${markerFor(helper.level, key) != null} " +
                        "transmitting=$transmitting " +
                        "ownerConnections=${connections(helper.level).row(owner).size}",
                )
            }
            helper.succeed()
        }
    }
}
