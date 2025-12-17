/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class EyeOfEnderEntity
extends Entity
implements FlyingItemEntity {
    private static final float field_52507 = 12.25f;
    private static final float field_60555 = 8.0f;
    private static final float field_60556 = 12.0f;
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(EyeOfEnderEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private @Nullable Vec3d targetPos;
    private int lifespan;
    private boolean dropsItem;

    public EyeOfEnderEntity(EntityType<? extends EyeOfEnderEntity> entityType, World world) {
        super(entityType, world);
    }

    public EyeOfEnderEntity(World world, double x, double y, double z) {
        this((EntityType<? extends EyeOfEnderEntity>)EntityType.EYE_OF_ENDER, world);
        this.setPosition(x, y, z);
    }

    public void setItem(ItemStack stack) {
        if (stack.isEmpty()) {
            this.getDataTracker().set(ITEM, this.getItem());
        } else {
            this.getDataTracker().set(ITEM, stack.copyWithCount(1));
        }
    }

    @Override
    public ItemStack getStack() {
        return this.getDataTracker().get(ITEM);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM, this.getItem());
    }

    @Override
    public boolean shouldRender(double distance) {
        if (this.age < 2 && distance < 12.25) {
            return false;
        }
        double d = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(d)) {
            d = 4.0;
        }
        return distance < (d *= 64.0) * d;
    }

    public void initTargetPos(Vec3d pos) {
        Vec3d vec3d = pos.subtract(this.getEntityPos());
        double d = vec3d.horizontalLength();
        this.targetPos = d > 12.0 ? this.getEntityPos().add(vec3d.x / d * 12.0, 8.0, vec3d.z / d * 12.0) : pos;
        this.lifespan = 0;
        this.dropsItem = this.random.nextInt(5) > 0;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d vec3d = this.getEntityPos().add(this.getVelocity());
        if (!this.getEntityWorld().isClient() && this.targetPos != null) {
            this.setVelocity(EyeOfEnderEntity.updateVelocity(this.getVelocity(), vec3d, this.targetPos));
        }
        if (this.getEntityWorld().isClient()) {
            Vec3d vec3d2 = vec3d.subtract(this.getVelocity().multiply(0.25));
            this.addParticles(vec3d2, this.getVelocity());
        }
        this.setPosition(vec3d);
        if (!this.getEntityWorld().isClient()) {
            ++this.lifespan;
            if (this.lifespan > 80 && !this.getEntityWorld().isClient()) {
                this.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);
                this.discard();
                if (this.dropsItem) {
                    this.getEntityWorld().spawnEntity(new ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), this.getStack()));
                } else {
                    this.getEntityWorld().syncWorldEvent(2003, this.getBlockPos(), 0);
                }
            }
        }
    }

    private void addParticles(Vec3d pos, Vec3d velocity) {
        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) {
                this.getEntityWorld().addParticleClient(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
            }
        } else {
            this.getEntityWorld().addParticleClient(ParticleTypes.PORTAL, pos.x + this.random.nextDouble() * 0.6 - 0.3, pos.y - 0.5, pos.z + this.random.nextDouble() * 0.6 - 0.3, velocity.x, velocity.y, velocity.z);
        }
    }

    private static Vec3d updateVelocity(Vec3d velocity, Vec3d currentPos, Vec3d targetPos) {
        Vec3d vec3d = new Vec3d(targetPos.x - currentPos.x, 0.0, targetPos.z - currentPos.z);
        double d = vec3d.length();
        double e = MathHelper.lerp(0.0025, velocity.horizontalLength(), d);
        double f = velocity.y;
        if (d < 1.0) {
            e *= 0.8;
            f *= 0.8;
        }
        double g = currentPos.y - velocity.y < targetPos.y ? 1.0 : -1.0;
        return vec3d.multiply(e / d).add(0.0, f + (g - f) * 0.015, 0.0);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("Item", ItemStack.CODEC, this.getStack());
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setItem(view.read("Item", ItemStack.CODEC).orElse(this.getItem()));
    }

    private ItemStack getItem() {
        return new ItemStack(Items.ENDER_EYE);
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }
}

