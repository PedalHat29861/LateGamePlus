/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public abstract class ServerConfigEntry<T> {
    private final @Nullable T key;

    public ServerConfigEntry(@Nullable T key) {
        this.key = key;
    }

    public @Nullable T getKey() {
        return this.key;
    }

    boolean isInvalid() {
        return false;
    }

    protected abstract void write(JsonObject var1);
}

