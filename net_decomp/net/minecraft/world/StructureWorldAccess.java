/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.jspecify.annotations.Nullable;

public interface StructureWorldAccess
extends ServerWorldAccess {
    public long getSeed();

    default public boolean isValidForSetBlock(BlockPos pos) {
        return true;
    }

    default public void setCurrentlyGeneratingStructureName(@Nullable Supplier<String> structureName) {
    }
}

