/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import java.util.Optional;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.world.World;

public interface SmithingRecipe
extends Recipe<SmithingRecipeInput> {
    @Override
    default public RecipeType<SmithingRecipe> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public RecipeSerializer<? extends SmithingRecipe> getSerializer();

    @Override
    default public boolean matches(SmithingRecipeInput smithingRecipeInput, World world) {
        return Ingredient.matches(this.template(), smithingRecipeInput.template()) && this.base().test(smithingRecipeInput.base()) && Ingredient.matches(this.addition(), smithingRecipeInput.addition());
    }

    public Optional<Ingredient> template();

    public Ingredient base();

    public Optional<Ingredient> addition();

    @Override
    default public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategories.SMITHING;
    }
}

