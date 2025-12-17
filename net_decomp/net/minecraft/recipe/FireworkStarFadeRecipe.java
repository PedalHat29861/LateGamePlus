/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 */
package net.minecraft.recipe;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class FireworkStarFadeRecipe
extends SpecialCraftingRecipe {
    private static final Ingredient INPUT_STAR = Ingredient.ofItem(Items.FIREWORK_STAR);

    public FireworkStarFadeRecipe(CraftingRecipeCategory craftingRecipeCategory) {
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
            if (itemStack.getItem() instanceof DyeItem) {
                bl = true;
                continue;
            }
            if (INPUT_STAR.test(itemStack)) {
                if (bl2) {
                    return false;
                }
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        IntArrayList intList = new IntArrayList();
        ItemStack itemStack = null;
        for (int i = 0; i < craftingRecipeInput.size(); ++i) {
            ItemStack itemStack2 = craftingRecipeInput.getStackInSlot(i);
            Item item = itemStack2.getItem();
            if (item instanceof DyeItem) {
                DyeItem dyeItem = (DyeItem)item;
                intList.add(dyeItem.getColor().getFireworkColor());
                continue;
            }
            if (!INPUT_STAR.test(itemStack2)) continue;
            itemStack = itemStack2.copyWithCount(1);
        }
        if (itemStack == null || intList.isEmpty()) {
            return ItemStack.EMPTY;
        }
        itemStack.apply(DataComponentTypes.FIREWORK_EXPLOSION, FireworkExplosionComponent.DEFAULT, intList, FireworkExplosionComponent::withFadeColors);
        return itemStack;
    }

    @Override
    public RecipeSerializer<FireworkStarFadeRecipe> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR_FADE;
    }
}

