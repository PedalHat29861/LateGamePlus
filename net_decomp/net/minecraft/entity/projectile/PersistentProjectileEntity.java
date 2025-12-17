/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.OminousItemSpawnerEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public abstract class PersistentProjectileEntity
extends ProjectileEntity {
    private static final double field_30657 = 2.0;
    private static final int field_54968 = 7;
    private static final float field_55017 = 0.6f;
    private static final float DEFAULT_DRAG = 0.99f;
    private static final short DEFAULT_LIFE = 0;
    private static final byte DEFAULT_SHAKE = 0;
    private static final boolean DEFAULT_IN_GROUND = false;
    private static final boolean DEFAULT_CRITICAL = false;
    private static final byte DEFAULT_PIERCE_LEVEL = 0;
    private static final TrackedData<Byte> PROJECTILE_FLAGS = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Byte> PIERCE_LEVEL = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> IN_GROUND = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final int CRITICAL_FLAG = 1;
    private static final int NO_CLIP_FLAG = 2;
    private @Nullable BlockState inBlockState;
    protected int inGroundTime;
    public PickupPermission pickupType = PickupPermission.DISALLOWED;
    public int shake = 0;
    private int life = 0;
    private double damage = 2.0;
    private SoundEvent sound = this.getHitSound();
    private @Nullable IntOpenHashSet piercedEntities;
    private @Nullable List<Entity> piercingKilledEntities;
    private ItemStack stack = this.getDefaultItemStack();
    private @Nullable ItemStack weapon = null;

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
    }

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world, ItemStack stack, @Nullable ItemStack weapon) {
        this(type, world);
        this.stack = stack.copy();
        this.copyComponentsFrom(stack);
        Unit unit = stack.remove(DataComponentTypes.INTANGIBLE_PROJECTILE);
        if (unit != null) {
            this.pickupType = PickupPermission.CREATIVE_ONLY;
        }
        this.setPosition(x, y, z);
        if (weapon != null && world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (weapon.isEmpty()) {
                throw new IllegalArgumentException("Invalid weapon firing an arrow");
            }
            this.weapon = weapon.copy();
            int i = EnchantmentHelper.getProjectilePiercing(serverWorld, weapon, this.stack);
            if (i > 0) {
                this.setPierceLevel((byte)i);
            }
        }
    }

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, LivingEntity owner, World world, ItemStack stack, @Nullable ItemStack shotFrom) {
        this(type, owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ(), world, stack, shotFrom);
        this.setOwner(owner);
    }

    public void setSound(SoundEvent sound) {
        this.sound = sound;
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = this.getBoundingBox().getAverageSideLength() * 10.0;
        if (Double.isNaN(d)) {
            d = 1.0;
        }
        return distance < (d *= 64.0 * PersistentProjectileEntity.getRenderDistanceMultiplier()) * d;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(PROJECTILE_FLAGS, (byte)0);
        builder.add(PIERCE_LEVEL, (byte)0);
        builder.add(IN_GROUND, false);
    }

    @Override
    public void setVelocity(double x, double y, double z, float power, float uncertainty) {
        super.setVelocity(x, y, z, power, uncertainty);
        this.life = 0;
    }

    @Override
    public void setVelocityClient(Vec3d clientVelocity) {
        super.setVelocityClient(clientVelocity);
        this.life = 0;
        if (this.isInGround() && clientVelocity.lengthSquared() > 0.0) {
            this.setInGround(false);
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (!this.firstUpdate && this.shake <= 0 && data.equals(IN_GROUND) && this.isInGround()) {
            this.shake = 7;
        }
    }

    @Override
    public void tick() {
        VoxelShape voxelShape;
        boolean bl = !this.isNoClip();
        Vec3d vec3d = this.getVelocity();
        BlockPos blockPos = this.getBlockPos();
        BlockState blockState = this.getEntityWorld().getBlockState(blockPos);
        if (!blockState.isAir() && bl && !(voxelShape = blockState.getCollisionShape(this.getEntityWorld(), blockPos)).isEmpty()) {
            Vec3d vec3d2 = this.getEntityPos();
            for (Box box : voxelShape.getBoundingBoxes()) {
                if (!box.offset(blockPos).contains(vec3d2)) continue;
                this.setVelocity(Vec3d.ZERO);
                this.setInGround(true);
                break;
            }
        }
        if (this.shake > 0) {
            --this.shake;
        }
        if (this.isTouchingWaterOrRain()) {
            this.extinguish();
        }
        if (this.isInGround() && bl) {
            if (!this.getEntityWorld().isClient()) {
                if (this.inBlockState != blockState && this.shouldFall()) {
                    this.fall();
                } else {
                    this.age();
                }
            }
            ++this.inGroundTime;
            if (this.isAlive()) {
                this.tickBlockCollision();
            }
            if (!this.getEntityWorld().isClient()) {
                this.setOnFire(this.getFireTicks() > 0);
            }
            return;
        }
        this.inGroundTime = 0;
        Vec3d vec3d3 = this.getEntityPos();
        if (this.isTouchingWater()) {
            this.applyDrag(this.getDragInWater());
            this.spawnBubbleParticles(vec3d3);
        }
        if (this.isCritical()) {
            for (int i = 0; i < 4; ++i) {
                this.getEntityWorld().addParticleClient(ParticleTypes.CRIT, vec3d3.x + vec3d.x * (double)i / 4.0, vec3d3.y + vec3d.y * (double)i / 4.0, vec3d3.z + vec3d.z * (double)i / 4.0, -vec3d.x, -vec3d.y + 0.2, -vec3d.z);
            }
        }
        float f = !bl ? (float)(MathHelper.atan2(-vec3d.x, -vec3d.z) * 57.2957763671875) : (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875);
        float g = (float)(MathHelper.atan2(vec3d.y, vec3d.horizontalLength()) * 57.2957763671875);
        this.setPitch(PersistentProjectileEntity.updateRotation(this.getPitch(), g));
        this.setYaw(PersistentProjectileEntity.updateRotation(this.getYaw(), f));
        this.tickLeftOwner();
        if (bl) {
            BlockHitResult blockHitResult = this.getEntityWorld().getCollisionsIncludingWorldBorder(new RaycastContext(vec3d3, vec3d3.add(vec3d), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
            this.applyCollision(blockHitResult);
        } else {
            this.setPosition(vec3d3.add(vec3d));
            this.tickBlockCollision();
        }
        if (!this.isTouchingWater()) {
            this.applyDrag(0.99f);
        }
        if (bl && !this.isInGround()) {
            this.applyGravity();
        }
        super.tick();
    }

    private void applyCollision(BlockHitResult blockHitResult) {
        while (this.isAlive()) {
            Vec3d vec3d = this.getEntityPos();
            ArrayList<EntityHitResult> arrayList = new ArrayList<EntityHitResult>(this.collectPiercingCollisions(vec3d, blockHitResult.getPos()));
            arrayList.sort(Comparator.comparingDouble(entityHitResult -> vec3d.squaredDistanceTo(entityHitResult.getEntity().getEntityPos())));
            EntityHitResult entityHitResult2 = arrayList.isEmpty() ? null : arrayList.getFirst();
            Vec3d vec3d2 = ((HitResult)Objects.requireNonNullElse(entityHitResult2, blockHitResult)).getPos();
            this.setPosition(vec3d2);
            this.tickBlockCollision(vec3d, vec3d2);
            if (this.portalManager != null && this.portalManager.isInPortal()) {
                this.tickPortalTeleportation();
            }
            if (arrayList.isEmpty()) {
                if (!this.isAlive() || blockHitResult.getType() == HitResult.Type.MISS) break;
                this.hitOrDeflect(blockHitResult);
                this.velocityDirty = true;
                break;
            }
            if (!this.isAlive() || this.noClip) continue;
            ProjectileDeflection projectileDeflection = this.hitOrDeflect(arrayList);
            this.velocityDirty = true;
            if (this.getPierceLevel() > 0 && projectileDeflection == ProjectileDeflection.NONE) continue;
            break;
        }
    }

    private ProjectileDeflection hitOrDeflect(Collection<EntityHitResult> hitResults) {
        for (EntityHitResult entityHitResult : hitResults) {
            ProjectileDeflection projectileDeflection = this.hitOrDeflect(entityHitResult);
            if (this.isAlive() && projectileDeflection == ProjectileDeflection.NONE) continue;
            return projectileDeflection;
        }
        return ProjectileDeflection.NONE;
    }

    private void applyDrag(float drag) {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.multiply(drag));
    }

    private void spawnBubbleParticles(Vec3d pos) {
        Vec3d vec3d = this.getVelocity();
        for (int i = 0; i < 4; ++i) {
            float f = 0.25f;
            this.getEntityWorld().addParticleClient(ParticleTypes.BUBBLE, pos.x - vec3d.x * 0.25, pos.y - vec3d.y * 0.25, pos.z - vec3d.z * 0.25, vec3d.x, vec3d.y, vec3d.z);
        }
    }

    @Override
    protected double getGravity() {
        return 0.05;
    }

    private boolean shouldFall() {
        return this.isInGround() && this.getEntityWorld().isSpaceEmpty(new Box(this.getEntityPos(), this.getEntityPos()).expand(0.06));
    }

    private void fall() {
        this.setInGround(false);
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.multiply(this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f));
        this.life = 0;
    }

    protected boolean isInGround() {
        return this.dataTracker.get(IN_GROUND);
    }

    protected void setInGround(boolean inGround) {
        this.dataTracker.set(IN_GROUND, inGround);
    }

    @Override
    public boolean isPushedByFluids() {
        return !this.isInGround();
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        super.move(type, movement);
        if (type != MovementType.SELF && this.shouldFall()) {
            this.fall();
        }
    }

    protected void age() {
        ++this.life;
        if (this.life >= 1200) {
            this.discard();
        }
    }

    private void clearPiercingStatus() {
        if (this.piercingKilledEntities != null) {
            this.piercingKilledEntities.clear();
        }
        if (this.piercedEntities != null) {
            this.piercedEntities.clear();
        }
    }

    @Override
    public void onBroken(Item item) {
        this.weapon = null;
    }

    @Override
    public void onBubbleColumnSurfaceCollision(boolean drag, BlockPos pos) {
        if (this.isInGround()) {
            return;
        }
        super.onBubbleColumnSurfaceCollision(drag, pos);
    }

    @Override
    public void onBubbleColumnCollision(boolean drag) {
        if (this.isInGround()) {
            return;
        }
        super.onBubbleColumnCollision(drag);
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        if (this.isInGround()) {
            return;
        }
        super.addVelocity(deltaX, deltaY, deltaZ);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        World world;
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        float f = (float)this.getVelocity().length();
        double d = this.damage;
        Entity entity2 = this.getOwner();
        DamageSource damageSource = this.getDamageSources().arrow(this, entity2 != null ? entity2 : this);
        if (this.getWeaponStack() != null && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            d = EnchantmentHelper.getDamage(serverWorld, this.getWeaponStack(), entity, damageSource, (float)d);
        }
        int i = MathHelper.ceil(MathHelper.clamp((double)f * d, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercedEntities == null) {
                this.piercedEntities = new IntOpenHashSet(5);
            }
            if (this.piercingKilledEntities == null) {
                this.piercingKilledEntities = Lists.newArrayListWithCapacity((int)5);
            }
            if (this.piercedEntities.size() < this.getPierceLevel() + 1) {
                this.piercedEntities.add(entity.getId());
            } else {
                this.discard();
                return;
            }
        }
        if (this.isCritical()) {
            long l = this.random.nextInt(i / 2 + 2);
            i = (int)Math.min(l + (long)i, Integer.MAX_VALUE);
        }
        if (entity2 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity2;
            livingEntity.onAttacking(entity);
        }
        boolean bl = entity.getType() == EntityType.ENDERMAN;
        int j = entity.getFireTicks();
        if (this.isOnFire() && !bl) {
            entity.setOnFireFor(5.0f);
        }
        if (entity.sidedDamage(damageSource, i)) {
            if (bl) {
                return;
            }
            if (entity instanceof LivingEntity) {
                ServerPlayerEntity serverPlayerEntity;
                LivingEntity livingEntity2 = (LivingEntity)entity;
                if (!this.getEntityWorld().isClient() && this.getPierceLevel() <= 0) {
                    livingEntity2.setStuckArrowCount(livingEntity2.getStuckArrowCount() + 1);
                }
                this.knockback(livingEntity2, damageSource);
                World world2 = this.getEntityWorld();
                if (world2 instanceof ServerWorld) {
                    ServerWorld serverWorld2 = (ServerWorld)world2;
                    EnchantmentHelper.onTargetDamaged(serverWorld2, livingEntity2, damageSource, this.getWeaponStack());
                }
                this.onHit(livingEntity2);
                if (livingEntity2 instanceof PlayerEntity && entity2 instanceof ServerPlayerEntity) {
                    serverPlayerEntity = (ServerPlayerEntity)entity2;
                    if (!this.isSilent() && livingEntity2 != serverPlayerEntity) {
                        serverPlayerEntity.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER, 0.0f));
                    }
                }
                if (!entity.isAlive() && this.piercingKilledEntities != null) {
                    this.piercingKilledEntities.add(livingEntity2);
                }
                if (!this.getEntityWorld().isClient() && entity2 instanceof ServerPlayerEntity) {
                    serverPlayerEntity = (ServerPlayerEntity)entity2;
                    if (this.piercingKilledEntities != null) {
                        Criteria.KILLED_BY_ARROW.trigger(serverPlayerEntity, this.piercingKilledEntities, this.weapon);
                    } else if (!entity.isAlive()) {
                        Criteria.KILLED_BY_ARROW.trigger(serverPlayerEntity, List.of(entity), this.weapon);
                    }
                }
            }
            this.playSound(this.sound, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setFireTicks(j);
            this.deflect(ProjectileDeflection.SIMPLE, entity, this.owner, false);
            this.setVelocity(this.getVelocity().multiply(0.2));
            World world3 = this.getEntityWorld();
            if (world3 instanceof ServerWorld) {
                ServerWorld serverWorld3 = (ServerWorld)world3;
                if (this.getVelocity().lengthSquared() < 1.0E-7) {
                    if (this.pickupType == PickupPermission.ALLOWED) {
                        this.dropStack(serverWorld3, this.asItemStack(), 0.1f);
                    }
                    this.discard();
                }
            }
        }
    }

    protected void knockback(LivingEntity target, DamageSource source) {
        float f;
        World world;
        if (this.weapon != null && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            f = EnchantmentHelper.modifyKnockback(serverWorld, this.weapon, target, source, 0.0f);
        } else {
            f = 0.0f;
        }
        double d = f;
        if (d > 0.0) {
            double e = Math.max(0.0, 1.0 - target.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE));
            Vec3d vec3d = this.getVelocity().multiply(1.0, 0.0, 1.0).normalize().multiply(d * 0.6 * e);
            if (vec3d.lengthSquared() > 0.0) {
                target.addVelocity(vec3d.x, 0.1, vec3d.z);
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.inBlockState = this.getEntityWorld().getBlockState(blockHitResult.getBlockPos());
        super.onBlockHit(blockHitResult);
        ItemStack itemStack = this.getWeaponStack();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (itemStack != null) {
                this.onBlockHitEnchantmentEffects(serverWorld, blockHitResult, itemStack);
            }
        }
        Vec3d vec3d = this.getVelocity();
        Vec3d vec3d2 = new Vec3d(Math.signum(vec3d.x), Math.signum(vec3d.y), Math.signum(vec3d.z));
        Vec3d vec3d3 = vec3d2.multiply(0.05f);
        this.setPosition(this.getEntityPos().subtract(vec3d3));
        this.setVelocity(Vec3d.ZERO);
        this.playSound(this.getSound(), 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
        this.setInGround(true);
        this.shake = 7;
        this.setCritical(false);
        this.setPierceLevel((byte)0);
        this.setSound(SoundEvents.ENTITY_ARROW_HIT);
        this.clearPiercingStatus();
    }

    protected void onBlockHitEnchantmentEffects(ServerWorld world, BlockHitResult blockHitResult, ItemStack weaponStack) {
        LivingEntity livingEntity;
        Vec3d vec3d = blockHitResult.getBlockPos().clampToWithin(blockHitResult.getPos());
        Entity entity = this.getOwner();
        EnchantmentHelper.onHitBlock(world, weaponStack, entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : null, this, null, vec3d, world.getBlockState(blockHitResult.getBlockPos()), item -> {
            this.weapon = null;
        });
    }

    @Override
    public @Nullable ItemStack getWeaponStack() {
        return this.weapon;
    }

    protected SoundEvent getHitSound() {
        return SoundEvents.ENTITY_ARROW_HIT;
    }

    protected final SoundEvent getSound() {
        return this.sound;
    }

    protected void onHit(LivingEntity target) {
    }

    protected @Nullable EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return ProjectileUtil.getEntityCollision(this.getEntityWorld(), this, currentPosition, nextPosition, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0), this::canHit);
    }

    protected Collection<EntityHitResult> collectPiercingCollisions(Vec3d from, Vec3d to) {
        return ProjectileUtil.collectPiercingCollisions(this.getEntityWorld(), this, from, to, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0), this::canHit, false);
    }

    @Override
    protected boolean canHit(Entity entity) {
        PlayerEntity playerEntity;
        Entity entity2;
        if (entity instanceof PlayerEntity && (entity2 = this.getOwner()) instanceof PlayerEntity && !(playerEntity = (PlayerEntity)entity2).shouldDamagePlayer((PlayerEntity)entity)) {
            return false;
        }
        return super.canHit(entity) && (this.piercedEntities == null || !this.piercedEntities.contains(entity.getId()));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putShort("life", (short)this.life);
        view.putNullable("inBlockState", BlockState.CODEC, this.inBlockState);
        view.putByte("shake", (byte)this.shake);
        view.putBoolean("inGround", this.isInGround());
        view.put("pickup", PickupPermission.CODEC, this.pickupType);
        view.putDouble("damage", this.damage);
        view.putBoolean("crit", this.isCritical());
        view.putByte("PierceLevel", this.getPierceLevel());
        view.put("SoundEvent", Registries.SOUND_EVENT.getCodec(), this.sound);
        view.put("item", ItemStack.CODEC, this.stack);
        view.putNullable("weapon", ItemStack.CODEC, this.weapon);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.life = view.getShort("life", (short)0);
        this.inBlockState = view.read("inBlockState", BlockState.CODEC).orElse(null);
        this.shake = view.getByte("shake", (byte)0) & 0xFF;
        this.setInGround(view.getBoolean("inGround", false));
        this.damage = view.getDouble("damage", 2.0);
        this.pickupType = view.read("pickup", PickupPermission.CODEC).orElse(PickupPermission.DISALLOWED);
        this.setCritical(view.getBoolean("crit", false));
        this.setPierceLevel(view.getByte("PierceLevel", (byte)0));
        this.sound = view.read("SoundEvent", Registries.SOUND_EVENT.getCodec()).orElse(this.getHitSound());
        this.setStack(view.read("item", ItemStack.CODEC).orElse(this.getDefaultItemStack()));
        this.weapon = view.read("weapon", ItemStack.CODEC).orElse(null);
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        PickupPermission pickupPermission;
        super.setOwner(owner);
        Entity entity = owner;
        int n = 0;
        block4: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PlayerEntity.class, OminousItemSpawnerEntity.class}, (Object)entity, n)) {
                case 0: {
                    PlayerEntity playerEntity = (PlayerEntity)entity;
                    if (this.pickupType != PickupPermission.DISALLOWED) {
                        n = 1;
                        continue block4;
                    }
                    pickupPermission = PickupPermission.ALLOWED;
                    break block4;
                }
                case 1: {
                    OminousItemSpawnerEntity ominousItemSpawnerEntity = (OminousItemSpawnerEntity)entity;
                    pickupPermission = PickupPermission.DISALLOWED;
                    break block4;
                }
                default: {
                    pickupPermission = this.pickupType;
                    break block4;
                }
            }
            break;
        }
        this.pickupType = pickupPermission;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (this.getEntityWorld().isClient() || !this.isInGround() && !this.isNoClip() || this.shake > 0) {
            return;
        }
        if (this.tryPickup(player)) {
            player.sendPickup(this, 1);
            this.discard();
        }
    }

    protected boolean tryPickup(PlayerEntity player) {
        return switch (this.pickupType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> false;
            case 1 -> player.getInventory().insertStack(this.asItemStack());
            case 2 -> player.isInCreativeMode();
        };
    }

    protected ItemStack asItemStack() {
        return this.stack.copy();
    }

    protected abstract ItemStack getDefaultItemStack();

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public boolean isAttackable() {
        return this.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    public void setCritical(boolean critical) {
        this.setProjectileFlag(1, critical);
    }

    private void setPierceLevel(byte level) {
        this.dataTracker.set(PIERCE_LEVEL, level);
    }

    private void setProjectileFlag(int index, boolean flag) {
        byte b = this.dataTracker.get(PROJECTILE_FLAGS);
        if (flag) {
            this.dataTracker.set(PROJECTILE_FLAGS, (byte)(b | index));
        } else {
            this.dataTracker.set(PROJECTILE_FLAGS, (byte)(b & ~index));
        }
    }

    protected void setStack(ItemStack stack) {
        this.stack = !stack.isEmpty() ? stack : this.getDefaultItemStack();
    }

    public boolean isCritical() {
        byte b = this.dataTracker.get(PROJECTILE_FLAGS);
        return (b & 1) != 0;
    }

    public byte getPierceLevel() {
        return this.dataTracker.get(PIERCE_LEVEL);
    }

    public void applyDamageModifier(float damageModifier) {
        this.setDamage((double)(damageModifier * 2.0f) + this.random.nextTriangular((double)this.getEntityWorld().getDifficulty().getId() * 0.11, 0.57425));
    }

    protected float getDragInWater() {
        return 0.6f;
    }

    public void setNoClip(boolean noClip) {
        this.noClip = noClip;
        this.setProjectileFlag(2, noClip);
    }

    public boolean isNoClip() {
        if (!this.getEntityWorld().isClient()) {
            return this.noClip;
        }
        return (this.dataTracker.get(PROJECTILE_FLAGS) & 2) != 0;
    }

    @Override
    public boolean canHit() {
        return super.canHit() && !this.isInGround();
    }

    @Override
    public @Nullable StackReference getStackReference(int slot) {
        if (slot == 0) {
            return StackReference.of(this::getItemStack, this::setStack);
        }
        return super.getStackReference(slot);
    }

    @Override
    protected boolean deflectsAgainstWorldBorder() {
        return true;
    }

    public static final class PickupPermission
    extends Enum<PickupPermission> {
        public static final /* enum */ PickupPermission DISALLOWED = new PickupPermission();
        public static final /* enum */ PickupPermission ALLOWED = new PickupPermission();
        public static final /* enum */ PickupPermission CREATIVE_ONLY = new PickupPermission();
        public static final Codec<PickupPermission> CODEC;
        private static final /* synthetic */ PickupPermission[] field_7591;

        public static PickupPermission[] values() {
            return (PickupPermission[])field_7591.clone();
        }

        public static PickupPermission valueOf(String string) {
            return Enum.valueOf(PickupPermission.class, string);
        }

        public static PickupPermission fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal > PickupPermission.values().length) {
                ordinal = 0;
            }
            return PickupPermission.values()[ordinal];
        }

        private static /* synthetic */ PickupPermission[] method_36663() {
            return new PickupPermission[]{DISALLOWED, ALLOWED, CREATIVE_ONLY};
        }

        static {
            field_7591 = PickupPermission.method_36663();
            CODEC = Codec.BYTE.xmap(PickupPermission::fromOrdinal, pickupPermission -> (byte)pickupPermission.ordinal());
        }
    }
}

