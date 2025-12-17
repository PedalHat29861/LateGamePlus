/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.predicate.entity;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.SlotRanges;
import net.minecraft.inventory.StackReference;
import net.minecraft.inventory.StackReferenceGetter;
import net.minecraft.predicate.item.ItemPredicate;

public record SlotsPredicate(Map<SlotRange, ItemPredicate> slots) {
    public static final Codec<SlotsPredicate> CODEC = Codec.unboundedMap(SlotRanges.CODEC, ItemPredicate.CODEC).xmap(SlotsPredicate::new, SlotsPredicate::slots);

    public boolean matches(StackReferenceGetter stackReferenceGetter) {
        for (Map.Entry<SlotRange, ItemPredicate> entry : this.slots.entrySet()) {
            if (SlotsPredicate.matches(stackReferenceGetter, entry.getValue(), entry.getKey().getSlotIds())) continue;
            return false;
        }
        return true;
    }

    private static boolean matches(StackReferenceGetter stackReferenceGetter, ItemPredicate itemPredicate, IntList slotIds) {
        for (int i = 0; i < slotIds.size(); ++i) {
            int j = slotIds.getInt(i);
            StackReference stackReference = stackReferenceGetter.getStackReference(j);
            if (stackReference == null || !itemPredicate.test(stackReference.get())) continue;
            return true;
        }
        return false;
    }
}

