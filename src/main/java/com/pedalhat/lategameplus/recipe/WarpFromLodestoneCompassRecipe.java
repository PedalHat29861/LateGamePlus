package com.pedalhat.lategameplus.recipe;

import com.pedalhat.lategameplus.registry.ModItems;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Special crafting recipe that converts a lodestone-bound compass and an ender
 * pearl into a Lodestone Warp item. The resulting item inherits the
 * {@link DataComponentTypes#LODESTONE_TRACKER} component from the compass
 * used in the crafting grid.
 */
public class WarpFromLodestoneCompassRecipe extends SpecialCraftingRecipe {

    public WarpFromLodestoneCompassRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        ItemStack compass = ItemStack.EMPTY;
        ItemStack pearl = ItemStack.EMPTY;
        List<ItemStack> stacks = input.getStacks();

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;

            if (stack.isOf(Items.ENDER_PEARL) && pearl.isEmpty()) {
                pearl = stack;
            } else if (stack.isOf(Items.COMPASS) && compass.isEmpty()) {
                LodestoneTrackerComponent tracker = stack.get(DataComponentTypes.LODESTONE_TRACKER);
                if (tracker != null && tracker.target().isPresent()) {
                    compass = stack;
                } else {
                    return false; // Compass without lodestone data
                }
            } else {
                return false; // Unexpected ingredient
            }
        }

        return !compass.isEmpty() && !pearl.isEmpty();
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        for (ItemStack stack : input.getStacks()) {
            if (stack.isOf(Items.COMPASS)) {
                LodestoneTrackerComponent tracker = stack.get(DataComponentTypes.LODESTONE_TRACKER);
                ItemStack result = new ItemStack(ModItems.LODESTONE_WARP);
                if (tracker != null && tracker.target().isPresent()) {
                    result.set(DataComponentTypes.LODESTONE_TRACKER, tracker);
                }
                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return ModRecipes.WARP_FROM_LODESTONE_COMPASS;
    }
}

