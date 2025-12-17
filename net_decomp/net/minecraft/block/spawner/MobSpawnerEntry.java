/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.block.spawner;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.entity.EquipmentTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public record MobSpawnerEntry(NbtCompound entity, Optional<CustomSpawnRules> customSpawnRules, Optional<EquipmentTable> equipment) {
    public static final String ENTITY_KEY = "entity";
    public static final Codec<MobSpawnerEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NbtCompound.CODEC.fieldOf(ENTITY_KEY).forGetter(entry -> entry.entity), (App)CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(entry -> entry.customSpawnRules), (App)EquipmentTable.CODEC.optionalFieldOf("equipment").forGetter(entry -> entry.equipment)).apply((Applicative)instance, MobSpawnerEntry::new));
    public static final Codec<Pool<MobSpawnerEntry>> DATA_POOL_CODEC = Pool.createCodec(CODEC);

    public MobSpawnerEntry() {
        this(new NbtCompound(), Optional.empty(), Optional.empty());
    }

    public MobSpawnerEntry {
        Optional<Identifier> optional = entity.get("id", Identifier.CODEC);
        if (optional.isPresent()) {
            entity.put("id", Identifier.CODEC, optional.get());
        } else {
            entity.remove("id");
        }
    }

    public NbtCompound getNbt() {
        return this.entity;
    }

    public Optional<CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public Optional<EquipmentTable> getEquipment() {
        return this.equipment;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{MobSpawnerEntry.class, "entityToSpawn;customSpawnRules;equipment", "entity", "customSpawnRules", "equipment"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MobSpawnerEntry.class, "entityToSpawn;customSpawnRules;equipment", "entity", "customSpawnRules", "equipment"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MobSpawnerEntry.class, "entityToSpawn;customSpawnRules;equipment", "entity", "customSpawnRules", "equipment"}, this, o);
    }

    public record CustomSpawnRules(Range<Integer> blockLightLimit, Range<Integer> skyLightLimit) {
        private static final Range<Integer> DEFAULT = new Range<Integer>(0, 15);
        public static final Codec<CustomSpawnRules> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CustomSpawnRules.createLightLimitCodec("block_light_limit").forGetter(rules -> rules.blockLightLimit), (App)CustomSpawnRules.createLightLimitCodec("sky_light_limit").forGetter(rules -> rules.skyLightLimit)).apply((Applicative)instance, CustomSpawnRules::new));

        private static DataResult<Range<Integer>> validate(Range<Integer> provider) {
            if (!DEFAULT.contains(provider)) {
                return DataResult.error(() -> "Light values must be withing range " + String.valueOf(DEFAULT));
            }
            return DataResult.success(provider);
        }

        private static MapCodec<Range<Integer>> createLightLimitCodec(String name) {
            return Range.CODEC.lenientOptionalFieldOf(name, DEFAULT).validate(CustomSpawnRules::validate);
        }

        public boolean canSpawn(BlockPos pos, ServerWorld world) {
            return this.blockLightLimit.contains(world.getLightLevel(LightType.BLOCK, pos)) && this.skyLightLimit.contains(world.getLightLevel(LightType.SKY, pos));
        }
    }
}

