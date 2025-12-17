/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.command.argument;

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
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ScoreboardObjectiveArgumentType
implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
    private static final DynamicCommandExceptionType UNKNOWN_OBJECTIVE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("arguments.objective.notFound", name));
    private static final DynamicCommandExceptionType READONLY_OBJECTIVE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("arguments.objective.readonly", name));

    public static ScoreboardObjectiveArgumentType scoreboardObjective() {
        return new ScoreboardObjectiveArgumentType();
    }

    public static ScoreboardObjective getObjective(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        String string = (String)context.getArgument(name, String.class);
        ServerScoreboard scoreboard = ((ServerCommandSource)context.getSource()).getServer().getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(string);
        if (scoreboardObjective == null) {
            throw UNKNOWN_OBJECTIVE_EXCEPTION.create((Object)string);
        }
        return scoreboardObjective;
    }

    public static ScoreboardObjective getWritableObjective(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        ScoreboardObjective scoreboardObjective = ScoreboardObjectiveArgumentType.getObjective(context, name);
        if (scoreboardObjective.getCriterion().isReadOnly()) {
            throw READONLY_OBJECTIVE_EXCEPTION.create((Object)scoreboardObjective.getName());
        }
        return scoreboardObjective;
    }

    public String parse(StringReader stringReader) throws CommandSyntaxException {
        return stringReader.readUnquotedString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Object object = context.getSource();
        if (object instanceof ServerCommandSource) {
            ServerCommandSource serverCommandSource = (ServerCommandSource)object;
            return CommandSource.suggestMatching(serverCommandSource.getServer().getScoreboard().getObjectiveNames(), builder);
        }
        if (object instanceof CommandSource) {
            CommandSource commandSource = (CommandSource)object;
            return commandSource.getCompletions(context);
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

