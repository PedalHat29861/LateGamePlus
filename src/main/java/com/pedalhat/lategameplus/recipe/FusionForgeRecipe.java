package com.pedalhat.lategameplus.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public record FusionForgeRecipe(Ingredient inputA, Ingredient inputB, ItemStack output,
                                int cookTime, int fuelCost, float experience)
    implements Recipe<FusionForgeRecipeInput> {

    @Override
    public boolean matches(FusionForgeRecipeInput input, World world) {
        ItemStack stackA = input.inputA();
        ItemStack stackB = input.inputB();
        boolean direct = inputA.test(stackA) && inputB.test(stackB);
        boolean swapped = inputA.test(stackB) && inputB.test(stackA);
        return direct || swapped;
    }

    @Override
    public ItemStack craft(FusionForgeRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<FusionForgeRecipeInput>> getSerializer() {
        return ModRecipes.FUSION_FORGE_SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<FusionForgeRecipeInput>> getType() {
        return ModRecipes.FUSION_FORGE;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.forMultipleSlots(List.of(
            Optional.of(inputA),
            Optional.of(inputB)
        ));
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategories.FURNACE_MISC;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public Ingredient getInputA() {
        return inputA;
    }

    public Ingredient getInputB() {
        return inputB;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getFuelCost() {
        return fuelCost;
    }

    public float getExperience() {
        return experience;
    }
}
