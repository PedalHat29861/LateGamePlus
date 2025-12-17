/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.vehicle;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FurnaceMinecartEntity
extends AbstractMinecartEntity {
    private static final TrackedData<Boolean> LIT = DataTracker.registerData(FurnaceMinecartEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final int FUEL_PER_ITEM = 3600;
    private static final int MAX_FUEL = 32000;
    private static final short DEFAULT_FUEL = 0;
    private static final Vec3d DEFAULT_PUSH_VEC = Vec3d.ZERO;
    private int fuel = 0;
    public Vec3d pushVec = DEFAULT_PUSH_VEC;

    public FurnaceMinecartEntity(EntityType<? extends FurnaceMinecartEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean isSelfPropelling() {
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LIT, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getEntityWorld().isClient()) {
            if (this.fuel > 0) {
                --this.fuel;
            }
            if (this.fuel <= 0) {
                this.pushVec = Vec3d.ZERO;
            }
            this.setLit(this.fuel > 0);
        }
        if (this.isLit() && this.random.nextInt(4) == 0) {
            this.getEntityWorld().addParticleClient(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected double getMaxSpeed(ServerWorld world) {
        return this.isTouchingWater() ? super.getMaxSpeed(world) * 0.75 : super.getMaxSpeed(world) * 0.5;
    }

    @Override
    protected Item asItem() {
        return Items.FURNACE_MINECART;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.FURNACE_MINECART);
    }

    @Override
    protected Vec3d applySlowdown(Vec3d velocity) {
        Vec3d vec3d;
        if (this.pushVec.lengthSquared() > 1.0E-7) {
            this.pushVec = this.calcNextPushVec(velocity);
            vec3d = velocity.multiply(0.8, 0.0, 0.8).add(this.pushVec);
            if (this.isTouchingWater()) {
                vec3d = vec3d.multiply(0.1);
            }
        } else {
            vec3d = velocity.multiply(0.98, 0.0, 0.98);
        }
        return super.applySlowdown(vec3d);
    }

    private Vec3d calcNextPushVec(Vec3d velocity) {
        double d = 1.0E-4;
        double e = 0.001;
        if (this.pushVec.horizontalLengthSquared() > 1.0E-4 && velocity.horizontalLengthSquared() > 0.001) {
            return this.pushVec.projectOnto(velocity).normalize().multiply(this.pushVec.length());
        }
        return this.pushVec;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (this.addFuel(player.getEntityPos(), itemStack)) {
            itemStack.decrementUnlessCreative(1, player);
        }
        return ActionResult.SUCCESS;
    }

    public boolean addFuel(Vec3d velocity, ItemStack stack) {
        if (stack.isIn(ItemTags.FURNACE_MINECART_FUEL) && this.fuel + 3600 <= 32000) {
            this.fuel += 3600;
        } else {
            return false;
        }
        if (this.fuel > 0) {
            this.pushVec = this.getEntityPos().subtract(velocity).getHorizontal();
        }
        return true;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putDouble("PushX", this.pushVec.x);
        view.putDouble("PushZ", this.pushVec.z);
        view.putShort("Fuel", (short)this.fuel);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        double d = view.getDouble("PushX", FurnaceMinecartEntity.DEFAULT_PUSH_VEC.x);
        double e = view.getDouble("PushZ", FurnaceMinecartEntity.DEFAULT_PUSH_VEC.z);
        this.pushVec = new Vec3d(d, 0.0, e);
        this.fuel = view.getShort("Fuel", (short)0);
    }

    protected boolean isLit() {
        return this.dataTracker.get(LIT);
    }

    protected void setLit(boolean lit) {
        this.dataTracker.set(LIT, lit);
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return (BlockState)((BlockState)Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, Direction.NORTH)).with(FurnaceBlock.LIT, this.isLit());
    }
}

