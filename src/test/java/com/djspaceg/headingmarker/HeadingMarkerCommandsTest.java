package com.djspaceg.headingmarker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeadingMarkerCommandsTest {

    @Test
    public void testHmRegistrationContainsExpectedLiterals() {
        // Verify expected subcommands are registered
        List<String> expected = List.of("help", "list", "remove", "set");

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
    }
}
