/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.CopperGolemStatueBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;

public class CopperGolemStatueBlockEntity
extends BlockEntity {
    public CopperGolemStatueBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.COPPER_GOLEM_STATUE, pos, state);
    }

    public void copyDataFrom(CopperGolemEntity copperGolemEntity) {
        this.setComponents(ComponentMap.builder().addAll(this.getComponents()).add(DataComponentTypes.CUSTOM_NAME, copperGolemEntity.getCustomName()).build());
        super.markDirty();
    }

    public @Nullable CopperGolemEntity createCopperGolem(BlockState state) {
        CopperGolemEntity copperGolemEntity = EntityType.COPPER_GOLEM.create(this.world, SpawnReason.TRIGGERED);
        if (copperGolemEntity != null) {
            copperGolemEntity.setCustomName(this.getComponents().get(DataComponentTypes.CUSTOM_NAME));
            return this.setupEntity(state, copperGolemEntity);
        }
        return null;
    }

    private CopperGolemEntity setupEntity(BlockState state, CopperGolemEntity entity) {
        BlockPos blockPos = this.getPos();
        entity.refreshPositionAndAngles(blockPos.toCenterPos().x, blockPos.getY(), blockPos.toCenterPos().z, state.get(CopperGolemStatueBlock.FACING).getPositiveHorizontalDegrees(), 0.0f);
        entity.headYaw = entity.getYaw();
        entity.bodyYaw = entity.getYaw();
        entity.playSpawnSound();
        return entity;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public ItemStack withComponents(ItemStack stack, CopperGolemStatueBlock.Pose pose) {
        stack.applyComponentsFrom(this.createComponentMap());
        stack.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT.with(CopperGolemStatueBlock.POSE, pose));
        return stack;
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

