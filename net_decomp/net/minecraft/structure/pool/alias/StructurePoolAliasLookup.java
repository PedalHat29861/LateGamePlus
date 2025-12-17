/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 */
package net.minecraft.structure.pool.alias;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

@FunctionalInterface
public interface StructurePoolAliasLookup {
    public static final StructurePoolAliasLookup EMPTY = pool -> pool;

    public RegistryKey<StructurePool> lookup(RegistryKey<StructurePool> var1);

    public static StructurePoolAliasLookup create(List<StructurePoolAliasBinding> bindings, BlockPos pos, long seed) {
        if (bindings.isEmpty()) {
            return EMPTY;
        }
        Random random = Random.create(seed).nextSplitter().split(pos);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        bindings.forEach(binding -> binding.forEach(random, (arg_0, arg_1) -> ((ImmutableMap.Builder)builder).put(arg_0, arg_1)));
        ImmutableMap map = builder.build();
        return arg_0 -> StructurePoolAliasLookup.method_54512((Map)map, arg_0);
    }

    private static /* synthetic */ RegistryKey method_54512(Map map, RegistryKey alias) {
        return Objects.requireNonNull(map.getOrDefault(alias, alias), () -> "alias " + String.valueOf(alias.getValue()) + " was mapped to null value");
    }
}

