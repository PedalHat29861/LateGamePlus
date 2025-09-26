package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.util.TimeBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;

public class LateGamePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);

        TimeBridge.setNowSupplier(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.world != null) {
                // segundos de juego (pausables). Si no hay mundo, cae a wall-clock.
                return mc.world.getTime() / 20L;
            }
            // fallback si no hay mundo (menús)
            return System.currentTimeMillis() / 1000L;
        });
    }
}
