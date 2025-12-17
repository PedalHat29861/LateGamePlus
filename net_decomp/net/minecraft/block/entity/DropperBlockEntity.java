/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class DropperBlockEntity
extends DispenserBlockEntity {
    private static final Text CONTAINER_NAME_TEXT = Text.translatable("container.dropper");

    public DropperBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.DROPPER, blockPos, blockState);
    }

    @Override
    protected Text getContainerName() {
        return CONTAINER_NAME_TEXT;
    }
}

