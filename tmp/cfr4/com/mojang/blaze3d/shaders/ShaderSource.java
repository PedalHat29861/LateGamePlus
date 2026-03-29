/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.Identifier
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ShaderSource {
    public @Nullable String get(Identifier var1, ShaderType var2);
}

