/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public interface Attackable {
    public @Nullable LivingEntity getLastAttacker();
}

