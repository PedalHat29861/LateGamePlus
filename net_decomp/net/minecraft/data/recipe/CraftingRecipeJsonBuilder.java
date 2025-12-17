/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.recipe;

import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public interface CraftingRecipeJsonBuilder {
    public static final Identifier ROOT = Identifier.ofVanilla("recipes/root");

    public CraftingRecipeJsonBuilder criterion(String var1, AdvancementCriterion<?> var2);

    public CraftingRecipeJsonBuilder group(@Nullable String var1);

    public Item getOutputItem();

    public void offerTo(RecipeExporter var1, RegistryKey<Recipe<?>> var2);

    default public void offerTo(RecipeExporter exporter) {
        this.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, CraftingRecipeJsonBuilder.getItemId(this.getOutputItem())));
    }

    default public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier identifier = CraftingRecipeJsonBuilder.getItemId(this.getOutputItem());
        Identifier identifier2 = Identifier.of(recipePath);
        if (identifier2.equals(identifier)) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        this.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, identifier2));
    }

    public static Identifier getItemId(ItemConvertible item) {
        return Registries.ITEM.getId(item.asItem());
    }

    public static CraftingRecipeCategory toCraftingCategory(RecipeCategory category) {
        return switch (category) {
            case RecipeCategory.BUILDING_BLOCKS -> CraftingRecipeCategory.BUILDING;
            case RecipeCategory.TOOLS, RecipeCategory.COMBAT -> CraftingRecipeCategory.EQUIPMENT;
            case RecipeCategory.REDSTONE -> CraftingRecipeCategory.REDSTONE;
            default -> CraftingRecipeCategory.MISC;
        };
    }
}

