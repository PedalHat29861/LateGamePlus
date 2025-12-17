/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity.decoration;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int attachCheckTimer;
    protected BlockPos attachedBlockPos;

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> entityType, World world) {
        super(entityType, world);
    }

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> type, World world, BlockPos attachedBlockPos) {
        this(type, world);
        this.attachedBlockPos = attachedBlockPos;
    }

    protected abstract void updateAttachmentPosition();

    @Override
    public void tick() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.attemptTickInVoid();
            if (this.attachCheckTimer++ == 100) {
                this.attachCheckTimer = 0;
                if (!this.isRemoved() && !this.canStayAttached()) {
                    this.discard();
                    this.onBreak(serverWorld, null);
                }
            }
        }
    }

    public abstract boolean canStayAttached();

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)attacker;
            if (!this.getEntityWorld().canEntityModifyAt(playerEntity, this.attachedBlockPos)) {
                return true;
            }
            return this.sidedDamage(this.getDamageSources().playerAttack(playerEntity), 0.0f);
        }
        return false;
    }

    @Override
    public boolean clientDamage(DamageSource source) {
        return !this.isAlwaysInvulnerableTo(source);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isAlwaysInvulnerableTo(source)) {
            return false;
        }
        if (!world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue() && source.getAttacker() instanceof MobEntity) {
            return false;
        }
        if (!this.isRemoved()) {
            this.kill(world);
            this.scheduleVelocityUpdate();
            this.onBreak(world, source.getAttacker());
        }
        return true;
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        Entity entity = explosion.getEntity();
        if (entity != null && entity.isTouchingWater()) {
            return true;
        }
        if (explosion.preservesDecorativeEntities()) {
            return super.isImmuneToExplosion(explosion);
        }
        return true;
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (!this.isRemoved() && movement.lengthSquared() > 0.0) {
                this.kill(serverWorld);
                this.onBreak(serverWorld, null);
            }
        }
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (!this.isRemoved() && deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 0.0) {
                this.kill(serverWorld);
                this.onBreak(serverWorld, null);
            }
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("block_pos", BlockPos.CODEC, this.getAttachedBlockPos());
    }

    @Override
    protected void readCustomData(ReadView view) {
        BlockPos blockPos = view.read("block_pos", BlockPos.CODEC).orElse(null);
        if (blockPos == null || !blockPos.isWithinDistance(this.getBlockPos(), 16.0)) {
            LOGGER.error("Block-attached entity at invalid position: {}", (Object)blockPos);
            return;
        }
        this.attachedBlockPos = blockPos;
    }

    public abstract void onBreak(ServerWorld var1, @Nullable Entity var2);

    @Override
    protected boolean shouldSetPositionOnLoad() {
        return false;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.attachedBlockPos = BlockPos.ofFloored(x, y, z);
        this.updateAttachmentPosition();
        this.velocityDirty = true;
    }

    public BlockPos getAttachedBlockPos() {
        return this.attachedBlockPos;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
    }

    @Override
    public void calculateDimensions() {
    }
}

