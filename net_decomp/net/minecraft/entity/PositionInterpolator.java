/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class PositionInterpolator {
    public static final int DEFAULT_INTERPOLATION_DURATION = 3;
    private final Entity entity;
    private int lerpDuration;
    private final Data data = new Data(0, Vec3d.ZERO, 0.0f, 0.0f);
    private @Nullable Vec3d lastPos;
    private @Nullable Vec2f lastRotation;
    private final @Nullable Consumer<PositionInterpolator> callback;

    public PositionInterpolator(Entity entity) {
        this(entity, 3, null);
    }

    public PositionInterpolator(Entity entity, int lerpDuration) {
        this(entity, lerpDuration, null);
    }

    public PositionInterpolator(Entity entity, @Nullable Consumer<PositionInterpolator> callback) {
        this(entity, 3, callback);
    }

    public PositionInterpolator(Entity entity, int lerpDuration, @Nullable Consumer<PositionInterpolator> callback) {
        this.lerpDuration = lerpDuration;
        this.entity = entity;
        this.callback = callback;
    }

    public Vec3d getLerpedPos() {
        return this.data.step > 0 ? this.data.pos : this.entity.getEntityPos();
    }

    public float getLerpedYaw() {
        return this.data.step > 0 ? this.data.yaw : this.entity.getYaw();
    }

    public float getLerpedPitch() {
        return this.data.step > 0 ? this.data.pitch : this.entity.getPitch();
    }

    public void refreshPositionAndAngles(Vec3d pow, float yaw, float pitch) {
        if (this.lerpDuration == 0) {
            this.entity.refreshPositionAndAngles(pow, yaw, pitch);
            this.clear();
            return;
        }
        if (this.isInterpolating() && Objects.equals(Float.valueOf(this.getLerpedYaw()), Float.valueOf(yaw)) && Objects.equals(Float.valueOf(this.getLerpedPitch()), Float.valueOf(pitch)) && Objects.equals(this.getLerpedPos(), pow)) {
            return;
        }
        this.data.step = this.lerpDuration;
        this.data.pos = pow;
        this.data.yaw = yaw;
        this.data.pitch = pitch;
        this.lastPos = this.entity.getEntityPos();
        this.lastRotation = new Vec2f(this.entity.getPitch(), this.entity.getYaw());
        if (this.callback != null) {
            this.callback.accept(this);
        }
    }

    public boolean isInterpolating() {
        return this.data.step > 0;
    }

    public void setLerpDuration(int lerpDuration) {
        this.lerpDuration = lerpDuration;
    }

    public void tick() {
        if (!this.isInterpolating()) {
            this.clear();
            return;
        }
        double d = 1.0 / (double)this.data.step;
        if (this.lastPos != null) {
            Vec3d vec3d = this.entity.getEntityPos().subtract(this.lastPos);
            if (this.entity.getEntityWorld().isSpaceEmpty(this.entity, this.entity.calculateDefaultBoundingBox(this.data.pos.add(vec3d)))) {
                this.data.addPos(vec3d);
            }
        }
        if (this.lastRotation != null) {
            float f = this.entity.getYaw() - this.lastRotation.y;
            float g = this.entity.getPitch() - this.lastRotation.x;
            this.data.addRotation(f, g);
        }
        double e = MathHelper.lerp(d, this.entity.getX(), this.data.pos.x);
        double h = MathHelper.lerp(d, this.entity.getY(), this.data.pos.y);
        double i = MathHelper.lerp(d, this.entity.getZ(), this.data.pos.z);
        Vec3d vec3d2 = new Vec3d(e, h, i);
        float j = (float)MathHelper.lerpAngleDegrees(d, (double)this.entity.getYaw(), (double)this.data.yaw);
        float k = (float)MathHelper.lerp(d, (double)this.entity.getPitch(), (double)this.data.pitch);
        this.entity.setPosition(vec3d2);
        this.entity.setRotation(j, k);
        this.data.tick();
        this.lastPos = vec3d2;
        this.lastRotation = new Vec2f(this.entity.getPitch(), this.entity.getYaw());
    }

    public void clear() {
        this.data.step = 0;
        this.lastPos = null;
        this.lastRotation = null;
    }

    static class Data {
        protected int step;
        Vec3d pos;
        float yaw;
        float pitch;

        Data(int step, Vec3d pos, float yaw, float pitch) {
            this.step = step;
            this.pos = pos;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public void tick() {
            --this.step;
        }

        public void addPos(Vec3d pos) {
            this.pos = this.pos.add(pos);
        }

        public void addRotation(float yaw, float pitch) {
            this.yaw += yaw;
            this.pitch += pitch;
        }
    }
}

