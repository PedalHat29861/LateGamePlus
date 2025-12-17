/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.structure;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;

public record StructureSet(List<WeightedEntry> structures, StructurePlacement placement) {
    public static final Codec<StructureSet> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)WeightedEntry.CODEC.listOf().fieldOf("structures").forGetter(StructureSet::structures), (App)StructurePlacement.TYPE_CODEC.fieldOf("placement").forGetter(StructureSet::placement)).apply((Applicative)instance, StructureSet::new));
    public static final Codec<RegistryEntry<StructureSet>> REGISTRY_CODEC = RegistryElementCodec.of(RegistryKeys.STRUCTURE_SET, CODEC);

    public StructureSet(RegistryEntry<Structure> structure, StructurePlacement placement) {
        this(List.of(new WeightedEntry(structure, 1)), placement);
    }

    public static WeightedEntry createEntry(RegistryEntry<Structure> structure, int weight) {
        return new WeightedEntry(structure, weight);
    }

    public static WeightedEntry createEntry(RegistryEntry<Structure> structure) {
        return new WeightedEntry(structure, 1);
    }

    public record WeightedEntry(RegistryEntry<Structure> structure, int weight) {
        public static final Codec<WeightedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Structure.ENTRY_CODEC.fieldOf("structure").forGetter(WeightedEntry::structure), (App)Codecs.POSITIVE_INT.fieldOf("weight").forGetter(WeightedEntry::weight)).apply((Applicative)instance, WeightedEntry::new));
    }
}

