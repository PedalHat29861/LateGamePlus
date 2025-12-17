/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class ItemEntity
extends Entity
implements Ownable {
    private static final TrackedData<ItemStack> STACK = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final float field_48703 = 0.1f;
    public static final float field_48702 = 0.2125f;
    private static final int DESPAWN_AGE = 6000;
    private static final int CANNOT_PICK_UP_DELAY = Short.MAX_VALUE;
    private static final int NEVER_DESPAWN_AGE = Short.MIN_VALUE;
    private static final int DEFAULT_HEALTH = 5;
    private static final short DEFAULT_AGE = 0;
    private static final short DEFAULT_PICKUP_DELAY = 0;
    private int itemAge = 0;
    private int pickupDelay = 0;
    private int health = 5;
    private @Nullable LazyEntityReference<Entity> thrower;
    private @Nullable UUID owner;
    public final float uniqueOffset = this.random.nextFloat() * (float)Math.PI * 2.0f;

    public ItemEntity(EntityType<? extends ItemEntity> entityType, World world) {
        super(entityType, world);
        this.setYaw(this.random.nextFloat() * 360.0f);
    }

    public ItemEntity(World world, double x, double y, double z, ItemStack stack) {
        this(world, x, y, z, stack, world.random.nextDouble() * 0.2 - 0.1, 0.2, world.random.nextDouble() * 0.2 - 0.1);
    }

    public ItemEntity(World world, double x, double y, double z, ItemStack stack, double velocityX, double velocityY, double velocityZ) {
        this((EntityType<? extends ItemEntity>)EntityType.ITEM, world);
        this.setPosition(x, y, z);
        this.setVelocity(velocityX, velocityY, velocityZ);
        this.setStack(stack);
    }

    @Override
    public boolean occludeVibrationSignals() {
        return this.getStack().isIn(ItemTags.DAMPENS_VIBRATIONS);
    }

    @Override
    public @Nullable Entity getOwner() {
        return LazyEntityReference.getEntity(this.thrower, this.getEntityWorld());
    }

    @Override
    public void copyFrom(Entity original) {
        super.copyFrom(original);
        if (original instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity)original;
            this.thrower = itemEntity.thrower;
        }
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(STACK, ItemStack.EMPTY);
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        double d;
        int i;
        if (this.getStack().isEmpty()) {
            this.discard();
            return;
        }
        super.tick();
        if (this.pickupDelay > 0 && this.pickupDelay != Short.MAX_VALUE) {
            --this.pickupDelay;
        }
        this.lastX = this.getX();
        this.lastY = this.getY();
        this.lastZ = this.getZ();
        Vec3d vec3d = this.getVelocity();
        if (this.isTouchingWater() && this.getFluidHeight(FluidTags.WATER) > (double)0.1f) {
            this.applyWaterBuoyancy();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)0.1f) {
            this.applyLavaBuoyancy();
        } else {
            this.applyGravity();
        }
        if (this.getEntityWorld().isClient()) {
            this.noClip = false;
        } else {
            boolean bl = this.noClip = !this.getEntityWorld().isSpaceEmpty(this, this.getBoundingBox().contract(1.0E-7));
            if (this.noClip) {
                this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }
        if (!this.isOnGround() || this.getVelocity().horizontalLengthSquared() > (double)1.0E-5f || (this.age + this.getId()) % 4 == 0) {
            this.move(MovementType.SELF, this.getVelocity());
            this.tickBlockCollision();
            float f = 0.98f;
            if (this.isOnGround()) {
                f = this.getEntityWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.98f;
            }
            this.setVelocity(this.getVelocity().multiply(f, 0.98, f));
            if (this.isOnGround()) {
                Vec3d vec3d2 = this.getVelocity();
                if (vec3d2.y < 0.0) {
                    this.setVelocity(vec3d2.multiply(1.0, -0.5, 1.0));
                }
            }
        }
        boolean bl = MathHelper.floor(this.lastX) != MathHelper.floor(this.getX()) || MathHelper.floor(this.lastY) != MathHelper.floor(this.getY()) || MathHelper.floor(this.lastZ) != MathHelper.floor(this.getZ());
        int n = i = bl ? 2 : 40;
        if (this.age % i == 0 && !this.getEntityWorld().isClient() && this.canMerge()) {
            this.tryMerge();
        }
        if (this.itemAge != Short.MIN_VALUE) {
            ++this.itemAge;
        }
        this.velocityDirty |= this.updateWaterState();
        if (!this.getEntityWorld().isClient() && (d = this.getVelocity().subtract(vec3d).lengthSquared()) > 0.01) {
            this.velocityDirty = true;
        }
        if (!this.getEntityWorld().isClient() && this.itemAge >= 6000) {
            this.discard();
        }
    }

    @Override
    public BlockPos getVelocityAffectingPos() {
        return this.getPosWithYOffset(0.999999f);
    }

    private void applyWaterBuoyancy() {
        this.applyBuoyancy(0.99f);
    }

    private void applyLavaBuoyancy() {
        this.applyBuoyancy(0.95f);
    }

    private void applyBuoyancy(double horizontalMultiplier) {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x * horizontalMultiplier, vec3d.y + (double)(vec3d.y < (double)0.06f ? 5.0E-4f : 0.0f), vec3d.z * horizontalMultiplier);
    }

    private void tryMerge() {
        if (!this.canMerge()) {
            return;
        }
        List<ItemEntity> list = this.getEntityWorld().getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(0.5, 0.0, 0.5), otherItemEntity -> otherItemEntity != this && otherItemEntity.canMerge());
        for (ItemEntity itemEntity : list) {
            if (!itemEntity.canMerge()) continue;
            this.tryMerge(itemEntity);
            if (!this.isRemoved()) continue;
            break;
        }
    }

    private boolean canMerge() {
        ItemStack itemStack = this.getStack();
        return this.isAlive() && this.pickupDelay != Short.MAX_VALUE && this.itemAge != Short.MIN_VALUE && this.itemAge < 6000 && itemStack.getCount() < itemStack.getMaxCount();
    }

    private void tryMerge(ItemEntity other) {
        ItemStack itemStack = this.getStack();
        ItemStack itemStack2 = other.getStack();
        if (!Objects.equals(this.owner, other.owner) || !ItemEntity.canMerge(itemStack, itemStack2)) {
            return;
        }
        if (itemStack2.getCount() < itemStack.getCount()) {
            ItemEntity.merge(this, itemStack, other, itemStack2);
        } else {
            ItemEntity.merge(other, itemStack2, this, itemStack);
        }
    }

    public static boolean canMerge(ItemStack stack1, ItemStack stack2) {
        if (stack2.getCount() + stack1.getCount() > stack2.getMaxCount()) {
            return false;
        }
        return ItemStack.areItemsAndComponentsEqual(stack1, stack2);
    }

    public static ItemStack merge(ItemStack stack1, ItemStack stack2, int maxCount) {
        int i = Math.min(Math.min(stack1.getMaxCount(), maxCount) - stack1.getCount(), stack2.getCount());
        ItemStack itemStack = stack1.copyWithCount(stack1.getCount() + i);
        stack2.decrement(i);
        return itemStack;
    }

    private static void merge(ItemEntity targetEntity, ItemStack stack1, ItemStack stack2) {
        ItemStack itemStack = ItemEntity.merge(stack1, stack2, 64);
        targetEntity.setStack(itemStack);
    }

    private static void merge(ItemEntity targetEntity, ItemStack targetStack, ItemEntity sourceEntity, ItemStack sourceStack) {
        ItemEntity.merge(targetEntity, targetStack, sourceStack);
        targetEntity.pickupDelay = Math.max(targetEntity.pickupDelay, sourceEntity.pickupDelay);
        targetEntity.itemAge = Math.min(targetEntity.itemAge, sourceEntity.itemAge);
        if (sourceStack.isEmpty()) {
            sourceEntity.discard();
        }
    }

    @Override
    public boolean isFireImmune() {
        return !this.getStack().takesDamageFrom(this.getDamageSources().inFire()) || super.isFireImmune();
    }

    @Override
    protected boolean shouldPlayBurnSoundInLava() {
        if (this.health <= 0) {
            return true;
        }
        return this.age % 10 == 0;
    }

    @Override
    public final boolean clientDamage(DamageSource source) {
        if (this.isAlwaysInvulnerableTo(source)) {
            return false;
        }
        return this.getStack().takesDamageFrom(source);
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isAlwaysInvulnerableTo(source)) {
            return false;
        }
        if (!world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue() && source.getAttacker() instanceof MobEntity) {
            return false;
        }
        if (!this.getStack().takesDamageFrom(source)) {
            return false;
        }
        this.scheduleVelocityUpdate();
        this.health = (int)((float)this.health - amount);
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
        if (this.health <= 0) {
            this.getStack().onItemEntityDestroyed(this);
            this.discard();
        }
        return true;
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        if (explosion.preservesDecorativeEntities()) {
            return super.isImmuneToExplosion(explosion);
        }
        return true;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putShort("Health", (short)this.health);
        view.putShort("Age", (short)this.itemAge);
        view.putShort("PickupDelay", (short)this.pickupDelay);
        LazyEntityReference.writeData(this.thrower, view, "Thrower");
        view.putNullable("Owner", Uuids.INT_STREAM_CODEC, this.owner);
        if (!this.getStack().isEmpty()) {
            view.put("Item", ItemStack.CODEC, this.getStack());
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.health = view.getShort("Health", (short)5);
        this.itemAge = view.getShort("Age", (short)0);
        this.pickupDelay = view.getShort("PickupDelay", (short)0);
        this.owner = view.read("Owner", Uuids.INT_STREAM_CODEC).orElse(null);
        this.thrower = LazyEntityReference.fromData(view, "Thrower");
        this.setStack(view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        if (this.getStack().isEmpty()) {
            this.discard();
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (this.getEntityWorld().isClient()) {
            return;
        }
        ItemStack itemStack = this.getStack();
        Item item = itemStack.getItem();
        int i = itemStack.getCount();
        if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUuid())) && player.getInventory().insertStack(itemStack)) {
            player.sendPickup(this, i);
            if (itemStack.isEmpty()) {
                this.discard();
                itemStack.setCount(i);
            }
            player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), i);
            player.triggerItemPickedUpByEntityCriteria(this);
        }
    }

    @Override
    public Text getName() {
        Text text = this.getCustomName();
        if (text != null) {
            return text;
        }
        return this.getStack().getItemName();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public @Nullable Entity teleportTo(TeleportTarget teleportTarget) {
        Entity entity = super.teleportTo(teleportTarget);
        if (!this.getEntityWorld().isClient() && entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity)entity;
            itemEntity.tryMerge();
        }
        return entity;
    }

    public ItemStack getStack() {
        return this.getDataTracker().get(STACK);
    }

    public void setStack(ItemStack stack) {
        this.getDataTracker().set(STACK, stack);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (STACK.equals(data)) {
            this.getStack().setHolder(this);
        }
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    public void setThrower(Entity thrower) {
        this.thrower = LazyEntityReference.of(thrower);
    }

    public int getItemAge() {
        return this.itemAge;
    }

    public void setToDefaultPickupDelay() {
        this.pickupDelay = 10;
    }

    public void resetPickupDelay() {
        this.pickupDelay = 0;
    }

    public void setPickupDelayInfinite() {
        this.pickupDelay = Short.MAX_VALUE;
    }

    public void setPickupDelay(int pickupDelay) {
        this.pickupDelay = pickupDelay;
    }

    public boolean cannotPickup() {
        return this.pickupDelay > 0;
    }

    public void setNeverDespawn() {
        this.itemAge = Short.MIN_VALUE;
    }

    public void setCovetedItem() {
        this.itemAge = -6000;
    }

    public void setDespawnImmediately() {
        this.setPickupDelayInfinite();
        this.itemAge = 5999;
    }

    public static float getRotation(float f, float g) {
        return f / 20.0f + g;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.AMBIENT;
    }

    @Override
    public float getBodyYaw() {
        return 180.0f - ItemEntity.getRotation((float)this.getItemAge() + 0.5f, this.uniqueOffset) / ((float)Math.PI * 2) * 360.0f;
    }

    @Override
    public @Nullable StackReference getStackReference(int slot) {
        if (slot == 0) {
            return StackReference.of(this::getStack, this::setStack);
        }
        return super.getStackReference(slot);
    }
}

