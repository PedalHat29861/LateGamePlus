/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.provider.number;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.provider.number.LootNumberProvider;

public record LootNumberProviderType(MapCodec<? extends LootNumberProvider> codec) {
}

