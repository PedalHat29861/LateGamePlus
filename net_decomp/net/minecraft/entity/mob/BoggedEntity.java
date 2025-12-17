/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class BoggedEntity
extends AbstractSkeletonEntity
implements Shearable {
    private static final TrackedData<Boolean> SHEARED = DataTracker.registerData(BoggedEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final String SHEARED_KEY = "sheared";
    private static final boolean DEFAULT_SHEARED = false;

    public static DefaultAttributeContainer.Builder createBoggedAttributes() {
        return AbstractSkeletonEntity.createAbstractSkeletonAttributes().add(EntityAttributes.MAX_HEALTH, 16.0);
    }

    public BoggedEntity(EntityType<? extends BoggedEntity> entityType, World world) {
        super((EntityType<? extends AbstractSkeletonEntity>)entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SHEARED, false);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean(SHEARED_KEY, this.isSheared());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setSheared(view.getBoolean(SHEARED_KEY, false));
    }

    public boolean isSheared() {
        return this.dataTracker.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.dataTracker.set(SHEARED, sheared);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.SHEARS) && this.isShearable()) {
            World world = this.getEntityWorld();
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                this.sheared(serverWorld, SoundCategory.PLAYERS, itemStack);
                this.emitGameEvent(GameEvent.SHEAR, player);
                itemStack.damage(1, (LivingEntity)player, hand.getEquipmentSlot());
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_BOGGED_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_BOGGED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BOGGED_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_BOGGED_STEP;
    }

    @Override
    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom) {
        PersistentProjectileEntity persistentProjectileEntity = super.createArrowProjectile(arrow, damageModifier, shotFrom);
        if (persistentProjectileEntity instanceof ArrowEntity) {
            ArrowEntity arrowEntity = (ArrowEntity)persistentProjectileEntity;
            arrowEntity.addEffect(new StatusEffectInstance(StatusEffects.POISON, 100));
        }
        return persistentProjectileEntity;
    }

    @Override
    protected int getHardAttackInterval() {
        return 50;
    }

    @Override
    protected int getRegularAttackInterval() {
        return 70;
    }

    @Override
    public void sheared(ServerWorld world, SoundCategory shearedSoundCategory, ItemStack shears) {
        world.playSoundFromEntity(null, this, SoundEvents.ENTITY_BOGGED_SHEAR, shearedSoundCategory, 1.0f, 1.0f);
        this.dropShearedItems(world, shears);
        this.setSheared(true);
    }

    private void dropShearedItems(ServerWorld world, ItemStack shears) {
        this.forEachShearedItem(world, LootTables.BOGGED_SHEARING, shears, (worldx, stack) -> this.dropStack((ServerWorld)worldx, (ItemStack)stack, this.getHeight()));
    }

    @Override
    public boolean isShearable() {
        return !this.isSheared() && this.isAlive();
    }
}

