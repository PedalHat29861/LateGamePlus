/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface TestInstanceBlockFinder {
    public Stream<BlockPos> findTestPos();
}

