/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public abstract class TameableEntity
extends AnimalEntity
implements Tameable {
    public static final int field_52002 = 144;
    private static final int field_52003 = 2;
    private static final int field_52004 = 3;
    private static final int field_52005 = 1;
    private static final boolean DEFAULT_SITTING = false;
    protected static final TrackedData<Byte> TAMEABLE_FLAGS = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> OWNER_UUID = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE);
    private boolean sitting = false;

    protected TameableEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super((EntityType<? extends AnimalEntity>)entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TAMEABLE_FLAGS, (byte)0);
        builder.add(OWNER_UUID, Optional.empty());
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        LazyEntityReference<LivingEntity> lazyEntityReference = this.getOwnerReference();
        LazyEntityReference.writeData(lazyEntityReference, view, "Owner");
        view.putBoolean("Sitting", this.sitting);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        LazyEntityReference lazyEntityReference = LazyEntityReference.fromDataOrPlayerName(view, "Owner", this.getEntityWorld());
        if (lazyEntityReference != null) {
            try {
                this.dataTracker.set(OWNER_UUID, Optional.of(lazyEntityReference));
                this.setTamed(true, false);
            }
            catch (Throwable throwable) {
                this.setTamed(false, true);
            }
        } else {
            this.dataTracker.set(OWNER_UUID, Optional.empty());
            this.setTamed(false, true);
        }
        this.sitting = view.getBoolean("Sitting", false);
        this.setInSittingPose(this.sitting);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    protected void showEmoteParticle(boolean positive) {
        SimpleParticleType particleEffect = ParticleTypes.HEART;
        if (!positive) {
            particleEffect = ParticleTypes.SMOKE;
        }
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.getEntityWorld().addParticleClient(particleEffect, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 7) {
            this.showEmoteParticle(true);
        } else if (status == 6) {
            this.showEmoteParticle(false);
        } else {
            super.handleStatus(status);
        }
    }

    public boolean isTamed() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0;
    }

    public void setTamed(boolean tamed, boolean updateAttributes) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (tamed) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & 0xFFFFFFFB));
        }
        if (updateAttributes) {
            this.updateAttributesForTamed();
        }
    }

    protected void updateAttributesForTamed() {
    }

    public boolean isInSittingPose() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 1) != 0;
    }

    public void setInSittingPose(boolean inSittingPose) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (inSittingPose) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 1));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & 0xFFFFFFFE));
        }
    }

    @Override
    public @Nullable LazyEntityReference<LivingEntity> getOwnerReference() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(owner).map(LazyEntityReference::of));
    }

    public void setOwner(@Nullable LazyEntityReference<LivingEntity> owner) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(owner));
    }

    public void setTamedBy(PlayerEntity player) {
        this.setTamed(true, true);
        this.setOwner(player);
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
            Criteria.TAME_ANIMAL.trigger(serverPlayerEntity, this);
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (this.isOwner(target)) {
            return false;
        }
        return super.canTarget(target);
    }

    public boolean isOwner(LivingEntity entity) {
        return entity == this.getOwner();
    }

    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        return true;
    }

    @Override
    public @Nullable Team getScoreboardTeam() {
        LivingEntity livingEntity;
        Team team = super.getScoreboardTeam();
        if (team != null) {
            return team;
        }
        if (this.isTamed() && (livingEntity = this.getTopLevelOwner()) != null) {
            return livingEntity.getScoreboardTeam();
        }
        return null;
    }

    @Override
    protected boolean isInSameTeam(Entity other) {
        if (this.isTamed()) {
            LivingEntity livingEntity = this.getTopLevelOwner();
            if (other == livingEntity) {
                return true;
            }
            if (livingEntity != null) {
                return livingEntity.isInSameTeam(other);
            }
        }
        return super.isInSameTeam(other);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        LivingEntity livingEntity;
        ServerWorld serverWorld;
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld && (serverWorld = (ServerWorld)world).getGameRules().getValue(GameRules.SHOW_DEATH_MESSAGES).booleanValue() && (livingEntity = this.getOwner()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
            serverPlayerEntity.sendMessage(this.getDamageTracker().getDeathMessage());
        }
        super.onDeath(damageSource);
    }

    public boolean isSitting() {
        return this.sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void tryTeleportToOwner() {
        LivingEntity livingEntity = this.getOwner();
        if (livingEntity != null) {
            this.tryTeleportNear(livingEntity.getBlockPos());
        }
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity livingEntity = this.getOwner();
        return livingEntity != null && this.squaredDistanceTo(this.getOwner()) >= 144.0;
    }

    private void tryTeleportNear(BlockPos pos) {
        for (int i = 0; i < 10; ++i) {
            int j = this.random.nextBetween(-3, 3);
            int k = this.random.nextBetween(-3, 3);
            if (Math.abs(j) < 2 && Math.abs(k) < 2) continue;
            int l = this.random.nextBetween(-1, 1);
            if (!this.tryTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) continue;
            return;
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        }
        this.refreshPositionAndAngles((double)x + 0.5, y, (double)z + 0.5, this.getYaw(), this.getPitch());
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this, pos);
        if (pathNodeType != PathNodeType.WALKABLE) {
            return false;
        }
        BlockState blockState = this.getEntityWorld().getBlockState(pos.down());
        if (!this.canTeleportOntoLeaves() && blockState.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos blockPos = pos.subtract(this.getBlockPos());
        return this.getEntityWorld().isSpaceEmpty(this, this.getBoundingBox().offset(blockPos));
    }

    public final boolean cannotFollowOwner() {
        return this.isSitting() || this.hasVehicle() || this.mightBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    protected boolean canTeleportOntoLeaves() {
        return false;
    }

    public class TameableEscapeDangerGoal
    extends EscapeDangerGoal {
        public TameableEscapeDangerGoal(double speed, TagKey<DamageType> dangerousDamageTypes) {
            super((PathAwareEntity)TameableEntity.this, speed, dangerousDamageTypes);
        }

        public TameableEscapeDangerGoal(double speed) {
            super(TameableEntity.this, speed);
        }

        @Override
        public void tick() {
            if (!TameableEntity.this.cannotFollowOwner() && TameableEntity.this.shouldTryTeleportToOwner()) {
                TameableEntity.this.tryTeleportToOwner();
            }
            super.tick();
        }
    }
}

