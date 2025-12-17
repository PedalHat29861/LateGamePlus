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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;

public class BastionData {
    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructurePool> registryEntryLookup = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> registryEntry = registryEntryLookup.getOrThrow(StructurePools.EMPTY);
        StructurePools.register(poolRegisterable, "bastion/mobs/piglin", new StructurePool(registryEntry, (List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/melee_piglin"), (Object)1), (Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/sword_piglin"), (Object)4), (Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/crossbow_piglin"), (Object)4), (Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/empty"), (Object)1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/mobs/hoglin", new StructurePool(registryEntry, (List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/hoglin"), (Object)2), (Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/empty"), (Object)1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/blocks/gold", new StructurePool(registryEntry, (List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.ofSingle("bastion/blocks/air"), (Object)3), (Object)Pair.of(StructurePoolElement.ofSingle("bastion/blocks/gold"), (Object)1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/mobs/piglin_melee", new StructurePool(registryEntry, (List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/melee_piglin_always"), (Object)1), (Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/melee_piglin"), (Object)5), (Object)Pair.of(StructurePoolElement.ofSingle("bastion/mobs/sword_piglin"), (Object)1)), StructurePool.Projection.RIGID));
    }
}

