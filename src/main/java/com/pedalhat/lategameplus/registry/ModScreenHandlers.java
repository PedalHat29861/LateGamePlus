package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.screen.FusionForgeScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {
    public static ScreenHandlerType<FusionForgeScreenHandler> FUSION_FORGE;

    public static void init() {
        FUSION_FORGE = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(LateGamePlus.MOD_ID, "fusion_forge"),
            new ScreenHandlerType<>(FusionForgeScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );
    }

    private ModScreenHandlers() {}
}
