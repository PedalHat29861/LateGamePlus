/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.RegistryKey;

public interface RecipeManager {
    public RecipePropertySet getPropertySet(RegistryKey<RecipePropertySet> var1);

    public CuttingRecipeDisplay.Grouping<StonecuttingRecipe> getStonecutterRecipes();
}

