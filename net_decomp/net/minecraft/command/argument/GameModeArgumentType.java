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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class GameModeArgumentType
implements ArgumentType<GameMode> {
    private static final Collection<String> EXAMPLES = Stream.of(GameMode.SURVIVAL, GameMode.CREATIVE).map(GameMode::getId).collect(Collectors.toList());
    private static final GameMode[] VALUES = GameMode.values();
    private static final DynamicCommandExceptionType INVALID_GAME_MODE_EXCEPTION = new DynamicCommandExceptionType(gameMode -> Text.stringifiedTranslatable("argument.gamemode.invalid", gameMode));

    public GameMode parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        GameMode gameMode = GameMode.byId(string, null);
        if (gameMode == null) {
            throw INVALID_GAME_MODE_EXCEPTION.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
        return gameMode;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof CommandSource) {
            return CommandSource.suggestMatching(Arrays.stream(VALUES).map(GameMode::getId), builder);
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static GameModeArgumentType gameMode() {
        return new GameModeArgumentType();
    }

    public static GameMode getGameMode(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return (GameMode)context.getArgument(name, GameMode.class);
    }

    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

