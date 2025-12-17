/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.inventory;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.Codecs;

public record StackWithSlot(int slot, ItemStack stack) {
    public static final Codec<StackWithSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.UNSIGNED_BYTE.fieldOf("Slot").orElse((Object)0).forGetter(StackWithSlot::slot), (App)ItemStack.MAP_CODEC.forGetter(StackWithSlot::stack)).apply((Applicative)instance, StackWithSlot::new));

    public boolean isValidSlot(int slots) {
        return this.slot >= 0 && this.slot < slots;
    }
}

