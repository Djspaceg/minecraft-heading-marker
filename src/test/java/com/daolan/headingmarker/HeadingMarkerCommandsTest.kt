package com.daolan.headingmarker

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class HeadingMarkerCommandsTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            // Commands' static initializer touches registries, so vanilla must be bootstrapped.
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private fun withDispatcher(block: (CommandDispatcher<CommandSourceStack>) -> Unit) {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        HeadingMarkerCommands.register(dispatcher, null, Commands.CommandSelection.DEDICATED)
        block(dispatcher)
    }

    private fun parse(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        command: String,
    ): ParseResults<CommandSourceStack> {
        val result = dispatcher.parse(command, null)
        assertNotNull(result, "Parse result should not be null for: $command")
        return result
    }

    private fun assertParses(dispatcher: CommandDispatcher<CommandSourceStack>, command: String) {
        val result = parse(dispatcher, command)
        assertTrue(
            result.exceptions.isEmpty(),
            "Should parse without errors: $command (errors: ${result.exceptions})",
        )
        assertFalse(result.reader.canRead(), "Input should be fully consumed: $command")
        assertNotNull(
            result.context.lastChild.command,
            "Should resolve to an executable command, not an incomplete one: $command",
        )
    }

    /** Asserts [command] resolves to an executable node reached via exactly [expectedPath]. */
    private fun assertResolvesTo(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        command: String,
        expectedPath: List<String>,
    ) {
        assertParses(dispatcher, command)
        val path = parse(dispatcher, command).context.lastChild.nodes.map { it.node.name }
        assertEquals(expectedPath, path, "Wrong branch chosen for: $command")
    }

    @Test
    fun `hm registration contains expected subcommands`() = withDispatcher { dispatcher ->
        val expected =
            listOf("help", "list", "remove", "set", "clear", "clearall", "rename", "share", "purge")

        // Register twice to test idempotent behavior
        HeadingMarkerCommands.register(dispatcher, null, Commands.CommandSelection.DEDICATED)

        val present = dispatcher.root.getChild("hm").children.map { it.name }.toSet()
        assertTrue(present.containsAll(expected), "Missing subcommands: ${expected - present}")
    }

    @Test
    fun `all set command variations resolve to the intended branch`() = withDispatcher { dispatcher ->
        val cases =
            listOf(
                "hm set" to listOf("hm", "set"), // player pos, auto color
                "hm set red" to listOf("hm", "set", "color"), // player pos, specified color
                "hm set 100 200" to listOf("hm", "set", "n1", "n2"), // x z, auto color
                "hm set 100 64 200" to listOf("hm", "set", "n1", "n2", "n3"), // x y z, auto color
                "hm set red 100 200" to listOf("hm", "set", "color", "n1", "n2"), // color x z
                "hm set 100 200 red" to listOf("hm", "set", "n1", "n2", "color"), // x z color
                "hm set red 100 64 200" to
                    listOf("hm", "set", "color", "n1", "n2", "n3"), // color x y z
                "hm set 100 64 200 red" to
                    listOf("hm", "set", "n1", "n2", "n3", "color"), // x y z color
                "hm set -100.5 64 200.25" to listOf("hm", "set", "n1", "n2", "n3"), // decimals
            )
        cases.forEach { (cmd, path) -> assertResolvesTo(dispatcher, cmd, path) }
    }

    @Test
    fun `set command works with all colors`() = withDispatcher { dispatcher ->
        val commands =
            listOf(
                "hm set blue",
                "hm set green 150 250",
                "hm set yellow 150 70 250",
                "hm set 150 250 purple",
                "hm set 150 70 250 blue",
            )
        commands.forEach { assertParses(dispatcher, it) }
    }

    @Test
    fun `lone coordinate resolves to a node that reports incomplete coordinates`() =
        withDispatcher { dispatcher ->
            assertResolvesTo(dispatcher, "hm set 100", listOf("hm", "set", "n1"))
        }

    @Test
    fun `invalid set inputs still parse at brigadier level`() = withDispatcher { dispatcher ->
        // These parse (Brigadier accepts string args) but fail at execution with a message
        val commands = listOf("hm set invalidcolor", "hm set notacolor 100 200")
        commands.forEach { cmd -> assertParses(dispatcher, cmd) }
    }

    @Test
    fun `headingmarker alias works`() = withDispatcher { dispatcher ->
        assertParses(dispatcher, "hm set red")
        assertParses(dispatcher, "headingmarker set red")
        assertParses(dispatcher, "headingmarker set 100 200")
    }

    @Test
    fun `clear commands parse correctly`() = withDispatcher { dispatcher ->
        listOf("hm clear", "hm clearall", "headingmarker clear", "headingmarker clearall").forEach {
            assertParses(dispatcher, it)
        }
    }

    @Test
    fun `share commands parse correctly`() = withDispatcher { dispatcher ->
        listOf(
                "hm share SomePlayer red",
                "hm share SomePlayer blue",
                "hm share SomePlayer green",
                "hm share SomePlayer yellow",
                "hm share SomePlayer purple",
                "hm share SomePlayer Home Base", // selector by multi-word name
                "headingmarker share SomePlayer red",
            )
            .forEach { assertParses(dispatcher, it) }
    }

    @Test
    fun `remove commands parse correctly`() = withDispatcher { dispatcher ->
        listOf("hm remove red", "hm remove a1b2c3d4", "hm remove Home Base").forEach {
            assertParses(dispatcher, it)
        }
    }

    @Test
    fun `rename commands parse correctly`() = withDispatcher { dispatcher ->
        listOf(
                "hm rename red", // clear name
                "hm rename red Home Base", // name with spaces
                "hm rename blue My Favorite Spot", // greedy string
                "hm rename \"Home Base\" Base", // quoted multi-word selector
                "headingmarker rename green Mine", // alias
            )
            .forEach { assertParses(dispatcher, it) }
    }
}
