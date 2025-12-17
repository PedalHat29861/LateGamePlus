/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class OminousItemSpawnerEntity
extends Entity {
    private static final int MIN_SPAWN_ITEM_AFTER_TICKS = 60;
    private static final int MAX_SPAWN_ITEM_AFTER_TICKS = 120;
    private static final String SPAWN_ITEM_AFTER_TICKS_NBT_KEY = "spawn_item_after_ticks";
    private static final String ITEM_NBT_KEY = "item";
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(OminousItemSpawnerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    public static final int field_50128 = 36;
    private long spawnItemAfterTicks;

    public OminousItemSpawnerEntity(EntityType<? extends OminousItemSpawnerEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    public static OminousItemSpawnerEntity create(World world, ItemStack stack) {
        OminousItemSpawnerEntity ominousItemSpawnerEntity = new OminousItemSpawnerEntity((EntityType<? extends OminousItemSpawnerEntity>)EntityType.OMINOUS_ITEM_SPAWNER, world);
        ominousItemSpawnerEntity.spawnItemAfterTicks = world.random.nextBetween(60, 120);
        ominousItemSpawnerEntity.setItem(stack);
        return ominousItemSpawnerEntity;
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.tickServer(serverWorld);
        } else {
            this.tickClient();
        }
    }

    private void tickServer(ServerWorld world) {
        if ((long)this.age == this.spawnItemAfterTicks - 36L) {
            world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, SoundCategory.NEUTRAL);
        }
        if ((long)this.age >= this.spawnItemAfterTicks) {
            this.spawnItem();
            this.kill(world);
        }
    }

    private void tickClient() {
        if (this.getEntityWorld().getTime() % 5L == 0L) {
            this.addParticles();
        }
    }

    private void spawnItem() {
        Entity entity;
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        ItemStack itemStack = this.getItem();
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        if (item instanceof ProjectileItem) {
            ProjectileItem projectileItem = (ProjectileItem)((Object)item);
            entity = this.spawnProjectile(serverWorld, projectileItem, itemStack);
        } else {
            entity = new ItemEntity(serverWorld, this.getX(), this.getY(), this.getZ(), itemStack);
            serverWorld.spawnEntity(entity);
        }
        serverWorld.syncWorldEvent(3021, this.getBlockPos(), 1);
        serverWorld.emitGameEvent(entity, GameEvent.ENTITY_PLACE, this.getEntityPos());
        this.setItem(ItemStack.EMPTY);
    }

    private Entity spawnProjectile(ServerWorld world, ProjectileItem item, ItemStack stack) {
        ProjectileItem.Settings settings = item.getProjectileSettings();
        settings.overrideDispenseEvent().ifPresent(dispenseEvent -> world.syncWorldEvent(dispenseEvent, this.getBlockPos(), 0));
        Direction direction = Direction.DOWN;
        ProjectileEntity projectileEntity = ProjectileEntity.spawnWithVelocity(item.createEntity(world, this.getEntityPos(), stack, direction), world, stack, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ(), settings.power(), settings.uncertainty());
        projectileEntity.setOwner(this);
        return projectileEntity;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setItem(view.read(ITEM_NBT_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        this.spawnItemAfterTicks = view.getLong(SPAWN_ITEM_AFTER_TICKS_NBT_KEY, 0L);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        if (!this.getItem().isEmpty()) {
            view.put(ITEM_NBT_KEY, ItemStack.CODEC, this.getItem());
        }
        view.putLong(SPAWN_ITEM_AFTER_TICKS_NBT_KEY, this.spawnItemAfterTicks);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    public void addParticles() {
        Vec3d vec3d = this.getEntityPos();
        int i = this.random.nextBetween(1, 3);
        for (int j = 0; j < i; ++j) {
            double d = 0.4;
            Vec3d vec3d2 = new Vec3d(this.getX() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getY() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getZ() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()));
            Vec3d vec3d3 = vec3d.relativize(vec3d2);
            this.getEntityWorld().addParticleClient(ParticleTypes.OMINOUS_SPAWNING, vec3d.getX(), vec3d.getY(), vec3d.getZ(), vec3d3.getX(), vec3d3.getY(), vec3d3.getZ());
        }
    }

    public ItemStack getItem() {
        return this.getDataTracker().get(ITEM);
    }

    private void setItem(ItemStack stack) {
        this.getDataTracker().set(ITEM, stack);
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }
}

