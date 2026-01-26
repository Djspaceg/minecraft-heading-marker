package com.djspaceg.headingmarker;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ColorArgumentType implements ArgumentType<String> {
    private static final List<String> COLORS = Arrays.asList("red", "blue", "green", "yellow", "purple");
    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(o -> Text.literal("Unknown color: " + o));

    public static ColorArgumentType color() {
        return new ColorArgumentType();
    }

    public static List<String> getColors() {
        return COLORS;
    }

    public static String getColor(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String word = reader.readUnquotedString();
        if (!COLORS.contains(word.toLowerCase())) {
            throw UNKNOWN.create(word);
        }
        return word;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (String c : COLORS) builder.suggest(c);
        return builder.buildFuture();
    }
}
