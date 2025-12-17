/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.vehicle;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnerMinecartEntity
extends AbstractMinecartEntity {
    private final MobSpawnerLogic logic = new MobSpawnerLogic(){

        @Override
        public void sendStatus(World world, BlockPos pos, int status) {
            world.sendEntityStatus(SpawnerMinecartEntity.this, (byte)status);
        }
    };
    private final Runnable ticker;

    public SpawnerMinecartEntity(EntityType<? extends SpawnerMinecartEntity> entityType, World world) {
        super(entityType, world);
        this.ticker = this.getTicker(world);
    }

    @Override
    protected Item asItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.MINECART);
    }

    private Runnable getTicker(World world) {
        return world instanceof ServerWorld ? () -> this.logic.serverTick((ServerWorld)world, this.getBlockPos()) : () -> this.logic.clientTick(world, this.getBlockPos());
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return Blocks.SPAWNER.getDefaultState();
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.logic.readData(this.getEntityWorld(), this.getBlockPos(), view);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        this.logic.writeData(view);
    }

    @Override
    public void handleStatus(byte status) {
        this.logic.handleStatus(this.getEntityWorld(), status);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticker.run();
    }

    public MobSpawnerLogic getLogic() {
        return this.logic;
    }
}

