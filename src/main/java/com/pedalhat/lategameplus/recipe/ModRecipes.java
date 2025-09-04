package com.pedalhat.lategameplus.recipe;

import com.pedalhat.lategameplus.LateGamePlus;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers custom recipe serializers used by the mod.
 */
public class ModRecipes {

    /** Serializer for crafting a {@code Lodestone Warp} from a lodestone-bound
     * compass and an ender pearl. */
    public static final RecipeSerializer<WarpFromLodestoneCompassRecipe> WARP_FROM_LODESTONE_COMPASS =
            Registry.register(Registries.RECIPE_SERIALIZER,
                    Identifier.of(LateGamePlus.MOD_ID, "crafting_special_lodestone_warp"),
                    new SpecialCraftingRecipe.SpecialRecipeSerializer<>(WarpFromLodestoneCompassRecipe::new));

    // No custom smithing serializer is needed; we patch smithing craft via mixin for NBT copy.

    /** Called from mod initialization to ensure class loading. */
    public static void init() {
        // no-op
    }
}

