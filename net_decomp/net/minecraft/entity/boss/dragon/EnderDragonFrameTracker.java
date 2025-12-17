/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.boss.dragon;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import net.minecraft.util.math.MathHelper;

public class EnderDragonFrameTracker {
    public static final int field_52489 = 64;
    private static final int field_52490 = 63;
    private final Frame[] frames = new Frame[64];
    private int currentIndex = -1;

    public EnderDragonFrameTracker() {
        Arrays.fill(this.frames, new Frame(0.0, 0.0f));
    }

    public void copyFrom(EnderDragonFrameTracker other) {
        System.arraycopy(other.frames, 0, this.frames, 0, 64);
        this.currentIndex = other.currentIndex;
    }

    public void tick(double y, float yaw) {
        Frame frame = new Frame(y, yaw);
        if (this.currentIndex < 0) {
            Arrays.fill(this.frames, frame);
        }
        if (++this.currentIndex == 64) {
            this.currentIndex = 0;
        }
        this.frames[this.currentIndex] = frame;
    }

    public Frame getFrame(int age) {
        return this.frames[this.currentIndex - age & 0x3F];
    }

    public Frame getLerpedFrame(int age, float tickProgress) {
        Frame frame = this.getFrame(age);
        Frame frame2 = this.getFrame(age + 1);
        return new Frame(MathHelper.lerp((double)tickProgress, frame2.y, frame.y), MathHelper.lerpAngleDegrees(tickProgress, frame2.yRot, frame.yRot));
    }

    public static final class Frame
    extends Record {
        final double y;
        final float yRot;

        public Frame(double y, float yRot) {
            this.y = y;
            this.yRot = yRot;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Frame.class, "y;yRot", "y", "yRot"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Frame.class, "y;yRot", "y", "yRot"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Frame.class, "y;yRot", "y", "yRot"}, this, object);
        }

        public double y() {
            return this.y;
        }

        public float yRot() {
            return this.yRot;
        }
    }
}

