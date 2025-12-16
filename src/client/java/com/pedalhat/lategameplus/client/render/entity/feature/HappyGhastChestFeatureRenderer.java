package com.pedalhat.lategameplus.client.render.entity.feature;

import com.pedalhat.lategameplus.mixinutil.LGPChestedRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.HappyGhastEntityModel;
import net.minecraft.client.render.entity.state.HappyGhastEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class HappyGhastChestFeatureRenderer
        extends FeatureRenderer<HappyGhastEntityRenderState, HappyGhastEntityModel> {

    private final BlockState chestState = Blocks.CHEST.getDefaultState();

    public HappyGhastChestFeatureRenderer(FeatureRendererContext<HappyGhastEntityRenderState, HappyGhastEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue renderQueue, int light,
                       HappyGhastEntityRenderState state, float limbAngle, float limbDistance) {
        if (!(state instanceof LGPChestedRenderState chestedState)) return;
        int count = chestedState.lategameplus$getChestCount();
        if (count <= 0) return;

        float yOffset = state.baby ? 0.25f : 0.5f;
        this.renderChest(matrices, renderQueue, light, yOffset, -0.9f, count >= 1);
        this.renderChest(matrices, renderQueue, light, yOffset, 0.9f, count >= 2);
    }

    private void renderChest(MatrixStack matrices, OrderedRenderCommandQueue renderQueue, int light,
                             float yOffset, float xOffset, boolean render) {
        if (!render) return;
        matrices.push();
        matrices.translate(xOffset, yOffset, 0.0f);
        matrices.scale(0.6f, 0.6f, 0.6f);
        Direction facing = xOffset > 0 ? Direction.EAST : Direction.WEST;
        renderQueue.submitBlock(matrices, this.chestState.with(ChestBlock.FACING, facing), light, OverlayTexture.DEFAULT_UV, 0);
        matrices.pop();
    }
}
