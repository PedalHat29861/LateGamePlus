/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  org.slf4j.Logger
 */
package net.minecraft.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class DropperBlock
extends DispenserBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<DropperBlock> CODEC = DropperBlock.createCodec(DropperBlock::new);
    private static final DispenserBehavior BEHAVIOR = new ItemDispenserBehavior();

    public MapCodec<DropperBlock> getCodec() {
        return CODEC;
    }

    public DropperBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected DispenserBehavior getBehaviorForItem(World world, ItemStack stack) {
        return BEHAVIOR;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DropperBlockEntity(pos, state);
    }

    @Override
    protected void dispense(ServerWorld world, BlockState state, BlockPos pos) {
        ItemStack itemStack2;
        DispenserBlockEntity dispenserBlockEntity = world.getBlockEntity(pos, BlockEntityType.DROPPER).orElse(null);
        if (dispenserBlockEntity == null) {
            LOGGER.warn("Ignoring dispensing attempt for Dropper without matching block entity at {}", (Object)pos);
            return;
        }
        BlockPointer blockPointer = new BlockPointer(world, pos, state, dispenserBlockEntity);
        int i = dispenserBlockEntity.chooseNonEmptySlot(world.random);
        if (i < 0) {
            world.syncWorldEvent(1001, pos, 0);
            return;
        }
        ItemStack itemStack = dispenserBlockEntity.getStack(i);
        if (itemStack.isEmpty()) {
            return;
        }
        Direction direction = (Direction)world.getBlockState(pos).get(FACING);
        Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
        if (inventory == null) {
            itemStack2 = BEHAVIOR.dispense(blockPointer, itemStack);
        } else {
            itemStack2 = HopperBlockEntity.transfer(dispenserBlockEntity, inventory, itemStack.copyWithCount(1), direction.getOpposite());
            if (itemStack2.isEmpty()) {
                itemStack2 = itemStack.copy();
                itemStack2.decrement(1);
            } else {
                itemStack2 = itemStack.copy();
            }
        }
        dispenserBlockEntity.setStack(i, itemStack2);
    }
}

