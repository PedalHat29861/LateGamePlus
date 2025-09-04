package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.client.render.NetheriteShulkerBoxRenderer;
import com.pedalhat.lategameplus.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class LateGamePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);
        BlockEntityRendererFactories.register(ModBlocks.NETHERITE_SHULKER_BOX_ENTITY, NetheriteShulkerBoxRenderer::new);
    }
}
