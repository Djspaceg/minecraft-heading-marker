package com.daolan.headingmarker.gametest

import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.decoration.ArmorStand

/** Marker entities must follow their waypoint no matter where the chunk is or who removed them. */
class MarkerLifecycleGameTests {

    /**
     * Markers in unloaded chunks are still registered with the waypoint manager (vanilla tracks
     * them from ServerLevel.EntityCallbacks.onCreated), but Level.getEntity can't see them.
     */
    private fun transmitterFor(helper: GameTestHelper, key: String): ArmorStand? =
        helper.level.waypointManager.transmitters().filterIsInstance<ArmorStand>().firstOrNull {
            it.customName?.string == "hm:$key"
        }

    @GameTest
    fun removingFarWaypointStopsTransmitting(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_far_rm")
        withPlayers(player) {
            TestPlayers.run(player, "hm set purple ${coords(helper.spot(4000.0, -4000.0))}")
            val key = waypoints(player).keys.single()
            val stand = transmitterFor(helper, key)
            helper.check(stand != null) { "far marker should be transmitting" }

            TestPlayers.run(player, "hm remove purple")

            helper.check(stand!!.isRemoved) { "far marker entity should be discarded" }
            helper.check(transmitterFor(helper, key) == null) {
                "removed far marker is still on the locator bar"
            }
        }
        helper.succeed()
    }

    @GameTest
    fun disconnectStopsFarWaypointTransmitting(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_far_dc")
        TestPlayers.run(player, "hm set purple ${coords(helper.spot(-4000.0, 4000.0))}")
        val key = waypoints(player).keys.single()
        val stand = transmitterFor(helper, key)!!

        TestPlayers.leave(player)

        helper.check(stand.isRemoved) { "far marker entity should be discarded on disconnect" }
        helper.check(transmitterFor(helper, key) == null) { "far marker outlived its owner" }
        helper.succeed()
    }

    @GameTest(maxTicks = 200)
    fun farWaypointSurvivesWorldSaves(helper: GameTestHelper) {
        // An entity sitting in an unloaded chunk gets written out and unloaded by the next
        // world save, which would silently drop it from the owner's locator bar.
        val player = TestPlayers.join(helper, "hm_far_save")
        TestPlayers.run(player, "hm set blue ${coords(helper.spot(5000.0, 5000.0))}")
        val key = waypoints(player).keys.single()
        helper.check(transmitterFor(helper, key) != null) { "far marker should start transmitting" }

        helper.startSequence()
            .thenExecute { helper.level.save(null, false, false) }
            .thenIdle(40)
            .thenExecute { helper.level.save(null, false, false) }
            .thenIdle(40)
            .thenExecute { helper.level.save(null, false, false) }
            .thenIdle(20)
            .thenExecute {
                helper.check(transmitterFor(helper, key) != null) {
                    "far marker stopped transmitting after a world save"
                }
                TestPlayers.leave(player)
            }
            .thenSucceed()
    }

    @GameTest(maxTicks = 40)
    fun orphanedMarkerEntityIsRemovedWhenLoaded(helper: GameTestHelper) {
        // Stands left behind by a crash or an old session come back when their chunk loads.
        val orphan =
            ArmorStand(EntityTypes.ARMOR_STAND, helper.level).apply {
                val at = helper.spot(6.0, 6.0)
                setPos(at.x, at.y, at.z)
                customName = Component.literal("hm:orphan01")
            }
        helper.level.addFreshEntity(orphan)
        helper.succeedWhen { helper.check(orphan.isRemoved) { "orphan marker still present" } }
    }

    @GameTest(maxTicks = 60)
    fun killedMarkerIsRespawned(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_kill")
        TestPlayers.run(player, "hm set red ${coords(helper.spot(3.0, 6.0))}")
        val key = waypoints(player).keys.single()
        val original = markerFor(helper.level, key)!!

        original.kill(helper.level) // e.g. an admin running /kill @e[type=armor_stand]

        helper.succeedWhen {
            val replacement = transmitterFor(helper, key)
            helper.check(replacement != null && replacement !== original) {
                "killed marker was not respawned"
            }
            TestPlayers.leave(player)
        }
    }
}
