/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CreakingHeartBlock;
import net.minecraft.block.enums.CreakingHeartState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class CreakingHeartTreeDecorator
extends TreeDecorator {
    public static final MapCodec<CreakingHeartTreeDecorator> CODEC = Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").xmap(CreakingHeartTreeDecorator::new, treeDecorator -> Float.valueOf(treeDecorator.probability));
    private final float probability;

    public CreakingHeartTreeDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.CREAKING_HEART;
    }

    @Override
    public void generate(TreeDecorator.Generator generator) {
        Random random = generator.getRandom();
        ObjectArrayList<BlockPos> list = generator.getLogPositions();
        if (list.isEmpty()) {
            return;
        }
        if (random.nextFloat() >= this.probability) {
            return;
        }
        ArrayList<BlockPos> list2 = new ArrayList<BlockPos>((Collection<BlockPos>)list);
        Util.shuffle(list2, random);
        Optional<BlockPos> optional = list2.stream().filter(pos -> {
            for (Direction direction : Direction.values()) {
                if (generator.matches(pos.offset(direction), state -> state.isIn(BlockTags.LOGS))) continue;
                return false;
            }
            return true;
        }).findFirst();
        if (optional.isEmpty()) {
            return;
        }
        generator.replace(optional.get(), (BlockState)((BlockState)Blocks.CREAKING_HEART.getDefaultState().with(CreakingHeartBlock.ACTIVE, CreakingHeartState.DORMANT)).with(CreakingHeartBlock.NATURAL, true));
    }
}

