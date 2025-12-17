/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe.display;

import net.minecraft.recipe.display.FurnaceRecipeDisplay;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapedCraftingRecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.display.StonecutterRecipeDisplay;
import net.minecraft.registry.Registry;

public class RecipeDisplayBootstrap {
    public static RecipeDisplay.Serializer<?> registerAndGetDefault(Registry<RecipeDisplay.Serializer<?>> registry) {
        Registry.register(registry, "crafting_shapeless", ShapelessCraftingRecipeDisplay.SERIALIZER);
        Registry.register(registry, "crafting_shaped", ShapedCraftingRecipeDisplay.SERIALIZER);
        Registry.register(registry, "furnace", FurnaceRecipeDisplay.SERIALIZER);
        Registry.register(registry, "stonecutter", StonecutterRecipeDisplay.SERIALIZER);
        return Registry.register(registry, "smithing", SmithingRecipeDisplay.SERIALIZER);
    }
}

