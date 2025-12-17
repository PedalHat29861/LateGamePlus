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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;

public class ExperienceDroppingBlock
extends Block {
    public static final MapCodec<ExperienceDroppingBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)IntProvider.createValidatingCodec(0, 10).fieldOf("experience").forGetter(block -> block.experienceDropped), ExperienceDroppingBlock.createSettingsCodec()).apply((Applicative)instance, ExperienceDroppingBlock::new));
    private final IntProvider experienceDropped;

    public MapCodec<? extends ExperienceDroppingBlock> getCodec() {
        return CODEC;
    }

    public ExperienceDroppingBlock(IntProvider experienceDropped, AbstractBlock.Settings settings) {
        super(settings);
        this.experienceDropped = experienceDropped;
    }

    @Override
    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, tool, dropExperience);
        if (dropExperience) {
            this.dropExperienceWhenMined(world, pos, tool, this.experienceDropped);
        }
    }
}

