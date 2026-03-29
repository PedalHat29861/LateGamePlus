package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;

import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

public final class ModPotions {
    public static Holder.Reference<Potion> LAVA_VISION;
    public static Holder.Reference<Potion> STRONG_LAVA_VISION;
    public static Holder.Reference<Potion> LONG_LAVA_VISION;
    public static Holder.Reference<Potion> VOLCANIC_INFUSION;
    public static Holder.Reference<Potion> STRONG_VOLCANIC_INFUSION;
    public static Holder.Reference<Potion> LONG_VOLCANIC_INFUSION;
    public static Holder.Reference<Potion> VOLCANIC_MASTER;
    public static Holder.Reference<Potion> STRONG_VOLCANIC_MASTER;
    public static Holder.Reference<Potion> LONG_VOLCANIC_MASTER;


    private ModPotions() {
    }

    public static void init() {
        LAVA_VISION = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "lava_vision"),
            new Potion(
                "lategameplus.lava_vision",
                new MobEffectInstance(ModEffects.LAVA_VISION, 3600, 0)
            )
        );
        STRONG_LAVA_VISION = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "strong_lava_vision"),
            new Potion(
                "lategameplus.strong_lava_vision",
                new MobEffectInstance(ModEffects.LAVA_VISION, 1800, 1)
            )
        );
        LONG_LAVA_VISION = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "long_lava_vision"),
            new Potion(
                "lategameplus.long_lava_vision",
                new MobEffectInstance(ModEffects.LAVA_VISION, 9600, 0)
            )
        );
        VOLCANIC_INFUSION = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "volcanic_infusion"),
            new Potion(
                "lategameplus.volcanic_infusion",
                new MobEffectInstance(ModEffects.VOLCANIC_INFUSION, 3600, 0)
            )
        );
        STRONG_VOLCANIC_INFUSION = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "strong_volcanic_infusion"),
            new Potion(
                "lategameplus.strong_volcanic_infusion",
                new MobEffectInstance(ModEffects.VOLCANIC_INFUSION, 1800, 1)
            )
        );
        LONG_VOLCANIC_INFUSION = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "long_volcanic_infusion"),
            new Potion(
                "lategameplus.long_volcanic_infusion",
                new MobEffectInstance(ModEffects.VOLCANIC_INFUSION, 9600, 0)
            )
        );
        VOLCANIC_MASTER = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "volcanic_master"),
            new Potion(
                "lategameplus.volcanic_master",
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6400, 0),
                new MobEffectInstance(ModEffects.LAVA_VISION, 6000, 0),
                new MobEffectInstance(ModEffects.VOLCANIC_INFUSION, 6000, 0)
            )
        );
        STRONG_VOLCANIC_MASTER = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "strong_volcanic_master"),
            new Potion(
                "lategameplus.strong_volcanic_master",
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4000, 0),
                new MobEffectInstance(ModEffects.LAVA_VISION, 3600, 1),
                new MobEffectInstance(ModEffects.VOLCANIC_INFUSION, 3600, 1)
            )
        );
        LONG_VOLCANIC_MASTER = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "long_volcanic_master"),
            new Potion(
                "lategameplus.long_volcanic_master",
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12400, 0),
                new MobEffectInstance(ModEffects.LAVA_VISION, 12000, 0),
                new MobEffectInstance(ModEffects.VOLCANIC_INFUSION, 12000, 0)
            )
        );


        FabricPotionBrewingBuilder.BUILD.register(registry -> {
            // Base recipes
            registry.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(ModItems.BLIND_SHRIMP), ModPotions.LAVA_VISION);
            registry.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(ModItems.POMPEII_WORM), ModPotions.VOLCANIC_INFUSION);

            // Vanilla-style variants of Lava Vision
            registry.registerPotionRecipe(ModPotions.LAVA_VISION, Ingredient.of(Items.REDSTONE), ModPotions.LONG_LAVA_VISION);
            registry.registerPotionRecipe(ModPotions.LAVA_VISION, Ingredient.of(Items.GLOWSTONE_DUST), ModPotions.STRONG_LAVA_VISION);

            // Vanilla-style variants of Volcanic Infusion
            registry.registerPotionRecipe(ModPotions.VOLCANIC_INFUSION, Ingredient.of(Items.REDSTONE), ModPotions.LONG_VOLCANIC_INFUSION);
            registry.registerPotionRecipe(ModPotions.VOLCANIC_INFUSION, Ingredient.of(Items.GLOWSTONE_DUST), ModPotions.STRONG_VOLCANIC_INFUSION);

            // Super Potion recipe (using both ingredients)
            registry.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(ModItems.VOLCANIC_CONCOCTION), ModPotions.VOLCANIC_MASTER);

            // Vanilla-style variants of Super Volcanic
            registry.registerPotionRecipe(ModPotions.VOLCANIC_MASTER, Ingredient.of(Items.REDSTONE), ModPotions.LONG_VOLCANIC_MASTER);
            registry.registerPotionRecipe(ModPotions.VOLCANIC_MASTER, Ingredient.of(Items.GLOWSTONE_DUST), ModPotions.STRONG_VOLCANIC_MASTER);
        });
    }
}
