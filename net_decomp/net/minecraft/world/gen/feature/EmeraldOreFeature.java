/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.EmeraldOreFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EmeraldOreFeature
extends Feature<EmeraldOreFeatureConfig> {
    public EmeraldOreFeature(Codec<EmeraldOreFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<EmeraldOreFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        EmeraldOreFeatureConfig emeraldOreFeatureConfig = context.getConfig();
        for (OreFeatureConfig.Target target : emeraldOreFeatureConfig.targets) {
            if (!target.target.test(structureWorldAccess.getBlockState(blockPos), context.getRandom())) continue;
            structureWorldAccess.setBlockState(blockPos, target.state, 2);
            break;
        }
        return true;
    }
}

