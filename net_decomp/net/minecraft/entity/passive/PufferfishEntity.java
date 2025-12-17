/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import java.util.List;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;

public class PufferfishEntity
extends FishEntity {
    private static final TrackedData<Integer> PUFF_STATE = DataTracker.registerData(PufferfishEntity.class, TrackedDataHandlerRegistry.INTEGER);
    int inflateTicks;
    int deflateTicks;
    private static final TargetPredicate.EntityPredicate BLOW_UP_FILTER = (entity, world) -> {
        PlayerEntity playerEntity;
        if (entity instanceof PlayerEntity && (playerEntity = (PlayerEntity)entity).isCreative()) {
            return false;
        }
        return !entity.getType().isIn(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH);
    };
    static final TargetPredicate BLOW_UP_TARGET_PREDICATE = TargetPredicate.createNonAttackable().ignoreDistanceScalingFactor().ignoreVisibility().setPredicate(BLOW_UP_FILTER);
    public static final int NOT_PUFFED = 0;
    public static final int SEMI_PUFFED = 1;
    public static final int FULLY_PUFFED = 2;
    private static final int DEFAULT_PUFF_STATE = 0;

    public PufferfishEntity(EntityType<? extends PufferfishEntity> entityType, World world) {
        super((EntityType<? extends FishEntity>)entityType, world);
        this.calculateDimensions();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(PUFF_STATE, 0);
    }

    public int getPuffState() {
        return this.dataTracker.get(PUFF_STATE);
    }

    public void setPuffState(int puffState) {
        this.dataTracker.set(PUFF_STATE, puffState);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (PUFF_STATE.equals(data)) {
            this.calculateDimensions();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("PuffState", this.getPuffState());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setPuffState(Math.min(view.getInt("PuffState", 0), 2));
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(Items.PUFFERFISH_BUCKET);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new InflateGoal(this));
    }

    @Override
    public void tick() {
        if (!this.getEntityWorld().isClient() && this.isAlive() && this.canActVoluntarily()) {
            if (this.inflateTicks > 0) {
                if (this.getPuffState() == 0) {
                    this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP);
                    this.setPuffState(1);
                } else if (this.inflateTicks > 40 && this.getPuffState() == 1) {
                    this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP);
                    this.setPuffState(2);
                }
                ++this.inflateTicks;
            } else if (this.getPuffState() != 0) {
                if (this.deflateTicks > 60 && this.getPuffState() == 2) {
                    this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT);
                    this.setPuffState(1);
                } else if (this.deflateTicks > 100 && this.getPuffState() == 1) {
                    this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT);
                    this.setPuffState(0);
                }
                ++this.deflateTicks;
            }
        }
        super.tick();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (this.isAlive() && this.getPuffState() > 0) {
                List<MobEntity> list = this.getEntityWorld().getEntitiesByClass(MobEntity.class, this.getBoundingBox().expand(0.3), mobEntity -> BLOW_UP_TARGET_PREDICATE.test(serverWorld, this, (LivingEntity)mobEntity));
                for (MobEntity mobEntity2 : list) {
                    if (!mobEntity2.isAlive()) continue;
                    this.sting(serverWorld, mobEntity2);
                }
            }
        }
    }

    private void sting(ServerWorld world, MobEntity target) {
        int i = this.getPuffState();
        if (target.damage(world, this.getDamageSources().mobAttack(this), 1 + i)) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60 * i, 0), this);
            this.playSound(SoundEvents.ENTITY_PUFFER_FISH_STING, 1.0f, 1.0f);
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        int i = this.getPuffState();
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
            if (i > 0 && player.damage(serverPlayerEntity.getEntityWorld(), this.getDamageSources().mobAttack(this), 1 + i)) {
                if (!this.isSilent()) {
                    serverPlayerEntity.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PUFFERFISH_STING, 0.0f));
                }
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60 * i, 0), this);
            }
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PUFFER_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PUFFER_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_PUFFER_FISH_FLOP;
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return super.getBaseDimensions(pose).scaled(PufferfishEntity.getScaleForPuffState(this.getPuffState()));
    }

    private static float getScaleForPuffState(int puffState) {
        switch (puffState) {
            case 1: {
                return 0.7f;
            }
            case 0: {
                return 0.5f;
            }
        }
        return 1.0f;
    }

    static class InflateGoal
    extends Goal {
        private final PufferfishEntity pufferfish;

        public InflateGoal(PufferfishEntity pufferfish) {
            this.pufferfish = pufferfish;
        }

        @Override
        public boolean canStart() {
            List<LivingEntity> list = this.pufferfish.getEntityWorld().getEntitiesByClass(LivingEntity.class, this.pufferfish.getBoundingBox().expand(2.0), livingEntity -> BLOW_UP_TARGET_PREDICATE.test(InflateGoal.getServerWorld(this.pufferfish), this.pufferfish, (LivingEntity)livingEntity));
            return !list.isEmpty();
        }

        @Override
        public void start() {
            this.pufferfish.inflateTicks = 1;
            this.pufferfish.deflateTicks = 0;
        }

        @Override
        public void stop() {
            this.pufferfish.inflateTicks = 0;
        }
    }
}

