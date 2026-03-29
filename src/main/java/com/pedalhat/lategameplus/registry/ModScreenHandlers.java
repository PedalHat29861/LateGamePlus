package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.screen.FusionForgeScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModScreenHandlers {
    public static MenuType<FusionForgeScreenHandler> FUSION_FORGE;

    public static void init() {
        FUSION_FORGE = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "fusion_forge"),
            new MenuType<>(FusionForgeScreenHandler::new, FeatureFlags.VANILLA_SET)
        );
    }

    private ModScreenHandlers() {}
}
