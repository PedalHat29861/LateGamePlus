/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface LoadedEntityProcessor {
    public static final LoadedEntityProcessor NOOP = entity -> entity;

    public @Nullable Entity process(Entity var1);
}

