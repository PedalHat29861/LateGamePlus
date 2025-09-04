package com.pedalhat.lategameplus.client.render;

import com.pedalhat.lategameplus.block.entity.NetheriteShulkerBoxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class NetheriteShulkerBoxRenderer implements BlockEntityRenderer<NetheriteShulkerBoxBlockEntity> {
    private static final Identifier BASE_TEXTURE = Identifier.of("minecraft", "textures/entity/shulker/shulker_purple.png");
    private static final Identifier OVERLAY_TEXTURE = Identifier.of("lategameplus", "textures/entity/shulker/netherite_shulker_overlay.png");

    private final ShulkerEntityModel model;

    public NetheriteShulkerBoxRenderer(BlockEntityRendererFactory.Context ctx) {
        this.model = new ShulkerEntityModel(ctx.getLayerModelPart(EntityModelLayers.SHULKER));
    }

    @Override
    public void render(NetheriteShulkerBoxBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d camera) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.scale(1.0f, -1.0f, -1.0f);

        VertexConsumer base = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(BASE_TEXTURE));
        this.model.render(matrices, base, light, overlay);

        VertexConsumer overlayConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(OVERLAY_TEXTURE));
        this.model.render(matrices, overlayConsumer, light, overlay);

        matrices.pop();
    }
}

