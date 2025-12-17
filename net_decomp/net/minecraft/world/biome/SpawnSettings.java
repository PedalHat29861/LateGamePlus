/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Keyable
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Map;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SpawnSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float field_30983 = 0.1f;
    public static final Pool<SpawnEntry> EMPTY_ENTRY_POOL = Pool.empty();
    public static final SpawnSettings INSTANCE = new Builder().build();
    public static final MapCodec<SpawnSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)0.9999999f).optionalFieldOf("creature_spawn_probability", (Object)Float.valueOf(0.1f)).forGetter(settings -> Float.valueOf(settings.creatureSpawnProbability)), (App)Codec.simpleMap(SpawnGroup.CODEC, (Codec)Pool.createCodec(SpawnEntry.CODEC).promotePartial(Util.addPrefix("Spawn data: ", arg_0 -> ((Logger)LOGGER).error(arg_0))), (Keyable)StringIdentifiable.toKeyable(SpawnGroup.values())).fieldOf("spawners").forGetter(settings -> settings.spawners), (App)Codec.simpleMap(Registries.ENTITY_TYPE.getCodec(), SpawnDensity.CODEC, Registries.ENTITY_TYPE).fieldOf("spawn_costs").forGetter(settings -> settings.spawnCosts)).apply((Applicative)instance, SpawnSettings::new));
    private final float creatureSpawnProbability;
    private final Map<SpawnGroup, Pool<SpawnEntry>> spawners;
    private final Map<EntityType<?>, SpawnDensity> spawnCosts;

    SpawnSettings(float creatureSpawnProbability, Map<SpawnGroup, Pool<SpawnEntry>> spawners, Map<EntityType<?>, SpawnDensity> spawnCosts) {
        this.creatureSpawnProbability = creatureSpawnProbability;
        this.spawners = ImmutableMap.copyOf(spawners);
        this.spawnCosts = ImmutableMap.copyOf(spawnCosts);
    }

    public Pool<SpawnEntry> getSpawnEntries(SpawnGroup spawnGroup) {
        return this.spawners.getOrDefault(spawnGroup, EMPTY_ENTRY_POOL);
    }

    public @Nullable SpawnDensity getSpawnDensity(EntityType<?> entityType) {
        return this.spawnCosts.get(entityType);
    }

    public float getCreatureSpawnProbability() {
        return this.creatureSpawnProbability;
    }

    public record SpawnDensity(double gravityLimit, double mass) {
        public static final Codec<SpawnDensity> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.DOUBLE.fieldOf("energy_budget").forGetter(spawnDensity -> spawnDensity.gravityLimit), (App)Codec.DOUBLE.fieldOf("charge").forGetter(spawnDensity -> spawnDensity.mass)).apply((Applicative)instance, SpawnDensity::new));

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SpawnDensity.class, "energyBudget;charge", "gravityLimit", "mass"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SpawnDensity.class, "energyBudget;charge", "gravityLimit", "mass"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SpawnDensity.class, "energyBudget;charge", "gravityLimit", "mass"}, this, object);
        }
    }

    public record SpawnEntry(EntityType<?> type, int minGroupSize, int maxGroupSize) {
        public static final MapCodec<SpawnEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Registries.ENTITY_TYPE.getCodec().fieldOf("type").forGetter(spawnEntry -> spawnEntry.type), (App)Codecs.POSITIVE_INT.fieldOf("minCount").forGetter(spawnEntry -> spawnEntry.minGroupSize), (App)Codecs.POSITIVE_INT.fieldOf("maxCount").forGetter(spawnEntry -> spawnEntry.maxGroupSize)).apply((Applicative)instance, SpawnEntry::new)).validate(spawnEntry -> {
            if (spawnEntry.minGroupSize > spawnEntry.maxGroupSize) {
                return DataResult.error(() -> "minCount needs to be smaller or equal to maxCount");
            }
            return DataResult.success((Object)spawnEntry);
        });

        public SpawnEntry {
            type = type.getSpawnGroup() == SpawnGroup.MISC ? EntityType.PIG : type;
        }

        @Override
        public String toString() {
            return String.valueOf(EntityType.getId(this.type)) + "*(" + this.minGroupSize + "-" + this.maxGroupSize + ")";
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SpawnEntry.class, "type;minCount;maxCount", "type", "minGroupSize", "maxGroupSize"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SpawnEntry.class, "type;minCount;maxCount", "type", "minGroupSize", "maxGroupSize"}, this, object);
        }
    }

    public static class Builder {
        private final Map<SpawnGroup, Pool.Builder<SpawnEntry>> spawners = Util.mapEnum(SpawnGroup.class, group -> Pool.builder());
        private final Map<EntityType<?>, SpawnDensity> spawnCosts = Maps.newLinkedHashMap();
        private float creatureSpawnProbability = 0.1f;

        public Builder spawn(SpawnGroup spawnGroup, int weight, SpawnEntry entry) {
            this.spawners.get(spawnGroup).add(entry, weight);
            return this;
        }

        public Builder spawnCost(EntityType<?> entityType, double mass, double gravityLimit) {
            this.spawnCosts.put(entityType, new SpawnDensity(gravityLimit, mass));
            return this;
        }

        public Builder creatureSpawnProbability(float probability) {
            this.creatureSpawnProbability = probability;
            return this;
        }

        public SpawnSettings build() {
            return new SpawnSettings(this.creatureSpawnProbability, (Map)this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, spawner -> ((Pool.Builder)spawner.getValue()).build())), (Map<EntityType<?>, SpawnDensity>)ImmutableMap.copyOf(this.spawnCosts));
        }
    }
}

