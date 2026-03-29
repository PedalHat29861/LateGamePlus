/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.resource;

public interface ResourceHandle<T> {
    public static final ResourceHandle<?> INVALID_HANDLE = () -> {
        throw new IllegalStateException("Cannot dereference handle with no underlying resource");
    };

    public static <T> ResourceHandle<T> invalid() {
        return INVALID_HANDLE;
    }

    public T get();
}

