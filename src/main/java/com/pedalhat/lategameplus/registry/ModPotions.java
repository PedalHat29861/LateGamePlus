package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModPotions {
    public static RegistryEntry.Reference<Potion> LAVA_VISION;
    public static RegistryEntry.Reference<Potion> STRONG_LAVA_VISION;
    public static RegistryEntry.Reference<Potion> LONG_LAVA_VISION;
    public static RegistryEntry.Reference<Potion> VOLCANIC_INFUSION;
    public static RegistryEntry.Reference<Potion> STRONG_VOLCANIC_INFUSION;
    public static RegistryEntry.Reference<Potion> LONG_VOLCANIC_INFUSION;

    private ModPotions() {
    }

    public static void init() {
        LAVA_VISION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "lava_vision"),
            new Potion(
                "lategameplus.lava_vision",
                new StatusEffectInstance(ModEffects.LAVA_VISION, 3600, 0)
            )
        );
        STRONG_LAVA_VISION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "strong_lava_vision"),
            new Potion(
                "lategameplus.strong_lava_vision",
                new StatusEffectInstance(ModEffects.LAVA_VISION, 1800, 1)
            )
        );
        LONG_LAVA_VISION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "long_lava_vision"),
            new Potion(
                "lategameplus.long_lava_vision",
                new StatusEffectInstance(ModEffects.LAVA_VISION, 9600, 0)
            )
        );
        VOLCANIC_INFUSION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "volcanic_infusion"),
            new Potion(
                "lategameplus.volcanic_infusion",
                new StatusEffectInstance(ModEffects.VOLCANIC_INFUSION, 3600, 0)
            )
        );
        STRONG_VOLCANIC_INFUSION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "strong_volcanic_infusion"),
            new Potion(
                "lategameplus.strong_volcanic_infusion",
                new StatusEffectInstance(ModEffects.VOLCANIC_INFUSION, 1800, 1)
            )
        );
        LONG_VOLCANIC_INFUSION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "long_volcanic_infusion"),
            new Potion(
                "lategameplus.long_volcanic_infusion",
                new StatusEffectInstance(ModEffects.VOLCANIC_INFUSION, 9600, 0)
            )
        );

        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            FabricBrewingRecipeRegistryBuilder registry = (FabricBrewingRecipeRegistryBuilder) builder;
            registry.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(Items.DIRT), LAVA_VISION);
            registry.registerPotionRecipe(LAVA_VISION, Ingredient.ofItems(Items.GLOWSTONE_DUST), STRONG_LAVA_VISION);
            registry.registerPotionRecipe(LAVA_VISION, Ingredient.ofItems(Items.REDSTONE), LONG_LAVA_VISION);
            registry.registerPotionRecipe(LAVA_VISION, Ingredient.ofItems(Items.MAGMA_CREAM), VOLCANIC_INFUSION);
            registry.registerPotionRecipe(STRONG_LAVA_VISION, Ingredient.ofItems(Items.MAGMA_CREAM), STRONG_VOLCANIC_INFUSION);
            registry.registerPotionRecipe(LONG_LAVA_VISION, Ingredient.ofItems(Items.MAGMA_CREAM), LONG_VOLCANIC_INFUSION);
        });
    }
}
