/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe.input;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record SingleStackRecipeInput(ItemStack item) implements RecipeInput
{
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot != 0) {
            throw new IllegalArgumentException("No item for index " + slot);
        }
        return this.item;
    }

    @Override
    public int size() {
        return 1;
    }
}

