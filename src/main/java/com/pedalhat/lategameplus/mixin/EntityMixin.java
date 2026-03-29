package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void lategameplus$fireImmuneNetheriteBobber(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object)this instanceof FishingHook bobber)) {
            return;
        }
        Player owner = bobber.getPlayerOwner();
        if (owner == null) {
            return;
        }
        ItemStack main = owner.getMainHandItem();
        ItemStack off = owner.getOffhandItem();
        if (main.is(ModItems.NETHERITE_FISHING_ROD) || off.is(ModItems.NETHERITE_FISHING_ROD)) {
            cir.setReturnValue(true);
        }
    }
}
