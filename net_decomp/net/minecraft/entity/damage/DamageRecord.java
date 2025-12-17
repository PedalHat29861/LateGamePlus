/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.damage;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.FallLocation;
import org.jspecify.annotations.Nullable;

public record DamageRecord(DamageSource damageSource, float damage, @Nullable FallLocation fallLocation, float fallDistance) {
    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{DamageRecord.class, "source;damage;fallLocation;fallDistance", "damageSource", "damage", "fallLocation", "fallDistance"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DamageRecord.class, "source;damage;fallLocation;fallDistance", "damageSource", "damage", "fallLocation", "fallDistance"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DamageRecord.class, "source;damage;fallLocation;fallDistance", "damageSource", "damage", "fallLocation", "fallDistance"}, this, object);
    }
}

