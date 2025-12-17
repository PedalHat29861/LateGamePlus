/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SuggestionProviders {
    private static final Map<Identifier, SuggestionProvider<CommandSource>> REGISTRY = new HashMap<Identifier, SuggestionProvider<CommandSource>>();
    private static final Identifier ASK_SERVER_ID = Identifier.ofVanilla("ask_server");
    public static final SuggestionProvider<CommandSource> ASK_SERVER = SuggestionProviders.register(ASK_SERVER_ID, (SuggestionProvider<CommandSource>)((SuggestionProvider)(context, builder) -> ((CommandSource)context.getSource()).getCompletions(context)));
    public static final SuggestionProvider<CommandSource> AVAILABLE_SOUNDS = SuggestionProviders.register(Identifier.ofVanilla("available_sounds"), (SuggestionProvider<CommandSource>)((SuggestionProvider)(context, builder) -> CommandSource.suggestIdentifiers(((CommandSource)context.getSource()).getSoundIds(), builder)));
    public static final SuggestionProvider<CommandSource> SUMMONABLE_ENTITIES = SuggestionProviders.register(Identifier.ofVanilla("summonable_entities"), (SuggestionProvider<CommandSource>)((SuggestionProvider)(context, builder) -> CommandSource.suggestFromIdentifier(Registries.ENTITY_TYPE.stream().filter(entityType -> entityType.isEnabled(((CommandSource)context.getSource()).getEnabledFeatures()) && entityType.isSummonable()), builder, EntityType::getId, EntityType::getName)));

    public static <S extends CommandSource> SuggestionProvider<S> register(Identifier id, SuggestionProvider<CommandSource> provider) {
        SuggestionProvider<CommandSource> suggestionProvider = REGISTRY.putIfAbsent(id, provider);
        if (suggestionProvider != null) {
            throw new IllegalArgumentException("A command suggestion provider is already registered with the name '" + String.valueOf(id) + "'");
        }
        return new LocalProvider(id, provider);
    }

    public static <S extends CommandSource> SuggestionProvider<S> cast(SuggestionProvider<CommandSource> suggestionProvider) {
        return suggestionProvider;
    }

    public static <S extends CommandSource> SuggestionProvider<S> byId(Identifier id) {
        return SuggestionProviders.cast(REGISTRY.getOrDefault(id, ASK_SERVER));
    }

    public static Identifier computeId(SuggestionProvider<?> provider) {
        Identifier identifier;
        if (provider instanceof LocalProvider) {
            LocalProvider localProvider = (LocalProvider)provider;
            identifier = localProvider.id;
        } else {
            identifier = ASK_SERVER_ID;
        }
        return identifier;
    }

    static final class LocalProvider
    extends Record
    implements SuggestionProvider<CommandSource> {
        final Identifier id;
        private final SuggestionProvider<CommandSource> provider;

        LocalProvider(Identifier id, SuggestionProvider<CommandSource> provider) {
            this.id = id;
            this.provider = provider;
        }

        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return this.provider.getSuggestions(context, builder);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LocalProvider.class, "name;delegate", "id", "provider"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LocalProvider.class, "name;delegate", "id", "provider"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LocalProvider.class, "name;delegate", "id", "provider"}, this, object);
        }

        public Identifier id() {
            return this.id;
        }

        public SuggestionProvider<CommandSource> provider() {
            return this.provider;
        }
    }
}

