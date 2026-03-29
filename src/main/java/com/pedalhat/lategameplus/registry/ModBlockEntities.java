package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.block.entity.FusionForgeBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static BlockEntityType<FusionForgeBlockEntity> FUSION_FORGE;

    public static void init() {
        FUSION_FORGE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "fusion_forge"),
            FabricBlockEntityTypeBuilder.create(FusionForgeBlockEntity::new, ModBlocks.FUSION_FORGE).build()
        );
    }

    private ModBlockEntities() {}
}
