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
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PropaguleBlock;
import net.minecraft.block.TintedParticleLeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class MangroveLeavesBlock
extends TintedParticleLeavesBlock
implements Fertilizable {
    public static final MapCodec<MangroveLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.rangedInclusiveFloat(0.0f, 1.0f).fieldOf("leaf_particle_chance").forGetter(block -> Float.valueOf(block.leafParticleChance)), MangroveLeavesBlock.createSettingsCodec()).apply((Applicative)instance, MangroveLeavesBlock::new));

    public MapCodec<MangroveLeavesBlock> getCodec() {
        return CODEC;
    }

    public MangroveLeavesBlock(float f, AbstractBlock.Settings settings) {
        super(f, settings);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return world.getBlockState(pos.down()).isAir();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        world.setBlockState(pos.down(), PropaguleBlock.getDefaultHangingState(), 2);
    }

    @Override
    public BlockPos getFertilizeParticlePos(BlockPos pos) {
        return pos.down();
    }
}

