/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.math;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record BlockPointer(ServerWorld world, BlockPos pos, BlockState state, DispenserBlockEntity blockEntity) {
    public Vec3d centerPos() {
        return this.pos.toCenterPos();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{BlockPointer.class, "level;pos;state;blockEntity", "world", "pos", "state", "blockEntity"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BlockPointer.class, "level;pos;state;blockEntity", "world", "pos", "state", "blockEntity"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BlockPointer.class, "level;pos;state;blockEntity", "world", "pos", "state", "blockEntity"}, this, object);
    }
}

