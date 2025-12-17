/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.structure.pool.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.pool.alias.DirectStructurePoolAliasBinding;
import net.minecraft.structure.pool.alias.RandomGroupStructurePoolAliasBinding;
import net.minecraft.structure.pool.alias.RandomStructurePoolAliasBinding;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.random.Random;

public interface StructurePoolAliasBinding {
    public static final Codec<StructurePoolAliasBinding> CODEC = Registries.POOL_ALIAS_BINDING.getCodec().dispatch(StructurePoolAliasBinding::getCodec, Function.identity());

    public void forEach(Random var1, BiConsumer<RegistryKey<StructurePool>, RegistryKey<StructurePool>> var2);

    public Stream<RegistryKey<StructurePool>> streamTargets();

    public static DirectStructurePoolAliasBinding direct(String alias, String target) {
        return StructurePoolAliasBinding.direct(StructurePools.ofVanilla(alias), StructurePools.ofVanilla(target));
    }

    public static DirectStructurePoolAliasBinding direct(RegistryKey<StructurePool> alias, RegistryKey<StructurePool> target) {
        return new DirectStructurePoolAliasBinding(alias, target);
    }

    public static RandomStructurePoolAliasBinding random(String alias, Pool<String> targets) {
        Pool.Builder builder = Pool.builder();
        targets.getEntries().forEach(target -> builder.add(StructurePools.ofVanilla((String)target.value()), target.weight()));
        return StructurePoolAliasBinding.random(StructurePools.ofVanilla(alias), builder.build());
    }

    public static RandomStructurePoolAliasBinding random(RegistryKey<StructurePool> alias, Pool<RegistryKey<StructurePool>> targets) {
        return new RandomStructurePoolAliasBinding(alias, targets);
    }

    public static RandomGroupStructurePoolAliasBinding randomGroup(Pool<List<StructurePoolAliasBinding>> groups) {
        return new RandomGroupStructurePoolAliasBinding(groups);
    }

    public MapCodec<? extends StructurePoolAliasBinding> getCodec();
}

