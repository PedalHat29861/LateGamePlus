package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void lategameplus$fireImmuneNetheriteBobber(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object)this instanceof FishingBobberEntity bobber)) {
            return;
        }
        PlayerEntity owner = bobber.getPlayerOwner();
        if (owner == null) {
            return;
        }
        ItemStack main = owner.getMainHandStack();
        ItemStack off = owner.getOffHandStack();
        if (main.isOf(ModItems.NETHERITE_FISHING_ROD) || off.isOf(ModItems.NETHERITE_FISHING_ROD)) {
            cir.setReturnValue(true);
        }
    }
}
