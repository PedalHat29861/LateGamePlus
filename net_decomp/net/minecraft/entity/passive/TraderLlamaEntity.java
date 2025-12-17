/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class TraderLlamaEntity
extends LlamaEntity {
    private static final int DEFAULT_DESPAWN_DELAY = 47999;
    private int despawnDelay = 47999;

    public TraderLlamaEntity(EntityType<? extends TraderLlamaEntity> entityType, World world) {
        super((EntityType<? extends LlamaEntity>)entityType, world);
    }

    @Override
    public boolean isTrader() {
        return true;
    }

    @Override
    protected @Nullable LlamaEntity createChild() {
        return EntityType.TRADER_LLAMA.create(this.getEntityWorld(), SpawnReason.BREEDING);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("DespawnDelay", this.despawnDelay);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.despawnDelay = view.getInt("DespawnDelay", 47999);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new EscapeDangerGoal(this, 2.0));
        this.targetSelector.add(1, new DefendTraderGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<ZombieEntity>((MobEntity)this, ZombieEntity.class, true, (entity, serverWorld) -> entity.getType() != EntityType.ZOMBIFIED_PIGLIN));
        this.targetSelector.add(2, new ActiveTargetGoal<IllagerEntity>((MobEntity)this, IllagerEntity.class, true));
    }

    public void setDespawnDelay(int despawnDelay) {
        this.despawnDelay = despawnDelay;
    }

    @Override
    protected void putPlayerOnBack(PlayerEntity player) {
        Entity entity = this.getLeashHolder();
        if (entity instanceof WanderingTraderEntity) {
            return;
        }
        super.putPlayerOnBack(player);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getEntityWorld().isClient()) {
            this.tryDespawn();
        }
    }

    private void tryDespawn() {
        if (!this.canDespawn()) {
            return;
        }
        int n = this.despawnDelay = this.heldByTrader() ? ((WanderingTraderEntity)this.getLeashHolder()).getDespawnDelay() - 1 : this.despawnDelay - 1;
        if (this.despawnDelay <= 0) {
            this.detachLeashWithoutDrop();
            this.discard();
        }
    }

    private boolean canDespawn() {
        return !this.isTame() && !this.leashedByPlayer() && !this.hasPlayerRider();
    }

    private boolean heldByTrader() {
        return this.getLeashHolder() instanceof WanderingTraderEntity;
    }

    private boolean leashedByPlayer() {
        return this.isLeashed() && !this.heldByTrader();
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        if (spawnReason == SpawnReason.EVENT) {
            this.setBreedingAge(0);
        }
        if (entityData == null) {
            entityData = new PassiveEntity.PassiveData(false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    protected static class DefendTraderGoal
    extends TrackTargetGoal {
        private final LlamaEntity llama;
        private LivingEntity offender;
        private int traderLastAttackedTime;

        public DefendTraderGoal(LlamaEntity llama) {
            super(llama, false);
            this.llama = llama;
            this.setControls(EnumSet.of(Goal.Control.TARGET));
        }

        @Override
        public boolean canStart() {
            if (!this.llama.isLeashed()) {
                return false;
            }
            Entity entity = this.llama.getLeashHolder();
            if (!(entity instanceof WanderingTraderEntity)) {
                return false;
            }
            WanderingTraderEntity wanderingTraderEntity = (WanderingTraderEntity)entity;
            this.offender = wanderingTraderEntity.getAttacker();
            int i = wanderingTraderEntity.getLastAttackedTime();
            return i != this.traderLastAttackedTime && this.canTrack(this.offender, TargetPredicate.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.offender);
            Entity entity = this.llama.getLeashHolder();
            if (entity instanceof WanderingTraderEntity) {
                this.traderLastAttackedTime = ((WanderingTraderEntity)entity).getLastAttackedTime();
            }
            super.start();
        }
    }
}

