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
import net.minecraft.block.LeavesBlock;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class UntintedParticleLeavesBlock
extends LeavesBlock {
    public static final MapCodec<UntintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.rangedInclusiveFloat(0.0f, 1.0f).fieldOf("leaf_particle_chance").forGetter(untintedParticleLeavesBlock -> Float.valueOf(untintedParticleLeavesBlock.leafParticleChance)), (App)ParticleTypes.TYPE_CODEC.fieldOf("leaf_particle").forGetter(untintedParticleLeavesBlock -> untintedParticleLeavesBlock.leafParticleEffect), UntintedParticleLeavesBlock.createSettingsCodec()).apply((Applicative)instance, UntintedParticleLeavesBlock::new));
    protected final ParticleEffect leafParticleEffect;

    public UntintedParticleLeavesBlock(float leafParticleChance, ParticleEffect leafParticleEffect, AbstractBlock.Settings settings) {
        super(leafParticleChance, settings);
        this.leafParticleEffect = leafParticleEffect;
    }

    @Override
    protected void spawnLeafParticle(World world, BlockPos pos, Random random) {
        ParticleUtil.spawnParticle(world, pos, random, this.leafParticleEffect);
    }

    public MapCodec<UntintedParticleLeavesBlock> getCodec() {
        return CODEC;
    }
}

