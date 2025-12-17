/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.provider.nbt;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.provider.nbt.LootNbtProvider;

public record LootNbtProviderType(MapCodec<? extends LootNbtProvider> codec) {
}

