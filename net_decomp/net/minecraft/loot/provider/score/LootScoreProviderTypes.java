/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.provider.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.loot.provider.score.ContextLootScoreProvider;
import net.minecraft.loot.provider.score.FixedLootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class LootScoreProviderTypes {
    private static final Codec<LootScoreProvider> BASE_CODEC = Registries.LOOT_SCORE_PROVIDER_TYPE.getCodec().dispatch(LootScoreProvider::getType, LootScoreProviderType::codec);
    public static final Codec<LootScoreProvider> CODEC = Codec.lazyInitialized(() -> Codec.either(ContextLootScoreProvider.INLINE_CODEC, BASE_CODEC).xmap(Either::unwrap, provider -> {
        Either either;
        if (provider instanceof ContextLootScoreProvider) {
            ContextLootScoreProvider contextLootScoreProvider = (ContextLootScoreProvider)provider;
            either = Either.left((Object)contextLootScoreProvider);
        } else {
            either = Either.right((Object)provider);
        }
        return either;
    }));
    public static final LootScoreProviderType FIXED = LootScoreProviderTypes.register("fixed", FixedLootScoreProvider.CODEC);
    public static final LootScoreProviderType CONTEXT = LootScoreProviderTypes.register("context", ContextLootScoreProvider.CODEC);

    private static LootScoreProviderType register(String id, MapCodec<? extends LootScoreProvider> codec) {
        return Registry.register(Registries.LOOT_SCORE_PROVIDER_TYPE, Identifier.ofVanilla(id), new LootScoreProviderType(codec));
    }
}

