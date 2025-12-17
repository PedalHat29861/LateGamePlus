/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 */
package net.minecraft.entity.projectile.thrown;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class PotionEntity
extends ThrownItemEntity {
    public static final double field_30667 = 4.0;
    protected static final double WATER_POTION_EXPLOSION_SQUARED_RADIUS = 16.0;
    public static final Predicate<LivingEntity> AFFECTED_BY_WATER = entity -> entity.hurtByWater() || entity.isOnFire();

    public PotionEntity(EntityType<? extends PotionEntity> entityType, World world) {
        super((EntityType<? extends ThrownItemEntity>)entityType, world);
    }

    public PotionEntity(EntityType<? extends PotionEntity> type, World world, LivingEntity owner, ItemStack stack) {
        super(type, owner, world, stack);
    }

    public PotionEntity(EntityType<? extends PotionEntity> type, World world, double x, double y, double z, ItemStack stack) {
        super(type, x, y, z, world, stack);
    }

    @Override
    protected double getGravity() {
        return 0.05;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (this.getEntityWorld().isClient()) {
            return;
        }
        ItemStack itemStack = this.getStack();
        Direction direction = blockHitResult.getSide();
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockPos blockPos2 = blockPos.offset(direction);
        PotionContentsComponent potionContentsComponent = itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (potionContentsComponent.matches(Potions.WATER)) {
            this.extinguishFire(blockPos2);
            this.extinguishFire(blockPos2.offset(direction.getOpposite()));
            for (Direction direction2 : Direction.Type.HORIZONTAL) {
                this.extinguishFire(blockPos2.offset(direction2));
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        ItemStack itemStack = this.getStack();
        PotionContentsComponent potionContentsComponent = itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (potionContentsComponent.matches(Potions.WATER)) {
            this.explodeWaterPotion(serverWorld);
        } else if (potionContentsComponent.hasEffects()) {
            this.spawnAreaEffectCloud(serverWorld, itemStack, hitResult);
        }
        int i = potionContentsComponent.potion().isPresent() && potionContentsComponent.potion().get().value().hasInstantEffect() ? 2007 : 2002;
        serverWorld.syncWorldEvent(i, this.getBlockPos(), potionContentsComponent.getColor());
        this.discard();
    }

    private void explodeWaterPotion(ServerWorld world) {
        Box box = this.getBoundingBox().expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.getEntityWorld().getEntitiesByClass(LivingEntity.class, box, AFFECTED_BY_WATER);
        for (LivingEntity livingEntity : list) {
            double d = this.squaredDistanceTo(livingEntity);
            if (!(d < 16.0)) continue;
            if (livingEntity.hurtByWater()) {
                livingEntity.damage(world, this.getDamageSources().indirectMagic(this, this.getOwner()), 1.0f);
            }
            if (!livingEntity.isOnFire() || !livingEntity.isAlive()) continue;
            livingEntity.extinguishWithSound();
        }
        List<AxolotlEntity> list2 = this.getEntityWorld().getNonSpectatingEntities(AxolotlEntity.class, box);
        for (AxolotlEntity axolotlEntity : list2) {
            axolotlEntity.hydrateFromPotion();
        }
    }

    protected abstract void spawnAreaEffectCloud(ServerWorld var1, ItemStack var2, HitResult var3);

    private void extinguishFire(BlockPos pos) {
        BlockState blockState = this.getEntityWorld().getBlockState(pos);
        if (blockState.isIn(BlockTags.FIRE)) {
            this.getEntityWorld().breakBlock(pos, false, this);
        } else if (AbstractCandleBlock.isLitCandle(blockState)) {
            AbstractCandleBlock.extinguish(null, blockState, this.getEntityWorld(), pos);
        } else if (CampfireBlock.isLitCampfire(blockState)) {
            this.getEntityWorld().syncWorldEvent(null, 1009, pos, 0);
            CampfireBlock.extinguish(this.getOwner(), this.getEntityWorld(), pos, blockState);
            this.getEntityWorld().setBlockState(pos, (BlockState)blockState.with(CampfireBlock.LIT, false));
        }
    }

    @Override
    public DoubleDoubleImmutablePair getKnockback(LivingEntity target, DamageSource source) {
        double d = target.getEntityPos().x - this.getEntityPos().x;
        double e = target.getEntityPos().z - this.getEntityPos().z;
        return DoubleDoubleImmutablePair.of((double)d, (double)e);
    }
}

