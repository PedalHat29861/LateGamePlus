package com.pedalhat.lategameplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.pedalhat.lategameplus.tag.LGPItemTags;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @ModifyExpressionValue(
        method = "getArmPose(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean lategameplus$acceptAnyCrossbow(boolean vanillaIsCrossbow,
                                                          PlayerLikeEntity player,
                                                          ItemStack stack,
                                                          Hand hand) {
        if (vanillaIsCrossbow) {
            return true;
        }
        if (stack.isIn(LGPItemTags.CROSSBOWS)) {
            return true;
        }
        return stack.getItem() instanceof CrossbowItem;
    }
}
