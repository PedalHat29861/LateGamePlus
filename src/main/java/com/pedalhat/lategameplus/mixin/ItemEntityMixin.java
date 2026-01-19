package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.mixinutil.LGPLavaImmuneItemEntity;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements LGPLavaImmuneItemEntity {
    @Unique
    private int lategameplus$lavaProtectionTicks = 0;

    @Override
    public void lategameplus$setLavaProtectionTicks(int ticks) {
        this.lategameplus$lavaProtectionTicks = Math.max(this.lategameplus$lavaProtectionTicks, ticks);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void lategameplus$applyLavaProtection(CallbackInfo ci) {
        if (this.lategameplus$lavaProtectionTicks <= 0) {
            return;
        }
        this.lategameplus$lavaProtectionTicks--;
        ItemEntity self = (ItemEntity)(Object)this;
        self.setInvulnerable(true);
        self.extinguish();
        if (this.lategameplus$lavaProtectionTicks == 0) {
            self.setInvulnerable(false);
        }
    }
}
