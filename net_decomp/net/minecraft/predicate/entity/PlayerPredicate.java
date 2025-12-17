/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMaps
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.predicate.entity.InputPredicate;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameModeList;
import org.jspecify.annotations.Nullable;

public record PlayerPredicate(NumberRange.IntRange experienceLevel, GameModeList gameMode, List<StatMatcher<?>> stats, Object2BooleanMap<RegistryKey<Recipe<?>>> recipes, Map<Identifier, AdvancementPredicate> advancements, Optional<EntityPredicate> lookingAt, Optional<InputPredicate> input) implements EntitySubPredicate
{
    public static final int LOOKING_AT_DISTANCE = 100;
    public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)NumberRange.IntRange.CODEC.optionalFieldOf("level", (Object)NumberRange.IntRange.ANY).forGetter(PlayerPredicate::experienceLevel), (App)GameModeList.CODEC.optionalFieldOf("gamemode", (Object)GameModeList.ALL).forGetter(PlayerPredicate::gameMode), (App)StatMatcher.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(PlayerPredicate::stats), (App)Codecs.object2BooleanMap(Recipe.KEY_CODEC).optionalFieldOf("recipes", (Object)Object2BooleanMaps.emptyMap()).forGetter(PlayerPredicate::recipes), (App)Codec.unboundedMap(Identifier.CODEC, AdvancementPredicate.CODEC).optionalFieldOf("advancements", Map.of()).forGetter(PlayerPredicate::advancements), (App)EntityPredicate.CODEC.optionalFieldOf("looking_at").forGetter(PlayerPredicate::lookingAt), (App)InputPredicate.CODEC.optionalFieldOf("input").forGetter(PlayerPredicate::input)).apply((Applicative)instance, PlayerPredicate::new));

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (!(entity instanceof ServerPlayerEntity)) {
            return false;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
        if (!this.experienceLevel.test(serverPlayerEntity.experienceLevel)) {
            return false;
        }
        if (!this.gameMode.contains(serverPlayerEntity.getGameMode())) {
            return false;
        }
        ServerStatHandler statHandler = serverPlayerEntity.getStatHandler();
        for (StatMatcher<?> statMatcher : this.stats) {
            if (statMatcher.test(statHandler)) continue;
            return false;
        }
        ServerRecipeBook serverRecipeBook = serverPlayerEntity.getRecipeBook();
        for (Object2BooleanMap.Entry entry : this.recipes.object2BooleanEntrySet()) {
            if (serverRecipeBook.isUnlocked((RegistryKey)entry.getKey()) == entry.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancementTracker playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
            ServerAdvancementLoader serverAdvancementLoader = serverPlayerEntity.getEntityWorld().getServer().getAdvancementLoader();
            for (Map.Entry<Identifier, AdvancementPredicate> entry2 : this.advancements.entrySet()) {
                AdvancementEntry advancementEntry = serverAdvancementLoader.get(entry2.getKey());
                if (advancementEntry != null && entry2.getValue().test(playerAdvancementTracker.getProgress(advancementEntry))) continue;
                return false;
            }
        }
        if (this.lookingAt.isPresent()) {
            Vec3d vec3d = serverPlayerEntity.getEyePos();
            Vec3d vec3d2 = serverPlayerEntity.getRotationVec(1.0f);
            Vec3d vec3d3 = vec3d.add(vec3d2.x * 100.0, vec3d2.y * 100.0, vec3d2.z * 100.0);
            EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(serverPlayerEntity.getEntityWorld(), serverPlayerEntity, vec3d, vec3d3, new Box(vec3d, vec3d3).expand(1.0), hitEntity -> !hitEntity.isSpectator(), 0.0f);
            if (entityHitResult == null || entityHitResult.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            Entity entity2 = entityHitResult.getEntity();
            if (!this.lookingAt.get().test(serverPlayerEntity, entity2) || !serverPlayerEntity.canSee(entity2)) {
                return false;
            }
        }
        return !this.input.isPresent() || this.input.get().matches(serverPlayerEntity.getPlayerInput());
    }

    public MapCodec<PlayerPredicate> getCodec() {
        return EntitySubPredicateTypes.PLAYER;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PlayerPredicate.class, "level;gameType;stats;recipes;advancements;lookingAt;input", "experienceLevel", "gameMode", "stats", "recipes", "advancements", "lookingAt", "input"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PlayerPredicate.class, "level;gameType;stats;recipes;advancements;lookingAt;input", "experienceLevel", "gameMode", "stats", "recipes", "advancements", "lookingAt", "input"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PlayerPredicate.class, "level;gameType;stats;recipes;advancements;lookingAt;input", "experienceLevel", "gameMode", "stats", "recipes", "advancements", "lookingAt", "input"}, this, object);
    }

    record StatMatcher<T>(StatType<T> type, RegistryEntry<T> value, NumberRange.IntRange range, Supplier<Stat<T>> stat) {
        public static final Codec<StatMatcher<?>> CODEC = Registries.STAT_TYPE.getCodec().dispatch(StatMatcher::type, StatMatcher::createCodec);

        public StatMatcher(StatType<T> type, RegistryEntry<T> value, NumberRange.IntRange range) {
            this(type, value, range, (Supplier<Stat<T>>)Suppliers.memoize(() -> type.getOrCreateStat(value.value())));
        }

        private static <T> MapCodec<StatMatcher<T>> createCodec(StatType<T> type) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)type.getRegistry().getEntryCodec().fieldOf("stat").forGetter(StatMatcher::value), (App)NumberRange.IntRange.CODEC.optionalFieldOf("value", (Object)NumberRange.IntRange.ANY).forGetter(StatMatcher::range)).apply((Applicative)instance, (value, range) -> new StatMatcher(type, value, (NumberRange.IntRange)range)));
        }

        public boolean test(StatHandler statHandler) {
            return this.range.test(statHandler.getStat(this.stat.get()));
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public static final Codec<AdvancementPredicate> CODEC = Codec.either(CompletedAdvancementPredicate.CODEC, AdvancementCriteriaPredicate.CODEC).xmap(Either::unwrap, predicate -> {
            if (predicate instanceof CompletedAdvancementPredicate) {
                CompletedAdvancementPredicate completedAdvancementPredicate = (CompletedAdvancementPredicate)predicate;
                return Either.left((Object)completedAdvancementPredicate);
            }
            if (predicate instanceof AdvancementCriteriaPredicate) {
                AdvancementCriteriaPredicate advancementCriteriaPredicate = (AdvancementCriteriaPredicate)predicate;
                return Either.right((Object)advancementCriteriaPredicate);
            }
            throw new UnsupportedOperationException();
        });
    }

    public static class Builder {
        private NumberRange.IntRange experienceLevel = NumberRange.IntRange.ANY;
        private GameModeList gameMode = GameModeList.ALL;
        private final ImmutableList.Builder<StatMatcher<?>> stats = ImmutableList.builder();
        private final Object2BooleanMap<RegistryKey<Recipe<?>>> recipes = new Object2BooleanOpenHashMap();
        private final Map<Identifier, AdvancementPredicate> advancements = Maps.newHashMap();
        private Optional<EntityPredicate> lookingAt = Optional.empty();
        private Optional<InputPredicate> input = Optional.empty();

        public static Builder create() {
            return new Builder();
        }

        public Builder experienceLevel(NumberRange.IntRange experienceLevel) {
            this.experienceLevel = experienceLevel;
            return this;
        }

        public <T> Builder stat(StatType<T> statType, RegistryEntry.Reference<T> value, NumberRange.IntRange range) {
            this.stats.add(new StatMatcher<T>(statType, value, range));
            return this;
        }

        public Builder recipe(RegistryKey<Recipe<?>> recipeKey, boolean unlocked) {
            this.recipes.put(recipeKey, unlocked);
            return this;
        }

        public Builder gameMode(GameModeList gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder lookingAt(EntityPredicate.Builder lookingAt) {
            this.lookingAt = Optional.of(lookingAt.build());
            return this;
        }

        public Builder advancement(Identifier id, boolean done) {
            this.advancements.put(id, new CompletedAdvancementPredicate(done));
            return this;
        }

        public Builder advancement(Identifier id, Map<String, Boolean> criteria) {
            this.advancements.put(id, new AdvancementCriteriaPredicate((Object2BooleanMap<String>)new Object2BooleanOpenHashMap(criteria)));
            return this;
        }

        public Builder input(InputPredicate input) {
            this.input = Optional.of(input);
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.experienceLevel, this.gameMode, (List<StatMatcher<?>>)this.stats.build(), this.recipes, this.advancements, this.lookingAt, this.input);
        }
    }

    record AdvancementCriteriaPredicate(Object2BooleanMap<String> criteria) implements AdvancementPredicate
    {
        public static final Codec<AdvancementCriteriaPredicate> CODEC = Codecs.object2BooleanMap(Codec.STRING).xmap(AdvancementCriteriaPredicate::new, AdvancementCriteriaPredicate::criteria);

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            for (Object2BooleanMap.Entry entry : this.criteria.object2BooleanEntrySet()) {
                CriterionProgress criterionProgress = advancementProgress.getCriterionProgress((String)entry.getKey());
                if (criterionProgress != null && criterionProgress.isObtained() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{AdvancementCriteriaPredicate.class, "criterions", "criteria"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{AdvancementCriteriaPredicate.class, "criterions", "criteria"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{AdvancementCriteriaPredicate.class, "criterions", "criteria"}, this, object);
        }

        @Override
        public /* synthetic */ boolean test(Object progress) {
            return this.test((AdvancementProgress)progress);
        }
    }

    record CompletedAdvancementPredicate(boolean done) implements AdvancementPredicate
    {
        public static final Codec<CompletedAdvancementPredicate> CODEC = Codec.BOOL.xmap(CompletedAdvancementPredicate::new, CompletedAdvancementPredicate::done);

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            return advancementProgress.isDone() == this.done;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CompletedAdvancementPredicate.class, "state", "done"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CompletedAdvancementPredicate.class, "state", "done"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CompletedAdvancementPredicate.class, "state", "done"}, this, object);
        }

        @Override
        public /* synthetic */ boolean test(Object progress) {
            return this.test((AdvancementProgress)progress);
        }
    }
}

