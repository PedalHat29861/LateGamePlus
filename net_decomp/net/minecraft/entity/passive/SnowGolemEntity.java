/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class SnowGolemEntity
extends GolemEntity
implements Shearable,
RangedAttackMob {
    private static final TrackedData<Byte> SNOW_GOLEM_FLAGS = DataTracker.registerData(SnowGolemEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final byte HAS_PUMPKIN_FLAG = 16;
    private static final boolean DEFAULT_HAS_PUMPKIN = true;

    public SnowGolemEntity(EntityType<? extends SnowGolemEntity> entityType, World world) {
        super((EntityType<? extends GolemEntity>)entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.25, 20, 10.0f));
        this.goalSelector.add(2, new WanderAroundFarGoal((PathAwareEntity)this, 1.0, 1.0000001E-5f));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<MobEntity>(this, MobEntity.class, 10, true, false, (entity, serverWorld) -> entity instanceof Monster));
    }

    public static DefaultAttributeContainer.Builder createSnowGolemAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 4.0).add(EntityAttributes.MOVEMENT_SPEED, 0.2f);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SNOW_GOLEM_FLAGS, (byte)16);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("Pumpkin", this.hasPumpkin());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setHasPumpkin(view.getBoolean("Pumpkin", true));
    }

    @Override
    public boolean hurtByWater() {
        return true;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (serverWorld.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.SNOW_GOLEM_MELTS_GAMEPLAY, this.getEntityPos()).booleanValue()) {
                this.damage(serverWorld, this.getDamageSources().onFire(), 1.0f);
            }
            if (!serverWorld.getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                return;
            }
            BlockState blockState = Blocks.SNOW.getDefaultState();
            for (int i = 0; i < 4; ++i) {
                int j = MathHelper.floor(this.getX() + (double)((float)(i % 2 * 2 - 1) * 0.25f));
                int k = MathHelper.floor(this.getY());
                int l = MathHelper.floor(this.getZ() + (double)((float)(i / 2 % 2 * 2 - 1) * 0.25f));
                BlockPos blockPos = new BlockPos(j, k, l);
                if (!this.getEntityWorld().getBlockState(blockPos).isAir() || !blockState.canPlaceAt(this.getEntityWorld(), blockPos)) continue;
                this.getEntityWorld().setBlockState(blockPos, blockState);
                this.getEntityWorld().emitGameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Emitter.of(this, blockState));
            }
        }
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        double d = target.getX() - this.getX();
        double e = target.getEyeY() - (double)1.1f;
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2f;
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            ItemStack itemStack = new ItemStack(Items.SNOWBALL);
            ProjectileEntity.spawn(new SnowballEntity(serverWorld, this, itemStack), serverWorld, itemStack, entity -> entity.setVelocity(d, e + g - entity.getY(), f, 1.6f, 12.0f));
        }
        this.playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.4f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
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
        return ActionResult.PASS;
    }

    @Override
    public void sheared(ServerWorld world, SoundCategory shearedSoundCategory, ItemStack shears) {
        world.playSoundFromEntity(null, this, SoundEvents.ENTITY_SNOW_GOLEM_SHEAR, shearedSoundCategory, 1.0f, 1.0f);
        this.setHasPumpkin(false);
        this.forEachShearedItem(world, LootTables.SNOW_GOLEM_SHEARING, shears, (serverWorld, itemStack) -> this.dropStack((ServerWorld)serverWorld, (ItemStack)itemStack, this.getStandingEyeHeight()));
    }

    @Override
    public boolean isShearable() {
        return this.isAlive() && this.hasPumpkin();
    }

    public boolean hasPumpkin() {
        return (this.dataTracker.get(SNOW_GOLEM_FLAGS) & 0x10) != 0;
    }

    public void setHasPumpkin(boolean hasPumpkin) {
        byte b = this.dataTracker.get(SNOW_GOLEM_FLAGS);
        if (hasPumpkin) {
            this.dataTracker.set(SNOW_GOLEM_FLAGS, (byte)(b | 0x10));
        } else {
            this.dataTracker.set(SNOW_GOLEM_FLAGS, (byte)(b & 0xFFFFFFEF));
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SNOW_GOLEM_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SNOW_GOLEM_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SNOW_GOLEM_DEATH;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.75f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }
}

