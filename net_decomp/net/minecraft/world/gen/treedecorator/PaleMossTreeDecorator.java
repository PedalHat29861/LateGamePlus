/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.treedecorator;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HangingMossBlock;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.VegetationConfiguredFeatures;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class PaleMossTreeDecorator
extends TreeDecorator {
    public static final MapCodec<PaleMossTreeDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("leaves_probability").forGetter(treeDecorator -> Float.valueOf(treeDecorator.leavesProbability)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("trunk_probability").forGetter(treeDecorator -> Float.valueOf(treeDecorator.trunkProbability)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("ground_probability").forGetter(treeDecorator -> Float.valueOf(treeDecorator.groundProbability))).apply((Applicative)instance, PaleMossTreeDecorator::new));
    private final float leavesProbability;
    private final float trunkProbability;
    private final float groundProbability;

    @Override
    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.PALE_MOSS;
    }

    public PaleMossTreeDecorator(float leavesProbability, float trunkProbability, float groundProbability) {
        this.leavesProbability = leavesProbability;
        this.trunkProbability = trunkProbability;
        this.groundProbability = groundProbability;
    }

    @Override
    public void generate(TreeDecorator.Generator generator) {
        Random random = generator.getRandom();
        StructureWorldAccess structureWorldAccess = (StructureWorldAccess)generator.getWorld();
        List<BlockPos> list = Util.copyShuffled(generator.getLogPositions(), random);
        if (list.isEmpty()) {
            return;
        }
        BlockPos blockPos = Collections.min(list, Comparator.comparingInt(Vec3i::getY));
        if (random.nextFloat() < this.groundProbability) {
            structureWorldAccess.getRegistryManager().getOptional(RegistryKeys.CONFIGURED_FEATURE).flatMap(registry -> registry.getOptional(VegetationConfiguredFeatures.PALE_MOSS_PATCH)).ifPresent(entry -> ((ConfiguredFeature)entry.value()).generate(structureWorldAccess, structureWorldAccess.toServerWorld().getChunkManager().getChunkGenerator(), random, blockPos.up()));
        }
        generator.getLogPositions().forEach(pos -> {
            BlockPos blockPos;
            if (random.nextFloat() < this.trunkProbability && generator.isAir(blockPos = pos.down())) {
                PaleMossTreeDecorator.decorate(blockPos, generator);
            }
        });
        generator.getLeavesPositions().forEach(pos -> {
            BlockPos blockPos;
            if (random.nextFloat() < this.leavesProbability && generator.isAir(blockPos = pos.down())) {
                PaleMossTreeDecorator.decorate(blockPos, generator);
            }
        });
    }

    private static void decorate(BlockPos pos, TreeDecorator.Generator generator) {
        while (generator.isAir(pos.down()) && !((double)generator.getRandom().nextFloat() < 0.5)) {
            generator.replace(pos, (BlockState)Blocks.PALE_HANGING_MOSS.getDefaultState().with(HangingMossBlock.TIP, false));
            pos = pos.down();
        }
        generator.replace(pos, (BlockState)Blocks.PALE_HANGING_MOSS.getDefaultState().with(HangingMossBlock.TIP, true));
    }
}

