package com.pedalhat.lategameplus.recipe;

import com.pedalhat.lategameplus.LateGamePlus;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingTransformRecipe;
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

    /** Serializer for upgrading shulker boxes to netherite shulker boxes, copying components. */
    public static final RecipeSerializer<SmithingTransformRecipe> NETHERITE_SHULKER_BOX_UPGRADE =
            Registry.register(Registries.RECIPE_SERIALIZER,
                    Identifier.of(LateGamePlus.MOD_ID, "netherite_shulker_box_upgrade"),
                    new net.minecraft.recipe.SmithingTransformRecipe.Serializer<>(NetheriteShulkerBoxUpgradeRecipe::new));

    /** Called from mod initialization to ensure class loading. */
    public static void init() {
        // no-op
    }
}

