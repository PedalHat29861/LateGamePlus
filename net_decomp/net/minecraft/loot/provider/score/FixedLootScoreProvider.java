/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
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

public record FixedLootScoreProvider(String name) implements LootScoreProvider
{
    public static final MapCodec<FixedLootScoreProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(FixedLootScoreProvider::name)).apply((Applicative)instance, FixedLootScoreProvider::new));

    public static LootScoreProvider create(String name) {
        return new FixedLootScoreProvider(name);
    }

    @Override
    public LootScoreProviderType getType() {
        return LootScoreProviderTypes.FIXED;
    }

    @Override
    public ScoreHolder getScoreHolder(LootContext context) {
        return ScoreHolder.fromName(this.name);
    }

    @Override
    public Set<ContextParameter<?>> getRequiredParameters() {
        return Set.of();
    }
}

