/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Function;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.AncientCityOutskirtsGenerator;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;

public class AncientCityGenerator {
    public static final RegistryKey<StructurePool> CITY_CENTER = StructurePools.ofVanilla("ancient_city/city_center");

    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructureProcessorList> registryEntryLookup = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> registryEntry = registryEntryLookup.getOrThrow(StructureProcessorLists.ANCIENT_CITY_START_DEGRADATION);
        RegistryEntryLookup<StructurePool> registryEntryLookup2 = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> registryEntry2 = registryEntryLookup2.getOrThrow(StructurePools.EMPTY);
        poolRegisterable.register(CITY_CENTER, new StructurePool(registryEntry2, (List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_1", registryEntry), (Object)1), (Object)Pair.of(StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_2", registryEntry), (Object)1), (Object)Pair.of(StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_3", registryEntry), (Object)1)), StructurePool.Projection.RIGID));
        AncientCityOutskirtsGenerator.bootstrap(poolRegisterable);
    }
}

