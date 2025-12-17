/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class DaylightDetectorBlock
extends BlockWithEntity {
    public static final MapCodec<DaylightDetectorBlock> CODEC = DaylightDetectorBlock.createCodec(DaylightDetectorBlock::new);
    public static final IntProperty POWER = Properties.POWER;
    public static final BooleanProperty INVERTED = Properties.INVERTED;
    private static final VoxelShape SHAPE = Block.createColumnShape(16.0, 0.0, 6.0);

    public MapCodec<DaylightDetectorBlock> getCodec() {
        return CODEC;
    }

    public DaylightDetectorBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWER, 0)).with(INVERTED, false));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWER);
    }

    private static void updateState(BlockState state, World world, BlockPos pos) {
        int i = world.getLightLevel(LightType.SKY, pos) - world.getAmbientDarkness();
        float f = world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.SUN_ANGLE_VISUAL, pos).floatValue() * ((float)Math.PI / 180);
        boolean bl = state.get(INVERTED);
        if (bl) {
            i = 15 - i;
        } else if (i > 0) {
            float g = f < (float)Math.PI ? 0.0f : (float)Math.PI * 2;
            f += (g - f) * 0.2f;
            i = Math.round((float)i * MathHelper.cos(f));
        }
        i = MathHelper.clamp(i, 0, 15);
        if (state.get(POWER) != i) {
            world.setBlockState(pos, (BlockState)state.with(POWER, i), 3);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.canModifyBlocks()) {
            return super.onUse(state, world, pos, player, hit);
        }
        if (!world.isClient()) {
            BlockState blockState = (BlockState)state.cycle(INVERTED);
            world.setBlockState(pos, blockState, 2);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, blockState));
            DaylightDetectorBlock.updateState(blockState, world, pos);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DaylightDetectorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient() && world.getDimension().hasSkyLight()) {
            return DaylightDetectorBlock.validateTicker(type, BlockEntityType.DAYLIGHT_DETECTOR, DaylightDetectorBlock::tick);
        }
        return null;
    }

    private static void tick(World world, BlockPos pos, BlockState state, DaylightDetectorBlockEntity blockEntity) {
        if (world.getTime() % 20L == 0L) {
            DaylightDetectorBlock.updateState(state, world, pos);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER, INVERTED);
    }
}

