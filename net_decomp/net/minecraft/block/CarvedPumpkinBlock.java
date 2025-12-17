/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CopperChestBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jspecify.annotations.Nullable;

public class CarvedPumpkinBlock
extends HorizontalFacingBlock {
    public static final MapCodec<CarvedPumpkinBlock> CODEC = CarvedPumpkinBlock.createCodec(CarvedPumpkinBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    private @Nullable BlockPattern snowGolemDispenserPattern;
    private @Nullable BlockPattern snowGolemPattern;
    private @Nullable BlockPattern ironGolemDispenserPattern;
    private @Nullable BlockPattern ironGolemPattern;
    private @Nullable BlockPattern copperGolemDispenserPattern;
    private @Nullable BlockPattern copperGolemPattern;
    private static final Predicate<BlockState> IS_GOLEM_HEAD_PREDICATE = state -> state.isOf(Blocks.CARVED_PUMPKIN) || state.isOf(Blocks.JACK_O_LANTERN);

    public MapCodec<? extends CarvedPumpkinBlock> getCodec() {
        return CODEC;
    }

    protected CarvedPumpkinBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        this.trySpawnEntity(world, pos);
    }

    public boolean canDispense(WorldView world, BlockPos pos) {
        return this.getSnowGolemDispenserPattern().searchAround(world, pos) != null || this.getIronGolemDispenserPattern().searchAround(world, pos) != null || this.getCopperGolemDispenserPattern().searchAround(world, pos) != null;
    }

    private void trySpawnEntity(World world, BlockPos pos) {
        CopperGolemEntity copperGolemEntity;
        IronGolemEntity ironGolemEntity;
        SnowGolemEntity snowGolemEntity;
        BlockPattern.Result result = this.getSnowGolemPattern().searchAround(world, pos);
        if (result != null && (snowGolemEntity = EntityType.SNOW_GOLEM.create(world, SpawnReason.TRIGGERED)) != null) {
            CarvedPumpkinBlock.spawnEntity(world, result, snowGolemEntity, result.translate(0, 2, 0).getBlockPos());
            return;
        }
        BlockPattern.Result result2 = this.getIronGolemPattern().searchAround(world, pos);
        if (result2 != null && (ironGolemEntity = EntityType.IRON_GOLEM.create(world, SpawnReason.TRIGGERED)) != null) {
            ironGolemEntity.setPlayerCreated(true);
            CarvedPumpkinBlock.spawnEntity(world, result2, ironGolemEntity, result2.translate(1, 2, 0).getBlockPos());
            return;
        }
        BlockPattern.Result result3 = this.getCopperGolemPattern().searchAround(world, pos);
        if (result3 != null && (copperGolemEntity = EntityType.COPPER_GOLEM.create(world, SpawnReason.TRIGGERED)) != null) {
            CarvedPumpkinBlock.spawnEntity(world, result3, copperGolemEntity, result3.translate(0, 0, 0).getBlockPos());
            this.replaceCopperBlockWithChest(world, result3);
            copperGolemEntity.onSpawn(this.getOxidationLevel(result3));
        }
    }

    private Oxidizable.OxidationLevel getOxidationLevel(BlockPattern.Result patternResult) {
        BlockState blockState = patternResult.translate(0, 1, 0).getBlockState();
        Block block2 = blockState.getBlock();
        if (block2 instanceof Oxidizable) {
            Oxidizable oxidizable = (Oxidizable)((Object)block2);
            return (Oxidizable.OxidationLevel)oxidizable.getDegradationLevel();
        }
        return (Oxidizable.OxidationLevel)Optional.ofNullable((Block)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get((Object)blockState.getBlock())).filter(block -> block instanceof Oxidizable).map(block -> (Oxidizable)((Object)block)).orElse((Oxidizable)((Object)Blocks.COPPER_BLOCK)).getDegradationLevel();
    }

    private static void spawnEntity(World world, BlockPattern.Result patternResult, Entity entity, BlockPos pos) {
        CarvedPumpkinBlock.breakPatternBlocks(world, patternResult);
        entity.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY() + 0.05, (double)pos.getZ() + 0.5, 0.0f, 0.0f);
        world.spawnEntity(entity);
        for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, entity.getBoundingBox().expand(5.0))) {
            Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, entity);
        }
        CarvedPumpkinBlock.updatePatternBlocks(world, patternResult);
    }

    public static void breakPatternBlocks(World world, BlockPattern.Result patternResult) {
        for (int i = 0; i < patternResult.getWidth(); ++i) {
            for (int j = 0; j < patternResult.getHeight(); ++j) {
                CachedBlockPosition cachedBlockPosition = patternResult.translate(i, j, 0);
                world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
                world.syncWorldEvent(2001, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
            }
        }
    }

    public static void updatePatternBlocks(World world, BlockPattern.Result patternResult) {
        for (int i = 0; i < patternResult.getWidth(); ++i) {
            for (int j = 0; j < patternResult.getHeight(); ++j) {
                CachedBlockPosition cachedBlockPosition = patternResult.translate(i, j, 0);
                world.updateNeighbors(cachedBlockPosition.getBlockPos(), Blocks.AIR);
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private BlockPattern getSnowGolemDispenserPattern() {
        if (this.snowGolemDispenserPattern == null) {
            this.snowGolemDispenserPattern = BlockPatternBuilder.start().aisle(" ", "#", "#").where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
        }
        return this.snowGolemDispenserPattern;
    }

    private BlockPattern getSnowGolemPattern() {
        if (this.snowGolemPattern == null) {
            this.snowGolemPattern = BlockPatternBuilder.start().aisle("^", "#", "#").where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
        }
        return this.snowGolemPattern;
    }

    private BlockPattern getIronGolemDispenserPattern() {
        if (this.ironGolemDispenserPattern == null) {
            this.ironGolemDispenserPattern = BlockPatternBuilder.start().aisle("~ ~", "###", "~#~").where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', CachedBlockPosition.matchesBlockState(AbstractBlock.AbstractBlockState::isAir)).build();
        }
        return this.ironGolemDispenserPattern;
    }

    private BlockPattern getIronGolemPattern() {
        if (this.ironGolemPattern == null) {
            this.ironGolemPattern = BlockPatternBuilder.start().aisle("~^~", "###", "~#~").where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', CachedBlockPosition.matchesBlockState(AbstractBlock.AbstractBlockState::isAir)).build();
        }
        return this.ironGolemPattern;
    }

    private BlockPattern getCopperGolemDispenserPattern() {
        if (this.copperGolemDispenserPattern == null) {
            this.copperGolemDispenserPattern = BlockPatternBuilder.start().aisle(" ", "#").where('#', CachedBlockPosition.matchesBlockState(state -> state.isIn(BlockTags.COPPER))).build();
        }
        return this.copperGolemDispenserPattern;
    }

    private BlockPattern getCopperGolemPattern() {
        if (this.copperGolemPattern == null) {
            this.copperGolemPattern = BlockPatternBuilder.start().aisle("^", "#").where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(state -> state.isIn(BlockTags.COPPER))).build();
        }
        return this.copperGolemPattern;
    }

    public void replaceCopperBlockWithChest(World world, BlockPattern.Result patternResult) {
        CachedBlockPosition cachedBlockPosition = patternResult.translate(0, 1, 0);
        CachedBlockPosition cachedBlockPosition2 = patternResult.translate(0, 0, 0);
        Direction direction = cachedBlockPosition2.getBlockState().get(FACING);
        BlockState blockState = CopperChestBlock.fromCopperBlock(cachedBlockPosition.getBlockState().getBlock(), direction, world, cachedBlockPosition.getBlockPos());
        world.setBlockState(cachedBlockPosition.getBlockPos(), blockState, 2);
    }
}

