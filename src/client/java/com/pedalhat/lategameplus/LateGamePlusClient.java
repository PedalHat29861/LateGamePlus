package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.registry.ModScreenHandlers;
import com.pedalhat.lategameplus.screen.FusionForgeScreen;
import com.pedalhat.lategameplus.util.AnimationSoundSynchronizer;
import com.pedalhat.lategameplus.util.TimeBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class LateGamePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.FUSION_FORGE, FusionForgeScreen::new);

        TimeBridge.setNowSupplier(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.world != null) {
                return mc.world.getTime() / 20L;
            }
            return System.currentTimeMillis() / 1000L;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AnimationSoundSynchronizer.tick();
        });
    }
}
