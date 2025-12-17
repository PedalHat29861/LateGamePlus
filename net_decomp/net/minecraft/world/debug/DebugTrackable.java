/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.debug;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.debug.DebugSubscriptionType;
import org.jspecify.annotations.Nullable;

public interface DebugTrackable {
    public void registerTracking(ServerWorld var1, Tracker var2);

    public static interface DebugDataSupplier<T> {
        public @Nullable T get();
    }

    public static interface Tracker {
        public <T> void track(DebugSubscriptionType<T> var1, DebugDataSupplier<T> var2);
    }
}

