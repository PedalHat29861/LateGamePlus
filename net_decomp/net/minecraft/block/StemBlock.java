/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.block;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class StemBlock
extends PlantBlock
implements Fertilizable {
    public static final MapCodec<StemBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryKey.createCodec(RegistryKeys.BLOCK).fieldOf("fruit").forGetter(block -> block.gourdBlock), (App)RegistryKey.createCodec(RegistryKeys.BLOCK).fieldOf("attached_stem").forGetter(block -> block.attachedStemBlock), (App)RegistryKey.createCodec(RegistryKeys.ITEM).fieldOf("seed").forGetter(block -> block.pickBlockItem), StemBlock.createSettingsCodec()).apply((Applicative)instance, StemBlock::new));
    public static final int MAX_AGE = 7;
    public static final IntProperty AGE = Properties.AGE_7;
    private static final VoxelShape[] SHAPES_BY_AGE = Block.createShapeArray(7, age -> Block.createColumnShape(2.0, 0.0, 2 + age * 2));
    private final RegistryKey<Block> gourdBlock;
    private final RegistryKey<Block> attachedStemBlock;
    private final RegistryKey<Item> pickBlockItem;

    public MapCodec<StemBlock> getCodec() {
        return CODEC;
    }

    protected StemBlock(RegistryKey<Block> gourdBlock, RegistryKey<Block> attachedStemBlock, RegistryKey<Item> pickBlockItem, AbstractBlock.Settings settings) {
        super(settings);
        this.gourdBlock = gourdBlock;
        this.attachedStemBlock = attachedStemBlock;
        this.pickBlockItem = pickBlockItem;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_AGE[state.get(AGE)];
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBaseLightLevel(pos, 0) < 9) {
            return;
        }
        float f = CropBlock.getAvailableMoisture(this, world, pos);
        if (random.nextInt((int)(25.0f / f) + 1) == 0) {
            int i = state.get(AGE);
            if (i < 7) {
                state = (BlockState)state.with(AGE, i + 1);
                world.setBlockState(pos, state, 2);
            } else {
                Direction direction = Direction.Type.HORIZONTAL.random(random);
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos.down());
                if (world.getBlockState(blockPos).isAir() && (blockState.isOf(Blocks.FARMLAND) || blockState.isIn(BlockTags.DIRT))) {
                    RegistryWrapper.Impl registry = world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);
                    Optional<Block> optional = registry.getOptionalValue(this.gourdBlock);
                    Optional<Block> optional2 = registry.getOptionalValue(this.attachedStemBlock);
                    if (optional.isPresent() && optional2.isPresent()) {
                        world.setBlockState(blockPos, optional.get().getDefaultState());
                        world.setBlockState(pos, (BlockState)optional2.get().getDefaultState().with(HorizontalFacingBlock.FACING, direction));
                    }
                }
            }
        }
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack((ItemConvertible)DataFixUtils.orElse(world.getRegistryManager().getOrThrow(RegistryKeys.ITEM).getOptionalValue(this.pickBlockItem), (Object)this));
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return state.get(AGE) != 7;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        int i = Math.min(7, state.get(AGE) + MathHelper.nextInt(world.random, 2, 5));
        BlockState blockState = (BlockState)state.with(AGE, i);
        world.setBlockState(pos, blockState, 2);
        if (i == 7) {
            blockState.randomTick(world, pos, world.random);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}

