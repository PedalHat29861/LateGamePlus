/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.gen;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jspecify.annotations.Nullable;

public record ChainedBlockSource(ChunkNoiseSampler.BlockStateSampler[] samplers) implements ChunkNoiseSampler.BlockStateSampler
{
    @Override
    public @Nullable BlockState sample(DensityFunction.NoisePos pos) {
        for (ChunkNoiseSampler.BlockStateSampler blockStateSampler : this.samplers) {
            BlockState blockState = blockStateSampler.sample(pos);
            if (blockState == null) continue;
            return blockState;
        }
        return null;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChainedBlockSource.class, "materialRuleList", "samplers"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChainedBlockSource.class, "materialRuleList", "samplers"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChainedBlockSource.class, "materialRuleList", "samplers"}, this, object);
    }
}

