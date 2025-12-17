/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ScoreboardSlotArgumentType
implements ArgumentType<ScoreboardDisplaySlot> {
    private static final Collection<String> EXAMPLES = Arrays.asList("sidebar", "foo.bar");
    public static final DynamicCommandExceptionType INVALID_SLOT_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("argument.scoreboardDisplaySlot.invalid", name));

    private ScoreboardSlotArgumentType() {
    }

    public static ScoreboardSlotArgumentType scoreboardSlot() {
        return new ScoreboardSlotArgumentType();
    }

    public static ScoreboardDisplaySlot getScoreboardSlot(CommandContext<ServerCommandSource> context, String name) {
        return (ScoreboardDisplaySlot)context.getArgument(name, ScoreboardDisplaySlot.class);
    }

    public ScoreboardDisplaySlot parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        ScoreboardDisplaySlot scoreboardDisplaySlot = ScoreboardDisplaySlot.CODEC.byId(string);
        if (scoreboardDisplaySlot == null) {
            throw INVALID_SLOT_EXCEPTION.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
        return scoreboardDisplaySlot;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(ScoreboardDisplaySlot.values()).map(ScoreboardDisplaySlot::asString), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

