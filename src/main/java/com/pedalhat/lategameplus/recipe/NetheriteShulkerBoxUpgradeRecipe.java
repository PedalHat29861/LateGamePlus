package com.pedalhat.lategameplus.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;

/** Smithing recipe that copies components from the base shulker to the netherite shulker result. */
public class NetheriteShulkerBoxUpgradeRecipe extends SmithingTransformRecipe {
    public NetheriteShulkerBoxUpgradeRecipe(Optional<Ingredient> template, Ingredient base, Ingredient addition, ItemStack result) {
        super(template, base, addition, result);
    }

    @Override
    public ItemStack craft(SmithingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack baseStack = input.base();
        ItemStack result = super.craft(input, registries);
        // Copy all components (including name and container data if present)
        result.applyComponentsFrom(baseStack.getComponents());
        return result;
    }

    @Override
    public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
        return ModRecipes.NETHERITE_SHULKER_BOX_UPGRADE;
    }
}

