/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.slot;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.slot.ItemStream;
import net.minecraft.loot.slot.SlotSource;

public record EmptySlotSourceType() implements SlotSource
{
    public static final MapCodec<EmptySlotSourceType> CODEC = MapCodec.unit((Object)new EmptySlotSourceType());

    public MapCodec<EmptySlotSourceType> getCodec() {
        return CODEC;
    }

    @Override
    public ItemStream stream(LootContext context) {
        return ItemStream.EMPTY;
    }
}

