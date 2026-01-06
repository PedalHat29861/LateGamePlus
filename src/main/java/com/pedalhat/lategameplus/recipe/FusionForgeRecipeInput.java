package com.pedalhat.lategameplus.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record FusionForgeRecipeInput(ItemStack inputA, ItemStack inputB) implements RecipeInput {

    @Override
    public ItemStack getStackInSlot(int slot) {
        return switch (slot) {
            case 0 -> inputA;
            case 1 -> inputB;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
