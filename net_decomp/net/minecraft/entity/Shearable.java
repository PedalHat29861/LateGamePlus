/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;

public interface Shearable {
    public void sheared(ServerWorld var1, SoundCategory var2, ItemStack var3);

    public boolean isShearable();
}

