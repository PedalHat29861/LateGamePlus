/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity.spawn;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.biome.Biome;

public record BiomeSpawnCondition(RegistryEntryList<Biome> requiredBiomes) implements SpawnCondition
{
    public static final MapCodec<BiomeSpawnCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("biomes").forGetter(BiomeSpawnCondition::requiredBiomes)).apply((Applicative)instance, BiomeSpawnCondition::new));

    @Override
    public boolean test(SpawnContext spawnContext) {
        return this.requiredBiomes.contains(spawnContext.biome());
    }

    public MapCodec<BiomeSpawnCondition> getCodec() {
        return CODEC;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((SpawnContext)context);
    }
}

