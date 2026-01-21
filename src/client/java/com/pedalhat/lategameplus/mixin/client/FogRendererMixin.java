package com.pedalhat.lategameplus.mixin.client;

import com.pedalhat.lategameplus.registry.ModEffects;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
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
        if (camera.getSubmersionType() != CameraSubmersionType.LAVA) {
            return;
        }
        if (!(camera.getFocusedEntity() instanceof LivingEntity living)) {
            return;
        }
        StatusEffectInstance effect = living.getStatusEffect(ModEffects.LAVA_VISION);
        if (effect == null) {
            return;
        }

        float viewDistanceBlocks = viewDistance * 16.0F;
        float baseEnd = Math.max(6.0F, viewDistanceBlocks * 0.2F);
        float radiusMultiplier = 1.4F;
        float tierMultiplier = effect.getAmplifier() >= 1 ? 2.0F : 1.0F;
        float cap = 14.0F * radiusMultiplier * tierMultiplier;
        float end = Math.min(cap, baseEnd * radiusMultiplier * tierMultiplier);
        float start = Math.max(1.5F, end * 0.25F);

        args.set(3, start);
        args.set(4, end);
        args.set(5, start);
        args.set(6, end);
        args.set(7, end);
        args.set(8, end);
    }
}
