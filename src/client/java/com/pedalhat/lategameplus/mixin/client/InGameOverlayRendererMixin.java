package com.pedalhat.lategameplus.mixin.client;

import com.pedalhat.lategameplus.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ScreenEffectRenderer.class)
public class InGameOverlayRendererMixin {
    @ModifyConstant(method = "renderFire", constant = @Constant(floatValue = 0.9f))
    private static float lategameplus$reduceFireOverlayAlpha(float original) {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.player != null
            && client.player.getEffect(ModEffects.LAVA_VISION) != null) {
            return 0.35f;
        }
        return original;
    }
}
