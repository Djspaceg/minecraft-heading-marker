package com.djspaceg.headingmarker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeadingMarkerCommandsTest {

    @Test
    public void testHmRegistrationContainsExpectedLiterals() {
        // Verify expected subcommand list constant is present and correct
        List<String> expected = List.of("help", "showdistance", "list", "remove", "set");
        assertTrue(HeadingMarkerCommands.EXPECTED_SUBCOMMANDS.containsAll(expected), "Expected subcommands constant to contain: " + expected);

        // Exercise idempotent/merge behavior: register twice and ensure all children are present
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
        HeadingMarkerCommands.register(dispatcher, null, null);
        // Register again to simulate duplicate registration path
        HeadingMarkerCommands.register(dispatcher, null, null);

        java.util.Set<String> present = dispatcher.getRoot().getChildren().stream()
            .filter(n -> "hm".equals(n.getName()))
            .flatMap(n -> n.getChildren().stream())
            .map(CommandNode::getName)
            .collect(java.util.stream.Collectors.toSet());
        assertTrue(present.containsAll(expected), "After duplicate register, /hm should still contain all subcommands: " + expected);
    }
}
