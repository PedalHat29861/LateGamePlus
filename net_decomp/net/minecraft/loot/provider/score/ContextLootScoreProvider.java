/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.loot.provider.score;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderTypes;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.util.context.ContextParameter;
import org.jspecify.annotations.Nullable;

public record ContextLootScoreProvider(LootContext.EntityReference target) implements LootScoreProvider
{
    public static final MapCodec<ContextLootScoreProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootContext.EntityReference.CODEC.fieldOf("target").forGetter(ContextLootScoreProvider::target)).apply((Applicative)instance, ContextLootScoreProvider::new));
    public static final Codec<ContextLootScoreProvider> INLINE_CODEC = LootContext.EntityReference.CODEC.xmap(ContextLootScoreProvider::new, ContextLootScoreProvider::target);

    public static LootScoreProvider create(LootContext.EntityReference target) {
        return new ContextLootScoreProvider(target);
    }

    @Override
    public LootScoreProviderType getType() {
        return LootScoreProviderTypes.CONTEXT;
    }

    @Override
    public @Nullable ScoreHolder getScoreHolder(LootContext context) {
        return context.get(this.target.contextParam());
    }

    @Override
    public Set<ContextParameter<?>> getRequiredParameters() {
        return Set.of(this.target.contextParam());
    }
}

