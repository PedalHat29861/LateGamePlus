/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.component;

import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import org.jspecify.annotations.Nullable;

public interface ComponentsAccess {
    public <T> @Nullable T get(ComponentType<? extends T> var1);

    default public <T> T getOrDefault(ComponentType<? extends T> type, T fallback) {
        T object = this.get(type);
        return object != null ? object : fallback;
    }

    default public <T> @Nullable Component<T> getTyped(ComponentType<T> type) {
        T object = this.get(type);
        return object != null ? new Component<T>(type, object) : null;
    }
}

