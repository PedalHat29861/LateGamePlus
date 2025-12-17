/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.tick;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickPriority;

public interface ScheduledTickView {
    public <T> OrderedTick<T> createOrderedTick(BlockPos var1, T var2, int var3, TickPriority var4);

    public <T> OrderedTick<T> createOrderedTick(BlockPos var1, T var2, int var3);

    public QueryableTickScheduler<Block> getBlockTickScheduler();

    default public void scheduleBlockTick(BlockPos pos, Block block, int delay, TickPriority priority) {
        this.getBlockTickScheduler().scheduleTick(this.createOrderedTick(pos, block, delay, priority));
    }

    default public void scheduleBlockTick(BlockPos pos, Block block, int delay) {
        this.getBlockTickScheduler().scheduleTick(this.createOrderedTick(pos, block, delay));
    }

    public QueryableTickScheduler<Fluid> getFluidTickScheduler();

    default public void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
        this.getFluidTickScheduler().scheduleTick(this.createOrderedTick(pos, fluid, delay, priority));
    }

    default public void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay) {
        this.getFluidTickScheduler().scheduleTick(this.createOrderedTick(pos, fluid, delay));
    }
}

