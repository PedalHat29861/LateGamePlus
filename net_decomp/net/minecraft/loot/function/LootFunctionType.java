/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.function;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.function.LootFunction;

public record LootFunctionType<T extends LootFunction>(MapCodec<T> codec) {
}

