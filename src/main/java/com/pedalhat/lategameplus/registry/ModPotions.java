package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
    public static RegistryEntry.Reference<Potion> VOLCANIC_MASTER;
    public static RegistryEntry.Reference<Potion> STRONG_VOLCANIC_MASTER;
    public static RegistryEntry.Reference<Potion> LONG_VOLCANIC_MASTER;


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
        VOLCANIC_MASTER = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "volcanic_master"),
            new Potion(
                "lategameplus.volcanic_master",
                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6400, 0),
                new StatusEffectInstance(ModEffects.LAVA_VISION, 6000, 0),
                new StatusEffectInstance(ModEffects.VOLCANIC_INFUSION, 6000, 0)
            )
        );
        STRONG_VOLCANIC_MASTER = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "strong_volcanic_master"),
            new Potion(
                "lategameplus.strong_volcanic_master",
                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 4000, 0),
                new StatusEffectInstance(ModEffects.LAVA_VISION, 3600, 1),
                new StatusEffectInstance(ModEffects.VOLCANIC_INFUSION, 3600, 1)
            )
        );
        LONG_VOLCANIC_MASTER = Registry.registerReference(
            Registries.POTION,
            Identifier.of(LateGamePlus.MOD_ID, "long_volcanic_master"),
            new Potion(
                "lategameplus.long_volcanic_master",
                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 12400, 0),
                new StatusEffectInstance(ModEffects.LAVA_VISION, 12000, 0),
                new StatusEffectInstance(ModEffects.VOLCANIC_INFUSION, 12000, 0)
            )
        );


        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            var registry = (FabricBrewingRecipeRegistryBuilder) builder;

            // Base recipes
            registry.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(ModItems.BLIND_SHRIMP), ModPotions.LAVA_VISION);
            registry.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(ModItems.POMPEII_WORM), ModPotions.VOLCANIC_INFUSION);

            // Vanilla-style variants of Lava Vision
            registry.registerPotionRecipe(ModPotions.LAVA_VISION, Ingredient.ofItems(Items.REDSTONE), ModPotions.LONG_LAVA_VISION);
            registry.registerPotionRecipe(ModPotions.LAVA_VISION, Ingredient.ofItems(Items.GLOWSTONE_DUST), ModPotions.STRONG_LAVA_VISION);

            // Vanilla-style variants of Volcanic Infusion
            registry.registerPotionRecipe(ModPotions.VOLCANIC_INFUSION, Ingredient.ofItems(Items.REDSTONE), ModPotions.LONG_VOLCANIC_INFUSION);
            registry.registerPotionRecipe(ModPotions.VOLCANIC_INFUSION, Ingredient.ofItems(Items.GLOWSTONE_DUST), ModPotions.STRONG_VOLCANIC_INFUSION);

            // Super Potion recipe (using both ingredients)
            registry.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(ModItems.VOLCANIC_CONCOCTION), ModPotions.VOLCANIC_MASTER);

            // Vanilla-style variants of Super Volcanic
            registry.registerPotionRecipe(ModPotions.VOLCANIC_MASTER, Ingredient.ofItems(Items.REDSTONE), ModPotions.LONG_VOLCANIC_MASTER);
            registry.registerPotionRecipe(ModPotions.VOLCANIC_MASTER, Ingredient.ofItems(Items.GLOWSTONE_DUST), ModPotions.STRONG_VOLCANIC_MASTER);
        });
    }
}
