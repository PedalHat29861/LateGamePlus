/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jspecify.annotations.Nullable;

public class ButtonBlock
extends WallMountedBlock {
    public static final MapCodec<ButtonBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockSetType.CODEC.fieldOf("block_set_type").forGetter(block -> block.blockSetType), (App)Codec.intRange((int)1, (int)1024).fieldOf("ticks_to_stay_pressed").forGetter(block -> block.pressTicks), ButtonBlock.createSettingsCodec()).apply((Applicative)instance, ButtonBlock::new));
    public static final BooleanProperty POWERED = Properties.POWERED;
    private final BlockSetType blockSetType;
    private final int pressTicks;
    private final Function<BlockState, VoxelShape> shapeFunction;

    public MapCodec<ButtonBlock> getCodec() {
        return CODEC;
    }

    protected ButtonBlock(BlockSetType blockSetType, int pressTicks, AbstractBlock.Settings settings) {
        super(settings.sounds(blockSetType.soundType()));
        this.blockSetType = blockSetType;
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(FACE, BlockFace.WALL));
        this.pressTicks = pressTicks;
        this.shapeFunction = this.createShapeFunction();
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        VoxelShape voxelShape = Block.createCubeShape(14.0);
        VoxelShape voxelShape2 = Block.createCubeShape(12.0);
        Map<BlockFace, Map<Direction, VoxelShape>> map = VoxelShapes.createBlockFaceHorizontalFacingShapeMap(Block.createCuboidZShape(6.0, 4.0, 8.0, 16.0));
        return this.createShapeFunction(state -> VoxelShapes.combineAndSimplify((VoxelShape)((Map)map.get(state.get(FACE))).get(state.get(FACING)), state.get(POWERED) != false ? voxelShape : voxelShape2, BooleanBiFunction.ONLY_FIRST));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (state.get(POWERED).booleanValue()) {
            return ActionResult.CONSUME;
        }
        this.powerOn(state, world, pos, player);
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (explosion.canTriggerBlocks() && !state.get(POWERED).booleanValue()) {
            this.powerOn(state, world, pos, null);
        }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    public void powerOn(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player) {
        world.setBlockState(pos, (BlockState)state.with(POWERED, true), 3);
        this.updateNeighbors(state, world, pos);
        world.scheduleBlockTick(pos, this, this.pressTicks);
        this.playClickSound(player, world, pos, true);
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_ACTIVATE, pos);
    }

    protected void playClickSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos, boolean powered) {
        world.playSound(powered ? player : null, pos, this.getClickSound(powered), SoundCategory.BLOCKS);
    }

    protected SoundEvent getClickSound(boolean powered) {
        return powered ? this.blockSetType.buttonClickOn() : this.blockSetType.buttonClickOff();
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!moved && state.get(POWERED).booleanValue()) {
            this.updateNeighbors(state, world, pos);
        }
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(POWERED).booleanValue() && ButtonBlock.getDirection(state) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.get(POWERED).booleanValue()) {
            return;
        }
        this.tryPowerWithProjectiles(state, world, pos);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (world.isClient() || !this.blockSetType.canButtonBeActivatedByArrows() || state.get(POWERED).booleanValue()) {
            return;
        }
        this.tryPowerWithProjectiles(state, world, pos);
    }

    protected void tryPowerWithProjectiles(BlockState state, World world, BlockPos pos) {
        boolean bl2;
        PersistentProjectileEntity persistentProjectileEntity = this.blockSetType.canButtonBeActivatedByArrows() ? (PersistentProjectileEntity)world.getNonSpectatingEntities(PersistentProjectileEntity.class, state.getOutlineShape(world, pos).getBoundingBox().offset(pos)).stream().findFirst().orElse(null) : null;
        boolean bl = persistentProjectileEntity != null;
        if (bl != (bl2 = state.get(POWERED).booleanValue())) {
            world.setBlockState(pos, (BlockState)state.with(POWERED, bl), 3);
            this.updateNeighbors(state, world, pos);
            this.playClickSound(null, world, pos, bl);
            world.emitGameEvent((Entity)persistentProjectileEntity, bl ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
        }
        if (bl) {
            world.scheduleBlockTick(new BlockPos(pos), this, this.pressTicks);
        }
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        Direction direction;
        WireOrientation wireOrientation = OrientationHelper.getEmissionOrientation(world, direction, (direction = ButtonBlock.getDirection(state).getOpposite()).getAxis().isHorizontal() ? Direction.UP : (Direction)state.get(FACING));
        world.updateNeighborsAlways(pos, this, wireOrientation);
        world.updateNeighborsAlways(pos.offset(direction), this, wireOrientation);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, FACE);
    }
}

