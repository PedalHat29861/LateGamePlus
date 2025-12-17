/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.registry.RegistryWrapper;

public interface ContextSwapper {
    public <T> DataResult<T> swapContext(Codec<T> var1, T var2, RegistryWrapper.WrapperLookup var3);
}

