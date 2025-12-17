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
import net.minecraft.block.FallingBlock;
import net.minecraft.util.ColorCode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ColoredFallingBlock
extends FallingBlock {
    public static final MapCodec<ColoredFallingBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ColorCode.CODEC.fieldOf("falling_dust_color").forGetter(block -> block.color), ColoredFallingBlock.createSettingsCodec()).apply((Applicative)instance, ColoredFallingBlock::new));
    protected final ColorCode color;

    public MapCodec<? extends ColoredFallingBlock> getCodec() {
        return CODEC;
    }

    public ColoredFallingBlock(ColorCode color, AbstractBlock.Settings settings) {
        super(settings);
        this.color = color;
    }

    @Override
    public int getColor(BlockState state, BlockView world, BlockPos pos) {
        return this.color.rgba();
    }
}

