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
 * Special crafting recipe that converts a lodestone-bound compass plus
 * shaped ingredients into a Lodestone Warp item. The resulting item inherits
 * the {@link DataComponentTypes#LODESTONE_TRACKER} from the center compass.
 *
 * Pattern (3x3):
 *  D Y D
 *  N C N
 *  D E D
 *  D = Diamond, Y = Ender Eye, E = Ender Pearl, N = Netherite Nugget, C = Lodestone-bound Compass
 */
public class WarpFromLodestoneCompassRecipe extends SpecialCraftingRecipe {

    public WarpFromLodestoneCompassRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    private static int idx(int x, int y, int w) {
        return y * w + x;
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getWidth() != 3 || input.getHeight() != 3) return false;

        List<ItemStack> s = input.getStacks();

        if (!s.get(idx(0,0,3)).isOf(Items.DIAMOND))     return false;
        if (!s.get(idx(1,0,3)).isOf(Items.ENDER_EYE))   return false;
        if (!s.get(idx(2,0,3)).isOf(Items.DIAMOND))     return false;

        if (!s.get(idx(0,1,3)).isOf(ModItems.NETHERITE_NUGGET)) return false;

        ItemStack compass = s.get(idx(1,1,3));
        if (!compass.isOf(Items.COMPASS)) return false;
        LodestoneTrackerComponent tracker = compass.get(DataComponentTypes.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) return false;

        if (!s.get(idx(2,1,3)).isOf(ModItems.NETHERITE_NUGGET)) return false;

        if (!s.get(idx(0,2,3)).isOf(Items.DIAMOND))     return false;
        if (!s.get(idx(1,2,3)).isOf(Items.ENDER_PEARL)) return false;
        if (!s.get(idx(2,2,3)).isOf(Items.DIAMOND))     return false;

        return true;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack compass = input.getStacks().get(idx(1,1,3));
        LodestoneTrackerComponent tracker = compass.get(DataComponentTypes.LODESTONE_TRACKER);

        ItemStack result = new ItemStack(ModItems.LODESTONE_WARP);
        if (tracker != null && tracker.target().isPresent()) {
            result.set(DataComponentTypes.LODESTONE_TRACKER, tracker);
        }
        return result;
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return ModRecipes.WARP_FROM_LODESTONE_COMPASS;
    }
}
