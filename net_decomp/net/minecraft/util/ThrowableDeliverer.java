/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import org.jspecify.annotations.Nullable;

public class ThrowableDeliverer<T extends Throwable> {
    private @Nullable T throwable;

    public void add(T throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        } else {
            ((Throwable)this.throwable).addSuppressed((Throwable)throwable);
        }
    }

    public void deliver() throws T {
        if (this.throwable != null) {
            throw this.throwable;
        }
    }
}

