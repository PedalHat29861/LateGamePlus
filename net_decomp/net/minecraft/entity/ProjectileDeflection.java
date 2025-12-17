/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ProjectileDeflection {
    public static final ProjectileDeflection NONE = (projectile, hitEntity, random) -> {};
    public static final ProjectileDeflection SIMPLE = (projectile, hitEntity, random) -> {
        float f = 170.0f + random.nextFloat() * 20.0f;
        projectile.setVelocity(projectile.getVelocity().multiply(-0.5));
        projectile.setYaw(projectile.getYaw() + f);
        projectile.lastYaw += f;
        projectile.velocityDirty = true;
    };
    public static final ProjectileDeflection REDIRECTED = (projectile, hitEntity, random) -> {
        if (hitEntity != null) {
            Vec3d vec3d = hitEntity.getRotationVector();
            projectile.setVelocity(vec3d);
            projectile.velocityDirty = true;
        }
    };
    public static final ProjectileDeflection TRANSFER_VELOCITY_DIRECTION = (projectile, hitEntity, random) -> {
        if (hitEntity != null) {
            Vec3d vec3d = hitEntity.getVelocity().normalize();
            projectile.setVelocity(vec3d);
            projectile.velocityDirty = true;
        }
    };

    public void deflect(ProjectileEntity var1, @Nullable Entity var2, Random var3);
}

