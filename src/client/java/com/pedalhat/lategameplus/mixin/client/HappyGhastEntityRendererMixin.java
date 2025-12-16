package com.pedalhat.lategameplus.mixin.client;

import com.pedalhat.lategameplus.client.render.entity.feature.HappyGhastChestFeatureRenderer;
import com.pedalhat.lategameplus.mixinutil.LGPChestedGhast;
import com.pedalhat.lategameplus.mixinutil.LGPChestedRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.HappyGhastEntityRenderer;
import net.minecraft.client.render.entity.state.HappyGhastEntityRenderState;
import net.minecraft.entity.passive.HappyGhastEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HappyGhastEntityRenderer.class)
public abstract class HappyGhastEntityRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void lategameplus$addChestFeature(EntityRendererFactory.Context context, CallbackInfo ci) {
        HappyGhastEntityRenderer self = (HappyGhastEntityRenderer)(Object)this;
        ((LivingEntityRendererAccessor)self).lategameplus$callAddFeature(new HappyGhastChestFeatureRenderer(self));
    }

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/passive/HappyGhastEntity;Lnet/minecraft/client/render/entity/state/HappyGhastEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void lategameplus$copyChestState(HappyGhastEntity entity, HappyGhastEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (entity instanceof LGPChestedGhast chested && state instanceof LGPChestedRenderState renderState) {
            renderState.lategameplus$setChestCount(chested.lategameplus$getChestCount());
        }
    }
}
