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
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DimensionArgumentType
implements ArgumentType<Identifier> {
    private static final Collection<String> EXAMPLES = Stream.of(World.OVERWORLD, World.NETHER).map(key -> key.getValue().toString()).collect(Collectors.toList());
    private static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("argument.dimension.invalid", id));

    public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
        return Identifier.fromCommandInput(stringReader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof CommandSource) {
            return CommandSource.suggestIdentifiers(((CommandSource)context.getSource()).getWorldKeys().stream().map(RegistryKey::getValue), builder);
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static DimensionArgumentType dimension() {
        return new DimensionArgumentType();
    }

    public static ServerWorld getDimensionArgument(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = (Identifier)context.getArgument(name, Identifier.class);
        RegistryKey<World> registryKey = RegistryKey.of(RegistryKeys.WORLD, identifier);
        ServerWorld serverWorld = ((ServerCommandSource)context.getSource()).getServer().getWorld(registryKey);
        if (serverWorld == null) {
            throw INVALID_DIMENSION_EXCEPTION.create((Object)identifier);
        }
        return serverWorld;
    }

    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

