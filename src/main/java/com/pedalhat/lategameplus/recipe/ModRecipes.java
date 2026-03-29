package com.pedalhat.lategameplus.recipe;

import com.pedalhat.lategameplus.LateGamePlus;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Registers custom recipe serializers used by the mod.
 */
public class ModRecipes {

    /** Serializer for crafting a {@code Lodestone Warp} from a lodestone-bound
     * compass and an ender pearl. */
    public static final RecipeSerializer<WarpFromLodestoneCompassRecipe> WARP_FROM_LODESTONE_COMPASS =
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                    Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "crafting_special_lodestone_warp"),
                    WarpFromLodestoneCompassRecipe.SERIALIZER);

    public static final RecipeType<FusionForgeRecipe> FUSION_FORGE =
            Registry.register(BuiltInRegistries.RECIPE_TYPE,
                    Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "fusion_forge"),
                    new RecipeType<>() {
                        @Override
                        public String toString() {
                            return LateGamePlus.MOD_ID + ":fusion_forge";
                        }
                    });

    public static final RecipeSerializer<FusionForgeRecipe> FUSION_FORGE_SERIALIZER =
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                    Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "fusion_forge"),
                    FusionForgeRecipeSerializer.INSTANCE);

    // No custom smithing serializer is needed; we patch smithing craft via mixin for NBT copy.

    /** Called from mod initialization to ensure class loading. */
    public static void init() {
        
    }
}

