/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.vehicle;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class TntMinecartEntity
extends AbstractMinecartEntity {
    private static final byte PRIME_TNT_STATUS = 10;
    private static final String EXPLOSION_POWER_NBT_KEY = "explosion_power";
    private static final String EXPLOSION_SPEED_FACTOR_NBT_KEY = "explosion_speed_factor";
    private static final String FUSE_NBT_KEY = "fuse";
    private static final float DEFAULT_EXPLOSION_POWER = 4.0f;
    private static final float DEFAULT_EXPLOSION_SPEED_FACTOR = 1.0f;
    private static final int DEFAULT_FUSE_TICKS = -1;
    private @Nullable DamageSource damageSource;
    private int fuseTicks = -1;
    private float explosionPower = 4.0f;
    private float explosionSpeedFactor = 1.0f;

    public TntMinecartEntity(EntityType<? extends TntMinecartEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return Blocks.TNT.getDefaultState();
    }

    @Override
    public void tick() {
        double d;
        super.tick();
        if (this.fuseTicks > 0) {
            --this.fuseTicks;
            this.getEntityWorld().addParticleClient(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.fuseTicks == 0) {
            this.explode(this.damageSource, this.getVelocity().horizontalLengthSquared());
        }
        if (this.horizontalCollision && (d = this.getVelocity().horizontalLengthSquared()) >= (double)0.01f) {
            this.explode(this.damageSource, d);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        PersistentProjectileEntity persistentProjectileEntity;
        Entity entity = source.getSource();
        if (entity instanceof PersistentProjectileEntity && (persistentProjectileEntity = (PersistentProjectileEntity)entity).isOnFire()) {
            DamageSource damageSource = this.getDamageSources().explosion(this, source.getAttacker());
            this.explode(damageSource, persistentProjectileEntity.getVelocity().lengthSquared());
        }
        return super.damage(world, source, amount);
    }

    @Override
    public void killAndDropSelf(ServerWorld world, DamageSource damageSource) {
        double d = this.getVelocity().horizontalLengthSquared();
        if (TntMinecartEntity.shouldDetonate(damageSource) || d >= (double)0.01f) {
            if (this.fuseTicks < 0) {
                this.prime(damageSource);
                this.fuseTicks = this.random.nextInt(20) + this.random.nextInt(20);
            }
            return;
        }
        this.killAndDropItem(world, this.asItem());
    }

    @Override
    protected Item asItem() {
        return Items.TNT_MINECART;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.TNT_MINECART);
    }

    protected void explode(@Nullable DamageSource damageSource, double power) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (serverWorld.getGameRules().getValue(GameRules.TNT_EXPLODES).booleanValue()) {
                double d = Math.min(Math.sqrt(power), 5.0);
                serverWorld.createExplosion(this, damageSource, null, this.getX(), this.getY(), this.getZ(), (float)((double)this.explosionPower + (double)this.explosionSpeedFactor * this.random.nextDouble() * 1.5 * d), false, World.ExplosionSourceType.TNT);
                this.discard();
            } else if (this.isPrimed()) {
                this.discard();
            }
        }
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        if (fallDistance >= 3.0) {
            double d = fallDistance / 10.0;
            this.explode(this.damageSource, d * d);
        }
        return super.handleFallDamage(fallDistance, damagePerDistance, damageSource);
    }

    @Override
    public void onActivatorRail(ServerWorld serverWorld, int y, int z, int i, boolean bl) {
        if (bl && this.fuseTicks < 0) {
            this.prime(null);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 10) {
            this.prime(null);
        } else {
            super.handleStatus(status);
        }
    }

    public void prime(@Nullable DamageSource source) {
        ServerWorld serverWorld;
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld && !(serverWorld = (ServerWorld)world).getGameRules().getValue(GameRules.TNT_EXPLODES).booleanValue()) {
            return;
        }
        this.fuseTicks = 80;
        if (!this.getEntityWorld().isClient()) {
            if (source != null && this.damageSource == null) {
                this.damageSource = this.getDamageSources().explosion(this, source.getAttacker());
            }
            this.getEntityWorld().sendEntityStatus(this, (byte)10);
            if (!this.isSilent()) {
                this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    public int getFuseTicks() {
        return this.fuseTicks;
    }

    public boolean isPrimed() {
        return this.fuseTicks > -1;
    }

    @Override
    public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
        if (this.isPrimed() && (blockState.isIn(BlockTags.RAILS) || world.getBlockState(pos.up()).isIn(BlockTags.RAILS))) {
            return 0.0f;
        }
        return super.getEffectiveExplosionResistance(explosion, world, pos, blockState, fluidState, max);
    }

    @Override
    public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
        if (this.isPrimed() && (state.isIn(BlockTags.RAILS) || world.getBlockState(pos.up()).isIn(BlockTags.RAILS))) {
            return false;
        }
        return super.canExplosionDestroyBlock(explosion, world, pos, state, explosionPower);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.fuseTicks = view.getInt(FUSE_NBT_KEY, -1);
        this.explosionPower = MathHelper.clamp(view.getFloat(EXPLOSION_POWER_NBT_KEY, 4.0f), 0.0f, 128.0f);
        this.explosionSpeedFactor = MathHelper.clamp(view.getFloat(EXPLOSION_SPEED_FACTOR_NBT_KEY, 1.0f), 0.0f, 128.0f);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt(FUSE_NBT_KEY, this.fuseTicks);
        if (this.explosionPower != 4.0f) {
            view.putFloat(EXPLOSION_POWER_NBT_KEY, this.explosionPower);
        }
        if (this.explosionSpeedFactor != 1.0f) {
            view.putFloat(EXPLOSION_SPEED_FACTOR_NBT_KEY, this.explosionSpeedFactor);
        }
    }

    @Override
    protected boolean shouldAlwaysKill(DamageSource source) {
        return TntMinecartEntity.shouldDetonate(source);
    }

    private static boolean shouldDetonate(DamageSource source) {
        Entity entity = source.getSource();
        if (entity instanceof ProjectileEntity) {
            ProjectileEntity projectileEntity = (ProjectileEntity)entity;
            return projectileEntity.isOnFire();
        }
        return source.isIn(DamageTypeTags.IS_FIRE) || source.isIn(DamageTypeTags.IS_EXPLOSION);
    }
}

