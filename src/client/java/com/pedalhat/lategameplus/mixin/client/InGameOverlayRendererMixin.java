package com.pedalhat.lategameplus.mixin.client;

import com.pedalhat.lategameplus.registry.ModEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    @ModifyConstant(method = "renderFireOverlay", constant = @Constant(floatValue = 0.9f))
    private static float lategameplus$reduceFireOverlayAlpha(float original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null
            && client.player.getStatusEffect(ModEffects.LAVA_VISION) != null) {
            return 0.35f;
        }
        return original;
    }
}
