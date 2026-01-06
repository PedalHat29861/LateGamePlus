package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.block.entity.FusionForgeBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
    public static BlockEntityType<FusionForgeBlockEntity> FUSION_FORGE;

    public static void init() {
        FUSION_FORGE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(LateGamePlus.MOD_ID, "fusion_forge"),
            FabricBlockEntityTypeBuilder.create(FusionForgeBlockEntity::new, ModBlocks.FUSION_FORGE).build()
        );
    }

    private ModBlockEntities() {}
}
