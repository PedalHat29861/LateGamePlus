/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.root;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record AboveRootPlacement(BlockStateProvider aboveRootProvider, float aboveRootPlacementChance) {
    public static final Codec<AboveRootPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.TYPE_CODEC.fieldOf("above_root_provider").forGetter(aboveRootPlacement -> aboveRootPlacement.aboveRootProvider), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("above_root_placement_chance").forGetter(aboveRootPlacement -> Float.valueOf(aboveRootPlacement.aboveRootPlacementChance))).apply((Applicative)instance, AboveRootPlacement::new));
}

