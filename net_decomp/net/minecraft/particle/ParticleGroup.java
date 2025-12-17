/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.particle;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;

public record ParticleGroup(int maxCount) {
    public static final ParticleGroup SPORE_BLOSSOM_AIR = new ParticleGroup(1000);

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ParticleGroup.class, "limit", "maxCount"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ParticleGroup.class, "limit", "maxCount"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ParticleGroup.class, "limit", "maxCount"}, this, object);
    }
}

