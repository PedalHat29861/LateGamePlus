package com.pedalhat.lategameplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

    @ModifyExpressionValue(
        method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"
        )
    )
    private boolean lategameplus$acceptAnyCrossbowFP(
        boolean original,
        AbstractClientPlayer player,
        float tickDelta,
        float pitch,
        InteractionHand hand,
        float swingProgress,
        ItemStack stack,
        float equipProgress,
        PoseStack matrices,
        SubmitNodeCollector commandQueue,
        int light
    ) {
        // Si ya era true (ballesta vanilla), mantenlo.
        if (original) return true;
        // Acepta cualquier Item que sea una ballesta (incluye tu NetheriteCrossbowItem).
        return stack.getItem() instanceof CrossbowItem;
    }
}
