package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.registry.ModBlocks;
import com.pedalhat.lategameplus.registry.ModScreenHandlers;
import com.pedalhat.lategameplus.screen.FusionForgeScreen;
import com.pedalhat.lategameplus.util.AnimationSoundSynchronizer;
import com.pedalhat.lategameplus.util.TimeBridge;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class LateGamePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.FUSION_FORGE, FusionForgeScreen::new);

        TimeBridge.setNowSupplier(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.level != null) {
                return mc.level.getGameTime() / 20L;
            }
            return System.currentTimeMillis() / 1000L;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AnimationSoundSynchronizer.tick();
        });
    }
}
