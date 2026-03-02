package com.pedalhat.lategameplus.mixin.client;

import com.pedalhat.lategameplus.registry.ModEffects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    private static final boolean LGP$SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");
    private static Field lategameplus$sodiumFogField;
    private static Constructor<?> lategameplus$sodiumFogConstructor;
    private static boolean lategameplus$sodiumReflectionInitialized;

    @ModifyArgs(
        method = "applyFog",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
        )
    )
    private void lategameplus$reduceLavaFog(
        Args args,
        Camera camera,
        int viewDistance,
        RenderTickCounter tickCounter,
        float tickDelta,
        ClientWorld world
    ) {
        StatusEffectInstance effect = getLavaVisionEffect(camera);
        if (effect == null) {
            return;
        }
        float start = computeFogStart(effect, viewDistance, tickDelta);
        float end = computeFogEnd(effect, viewDistance, tickDelta);

        args.set(3, start);
        args.set(4, end);
        args.set(5, start);
        args.set(6, end);
        args.set(7, end);
        args.set(8, end);
    }

    @Inject(method = "applyFog", at = @At("RETURN"))
    private void lategameplus$syncLavaFogForSodium(
        Camera camera,
        int viewDistance,
        RenderTickCounter tickCounter,
        float tickDelta,
        ClientWorld world,
        CallbackInfoReturnable<Vector4f> cir
    ) {
        if (!LGP$SODIUM_LOADED) {
            return;
        }
        StatusEffectInstance effect = getLavaVisionEffect(camera);
        if (effect == null) {
            return;
        }
        Vector4f fogColor = cir.getReturnValue();
        if (fogColor == null) {
            return;
        }
        float start = computeFogStart(effect, viewDistance, tickDelta);
        float end = computeFogEnd(effect, viewDistance, tickDelta);
        lategameplus$setSodiumFogParameters(fogColor, start, end);
    }

    private static StatusEffectInstance getLavaVisionEffect(Camera camera) {
        if (camera.getSubmersionType() != CameraSubmersionType.LAVA) {
            return null;
        }
        if (!(camera.getFocusedEntity() instanceof LivingEntity living)) {
            return null;
        }
        return living.getStatusEffect(ModEffects.LAVA_VISION);
    }

    private static float computeFogStart(StatusEffectInstance effect, int viewDistance, float tickDelta) {
        return Math.max(1.5F, computeFogEnd(effect, viewDistance, tickDelta) * 0.25F);
    }

    private static float computeFogEnd(StatusEffectInstance effect, int viewDistance, float tickDelta) {
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
    private static float computeLavaVisionStrength(StatusEffectInstance effect, float tickDelta) {
        int duration = effect.getDuration();
        if (duration > 200) {
            return 1.0F;
        }
        return 0.7F + (float) Math.sin(((float) duration - tickDelta) * Math.PI * 0.2F) * 0.3F;
    }

    private void lategameplus$setSodiumFogParameters(Vector4f fogColor, float start, float end) {
        if (!lategameplus$initSodiumReflection()) {
            return;
        }
        try {
            Object params = lategameplus$sodiumFogConstructor.newInstance(
                fogColor.x, fogColor.y, fogColor.z, fogColor.w,
                start, end, start, end
            );
            lategameplus$sodiumFogField.set(this, params);
        } catch (Throwable ignored) {
        }
    }

    private boolean lategameplus$initSodiumReflection() {
        if (lategameplus$sodiumReflectionInitialized) {
            return lategameplus$sodiumFogField != null && lategameplus$sodiumFogConstructor != null;
        }
        lategameplus$sodiumReflectionInitialized = true;
        try {
            ClassLoader loader = this.getClass().getClassLoader();
            Class<?> fogParamsClass = Class.forName(
                "net.caffeinemc.mods.sodium.client.util.FogParameters",
                false,
                loader
            );
            lategameplus$sodiumFogConstructor = fogParamsClass.getDeclaredConstructor(
                float.class, float.class, float.class, float.class,
                float.class, float.class, float.class, float.class
            );
            lategameplus$sodiumFogConstructor.setAccessible(true);

            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getType() == fogParamsClass) {
                    field.setAccessible(true);
                    lategameplus$sodiumFogField = field;
                    break;
                }
            }
        } catch (Throwable ignored) {
            lategameplus$sodiumFogField = null;
            lategameplus$sodiumFogConstructor = null;
        }
        return lategameplus$sodiumFogField != null && lategameplus$sodiumFogConstructor != null;
    }
}
