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
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CopperChestBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class OxidizableCopperChestBlock
extends CopperChestBlock
implements Oxidizable {
    public static final MapCodec<OxidizableCopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Oxidizable.OxidationLevel.CODEC.fieldOf("weathering_state").forGetter(CopperChestBlock::getOxidationLevel), (App)Registries.SOUND_EVENT.getCodec().fieldOf("open_sound").forGetter(ChestBlock::getOpenSound), (App)Registries.SOUND_EVENT.getCodec().fieldOf("close_sound").forGetter(ChestBlock::getCloseSound), OxidizableCopperChestBlock.createSettingsCodec()).apply((Applicative)instance, OxidizableCopperChestBlock::new));

    @Override
    public MapCodec<OxidizableCopperChestBlock> getCodec() {
        return CODEC;
    }

    public OxidizableCopperChestBlock(Oxidizable.OxidationLevel oxidationLevel, SoundEvent soundEvent, SoundEvent soundEvent2, AbstractBlock.Settings settings) {
        super(oxidationLevel, soundEvent, soundEvent2, settings);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ChestBlockEntity chestBlockEntity;
        BlockEntity blockEntity;
        if (!state.get(ChestBlock.CHEST_TYPE).equals(ChestType.RIGHT) && (blockEntity = world.getBlockEntity(pos)) instanceof ChestBlockEntity && (chestBlockEntity = (ChestBlockEntity)blockEntity).getViewingUsers().isEmpty()) {
            this.tickDegradation(state, world, pos, random);
        }
    }

    @Override
    public Oxidizable.OxidationLevel getDegradationLevel() {
        return this.getOxidationLevel();
    }

    @Override
    public boolean isWaxed() {
        return false;
    }

    @Override
    public /* synthetic */ Enum getDegradationLevel() {
        return this.getDegradationLevel();
    }
}

