package com.daolan.headingmarker.gametest

import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.decoration.ArmorStand

class HeadingMarkerGameTests {

    @GameTest
    fun setCreatesTrackedMarkerEntity(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_set")
        withPlayers(player) {
            val at = helper.spot(3.0, 3.0)
            TestPlayers.run(player, "hm set red ${coords(at)}")

            val entry = waypoints(player).entries.singleOrNull()
            helper.check(entry != null) { "expected one waypoint, chat=${player.chat}" }
            val (key, data) = entry!!
            helper.check(data.color == "red") { "color was ${data.color}" }

            val stand = markerFor(helper.level, key)
            helper.check(stand != null) { "no marker entity for $key" }
            stand!!
            helper.check(stand.isMarker) { "armor stand should be in marker mode" }
            helper.check(stand.isInvisible) { "armor stand should be invisible" }
            helper.check(stand.isNoGravity) { "armor stand should ignore gravity" }
            helper.check(stand.getAttributeValue(Attributes.WAYPOINT_TRANSMIT_RANGE) > 1000) {
                "transmit range too small"
            }
            helper.check(stand in helper.level.waypointManager.transmitters()) {
                "marker entity is not registered with the waypoint manager"
            }
        }
        helper.succeed()
    }

    @GameTest
    fun removeDiscardsMarkerEntity(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_remove")
        withPlayers(player) {
            val at = helper.spot(3.0, 3.0)
            TestPlayers.run(player, "hm set blue ${coords(at)}")
            val key = waypoints(player).keys.single()
            val stand = markerFor(helper.level, key)!!

            TestPlayers.run(player, "hm remove blue")

            helper.check(waypoints(player).isEmpty()) { "waypoint should be gone" }
            helper.check(stand.isRemoved) { "marker entity should be discarded" }
        }
        helper.succeed()
    }

    @GameTest
    fun disconnectDiscardsAndRejoinRecreates(helper: GameTestHelper) {
        val first = TestPlayers.join(helper, "hm_rejoin")
        val uuid = first.uuid
        val at = helper.spot(4.0, 4.0)
        TestPlayers.run(first, "hm set green ${coords(at)}")
        val key = waypoints(first).keys.single()
        val original = markerFor(helper.level, key)!!

        TestPlayers.leave(first)
        helper.check(original.isRemoved) { "marker entity should be discarded on disconnect" }

        val second = TestPlayers.join(helper, "hm_rejoin", uuid)
        withPlayers(second) {
            helper.check(waypoints(second).keys == setOf(key)) {
                "waypoint key should survive a reconnect, got ${waypoints(second).keys}"
            }
            val recreated = markerFor(helper.level, key)
            helper.check(recreated != null && recreated !== original) {
                "marker entity should be recreated on join"
            }
        }
        helper.succeed()
    }

    @GameTest
    fun shareCopiesWaypointToTarget(helper: GameTestHelper) {
        val owner = TestPlayers.join(helper, "hm_share_a")
        val friend = TestPlayers.join(helper, "hm_share_b")
        withPlayers(owner, friend) {
            val at = helper.spot(2.0, 5.0)
            TestPlayers.run(owner, "hm set yellow ${coords(at)}")
            TestPlayers.run(owner, "hm rename yellow Camp")
            TestPlayers.run(owner, "hm share hm_share_b Camp")

            val copy = waypoints(friend).values.singleOrNull()
            helper.check(copy != null) { "friend should have one copy, chat=${owner.chat}" }
            copy!!
            helper.check(copy.color == "yellow" && copy.name == "Camp") { "copy was $copy" }
            helper.check(copy.x == at.x && copy.z == at.z) { "copy moved: $copy" }
            helper.check(waypoints(owner).size == 1) { "owner should keep their waypoint" }
            helper.check(markerFor(helper.level, waypoints(friend).keys.single()) != null) {
                "shared copy should get its own marker entity"
            }
        }
        helper.succeed()
    }
}
