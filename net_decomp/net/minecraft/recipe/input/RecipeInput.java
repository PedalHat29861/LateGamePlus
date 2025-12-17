/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe.input;

import net.minecraft.item.ItemStack;

public interface RecipeInput {
    public ItemStack getStackInSlot(int var1);

    public int size();

    default public boolean isEmpty() {
        for (int i = 0; i < this.size(); ++i) {
            if (this.getStackInSlot(i).isEmpty()) continue;
            return false;
        }
        return true;
    }
}

