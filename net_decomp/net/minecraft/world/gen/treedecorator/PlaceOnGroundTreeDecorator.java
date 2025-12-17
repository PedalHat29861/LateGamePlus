/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.treedecorator;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class PlaceOnGroundTreeDecorator
extends TreeDecorator {
    public static final MapCodec<PlaceOnGroundTreeDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.POSITIVE_INT.fieldOf("tries").orElse((Object)128).forGetter(treeDecorator -> treeDecorator.tries), (App)Codecs.NON_NEGATIVE_INT.fieldOf("radius").orElse((Object)2).forGetter(treeDecorator -> treeDecorator.radius), (App)Codecs.NON_NEGATIVE_INT.fieldOf("height").orElse((Object)1).forGetter(treeDecorator -> treeDecorator.height), (App)BlockStateProvider.TYPE_CODEC.fieldOf("block_state_provider").forGetter(treeDecorator -> treeDecorator.blockStateProvider)).apply((Applicative)instance, PlaceOnGroundTreeDecorator::new));
    private final int tries;
    private final int radius;
    private final int height;
    private final BlockStateProvider blockStateProvider;

    public PlaceOnGroundTreeDecorator(int tries, int radius, int height, BlockStateProvider blockStateProvider) {
        this.tries = tries;
        this.radius = radius;
        this.height = height;
        this.blockStateProvider = blockStateProvider;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.PLACE_ON_GROUND;
    }

    @Override
    public void generate(TreeDecorator.Generator generator) {
        List<BlockPos> list = TreeFeature.getLeafLitterPositions(generator);
        if (list.isEmpty()) {
            return;
        }
        BlockPos blockPos = list.getFirst();
        int i = blockPos.getY();
        int j = blockPos.getX();
        int k = blockPos.getX();
        int l = blockPos.getZ();
        int m = blockPos.getZ();
        for (BlockPos blockPos2 : list) {
            if (blockPos2.getY() != i) continue;
            j = Math.min(j, blockPos2.getX());
            k = Math.max(k, blockPos2.getX());
            l = Math.min(l, blockPos2.getZ());
            m = Math.max(m, blockPos2.getZ());
        }
        Random random = generator.getRandom();
        BlockBox blockBox = new BlockBox(j, i, l, k, i, m).expand(this.radius, this.height, this.radius);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int n = 0; n < this.tries; ++n) {
            mutable.set(random.nextBetween(blockBox.getMinX(), blockBox.getMaxX()), random.nextBetween(blockBox.getMinY(), blockBox.getMaxY()), random.nextBetween(blockBox.getMinZ(), blockBox.getMaxZ()));
            this.generate(generator, mutable);
        }
    }

    private void generate(TreeDecorator.Generator generator, BlockPos pos) {
        BlockPos blockPos = pos.up();
        if (generator.getWorld().testBlockState(blockPos, state -> state.isAir() || state.isOf(Blocks.VINE)) && generator.matches(pos, AbstractBlock.AbstractBlockState::isOpaqueFullCube) && generator.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos).getY() <= blockPos.getY()) {
            generator.replace(blockPos, this.blockStateProvider.get(generator.getRandom(), blockPos));
        }
    }
}

