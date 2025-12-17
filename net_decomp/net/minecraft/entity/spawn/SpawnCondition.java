/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.entity.spawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.Registries;

public interface SpawnCondition
extends VariantSelectorProvider.SelectorCondition<SpawnContext> {
    public static final Codec<SpawnCondition> CODEC = Registries.SPAWN_CONDITION_TYPE.getCodec().dispatch(SpawnCondition::getCodec, codec -> codec);

    public MapCodec<? extends SpawnCondition> getCodec();
}

