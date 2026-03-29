/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.systems;

import java.util.OptionalLong;

public interface GpuQuery
extends AutoCloseable {
    public OptionalLong getValue();

    @Override
    public void close();
}

