/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public abstract class ProjectileEntity
extends Entity
implements Ownable {
    private static final boolean DEFAULT_LEFT_OWNER = false;
    private static final boolean DEFAULT_SHOT = false;
    protected @Nullable LazyEntityReference<Entity> owner;
    private boolean leftOwner = false;
    private boolean checkedForLeftOwner;
    private boolean shot = false;
    private @Nullable Entity lastDeflectedEntity;

    protected ProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void setOwner(@Nullable LazyEntityReference<Entity> owner) {
        this.owner = owner;
    }

    public void setOwner(@Nullable Entity owner) {
        this.setOwner(LazyEntityReference.of(owner));
    }

    @Override
    public @Nullable Entity getOwner() {
        return LazyEntityReference.getEntity(this.owner, this.getEntityWorld());
    }

    public Entity getEffectCause() {
        return (Entity)MoreObjects.firstNonNull((Object)this.getOwner(), (Object)this);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        LazyEntityReference.writeData(this.owner, view, "Owner");
        if (this.leftOwner) {
            view.putBoolean("LeftOwner", true);
        }
        view.putBoolean("HasBeenShot", this.shot);
    }

    protected boolean isOwner(Entity entity) {
        return this.owner != null && this.owner.uuidEquals(entity);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setOwner(LazyEntityReference.fromData(view, "Owner"));
        this.leftOwner = view.getBoolean("LeftOwner", false);
        this.shot = view.getBoolean("HasBeenShot", false);
    }

    @Override
    public void copyFrom(Entity original) {
        super.copyFrom(original);
        if (original instanceof ProjectileEntity) {
            ProjectileEntity projectileEntity = (ProjectileEntity)original;
            this.owner = projectileEntity.owner;
        }
    }

    @Override
    public void tick() {
        if (!this.shot) {
            this.emitGameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.shot = true;
        }
        this.tickLeftOwner();
        super.tick();
        this.checkedForLeftOwner = false;
    }

    protected void tickLeftOwner() {
        if (!this.leftOwner && !this.checkedForLeftOwner) {
            this.leftOwner = this.hasLeftOwner();
            this.checkedForLeftOwner = true;
        }
    }

    private boolean hasLeftOwner() {
        Entity entity2 = this.getOwner();
        if (entity2 != null) {
            Box box = this.getBoundingBox().stretch(this.getVelocity()).expand(1.0);
            return entity2.getRootVehicle().streamSelfAndPassengers().filter(EntityPredicates.CAN_HIT).noneMatch(entity -> box.intersects(entity.getBoundingBox()));
        }
        return true;
    }

    public Vec3d calculateVelocity(double x, double y, double z, float power, float uncertainty) {
        return new Vec3d(x, y, z).normalize().add(this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty), this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty), this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty)).multiply(power);
    }

    public void setVelocity(double x, double y, double z, float power, float uncertainty) {
        Vec3d vec3d = this.calculateVelocity(x, y, z, power, uncertainty);
        this.setVelocity(vec3d);
        this.velocityDirty = true;
        double d = vec3d.horizontalLength();
        this.setYaw((float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875));
        this.setPitch((float)(MathHelper.atan2(vec3d.y, d) * 57.2957763671875));
        this.lastYaw = this.getYaw();
        this.lastPitch = this.getPitch();
    }

    public void setVelocity(Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        float f = -MathHelper.sin(yaw * ((float)Math.PI / 180)) * MathHelper.cos(pitch * ((float)Math.PI / 180));
        float g = -MathHelper.sin((pitch + roll) * ((float)Math.PI / 180));
        float h = MathHelper.cos(yaw * ((float)Math.PI / 180)) * MathHelper.cos(pitch * ((float)Math.PI / 180));
        this.setVelocity(f, g, h, speed, divergence);
        Vec3d vec3d = shooter.getMovement();
        this.setVelocity(this.getVelocity().add(vec3d.x, shooter.isOnGround() ? 0.0 : vec3d.y, vec3d.z));
    }

    @Override
    public void onBubbleColumnSurfaceCollision(boolean drag, BlockPos pos) {
        double d = drag ? -0.03 : 0.1;
        this.setVelocity(this.getVelocity().add(0.0, d, 0.0));
        ProjectileEntity.spawnBubbleColumnParticles(this.getEntityWorld(), pos);
    }

    @Override
    public void onBubbleColumnCollision(boolean drag) {
        double d = drag ? -0.03 : 0.06;
        this.setVelocity(this.getVelocity().add(0.0, d, 0.0));
        this.onLanding();
    }

    public static <T extends ProjectileEntity> T spawnWithVelocity(ProjectileCreator<T> creator, ServerWorld world, ItemStack projectileStack, LivingEntity shooter, float roll, float power, float divergence) {
        return (T)ProjectileEntity.spawn(creator.create(world, shooter, projectileStack), world, projectileStack, entity -> entity.setVelocity(shooter, shooter.getPitch(), shooter.getYaw(), roll, power, divergence));
    }

    public static <T extends ProjectileEntity> T spawnWithVelocity(ProjectileCreator<T> creator, ServerWorld world, ItemStack projectileStack, LivingEntity shooter, double velocityX, double velocityY, double velocityZ, float power, float divergence) {
        return (T)ProjectileEntity.spawn(creator.create(world, shooter, projectileStack), world, projectileStack, entity -> entity.setVelocity(velocityX, velocityY, velocityZ, power, divergence));
    }

    public static <T extends ProjectileEntity> T spawnWithVelocity(T projectile, ServerWorld world, ItemStack projectileStack, double velocityX, double velocityY, double velocityZ, float power, float divergence) {
        return (T)ProjectileEntity.spawn(projectile, world, projectileStack, entity -> projectile.setVelocity(velocityX, velocityY, velocityZ, power, divergence));
    }

    public static <T extends ProjectileEntity> T spawn(T projectile, ServerWorld world, ItemStack projectileStack) {
        return (T)ProjectileEntity.spawn(projectile, world, projectileStack, entity -> {});
    }

    public static <T extends ProjectileEntity> T spawn(T projectile, ServerWorld world, ItemStack projectileStack, Consumer<T> beforeSpawn) {
        beforeSpawn.accept(projectile);
        world.spawnEntity(projectile);
        projectile.triggerProjectileSpawned(world, projectileStack);
        return projectile;
    }

    public void triggerProjectileSpawned(ServerWorld world, ItemStack projectileStack) {
        PersistentProjectileEntity persistentProjectileEntity;
        ItemStack itemStack;
        EnchantmentHelper.onProjectileSpawned(world, projectileStack, this, item -> {});
        ProjectileEntity projectileEntity = this;
        if (projectileEntity instanceof PersistentProjectileEntity && (itemStack = (persistentProjectileEntity = (PersistentProjectileEntity)projectileEntity).getWeaponStack()) != null && !itemStack.isEmpty() && !projectileStack.getItem().equals(itemStack.getItem())) {
            EnchantmentHelper.onProjectileSpawned(world, itemStack, this, persistentProjectileEntity::onBroken);
        }
    }

    protected ProjectileDeflection hitOrDeflect(HitResult hitResult) {
        ProjectileDeflection projectileDeflection2;
        BlockHitResult blockHitResult;
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entity = entityHitResult.getEntity();
            ProjectileDeflection projectileDeflection = entity.getProjectileDeflection(this);
            if (projectileDeflection != ProjectileDeflection.NONE) {
                if (entity != this.lastDeflectedEntity && this.deflect(projectileDeflection, entity, this.owner, false)) {
                    this.lastDeflectedEntity = entity;
                }
                return projectileDeflection;
            }
        } else if (this.deflectsAgainstWorldBorder() && hitResult instanceof BlockHitResult && (blockHitResult = (BlockHitResult)hitResult).isAgainstWorldBorder() && this.deflect(projectileDeflection2 = ProjectileDeflection.SIMPLE, null, this.owner, false)) {
            this.setVelocity(this.getVelocity().multiply(0.2));
            return projectileDeflection2;
        }
        this.onCollision(hitResult);
        return ProjectileDeflection.NONE;
    }

    protected boolean deflectsAgainstWorldBorder() {
        return false;
    }

    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflector, @Nullable LazyEntityReference<Entity> lazyEntityReference, boolean fromAttack) {
        deflection.deflect(this, deflector, this.random);
        if (!this.getEntityWorld().isClient()) {
            this.setOwner(lazyEntityReference);
            this.onDeflected(fromAttack);
        }
        return true;
    }

    protected void onDeflected(boolean bl) {
    }

    protected void onBroken(Item item) {
    }

    protected void onCollision(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entity = entityHitResult.getEntity();
            if (entity.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof ProjectileEntity) {
                ProjectileEntity projectileEntity = (ProjectileEntity)entity;
                projectileEntity.deflect(ProjectileDeflection.REDIRECTED, this.getOwner(), this.owner, true);
            }
            this.onEntityHit(entityHitResult);
            this.getEntityWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            this.onBlockHit(blockHitResult);
            BlockPos blockPos = blockHitResult.getBlockPos();
            this.getEntityWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Emitter.of(this, this.getEntityWorld().getBlockState(blockPos)));
        }
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
    }

    protected void onBlockHit(BlockHitResult blockHitResult) {
        BlockState blockState = this.getEntityWorld().getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.getEntityWorld(), blockState, blockHitResult, this);
    }

    protected boolean canHit(Entity entity) {
        if (!entity.canBeHitByProjectile()) {
            return false;
        }
        Entity entity2 = this.getOwner();
        return entity2 == null || this.leftOwner || !entity2.isConnectedThroughVehicle(entity);
    }

    protected void updateRotation() {
        Vec3d vec3d = this.getVelocity();
        double d = vec3d.horizontalLength();
        this.setPitch(ProjectileEntity.updateRotation(this.lastPitch, (float)(MathHelper.atan2(vec3d.y, d) * 57.2957763671875)));
        this.setYaw(ProjectileEntity.updateRotation(this.lastYaw, (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875)));
    }

    protected static float updateRotation(float lastRot, float newRot) {
        while (newRot - lastRot < -180.0f) {
            lastRot -= 360.0f;
        }
        while (newRot - lastRot >= 180.0f) {
            lastRot += 360.0f;
        }
        return MathHelper.lerp(0.2f, lastRot, newRot);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        Entity entity = this.getOwner();
        return new EntitySpawnS2CPacket((Entity)this, entityTrackerEntry, entity == null ? 0 : entity.getId());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        Entity entity = this.getEntityWorld().getEntityById(packet.getEntityData());
        if (entity != null) {
            this.setOwner(entity);
        }
    }

    @Override
    public boolean canModifyAt(ServerWorld world, BlockPos pos) {
        Entity entity = this.getOwner();
        if (entity instanceof PlayerEntity) {
            return entity.canModifyAt(world, pos);
        }
        return entity == null || world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING) != false;
    }

    public boolean canBreakBlocks(ServerWorld world) {
        return this.getType().isIn(EntityTypeTags.IMPACT_PROJECTILES) && world.getGameRules().getValue(GameRules.PROJECTILES_CAN_BREAK_BLOCKS) != false;
    }

    @Override
    public boolean canHit() {
        return this.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getTargetingMargin() {
        return this.canHit() ? 1.0f : 0.0f;
    }

    public DoubleDoubleImmutablePair getKnockback(LivingEntity target, DamageSource source) {
        double d = this.getVelocity().x;
        double e = this.getVelocity().z;
        return DoubleDoubleImmutablePair.of((double)d, (double)e);
    }

    @Override
    public int getDefaultPortalCooldown() {
        return 2;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (!this.isAlwaysInvulnerableTo(source)) {
            this.scheduleVelocityUpdate();
        }
        return false;
    }

    @FunctionalInterface
    public static interface ProjectileCreator<T extends ProjectileEntity> {
        public T create(ServerWorld var1, LivingEntity var2, ItemStack var3);
    }
}

