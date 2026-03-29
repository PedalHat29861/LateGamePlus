package com.pedalhat.lategameplus.mixin.client;

import com.pedalhat.lategameplus.registry.ModEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void lategameplus$reduceLavaFog(
        Camera camera,
        int viewDistance,
        DeltaTracker tickCounter,
        float tickDelta,
        ClientLevel world,
        CallbackInfoReturnable<FogData> cir
    ) {
        MobEffectInstance effect = getLavaVisionEffect(camera);
        if (effect == null) {
            return;
        }
        FogData fogData = cir.getReturnValue();
        if (fogData == null) {
            return;
        }
        float start = computeFogStart(effect, viewDistance, tickDelta);
        float end = computeFogEnd(effect, viewDistance, tickDelta);
        fogData.environmentalStart = start;
        fogData.environmentalEnd = end;
        fogData.renderDistanceStart = start;
        fogData.renderDistanceEnd = end;
        fogData.skyEnd = end;
        fogData.cloudEnd = end;
    }

    private static MobEffectInstance getLavaVisionEffect(Camera camera) {
        if (camera.getFluidInCamera() != FogType.LAVA) {
            return null;
        }
        if (!(camera.entity() instanceof LivingEntity living)) {
            return null;
        }
        return living.getEffect(ModEffects.LAVA_VISION);
    }

    private static float computeFogStart(MobEffectInstance effect, int viewDistance, float tickDelta) {
        return Math.max(1.5F, computeFogEnd(effect, viewDistance, tickDelta) * 0.25F);
    }

    private static float computeFogEnd(MobEffectInstance effect, int viewDistance, float tickDelta) {
        float viewDistanceBlocks = viewDistance * 16.0F;
        float baseEnd = Math.max(6.0F, viewDistanceBlocks * 0.2F);
        float radiusMultiplier = 1.4F;
        float tierMultiplier = effect.getAmplifier() >= 1 ? 2.0F : 1.0F;
        float cap = 14.0F * radiusMultiplier * tierMultiplier;
        float maxEnd = Math.min(cap, baseEnd * radiusMultiplier * tierMultiplier);
        return maxEnd * computeLavaVisionStrength(effect, tickDelta);
    }

    /**
     * Mirrors the Night Vision fade pulse in the last 10 seconds (200 ticks).
     */
    private static float computeLavaVisionStrength(MobEffectInstance effect, float tickDelta) {
        int duration = effect.getDuration();
        if (duration > 200) {
            return 1.0F;
        }
        return 0.7F + (float) Math.sin(((float) duration - tickDelta) * Math.PI * 0.2F) * 0.3F;
    }
}
