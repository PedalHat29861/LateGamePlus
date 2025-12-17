/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.context.ContextParameter;

public record EntityScoresLootCondition(Map<String, BoundedIntUnaryOperator> scores, LootContext.EntityReference entity) implements LootCondition
{
    public static final MapCodec<EntityScoresLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.unboundedMap((Codec)Codec.STRING, BoundedIntUnaryOperator.CODEC).fieldOf("scores").forGetter(EntityScoresLootCondition::scores), (App)LootContext.EntityReference.CODEC.fieldOf("entity").forGetter(EntityScoresLootCondition::entity)).apply((Applicative)instance, EntityScoresLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ENTITY_SCORES;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return (Set)Stream.concat(Stream.of(this.entity.contextParam()), this.scores.values().stream().flatMap(operator -> operator.getRequiredParameters().stream())).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.get(this.entity.contextParam());
        if (entity == null) {
            return false;
        }
        ServerScoreboard scoreboard = lootContext.getWorld().getScoreboard();
        for (Map.Entry<String, BoundedIntUnaryOperator> entry : this.scores.entrySet()) {
            if (this.entityScoreIsInRange(lootContext, entity, scoreboard, entry.getKey(), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    protected boolean entityScoreIsInRange(LootContext context, Entity entity, Scoreboard scoreboard, String objectiveName, BoundedIntUnaryOperator range) {
        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(objectiveName);
        if (scoreboardObjective == null) {
            return false;
        }
        ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore(entity, scoreboardObjective);
        if (readableScoreboardScore == null) {
            return false;
        }
        return range.test(context, readableScoreboardScore.getScore());
    }

    public static Builder create(LootContext.EntityReference target) {
        return new Builder(target);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntityScoresLootCondition.class, "scores;entityTarget", "scores", "entity"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntityScoresLootCondition.class, "scores;entityTarget", "scores", "entity"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntityScoresLootCondition.class, "scores;entityTarget", "scores", "entity"}, this, object);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Builder
    implements LootCondition.Builder {
        private final ImmutableMap.Builder<String, BoundedIntUnaryOperator> scores = ImmutableMap.builder();
        private final LootContext.EntityReference target;

        public Builder(LootContext.EntityReference target) {
            this.target = target;
        }

        public Builder score(String name, BoundedIntUnaryOperator value) {
            this.scores.put((Object)name, (Object)value);
            return this;
        }

        @Override
        public LootCondition build() {
            return new EntityScoresLootCondition((Map<String, BoundedIntUnaryOperator>)this.scores.build(), this.target);
        }
    }
}

