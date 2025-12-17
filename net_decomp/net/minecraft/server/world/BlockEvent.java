/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public record BlockEvent(BlockPos pos, Block block, int type, int data) {
    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{BlockEvent.class, "pos;block;paramA;paramB", "pos", "block", "type", "data"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BlockEvent.class, "pos;block;paramA;paramB", "pos", "block", "type", "data"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BlockEvent.class, "pos;block;paramA;paramB", "pos", "block", "type", "data"}, this, object);
    }
}

