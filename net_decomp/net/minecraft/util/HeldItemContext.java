/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public interface HeldItemContext {
    public World getEntityWorld();

    public Vec3d getEntityPos();

    public float getBodyYaw();

    default public @Nullable LivingEntity getEntity() {
        return null;
    }

    public static HeldItemContext offseted(HeldItemContext context, Vec3d offset) {
        return new Offset(context, offset);
    }

    public record Offset(HeldItemContext owner, Vec3d offset) implements HeldItemContext
    {
        @Override
        public World getEntityWorld() {
            return this.owner.getEntityWorld();
        }

        @Override
        public Vec3d getEntityPos() {
            return this.owner.getEntityPos().add(this.offset);
        }

        @Override
        public float getBodyYaw() {
            return this.owner.getBodyYaw();
        }

        @Override
        public @Nullable LivingEntity getEntity() {
            return this.owner.getEntity();
        }
    }
}

