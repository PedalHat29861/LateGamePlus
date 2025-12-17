/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jspecify.annotations.Nullable;

public interface FluidDrainable {
    public ItemStack tryDrainFluid(@Nullable LivingEntity var1, WorldAccess var2, BlockPos var3, BlockState var4);

    public Optional<SoundEvent> getBucketFillSound();
}

