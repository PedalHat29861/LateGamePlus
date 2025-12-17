/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;

public class SmeltingRecipe
extends AbstractCookingRecipe {
    public SmeltingRecipe(String string, CookingRecipeCategory cookingRecipeCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(string, cookingRecipeCategory, ingredient, itemStack, f, i);
    }

    @Override
    protected Item getCookerItem() {
        return Items.FURNACE;
    }

    @Override
    public RecipeSerializer<SmeltingRecipe> getSerializer() {
        return RecipeSerializer.SMELTING;
    }

    @Override
    public RecipeType<SmeltingRecipe> getType() {
        return RecipeType.SMELTING;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return switch (this.getCategory()) {
            default -> throw new MatchException(null, null);
            case CookingRecipeCategory.BLOCKS -> RecipeBookCategories.FURNACE_BLOCKS;
            case CookingRecipeCategory.FOOD -> RecipeBookCategories.FURNACE_FOOD;
            case CookingRecipeCategory.MISC -> RecipeBookCategories.FURNACE_MISC;
        };
    }
}

