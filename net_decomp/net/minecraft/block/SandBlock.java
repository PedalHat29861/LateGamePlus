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
import net.minecraft.block.ColoredFallingBlock;
import net.minecraft.sound.AmbientDesertBlockSounds;
import net.minecraft.util.ColorCode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class SandBlock
extends ColoredFallingBlock {
    public static final MapCodec<SandBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ColorCode.CODEC.fieldOf("falling_dust_color").forGetter(block -> block.color), SandBlock.createSettingsCodec()).apply((Applicative)instance, SandBlock::new));

    public MapCodec<SandBlock> getCodec() {
        return CODEC;
    }

    public SandBlock(ColorCode colorCode, AbstractBlock.Settings settings) {
        super(colorCode, settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        AmbientDesertBlockSounds.tryPlaySandSounds(world, pos, random);
    }
}

