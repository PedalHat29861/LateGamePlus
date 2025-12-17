/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import java.util.ArrayList;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;

public class ArmorDyeRecipe
extends SpecialCraftingRecipe {
    public ArmorDyeRecipe(CraftingRecipeCategory craftingRecipeCategory) {
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
            if (itemStack.isIn(ItemTags.DYEABLE)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (itemStack.getItem() instanceof DyeItem) {
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ArrayList<DyeItem> list = new ArrayList<DyeItem>();
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingRecipeInput.size(); ++i) {
            ItemStack itemStack2 = craftingRecipeInput.getStackInSlot(i);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.isIn(ItemTags.DYEABLE)) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2.copy();
                continue;
            }
            Item item = itemStack2.getItem();
            if (item instanceof DyeItem) {
                DyeItem dyeItem = (DyeItem)item;
                list.add(dyeItem);
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (itemStack.isEmpty() || list.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return DyedColorComponent.setColor(itemStack, list);
    }

    @Override
    public RecipeSerializer<ArmorDyeRecipe> getSerializer() {
        return RecipeSerializer.ARMOR_DYE;
    }
}

