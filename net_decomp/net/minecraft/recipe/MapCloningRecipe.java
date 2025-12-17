/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class MapCloningRecipe
extends SpecialCraftingRecipe {
    public MapCloningRecipe(CraftingRecipeCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    @Override
    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        if (craftingRecipeInput.getStackCount() < 2) {
            return false;
        }
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < craftingRecipeInput.size(); ++i) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.contains(DataComponentTypes.MAP_ID)) {
                if (bl2) {
                    return false;
                }
                bl2 = true;
                continue;
            }
            if (itemStack.isOf(Items.MAP)) {
                bl = true;
                continue;
            }
            return false;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int j = 0; j < craftingRecipeInput.size(); ++j) {
            ItemStack itemStack2 = craftingRecipeInput.getStackInSlot(j);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.contains(DataComponentTypes.MAP_ID)) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.isOf(Items.MAP)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (itemStack.isEmpty() || i < 1) {
            return ItemStack.EMPTY;
        }
        return itemStack.copyWithCount(i + 1);
    }

    @Override
    public RecipeSerializer<MapCloningRecipe> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}

