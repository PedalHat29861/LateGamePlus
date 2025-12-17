/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieNautilusEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jspecify.annotations.Nullable;

public class DrownedEntity
extends ZombieEntity
implements RangedAttackMob {
    public static final float field_30460 = 0.03f;
    private static final float field_63376 = 0.5f;
    boolean targetingUnderwater;

    public DrownedEntity(EntityType<? extends DrownedEntity> entityType, World world) {
        super((EntityType<? extends ZombieEntity>)entityType, world);
        this.moveControl = new DrownedMoveControl(this);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
    }

    public static DefaultAttributeContainer.Builder createDrownedAttributes() {
        return ZombieEntity.createZombieAttributes().add(EntityAttributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new AmphibiousSwimNavigation(this, world);
    }

    @Override
    protected void initCustomGoals() {
        this.goalSelector.add(1, new WanderAroundOnSurfaceGoal(this, 1.0));
        this.goalSelector.add(2, new TridentAttackGoal(this, 1.0, 40, 10.0f));
        this.goalSelector.add(2, new DrownedAttackGoal(this, 1.0, false));
        this.goalSelector.add(5, new LeaveWaterGoal(this, 1.0));
        this.goalSelector.add(6, new TargetAboveWaterGoal(this, 1.0, this.getEntityWorld().getSeaLevel()));
        this.goalSelector.add(7, new WanderAroundGoal(this, 1.0));
        this.targetSelector.add(1, new RevengeGoal(this, DrownedEntity.class).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, (target, world) -> this.canDrownedAttackTarget(target)));
        this.targetSelector.add(3, new ActiveTargetGoal<MerchantEntity>((MobEntity)this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<IronGolemEntity>((MobEntity)this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<AxolotlEntity>((MobEntity)this, AxolotlEntity.class, true, false));
        this.targetSelector.add(5, new ActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        ZombieNautilusEntity zombieNautilusEntity;
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        if (this.getEquippedStack(EquipmentSlot.OFFHAND).isEmpty() && world.getRandom().nextFloat() < 0.03f) {
            this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.NAUTILUS_SHELL));
            this.setDropGuaranteed(EquipmentSlot.OFFHAND);
        }
        if ((spawnReason == SpawnReason.NATURAL || spawnReason == SpawnReason.STRUCTURE) && this.getMainHandStack().isOf(Items.TRIDENT) && world.getRandom().nextFloat() < 0.5f && !this.isBaby() && !world.getBiome(this.getBlockPos()).isIn(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS) && (zombieNautilusEntity = EntityType.ZOMBIE_NAUTILUS.create(this.getEntityWorld(), SpawnReason.JOCKEY)) != null) {
            if (spawnReason == SpawnReason.STRUCTURE) {
                zombieNautilusEntity.setPersistent();
            }
            zombieNautilusEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0f);
            zombieNautilusEntity.initialize(world, difficulty, spawnReason, null);
            this.startRiding(zombieNautilusEntity, false, false);
            world.spawnEntity(zombieNautilusEntity);
        }
        return entityData;
    }

    public static boolean canSpawn(EntityType<DrownedEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        boolean bl;
        if (!world.getFluidState(pos.down()).isIn(FluidTags.WATER) && !SpawnReason.isAnySpawner(spawnReason)) {
            return false;
        }
        RegistryEntry<Biome> registryEntry = world.getBiome(pos);
        boolean bl2 = bl = !(world.getDifficulty() == Difficulty.PEACEFUL || !SpawnReason.isTrialSpawner(spawnReason) && !DrownedEntity.isSpawnDark(world, pos, random) || !SpawnReason.isAnySpawner(spawnReason) && !world.getFluidState(pos).isIn(FluidTags.WATER));
        if (bl && (SpawnReason.isAnySpawner(spawnReason) || spawnReason == SpawnReason.REINFORCEMENT)) {
            return true;
        }
        if (registryEntry.isIn(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS)) {
            return random.nextInt(15) == 0 && bl;
        }
        return random.nextInt(40) == 0 && DrownedEntity.isValidSpawnDepth(world, pos) && bl;
    }

    private static boolean isValidSpawnDepth(WorldAccess world, BlockPos pos) {
        return pos.getY() < world.getSeaLevel() - 5;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isTouchingWater()) {
            return SoundEvents.ENTITY_DROWNED_AMBIENT_WATER;
        }
        return SoundEvents.ENTITY_DROWNED_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isTouchingWater()) {
            return SoundEvents.ENTITY_DROWNED_HURT_WATER;
        }
        return SoundEvents.ENTITY_DROWNED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isTouchingWater()) {
            return SoundEvents.ENTITY_DROWNED_DEATH_WATER;
        }
        return SoundEvents.ENTITY_DROWNED_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_DROWNED_STEP;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_DROWNED_SWIM;
    }

    @Override
    protected boolean canSpawnAsReinforcementInFluid() {
        return true;
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if ((double)random.nextFloat() > 0.9) {
            int i = random.nextInt(16);
            if (i < 10) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            } else {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
            }
        }
    }

    @Override
    protected boolean prefersNewEquipment(ItemStack newStack, ItemStack currentStack, EquipmentSlot slot) {
        if (currentStack.isOf(Items.NAUTILUS_SHELL)) {
            return false;
        }
        return super.prefersNewEquipment(newStack, currentStack, slot);
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    public boolean canDrownedAttackTarget(@Nullable LivingEntity target) {
        if (target != null) {
            return !this.getEntityWorld().isDay() || target.isTouchingWater();
        }
        return false;
    }

    @Override
    public boolean isPushedByFluids() {
        return !this.isSwimming();
    }

    boolean isTargetingUnderwater() {
        if (this.targetingUnderwater) {
            return true;
        }
        LivingEntity livingEntity = this.getTarget();
        return livingEntity != null && livingEntity.isTouchingWater();
    }

    @Override
    protected void travelInWater(Vec3d movementInput, double gravity, boolean falling, double y) {
        if (this.isSubmergedInWater() && this.isTargetingUnderwater()) {
            this.updateVelocity(0.01f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9));
        } else {
            super.travelInWater(movementInput, gravity, falling, y);
        }
    }

    @Override
    public void updateSwimming() {
        if (!this.getEntityWorld().isClient()) {
            this.setSwimming(this.canActVoluntarily() && this.isSubmergedInWater() && this.isTargetingUnderwater());
        }
    }

    @Override
    public boolean isInSwimmingPose() {
        return this.isSwimming() && !this.hasVehicle();
    }

    protected boolean hasFinishedCurrentPath() {
        double d;
        BlockPos blockPos;
        Path path = this.getNavigation().getCurrentPath();
        return path != null && (blockPos = path.getTarget()) != null && (d = this.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ())) < 4.0;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getMainHandStack();
        ItemStack itemStack2 = itemStack.isOf(Items.TRIDENT) ? itemStack : new ItemStack(Items.TRIDENT);
        TridentEntity tridentEntity = new TridentEntity(this.getEntityWorld(), this, itemStack2);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - tridentEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            ProjectileEntity.spawnWithVelocity(tridentEntity, serverWorld, itemStack2, d, e + g * (double)0.2f, f, 1.6f, 14 - this.getEntityWorld().getDifficulty().getId() * 4);
        }
        this.playSound(SoundEvents.ENTITY_DROWNED_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    @Override
    public TagKey<Item> getPreferredWeapons() {
        return ItemTags.DROWNED_PREFERRED_WEAPONS;
    }

    public void setTargetingUnderwater(boolean targetingUnderwater) {
        this.targetingUnderwater = targetingUnderwater;
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        Entity entity = this.getControllingVehicle();
        if (entity instanceof PathAwareEntity) {
            PathAwareEntity pathAwareEntity = (PathAwareEntity)entity;
            this.bodyYaw = pathAwareEntity.bodyYaw;
        }
    }

    @Override
    public boolean canGather(ServerWorld world, ItemStack stack) {
        if (stack.isIn(ItemTags.SPEARS)) {
            return false;
        }
        return super.canGather(world, stack);
    }

    static class DrownedMoveControl
    extends MoveControl {
        private final DrownedEntity drowned;

        public DrownedMoveControl(DrownedEntity drowned) {
            super(drowned);
            this.drowned = drowned;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.drowned.getTarget();
            if (this.drowned.isTargetingUnderwater() && this.drowned.isTouchingWater()) {
                if (livingEntity != null && livingEntity.getY() > this.drowned.getY() || this.drowned.targetingUnderwater) {
                    this.drowned.setVelocity(this.drowned.getVelocity().add(0.0, 0.002, 0.0));
                }
                if (this.state != MoveControl.State.MOVE_TO || this.drowned.getNavigation().isIdle()) {
                    this.drowned.setMovementSpeed(0.0f);
                    return;
                }
                double d = this.targetX - this.drowned.getX();
                double e = this.targetY - this.drowned.getY();
                double f = this.targetZ - this.drowned.getZ();
                double g = Math.sqrt(d * d + e * e + f * f);
                e /= g;
                float h = (float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f;
                this.drowned.setYaw(this.wrapDegrees(this.drowned.getYaw(), h, 90.0f));
                this.drowned.bodyYaw = this.drowned.getYaw();
                float i = (float)(this.speed * this.drowned.getAttributeValue(EntityAttributes.MOVEMENT_SPEED));
                float j = MathHelper.lerp(0.125f, this.drowned.getMovementSpeed(), i);
                this.drowned.setMovementSpeed(j);
                this.drowned.setVelocity(this.drowned.getVelocity().add((double)j * d * 0.005, (double)j * e * 0.1, (double)j * f * 0.005));
            } else {
                if (!this.drowned.isOnGround()) {
                    this.drowned.setVelocity(this.drowned.getVelocity().add(0.0, -0.008, 0.0));
                }
                super.tick();
            }
        }
    }

    static class WanderAroundOnSurfaceGoal
    extends Goal {
        private final PathAwareEntity mob;
        private double x;
        private double y;
        private double z;
        private final double speed;
        private final World world;

        public WanderAroundOnSurfaceGoal(PathAwareEntity mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.world = mob.getEntityWorld();
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (!this.world.isDay()) {
                return false;
            }
            if (this.mob.isTouchingWater()) {
                return false;
            }
            Vec3d vec3d = this.getWanderTarget();
            if (vec3d == null) {
                return false;
            }
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            return true;
        }

        @Override
        public boolean shouldContinue() {
            return !this.mob.getNavigation().isIdle();
        }

        @Override
        public void start() {
            this.mob.getNavigation().startMovingTo(this.x, this.y, this.z, this.speed);
        }

        private @Nullable Vec3d getWanderTarget() {
            Random random = this.mob.getRandom();
            BlockPos blockPos = this.mob.getBlockPos();
            for (int i = 0; i < 10; ++i) {
                BlockPos blockPos2 = blockPos.add(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);
                if (!this.world.getBlockState(blockPos2).isOf(Blocks.WATER)) continue;
                return Vec3d.ofBottomCenter(blockPos2);
            }
            return null;
        }
    }

    static class TridentAttackGoal
    extends ProjectileAttackGoal {
        private final DrownedEntity drowned;

        public TridentAttackGoal(RangedAttackMob rangedAttackMob, double d, int i, float f) {
            super(rangedAttackMob, d, i, f);
            this.drowned = (DrownedEntity)rangedAttackMob;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && this.drowned.getMainHandStack().isOf(Items.TRIDENT);
        }

        @Override
        public void start() {
            super.start();
            this.drowned.setAttacking(true);
            this.drowned.setCurrentHand(Hand.MAIN_HAND);
        }

        @Override
        public void stop() {
            super.stop();
            this.drowned.clearActiveItem();
            this.drowned.setAttacking(false);
        }
    }

    static class DrownedAttackGoal
    extends ZombieAttackGoal {
        private final DrownedEntity drowned;

        public DrownedAttackGoal(DrownedEntity drowned, double speed, boolean pauseWhenMobIdle) {
            super(drowned, speed, pauseWhenMobIdle);
            this.drowned = drowned;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && this.drowned.canDrownedAttackTarget(this.drowned.getTarget());
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && this.drowned.canDrownedAttackTarget(this.drowned.getTarget());
        }
    }

    static class LeaveWaterGoal
    extends MoveToTargetPosGoal {
        private final DrownedEntity drowned;

        public LeaveWaterGoal(DrownedEntity drowned, double speed) {
            super(drowned, speed, 8, 2);
            this.drowned = drowned;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !this.drowned.getEntityWorld().isDay() && this.drowned.isTouchingWater() && this.drowned.getY() >= (double)(this.drowned.getEntityWorld().getSeaLevel() - 3);
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue();
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            BlockPos blockPos = pos.up();
            if (!world.isAir(blockPos) || !world.isAir(blockPos.up())) {
                return false;
            }
            return world.getBlockState(pos).hasSolidTopSurface(world, pos, this.drowned);
        }

        @Override
        public void start() {
            this.drowned.setTargetingUnderwater(false);
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
        }
    }

    static class TargetAboveWaterGoal
    extends Goal {
        private final DrownedEntity drowned;
        private final double speed;
        private final int minY;
        private boolean foundTarget;

        public TargetAboveWaterGoal(DrownedEntity drowned, double speed, int minY) {
            this.drowned = drowned;
            this.speed = speed;
            this.minY = minY;
        }

        @Override
        public boolean canStart() {
            return !this.drowned.getEntityWorld().isDay() && this.drowned.isTouchingWater() && this.drowned.getY() < (double)(this.minY - 2);
        }

        @Override
        public boolean shouldContinue() {
            return this.canStart() && !this.foundTarget;
        }

        @Override
        public void tick() {
            if (this.drowned.getY() < (double)(this.minY - 1) && (this.drowned.getNavigation().isIdle() || this.drowned.hasFinishedCurrentPath())) {
                Vec3d vec3d = NoPenaltyTargeting.findTo(this.drowned, 4, 8, new Vec3d(this.drowned.getX(), this.minY - 1, this.drowned.getZ()), 1.5707963705062866);
                if (vec3d == null) {
                    this.foundTarget = true;
                    return;
                }
                this.drowned.getNavigation().startMovingTo(vec3d.x, vec3d.y, vec3d.z, this.speed);
            }
        }

        @Override
        public void start() {
            this.drowned.setTargetingUnderwater(true);
            this.foundTarget = false;
        }

        @Override
        public void stop() {
            this.drowned.setTargetingUnderwater(false);
        }
    }
}

