/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public abstract class PlacementModifier {
    public static final Codec<PlacementModifier> CODEC = Registries.PLACEMENT_MODIFIER_TYPE.getCodec().dispatch(PlacementModifier::getType, PlacementModifierType::codec);

    public abstract Stream<BlockPos> getPositions(FeaturePlacementContext var1, Random var2, BlockPos var3);

    public abstract PlacementModifierType<?> getType();
}

