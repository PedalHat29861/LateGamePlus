package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.mixinutil.AutoReelDamageContext;
import net.minecraft.world.item.FishingRodItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {
    @ModifyVariable(method = "use", at = @At(value = "STORE"), ordinal = 0)
    private int lategameplus$addAutoReelPenaltyDamage(int vanillaDamage) {
        int extraDamage = AutoReelDamageContext.consumeExtraDamage();
        if (extraDamage <= 0 || vanillaDamage <= 0) {
            return vanillaDamage;
        }
        return vanillaDamage + extraDamage;
    }
}
