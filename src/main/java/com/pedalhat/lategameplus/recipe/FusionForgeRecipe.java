package com.pedalhat.lategameplus.recipe;

import net.minecraft.core.HolderLookup;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record FusionForgeRecipe(Ingredient inputA, Ingredient inputB, ItemStack output,
                                int cookTime, int fuelCost, float experience)
    implements Recipe<FusionForgeRecipeInput> {

    @Override
    public boolean matches(FusionForgeRecipeInput input, Level world) {
        ItemStack stackA = input.inputA();
        ItemStack stackB = input.inputB();
        boolean direct = inputA.test(stackA) && inputB.test(stackB);
        boolean swapped = inputA.test(stackB) && inputB.test(stackA);
        return direct || swapped;
    }

    @Override
    public ItemStack assemble(FusionForgeRecipeInput input) {
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
    public PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(List.of(
            Optional.of(inputA),
            Optional.of(inputB)
        ));
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.FURNACE_MISC;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
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
