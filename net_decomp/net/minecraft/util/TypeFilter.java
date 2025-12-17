/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import org.jspecify.annotations.Nullable;

public interface TypeFilter<B, T extends B> {
    public static <B, T extends B> TypeFilter<B, T> instanceOf(final Class<T> cls) {
        return new TypeFilter<B, T>(){

            @Override
            public @Nullable T downcast(B obj) {
                return cls.isInstance(obj) ? obj : null;
            }

            @Override
            public Class<? extends B> getBaseClass() {
                return cls;
            }
        };
    }

    public static <B, T extends B> TypeFilter<B, T> equals(final Class<T> cls) {
        return new TypeFilter<B, T>(){

            @Override
            public @Nullable T downcast(B obj) {
                return cls.equals(obj.getClass()) ? obj : null;
            }

            @Override
            public Class<? extends B> getBaseClass() {
                return cls;
            }
        };
    }

    public @Nullable T downcast(B var1);

    public Class<? extends B> getBaseClass();
}

