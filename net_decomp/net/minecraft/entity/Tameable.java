/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public interface Tameable {
    public @Nullable LazyEntityReference<LivingEntity> getOwnerReference();

    public World getEntityWorld();

    default public @Nullable LivingEntity getOwner() {
        return LazyEntityReference.getLivingEntity(this.getOwnerReference(), this.getEntityWorld());
    }

    default public @Nullable LivingEntity getTopLevelOwner() {
        ObjectArraySet set = new ObjectArraySet();
        LivingEntity livingEntity = this.getOwner();
        set.add(this);
        while (livingEntity instanceof Tameable) {
            Tameable tameable = (Tameable)((Object)livingEntity);
            LivingEntity livingEntity2 = tameable.getOwner();
            if (set.contains(livingEntity2)) {
                return null;
            }
            set.add(livingEntity);
            livingEntity = tameable.getOwner();
        }
        return livingEntity;
    }
}

