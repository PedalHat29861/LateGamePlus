package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.util.AnimationSoundSynchronizer;
import com.pedalhat.lategameplus.util.TimeBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class LateGamePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        TimeBridge.setNowSupplier(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.world != null) {
                // segundos de juego (pausables). Si no hay mundo, cae a wall-clock.
                return mc.world.getTime() / 20L;
            }
            // fallback si no hay mundo (menús)
            return System.currentTimeMillis() / 1000L;
        });

        // Registrar el tick handler para el AnimationSoundSynchronizer
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AnimationSoundSynchronizer.tick();
        });
    }
}
