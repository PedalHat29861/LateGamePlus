package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.mixinutil.LGPChestedGhastInternal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.HappyGhastEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropStorageMixin {
    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("TAIL"))
    private void lategameplus$dropGhastStorage(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self instanceof HappyGhastEntity && self instanceof LGPChestedGhastInternal chested) {
            chested.lategameplus$dropStorageContents();
        }
    }
}
