/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.vehicle;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.ContainerUser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ChestMinecartEntity
extends StorageMinecartEntity {
    public ChestMinecartEntity(EntityType<? extends ChestMinecartEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item asItem() {
        return Items.CHEST_MINECART;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.CHEST_MINECART);
    }

    @Override
    public int size() {
        return 27;
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return (BlockState)Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.NORTH);
    }

    @Override
    public int getDefaultBlockOffset() {
        return 8;
    }

    @Override
    public ScreenHandler getScreenHandler(int syncId, PlayerInventory playerInventory) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }

    @Override
    public void onClose(ContainerUser user) {
        this.getEntityWorld().emitGameEvent(GameEvent.CONTAINER_CLOSE, this.getEntityPos(), GameEvent.Emitter.of(user.asLivingEntity()));
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        World world;
        ActionResult actionResult = this.open(player);
        if (actionResult.isAccepted() && (world = player.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinBrain.onGuardedBlockInteracted(serverWorld, player, true);
        }
        return actionResult;
    }
}

