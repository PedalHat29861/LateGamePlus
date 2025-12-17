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
import net.minecraft.structure.BastionBridgeData;
import net.minecraft.structure.BastionData;
import net.minecraft.structure.BastionHoglinStableData;
import net.minecraft.structure.BastionTreasureData;
import net.minecraft.structure.BastionUnitsData;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;

public class BastionRemnantGenerator {
    public static final RegistryKey<StructurePool> STRUCTURE_POOLS = StructurePools.ofVanilla("bastion/starts");

    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructureProcessorList> registryEntryLookup = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> registryEntry = registryEntryLookup.getOrThrow(StructureProcessorLists.BASTION_GENERIC_DEGRADATION);
        RegistryEntryLookup<StructurePool> registryEntryLookup2 = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> registryEntry2 = registryEntryLookup2.getOrThrow(StructurePools.EMPTY);
        poolRegisterable.register(STRUCTURE_POOLS, new StructurePool(registryEntry2, (List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.ofProcessedSingle("bastion/units/air_base", registryEntry), (Object)1), (Object)Pair.of(StructurePoolElement.ofProcessedSingle("bastion/hoglin_stable/air_base", registryEntry), (Object)1), (Object)Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/big_air_full", registryEntry), (Object)1), (Object)Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/starting_pieces/entrance_base", registryEntry), (Object)1)), StructurePool.Projection.RIGID));
        BastionUnitsData.bootstrap(poolRegisterable);
        BastionHoglinStableData.bootstrap(poolRegisterable);
        BastionTreasureData.bootstrap(poolRegisterable);
        BastionBridgeData.bootstrap(poolRegisterable);
        BastionData.bootstrap(poolRegisterable);
    }
}

