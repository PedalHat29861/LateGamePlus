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
package net.minecraft.loot.provider.number;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.loot.provider.score.ContextLootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProviderTypes;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.context.ContextParameter;

public record ScoreLootNumberProvider(LootScoreProvider target, String score, float scale) implements LootNumberProvider
{
    public static final MapCodec<ScoreLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootScoreProviderTypes.CODEC.fieldOf("target").forGetter(ScoreLootNumberProvider::target), (App)Codec.STRING.fieldOf("score").forGetter(ScoreLootNumberProvider::score), (App)Codec.FLOAT.fieldOf("scale").orElse((Object)Float.valueOf(1.0f)).forGetter(ScoreLootNumberProvider::scale)).apply((Applicative)instance, ScoreLootNumberProvider::new));

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.SCORE;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return this.target.getRequiredParameters();
    }

    public static ScoreLootNumberProvider create(LootContext.EntityReference target, String score) {
        return ScoreLootNumberProvider.create(target, score, 1.0f);
    }

    public static ScoreLootNumberProvider create(LootContext.EntityReference target, String score, float scale) {
        return new ScoreLootNumberProvider(ContextLootScoreProvider.create(target), score, scale);
    }

    @Override
    public float nextFloat(LootContext context) {
        ScoreHolder scoreHolder = this.target.getScoreHolder(context);
        if (scoreHolder == null) {
            return 0.0f;
        }
        ServerScoreboard scoreboard = context.getWorld().getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(this.score);
        if (scoreboardObjective == null) {
            return 0.0f;
        }
        ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore(scoreHolder, scoreboardObjective);
        if (readableScoreboardScore == null) {
            return 0.0f;
        }
        return (float)readableScoreboardScore.getScore() * this.scale;
    }
}

