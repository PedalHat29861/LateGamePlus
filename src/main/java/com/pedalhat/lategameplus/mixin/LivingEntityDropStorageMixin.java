package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.mixinutil.LGPChestedGhastInternal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropStorageMixin {
    @Inject(method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At("TAIL"))
    private void lategameplus$dropGhastStorage(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self instanceof HappyGhast && self instanceof LGPChestedGhastInternal chested) {
            chested.lategameplus$dropStorageContents();
        }
    }
}
