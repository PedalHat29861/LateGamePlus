/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record UseRemainderComponent(ItemStack convertInto) {
    public static final Codec<UseRemainderComponent> CODEC = ItemStack.CODEC.xmap(UseRemainderComponent::new, UseRemainderComponent::convertInto);
    public static final PacketCodec<RegistryByteBuf, UseRemainderComponent> PACKET_CODEC = PacketCodec.tuple(ItemStack.PACKET_CODEC, UseRemainderComponent::convertInto, UseRemainderComponent::new);

    public ItemStack convert(ItemStack stack, int oldCount, boolean inCreative, StackInserter inserter) {
        if (inCreative) {
            return stack;
        }
        if (stack.getCount() >= oldCount) {
            return stack;
        }
        ItemStack itemStack = this.convertInto.copy();
        if (stack.isEmpty()) {
            return itemStack;
        }
        inserter.apply(itemStack);
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        UseRemainderComponent useRemainderComponent = (UseRemainderComponent)o;
        return ItemStack.areEqual(this.convertInto, useRemainderComponent.convertInto);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashCode(this.convertInto);
    }

    @FunctionalInterface
    public static interface StackInserter {
        public void apply(ItemStack var1);
    }
}

