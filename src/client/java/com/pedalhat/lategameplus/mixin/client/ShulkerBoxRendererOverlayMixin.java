package com.pedalhat.lategameplus.mixin.client;

import com.pedalhat.lategameplus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntityRenderer.class)
public abstract class ShulkerBoxRendererOverlayMixin {
    @Shadow @Final private ShulkerEntityModel model;

    private static final Identifier OVERLAY_TEXTURE = Identifier.of("lategameplus", "textures/entity/shulker/netherite_shulker_overlay.png");

    @Inject(method = "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("TAIL"))
    private void lategameplus$renderOverlay(ShulkerBoxBlockEntity be, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d camera, CallbackInfo ci) {
        Block b = be.getCachedState().getBlock();
        if (b == ModBlocks.NETHERITE_SHULKER_BOX) {
            VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(OVERLAY_TEXTURE));
            this.model.render(matrices, vc, light, overlay);
        }
    }
}

