/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.inventory;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Objects;
import net.minecraft.inventory.StackReference;
import net.minecraft.loot.slot.ItemStream;
import org.jspecify.annotations.Nullable;

public interface StackReferenceGetter {
    public @Nullable StackReference getStackReference(int var1);

    default public ItemStream getStackReferences(IntList slots) {
        List<StackReference> list = slots.intStream().mapToObj(this::getStackReference).filter(Objects::nonNull).toList();
        return ItemStream.of(list);
    }
}

