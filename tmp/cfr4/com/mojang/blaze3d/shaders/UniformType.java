/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.shaders;

public enum UniformType {
    UNIFORM_BUFFER("ubo"),
    TEXEL_BUFFER("utb");

    final String name;

    private UniformType(String name) {
        this.name = name;
    }
}

