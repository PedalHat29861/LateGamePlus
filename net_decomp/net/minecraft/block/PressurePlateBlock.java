/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PressurePlateBlock
extends AbstractPressurePlateBlock {
    public static final MapCodec<PressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockSetType.CODEC.fieldOf("block_set_type").forGetter(block -> block.blockSetType), PressurePlateBlock.createSettingsCodec()).apply((Applicative)instance, PressurePlateBlock::new));
    public static final BooleanProperty POWERED = Properties.POWERED;

    public MapCodec<PressurePlateBlock> getCodec() {
        return CODEC;
    }

    protected PressurePlateBlock(BlockSetType type, AbstractBlock.Settings settings) {
        super(settings, type);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false));
    }

    @Override
    protected int getRedstoneOutput(BlockState state) {
        return state.get(POWERED) != false ? 15 : 0;
    }

    @Override
    protected BlockState setRedstoneOutput(BlockState state, int rsOut) {
        return (BlockState)state.with(POWERED, rsOut > 0);
    }

    @Override
    protected int getRedstoneOutput(World world, BlockPos pos) {
        Class<Entity> class_ = switch (this.blockSetType.pressurePlateSensitivity()) {
            default -> throw new MatchException(null, null);
            case BlockSetType.ActivationRule.EVERYTHING -> Entity.class;
            case BlockSetType.ActivationRule.MOBS -> LivingEntity.class;
        };
        return PressurePlateBlock.getEntityCount(world, BOX.offset(pos), class_) > 0 ? 15 : 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}

