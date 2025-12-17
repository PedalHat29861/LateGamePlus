/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class TntEntity
extends Entity
implements Ownable {
    private static final TrackedData<Integer> FUSE = DataTracker.registerData(TntEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<BlockState> BLOCK_STATE = DataTracker.registerData(TntEntity.class, TrackedDataHandlerRegistry.BLOCK_STATE);
    private static final short DEFAULT_FUSE = 80;
    private static final float DEFAULT_EXPLOSION_POWER = 4.0f;
    private static final BlockState DEFAULT_BLOCK_STATE = Blocks.TNT.getDefaultState();
    private static final String BLOCK_STATE_NBT_KEY = "block_state";
    public static final String FUSE_NBT_KEY = "fuse";
    private static final String EXPLOSION_POWER_NBT_KEY = "explosion_power";
    private static final ExplosionBehavior TELEPORTED_EXPLOSION_BEHAVIOR = new ExplosionBehavior(){

        @Override
        public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
            if (state.isOf(Blocks.NETHER_PORTAL)) {
                return false;
            }
            return super.canDestroyBlock(explosion, world, pos, state, power);
        }

        @Override
        public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
            if (blockState.isOf(Blocks.NETHER_PORTAL)) {
                return Optional.empty();
            }
            return super.getBlastResistance(explosion, world, pos, blockState, fluidState);
        }
    };
    private @Nullable LazyEntityReference<LivingEntity> causingEntity;
    private boolean teleported;
    private float explosionPower = 4.0f;

    public TntEntity(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
        this.intersectionChecked = true;
    }

    public TntEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        this((EntityType<? extends TntEntity>)EntityType.TNT, world);
        this.setPosition(x, y, z);
        double d = world.random.nextDouble() * 6.2831854820251465;
        this.setVelocity(-Math.sin(d) * 0.02, 0.2f, -Math.cos(d) * 0.02);
        this.setFuse(80);
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.causingEntity = LazyEntityReference.of(igniter);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(FUSE, 80);
        builder.add(BLOCK_STATE, DEFAULT_BLOCK_STATE);
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        this.tickPortalTeleportation();
        this.applyGravity();
        this.move(MovementType.SELF, this.getVelocity());
        this.tickBlockCollision();
        this.setVelocity(this.getVelocity().multiply(0.98));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
        }
        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            this.discard();
            if (!this.getEntityWorld().isClient()) {
                this.explode();
            }
        } else {
            this.updateWaterState();
            if (this.getEntityWorld().isClient()) {
                this.getEntityWorld().addParticleClient(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explode() {
        ServerWorld serverWorld;
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld && (serverWorld = (ServerWorld)world).getGameRules().getValue(GameRules.TNT_EXPLODES).booleanValue()) {
            this.getEntityWorld().createExplosion(this, Explosion.createDamageSource(this.getEntityWorld(), this), this.teleported ? TELEPORTED_EXPLOSION_BEHAVIOR : null, this.getX(), this.getBodyY(0.0625), this.getZ(), this.explosionPower, false, World.ExplosionSourceType.TNT);
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putShort(FUSE_NBT_KEY, (short)this.getFuse());
        view.put(BLOCK_STATE_NBT_KEY, BlockState.CODEC, this.getBlockState());
        if (this.explosionPower != 4.0f) {
            view.putFloat(EXPLOSION_POWER_NBT_KEY, this.explosionPower);
        }
        LazyEntityReference.writeData(this.causingEntity, view, "owner");
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setFuse(view.getShort(FUSE_NBT_KEY, (short)80));
        this.setBlockState(view.read(BLOCK_STATE_NBT_KEY, BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE));
        this.explosionPower = MathHelper.clamp(view.getFloat(EXPLOSION_POWER_NBT_KEY, 4.0f), 0.0f, 128.0f);
        this.causingEntity = LazyEntityReference.fromData(view, "owner");
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return LazyEntityReference.getLivingEntity(this.causingEntity, this.getEntityWorld());
    }

    @Override
    public void copyFrom(Entity original) {
        super.copyFrom(original);
        if (original instanceof TntEntity) {
            TntEntity tntEntity = (TntEntity)original;
            this.causingEntity = tntEntity.causingEntity;
        }
    }

    public void setFuse(int fuse) {
        this.dataTracker.set(FUSE, fuse);
    }

    public int getFuse() {
        return this.dataTracker.get(FUSE);
    }

    public void setBlockState(BlockState state) {
        this.dataTracker.set(BLOCK_STATE, state);
    }

    public BlockState getBlockState() {
        return this.dataTracker.get(BLOCK_STATE);
    }

    private void setTeleported(boolean teleported) {
        this.teleported = teleported;
    }

    @Override
    public @Nullable Entity teleportTo(TeleportTarget teleportTarget) {
        Entity entity = super.teleportTo(teleportTarget);
        if (entity instanceof TntEntity) {
            TntEntity tntEntity = (TntEntity)entity;
            tntEntity.setTeleported(true);
        }
        return entity;
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public /* synthetic */ @Nullable Entity getOwner() {
        return this.getOwner();
    }
}

