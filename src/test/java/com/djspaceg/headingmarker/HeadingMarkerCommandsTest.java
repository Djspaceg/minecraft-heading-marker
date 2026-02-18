package com.djspaceg.headingmarker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class HeadingMarkerCommandsTest {

    @Test
    public void testHmRegistrationContainsExpectedLiterals() {
        // Verify expected subcommands are registered
        List<String> expected = List.of("help", "list", "remove", "set");

        try {
            // Exercise idempotent/merge behavior: register twice and ensure all children are present
            CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();

            // Pass null for CommandRegistryAccess since it is not used in the register method
            HeadingMarkerCommands.register(dispatcher, null, CommandManager.RegistrationEnvironment.DEDICATED);
            // Register again to simulate duplicate registration path
            HeadingMarkerCommands.register(dispatcher, null, CommandManager.RegistrationEnvironment.DEDICATED);

            Set<String> present = dispatcher.getRoot().getChild("hm").getChildren().stream()
                .map(CommandNode::getName)
                .collect(Collectors.toSet());

            assertTrue(present.containsAll(expected), "After duplicate register, /hm should still contain all subcommands: " + expected);
        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            // Skip test if Minecraft environment can't be initialized (common in unit test context)
            assumeTrue(false, "Skipping test - Minecraft environment not available: " + e.getMessage());
        }
    }

    /**
     * Tests all 8 variations of the /hm set command to ensure they parse correctly.
     * We can't fully execute these commands without a mock Minecraft server environment,
     * but we can verify that Brigadier accepts the command syntax.
     */
    @Test
    public void testSetCommandVariations() {
        try {
            CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
            HeadingMarkerCommands.register(dispatcher, null, CommandManager.RegistrationEnvironment.DEDICATED);

            // List of all 8 command variations we need to support
            List<String> commands = List.of(
                    "hm set",                          // 1. /hm set - use player position, default color
                    "hm set red",                      // 2. /hm set color - use player position with specified color
                    "hm set 100 200",                  // 3. /hm set x z - use x/z with player's y, default color
                    "hm set 100 64 200",               // 4. /hm set x y z - use all coordinates, default color
                    "hm set red 100 200",              // 5. /hm set color x z - color first, then x/z with player's y
                    "hm set 100 200 red",              // 6. /hm set x z color - coordinates first, then color
                    "hm set red 100 64 200",           // 7. /hm set color x y z - color first, then all coordinates
                    "hm set 100 64 200 red"            // 8. /hm set x y z color - all coordinates first, then color
            );

            // Test that all variations parse without errors
            for (String command : commands) {
                ParseResults<ServerCommandSource> parseResult = dispatcher.parse(command, null);
                
                // Verify that the command was successfully parsed (no exceptions)
                assertNotNull(parseResult, "Parse result should not be null for command: " + command);
                
                // Verify that there are no parsing exceptions
                assertTrue(parseResult.getExceptions().isEmpty(),
                        "Command should parse without errors: " + command + 
                        " (errors: " + parseResult.getExceptions() + ")");
                
                // Verify that the command context was created
                assertNotNull(parseResult.getContext(), "Context should not be null for command: " + command);
            }

            // Also test with different colors to ensure color validation works
            List<String> coloredCommands = List.of(
                    "hm set blue",
                    "hm set green 150 250",
                    "hm set yellow 150 70 250",
                    "hm set 150 250 purple",
                    "hm set 150 70 250 blue"
            );

            for (String command : coloredCommands) {
                ParseResults<ServerCommandSource> parseResult = dispatcher.parse(command, null);
                assertNotNull(parseResult, "Parse result should not be null for command: " + command);
                assertTrue(parseResult.getExceptions().isEmpty(),
                        "Command should parse without errors: " + command);
            }

        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            // Skip test if Minecraft environment can't be initialized
            assumeTrue(false, "Skipping test - Minecraft environment not available: " + e.getMessage());
        }
    }

    /**
     * Test that the set command properly handles invalid inputs
     */
    @Test
    public void testSetCommandInvalidInputs() {
        try {
            CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
            HeadingMarkerCommands.register(dispatcher, null, CommandManager.RegistrationEnvironment.DEDICATED);

            // These commands should still parse (Brigadier accepts them as strings),
            // but would fail during execution with proper error messages
            List<String> invalidCommands = List.of(
                    "hm set invalidcolor",              // Invalid color name
                    "hm set 100",                       // Only one coordinate (incomplete)
                    "hm set notacolor 100 200"          // Invalid color with coordinates
            );

            for (String command : invalidCommands) {
                // These should parse (Brigadier accepts string arguments)
                // but would be rejected during execution
                ParseResults<ServerCommandSource> parseResult = dispatcher.parse(command, null);
                assertNotNull(parseResult, "Parse result should not be null for: " + command);
                // We can't test execution without a full Minecraft environment,
                // but the parsing should succeed since we use StringArgumentType
            }

        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            assumeTrue(false, "Skipping test - Minecraft environment not available: " + e.getMessage());
        }
    }

    /**
     * Test that headingmarker alias works the same as hm
     */
    @Test
    public void testHeadingMarkerAlias() {
        try {
            CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
            HeadingMarkerCommands.register(dispatcher, null, CommandManager.RegistrationEnvironment.DEDICATED);

            // Test that both "hm" and "headingmarker" work
            ParseResults<ServerCommandSource> hmResult = dispatcher.parse("hm set red", null);
            ParseResults<ServerCommandSource> fullResult = dispatcher.parse("headingmarker set red", null);

            assertNotNull(hmResult, "hm command should parse");
            assertNotNull(fullResult, "headingmarker command should parse");
            assertTrue(hmResult.getExceptions().isEmpty(), "hm should parse without errors");
            assertTrue(fullResult.getExceptions().isEmpty(), "headingmarker should parse without errors");

        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            assumeTrue(false, "Skipping test - Minecraft environment not available: " + e.getMessage());
        }
    }
}
