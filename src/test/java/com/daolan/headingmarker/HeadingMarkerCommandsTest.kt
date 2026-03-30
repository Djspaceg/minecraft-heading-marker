package com.daolan.headingmarker

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

class HeadingMarkerCommandsTest {

    private fun withDispatcher(block: (CommandDispatcher<CommandSourceStack>) -> Unit) {
        try {
            val dispatcher = CommandDispatcher<CommandSourceStack>()
            HeadingMarkerCommands.register(dispatcher, null, Commands.CommandSelection.DEDICATED)
            block(dispatcher)
        } catch (e: ExceptionInInitializerError) {
            assumeTrue(false, "Skipping - Minecraft environment not available: ${e.message}")
        } catch (e: NoClassDefFoundError) {
            assumeTrue(false, "Skipping - Minecraft environment not available: ${e.message}")
        }
    }

    private fun assertParses(dispatcher: CommandDispatcher<CommandSourceStack>, command: String) {
        val result = dispatcher.parse(command, null)
        assertNotNull(result, "Parse result should not be null for: $command")
        assertTrue(
            result.exceptions.isEmpty(),
            "Should parse without errors: $command (errors: ${result.exceptions})",
        )
        assertNotNull(result.context, "Context should not be null for: $command")
    }

    @Test
    fun `hm registration contains expected subcommands`() = withDispatcher { dispatcher ->
        val expected =
            listOf("help", "list", "remove", "set", "clear", "clearall", "share", "purge")

        // Register twice to test idempotent behavior
        HeadingMarkerCommands.register(dispatcher, null, Commands.CommandSelection.DEDICATED)

        val present = dispatcher.root.getChild("hm").children.map { it.name }.toSet()
        assertTrue(present.containsAll(expected), "Missing subcommands: ${expected - present}")
    }

    @Test
    fun `all set command variations parse correctly`() = withDispatcher { dispatcher ->
        val commands =
            listOf(
                "hm set", // player pos, auto color
                "hm set red", // player pos, specified color
                "hm set 100 200", // x z, auto color
                "hm set 100 64 200", // x y z, auto color
                "hm set red 100 200", // color x z
                "hm set 100 200 red", // x z color
                "hm set red 100 64 200", // color x y z
                "hm set 100 64 200 red", // x y z color
            )
        commands.forEach { assertParses(dispatcher, it) }
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
    fun `invalid set inputs still parse at brigadier level`() = withDispatcher { dispatcher ->
        // These parse (Brigadier accepts string args) but would fail at execution
        val commands = listOf("hm set invalidcolor", "hm set 100", "hm set notacolor 100 200")
        commands.forEach { cmd ->
            val result = dispatcher.parse(cmd, null)
            assertNotNull(result, "Parse result should not be null for: $cmd")
        }
    }

    @Test
    fun `headingmarker alias works`() = withDispatcher { dispatcher ->
        assertParses(dispatcher, "hm set red")
        assertParses(dispatcher, "headingmarker set red")
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
                "headingmarker share SomePlayer red",
            )
            .forEach { assertParses(dispatcher, it) }
    }

    @Test
    fun `rename commands parse correctly`() = withDispatcher { dispatcher ->
        listOf(
                "hm rename red", // clear name
                "hm rename red Home Base", // name with spaces
                "hm rename blue My Favorite Spot", // greedy string
                "headingmarker rename green Mine", // alias
            )
            .forEach { assertParses(dispatcher, it) }
    }
}
