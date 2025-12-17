/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public interface ContainerUser {
    public boolean isViewingContainerAt(ViewerCountManager var1, BlockPos var2);

    public double getContainerInteractionRange();

    default public LivingEntity asLivingEntity() {
        if (this instanceof LivingEntity) {
            return (LivingEntity)((Object)this);
        }
        throw new IllegalStateException("A container user must be a LivingEntity");
    }
}

