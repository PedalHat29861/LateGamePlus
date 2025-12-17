/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.entry;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.entry.LootPoolEntry;

public record LootPoolEntryType(MapCodec<? extends LootPoolEntry> codec) {
}

