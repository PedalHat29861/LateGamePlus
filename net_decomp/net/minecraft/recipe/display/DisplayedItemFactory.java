/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe.display;

import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

public interface DisplayedItemFactory<T> {

    public static interface FromRemainder<T>
    extends DisplayedItemFactory<T> {
        public T toDisplayed(T var1, List<T> var2);
    }

    public static interface FromStack<T>
    extends DisplayedItemFactory<T> {
        default public T toDisplayed(RegistryEntry<Item> item) {
            return this.toDisplayed(new ItemStack(item));
        }

        default public T toDisplayed(Item item) {
            return this.toDisplayed(new ItemStack(item));
        }

        public T toDisplayed(ItemStack var1);
    }
}

