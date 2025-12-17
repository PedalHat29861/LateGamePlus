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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class BeehiveTreeDecorator
extends TreeDecorator {
    public static final MapCodec<BeehiveTreeDecorator> CODEC = Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").xmap(BeehiveTreeDecorator::new, decorator -> Float.valueOf(decorator.probability));
    private static final Direction BEE_NEST_FACE = Direction.SOUTH;
    private static final Direction[] GENERATE_DIRECTIONS = (Direction[])Direction.Type.HORIZONTAL.stream().filter(direction -> direction != BEE_NEST_FACE.getOpposite()).toArray(Direction[]::new);
    private final float probability;

    public BeehiveTreeDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override
    public void generate(TreeDecorator.Generator generator) {
        ObjectArrayList<BlockPos> list = generator.getLeavesPositions();
        ObjectArrayList<BlockPos> list2 = generator.getLogPositions();
        if (list2.isEmpty()) {
            return;
        }
        Random random = generator.getRandom();
        if (random.nextFloat() >= this.probability) {
            return;
        }
        int i = !list.isEmpty() ? Math.max(((BlockPos)list.getFirst()).getY() - 1, ((BlockPos)list2.getFirst()).getY() + 1) : Math.min(((BlockPos)list2.getFirst()).getY() + 1 + random.nextInt(3), ((BlockPos)list2.getLast()).getY());
        List list3 = list2.stream().filter(pos -> pos.getY() == i).flatMap(pos -> Stream.of(GENERATE_DIRECTIONS).map(pos::offset)).collect(Collectors.toList());
        if (list3.isEmpty()) {
            return;
        }
        Util.shuffle(list3, random);
        Optional<BlockPos> optional = list3.stream().filter(pos -> generator.isAir((BlockPos)pos) && generator.isAir(pos.offset(BEE_NEST_FACE))).findFirst();
        if (optional.isEmpty()) {
            return;
        }
        generator.replace(optional.get(), (BlockState)Blocks.BEE_NEST.getDefaultState().with(BeehiveBlock.FACING, BEE_NEST_FACE));
        generator.getWorld().getBlockEntity(optional.get(), BlockEntityType.BEEHIVE).ifPresent(blockEntity -> {
            int i = 2 + random.nextInt(2);
            for (int j = 0; j < i; ++j) {
                blockEntity.addBee(BeehiveBlockEntity.BeeData.create(random.nextInt(599)));
            }
        });
    }
}

