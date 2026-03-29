package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.effect.LavaVisionStatusEffect;
import com.pedalhat.lategameplus.effect.VolcanicInfusionStatusEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
    public static Holder.Reference<MobEffect> LAVA_VISION;
    public static Holder.Reference<MobEffect> VOLCANIC_INFUSION;

    private ModEffects() {
    }

    public static void init() {
        LAVA_VISION = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "lava_vision"),
            new LavaVisionStatusEffect()
        );
        VOLCANIC_INFUSION = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "volcanic_infusion"),
            new VolcanicInfusionStatusEffect()
        );
    }
}
