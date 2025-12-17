/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;

public class CraftingDecoratedPotRecipe
extends SpecialCraftingRecipe {
    public CraftingDecoratedPotRecipe(CraftingRecipeCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    private static ItemStack getBack(CraftingRecipeInput input) {
        return input.getStackInSlot(1, 0);
    }

    private static ItemStack getLeft(CraftingRecipeInput input) {
        return input.getStackInSlot(0, 1);
    }

    private static ItemStack getRight(CraftingRecipeInput input) {
        return input.getStackInSlot(2, 1);
    }

    private static ItemStack getFront(CraftingRecipeInput input) {
        return input.getStackInSlot(1, 2);
    }

    @Override
    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        if (craftingRecipeInput.getWidth() != 3 || craftingRecipeInput.getHeight() != 3 || craftingRecipeInput.getStackCount() != 4) {
            return false;
        }
        return CraftingDecoratedPotRecipe.getBack(craftingRecipeInput).isIn(ItemTags.DECORATED_POT_INGREDIENTS) && CraftingDecoratedPotRecipe.getLeft(craftingRecipeInput).isIn(ItemTags.DECORATED_POT_INGREDIENTS) && CraftingDecoratedPotRecipe.getRight(craftingRecipeInput).isIn(ItemTags.DECORATED_POT_INGREDIENTS) && CraftingDecoratedPotRecipe.getFront(craftingRecipeInput).isIn(ItemTags.DECORATED_POT_INGREDIENTS);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        Sherds sherds = new Sherds(CraftingDecoratedPotRecipe.getBack(craftingRecipeInput).getItem(), CraftingDecoratedPotRecipe.getLeft(craftingRecipeInput).getItem(), CraftingDecoratedPotRecipe.getRight(craftingRecipeInput).getItem(), CraftingDecoratedPotRecipe.getFront(craftingRecipeInput).getItem());
        return DecoratedPotBlockEntity.getStackWith(sherds);
    }

    @Override
    public RecipeSerializer<CraftingDecoratedPotRecipe> getSerializer() {
        return RecipeSerializer.CRAFTING_DECORATED_POT;
    }
}

