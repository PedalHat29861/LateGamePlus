package com.pedalhat.lategameplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.pedalhat.lategameplus.tag.LGPItemTags;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @ModifyExpressionValue(
        method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private static boolean lategameplus$acceptAnyCrossbow(boolean vanillaIsCrossbow,
                                                          Avatar player,
                                                          ItemStack stack,
                                                          InteractionHand hand) {
        if (vanillaIsCrossbow) {
            return true;
        }
        if (stack.is(LGPItemTags.CROSSBOWS)) {
            return true;
        }
        return stack.getItem() instanceof CrossbowItem;
    }
}
