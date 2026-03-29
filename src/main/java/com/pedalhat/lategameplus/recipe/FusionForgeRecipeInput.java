package com.pedalhat.lategameplus.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record FusionForgeRecipeInput(ItemStack inputA, ItemStack inputB) implements RecipeInput {

    @Override
    public ItemStack getItem(int slot) {
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
