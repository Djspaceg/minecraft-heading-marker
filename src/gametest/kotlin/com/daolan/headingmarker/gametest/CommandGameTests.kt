package com.daolan.headingmarker.gametest

import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.commands.CommandSource
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.Component

/** End-to-end command behavior: input handling and the feedback players see. */
class CommandGameTests {

    private class CapturingSource : CommandSource {
        val messages = mutableListOf<String>()

        override fun sendSystemMessage(message: Component) {
            messages += message.string
        }

        override fun acceptsSuccess() = true

        override fun acceptsFailure() = true

        override fun shouldInformAdmins() = false
    }

    @GameTest
    fun consoleGetsAnErrorForPlayerOnlyCommands(helper: GameTestHelper) {
        val server = helper.level.server
        val console = CapturingSource()
        server.commands.performPrefixedCommand(
            server.createCommandSourceStack().withSource(console),
            "hm list",
        )
        helper.check(console.messages.any { "player" in it.lowercase() }) {
            "console got no explanation, messages=${console.messages}"
        }
        helper.succeed()
    }

    @GameTest
    fun purgeNeedsGamemasterPermission(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_not_op")
        withPlayers(player) {
            TestPlayers.run(player, "hm purge")
            helper.check(player.chat.none { "orphaned" in it }) { "non-op ran purge: ${player.chat}" }

            val server = helper.level.server
            val console = CapturingSource()
            server.commands.performPrefixedCommand(
                server.createCommandSourceStack().withSource(console),
                "hm purge",
            )
            helper.check(console.messages.any { "orphaned" in it }) { "console=${console.messages}" }
        }
        helper.succeed()
    }

    @GameTest
    fun coordinatesOutsideTheWorldAreRejected(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_bounds")
        withPlayers(player) {
            TestPlayers.run(player, "hm set red 40000000 64 0")
            helper.check(waypoints(player).isEmpty()) { "created a waypoint outside the world" }
            helper.check(player.chat.any { "outside the world" in it }) { "chat=${player.chat}" }
        }
        helper.succeed()
    }

    @GameTest
    fun feedbackUsesBlockCoordinates(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_floor")
        withPlayers(player) {
            TestPlayers.run(player, "hm set red -10.5 64.9 -0.25")
            // F3 shows block -11, 64, -1 here; truncating toward zero said -10, 64, 0.
            helper.check(player.chat.any { "(-11, 64, -1)" in it }) { "chat=${player.chat}" }
        }
        helper.succeed()
    }

    @GameTest
    fun sharingTwiceDoesNotDuplicate(helper: GameTestHelper) {
        val owner = TestPlayers.join(helper, "hm_dupe_a")
        val friend = TestPlayers.join(helper, "hm_dupe_b")
        withPlayers(owner, friend) {
            TestPlayers.run(owner, "hm set blue ${coords(helper.spot(4.0, 2.0))}")
            TestPlayers.run(owner, "hm share hm_dupe_b blue")
            TestPlayers.run(owner, "hm share hm_dupe_b blue")
            helper.check(waypoints(friend).size == 1) { "friend has ${waypoints(friend).size} copies" }
            helper.check(owner.chat.any { "already has" in it }) { "chat=${owner.chat}" }
        }
        helper.succeed()
    }

    @GameTest
    fun quotedSelectorsWorkForRemove(helper: GameTestHelper) {
        val player = TestPlayers.join(helper, "hm_quotes")
        withPlayers(player) {
            TestPlayers.run(player, "hm set green ${coords(helper.spot(2.0, 4.0))}")
            TestPlayers.run(player, "hm rename green \"Home Base\"")
            TestPlayers.run(player, "hm remove \"Home Base\"")
            helper.check(waypoints(player).isEmpty()) { "chat=${player.chat}" }
        }
        helper.succeed()
    }
}
