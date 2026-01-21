package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.effect.LavaVisionStatusEffect;
import com.pedalhat.lategameplus.effect.VolcanicInfusionStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModEffects {
    public static RegistryEntry.Reference<StatusEffect> LAVA_VISION;
    public static RegistryEntry.Reference<StatusEffect> VOLCANIC_INFUSION;

    private ModEffects() {
    }

    public static void init() {
        LAVA_VISION = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of(LateGamePlus.MOD_ID, "lava_vision"),
            new LavaVisionStatusEffect()
        );
        VOLCANIC_INFUSION = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of(LateGamePlus.MOD_ID, "volcanic_infusion"),
            new VolcanicInfusionStatusEffect()
        );
    }
}
