/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class EndermanEntity
extends HostileEntity
implements Angerable {
    private static final Identifier ATTACKING_SPEED_MODIFIER_ID = Identifier.ofVanilla("attacking");
    private static final EntityAttributeModifier ATTACKING_SPEED_BOOST = new EntityAttributeModifier(ATTACKING_SPEED_MODIFIER_ID, 0.15f, EntityAttributeModifier.Operation.ADD_VALUE);
    private static final int field_30462 = 400;
    private static final int field_30461 = 600;
    private static final TrackedData<Optional<BlockState>> CARRIED_BLOCK = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE);
    private static final TrackedData<Boolean> ANGRY = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> PROVOKED = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int lastAngrySoundAge = Integer.MIN_VALUE;
    private int ageWhenTargetSet;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    private long angerEndTime;
    private @Nullable LazyEntityReference<LivingEntity> angryAt;

    public EndermanEntity(EntityType<? extends EndermanEntity> entityType, World world) {
        super((EntityType<? extends HostileEntity>)entityType, world);
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0f);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new ChasePlayerGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(7, new WanderAroundFarGoal((PathAwareEntity)this, 1.0, 0.0f));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.goalSelector.add(10, new PlaceBlockGoal(this));
        this.goalSelector.add(11, new PickUpBlockGoal(this));
        this.targetSelector.add(1, new TeleportTowardsPlayerGoal(this, this::shouldAngerAt));
        this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(3, new ActiveTargetGoal<EndermiteEntity>((MobEntity)this, EndermiteEntity.class, true, false));
        this.targetSelector.add(4, new UniversalAngerGoal<EndermanEntity>(this, false));
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return 0.0f;
    }

    public static DefaultAttributeContainer.Builder createEndermanAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.MAX_HEALTH, 40.0).add(EntityAttributes.MOVEMENT_SPEED, 0.3f).add(EntityAttributes.ATTACK_DAMAGE, 7.0).add(EntityAttributes.FOLLOW_RANGE, 64.0).add(EntityAttributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (target == null) {
            this.ageWhenTargetSet = 0;
            this.dataTracker.set(ANGRY, false);
            this.dataTracker.set(PROVOKED, false);
            entityAttributeInstance.removeModifier(ATTACKING_SPEED_MODIFIER_ID);
        } else {
            this.ageWhenTargetSet = this.age;
            this.dataTracker.set(ANGRY, true);
            if (!entityAttributeInstance.hasModifier(ATTACKING_SPEED_MODIFIER_ID)) {
                entityAttributeInstance.addTemporaryModifier(ATTACKING_SPEED_BOOST);
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CARRIED_BLOCK, Optional.empty());
        builder.add(ANGRY, false);
        builder.add(PROVOKED, false);
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerDuration(ANGER_TIME_RANGE.get(this.random));
    }

    @Override
    public void setAngerEndTime(long angerEndTime) {
        this.angerEndTime = angerEndTime;
    }

    @Override
    public long getAngerEndTime() {
        return this.angerEndTime;
    }

    @Override
    public void setAngryAt(@Nullable LazyEntityReference<LivingEntity> angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public @Nullable LazyEntityReference<LivingEntity> getAngryAt() {
        return this.angryAt;
    }

    public void playAngrySound() {
        if (this.age >= this.lastAngrySoundAge + 400) {
            this.lastAngrySoundAge = this.age;
            if (!this.isSilent()) {
                this.getEntityWorld().playSoundClient(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENTITY_ENDERMAN_STARE, this.getSoundCategory(), 2.5f, 1.0f, false);
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (ANGRY.equals(data) && this.isProvoked() && this.getEntityWorld().isClient()) {
            this.playAngrySound();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        BlockState blockState = this.getCarriedBlock();
        if (blockState != null) {
            view.put("carriedBlockState", BlockState.CODEC, blockState);
        }
        this.writeAngerToData(view);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setCarriedBlock(view.read("carriedBlockState", BlockState.CODEC).filter(blockState -> !blockState.isAir()).orElse(null));
        this.readAngerFromData(this.getEntityWorld(), view);
    }

    boolean isPlayerStaring(PlayerEntity player) {
        if (!LivingEntity.NOT_WEARING_GAZE_DISGUISE_PREDICATE.test(player)) {
            return false;
        }
        return this.isEntityLookingAtMe(player, 0.025, true, false, this.getEyeY());
    }

    @Override
    public void tickMovement() {
        if (this.getEntityWorld().isClient()) {
            for (int i = 0; i < 2; ++i) {
                this.getEntityWorld().addParticleClient(ParticleTypes.PORTAL, this.getParticleX(0.5), this.getRandomBodyY() - 0.25, this.getParticleZ(0.5), (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
            }
        }
        this.jumping = false;
        if (!this.getEntityWorld().isClient()) {
            this.tickAngerLogic((ServerWorld)this.getEntityWorld(), true);
        }
        super.tickMovement();
    }

    @Override
    public boolean hurtByWater() {
        return true;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        float f;
        if (world.isDay() && this.age >= this.ageWhenTargetSet + 600 && (f = this.getBrightnessAtEyes()) > 0.5f && world.isSkyVisible(this.getBlockPos()) && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f) {
            this.setTarget(null);
            this.teleportRandomly();
        }
        super.mobTick(world);
    }

    protected boolean teleportRandomly() {
        if (this.getEntityWorld().isClient() || !this.isAlive()) {
            return false;
        }
        double d = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
        double e = this.getY() + (double)(this.random.nextInt(64) - 32);
        double f = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
        return this.teleportTo(d, e, f);
    }

    boolean teleportTo(Entity entity) {
        Vec3d vec3d = new Vec3d(this.getX() - entity.getX(), this.getBodyY(0.5) - entity.getEyeY(), this.getZ() - entity.getZ());
        vec3d = vec3d.normalize();
        double d = 16.0;
        double e = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - vec3d.x * 16.0;
        double f = this.getY() + (double)(this.random.nextInt(16) - 8) - vec3d.y * 16.0;
        double g = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - vec3d.z * 16.0;
        return this.teleportTo(e, f, g);
    }

    private boolean teleportTo(double x, double y, double z) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);
        while (mutable.getY() > this.getEntityWorld().getBottomY() && !this.getEntityWorld().getBlockState(mutable).blocksMovement()) {
            mutable.move(Direction.DOWN);
        }
        BlockState blockState = this.getEntityWorld().getBlockState(mutable);
        boolean bl = blockState.blocksMovement();
        boolean bl2 = blockState.getFluidState().isIn(FluidTags.WATER);
        if (!bl || bl2) {
            return false;
        }
        Vec3d vec3d = this.getEntityPos();
        boolean bl3 = this.teleport(x, y, z, true);
        if (bl3) {
            this.getEntityWorld().emitGameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Emitter.of(this));
            if (!this.isSilent()) {
                this.getEntityWorld().playSound(null, this.lastX, this.lastY, this.lastZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0f, 1.0f);
                this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }
        return bl3;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEvents.ENTITY_ENDERMAN_SCREAM : SoundEvents.ENTITY_ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMAN_DEATH;
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        BlockState blockState = this.getCarriedBlock();
        if (blockState != null) {
            ItemStack itemStack = new ItemStack(Items.DIAMOND_AXE);
            EnchantmentHelper.applyEnchantmentProvider(itemStack, world.getRegistryManager(), EnchantmentProviders.ENDERMAN_LOOT_DROP, world.getLocalDifficulty(this.getBlockPos()), this.getRandom());
            LootWorldContext.Builder builder = new LootWorldContext.Builder((ServerWorld)this.getEntityWorld()).add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.TOOL, itemStack).addOptional(LootContextParameters.THIS_ENTITY, this);
            List<ItemStack> list = blockState.getDroppedStacks(builder);
            for (ItemStack itemStack2 : list) {
                this.dropStack(world, itemStack2);
            }
        }
    }

    public void setCarriedBlock(@Nullable BlockState state) {
        this.dataTracker.set(CARRIED_BLOCK, Optional.ofNullable(state));
    }

    public @Nullable BlockState getCarriedBlock() {
        return this.dataTracker.get(CARRIED_BLOCK).orElse(null);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        PotionEntity potionEntity;
        PotionEntity potionEntity2;
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        Entity entity = source.getSource();
        PotionEntity potionEntity3 = potionEntity2 = entity instanceof PotionEntity ? (potionEntity = (PotionEntity)entity) : null;
        if (source.isIn(DamageTypeTags.IS_PROJECTILE) || potionEntity2 != null) {
            boolean bl = potionEntity2 != null && this.damageFromPotion(world, source, potionEntity2, amount);
            for (int i = 0; i < 64; ++i) {
                if (!this.teleportRandomly()) continue;
                return true;
            }
            return bl;
        }
        boolean bl = super.damage(world, source, amount);
        if (!(source.getAttacker() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
            this.teleportRandomly();
        }
        return bl;
    }

    private boolean damageFromPotion(ServerWorld world, DamageSource source, PotionEntity potion, float amount) {
        ItemStack itemStack = potion.getStack();
        PotionContentsComponent potionContentsComponent = itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (potionContentsComponent.matches(Potions.WATER)) {
            return super.damage(world, source, amount);
        }
        return false;
    }

    public boolean isAngry() {
        return this.dataTracker.get(ANGRY);
    }

    public boolean isProvoked() {
        return this.dataTracker.get(PROVOKED);
    }

    public void setProvoked() {
        this.dataTracker.set(PROVOKED, true);
    }

    @Override
    public boolean cannotDespawn() {
        return super.cannotDespawn() || this.getCarriedBlock() != null;
    }

    static class ChasePlayerGoal
    extends Goal {
        private final EndermanEntity enderman;
        private @Nullable LivingEntity target;

        public ChasePlayerGoal(EndermanEntity enderman) {
            this.enderman = enderman;
            this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            this.target = this.enderman.getTarget();
            LivingEntity livingEntity = this.target;
            if (!(livingEntity instanceof PlayerEntity)) {
                return false;
            }
            PlayerEntity playerEntity = (PlayerEntity)livingEntity;
            double d = this.target.squaredDistanceTo(this.enderman);
            if (d > 256.0) {
                return false;
            }
            return this.enderman.isPlayerStaring(playerEntity);
        }

        @Override
        public void start() {
            this.enderman.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.enderman.getLookControl().lookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }

    static class PlaceBlockGoal
    extends Goal {
        private final EndermanEntity enderman;

        public PlaceBlockGoal(EndermanEntity enderman) {
            this.enderman = enderman;
        }

        @Override
        public boolean canStart() {
            if (this.enderman.getCarriedBlock() == null) {
                return false;
            }
            if (!PlaceBlockGoal.getServerWorld(this.enderman).getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                return false;
            }
            return this.enderman.getRandom().nextInt(PlaceBlockGoal.toGoalTicks(2000)) == 0;
        }

        @Override
        public void tick() {
            Random random = this.enderman.getRandom();
            World world = this.enderman.getEntityWorld();
            int i = MathHelper.floor(this.enderman.getX() - 1.0 + random.nextDouble() * 2.0);
            int j = MathHelper.floor(this.enderman.getY() + random.nextDouble() * 2.0);
            int k = MathHelper.floor(this.enderman.getZ() - 1.0 + random.nextDouble() * 2.0);
            BlockPos blockPos = new BlockPos(i, j, k);
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos blockPos2 = blockPos.down();
            BlockState blockState2 = world.getBlockState(blockPos2);
            BlockState blockState3 = this.enderman.getCarriedBlock();
            if (blockState3 == null) {
                return;
            }
            if (this.canPlaceOn(world, blockPos, blockState3 = Block.postProcessState(blockState3, this.enderman.getEntityWorld(), blockPos), blockState, blockState2, blockPos2)) {
                world.setBlockState(blockPos, blockState3, 3);
                world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Emitter.of(this.enderman, blockState3));
                this.enderman.setCarriedBlock(null);
            }
        }

        private boolean canPlaceOn(World world, BlockPos posAbove, BlockState carriedState, BlockState stateAbove, BlockState state, BlockPos pos) {
            return stateAbove.isAir() && !state.isAir() && !state.isOf(Blocks.BEDROCK) && state.isFullCube(world, pos) && carriedState.canPlaceAt(world, posAbove) && world.getOtherEntities(this.enderman, Box.from(Vec3d.of(posAbove))).isEmpty();
        }
    }

    static class PickUpBlockGoal
    extends Goal {
        private final EndermanEntity enderman;

        public PickUpBlockGoal(EndermanEntity enderman) {
            this.enderman = enderman;
        }

        @Override
        public boolean canStart() {
            if (this.enderman.getCarriedBlock() != null) {
                return false;
            }
            if (!PickUpBlockGoal.getServerWorld(this.enderman).getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                return false;
            }
            return this.enderman.getRandom().nextInt(PickUpBlockGoal.toGoalTicks(20)) == 0;
        }

        @Override
        public void tick() {
            Random random = this.enderman.getRandom();
            World world = this.enderman.getEntityWorld();
            int i = MathHelper.floor(this.enderman.getX() - 2.0 + random.nextDouble() * 4.0);
            int j = MathHelper.floor(this.enderman.getY() + random.nextDouble() * 3.0);
            int k = MathHelper.floor(this.enderman.getZ() - 2.0 + random.nextDouble() * 4.0);
            BlockPos blockPos = new BlockPos(i, j, k);
            BlockState blockState = world.getBlockState(blockPos);
            Vec3d vec3d = new Vec3d((double)this.enderman.getBlockX() + 0.5, (double)j + 0.5, (double)this.enderman.getBlockZ() + 0.5);
            Vec3d vec3d2 = new Vec3d((double)i + 0.5, (double)j + 0.5, (double)k + 0.5);
            BlockHitResult blockHitResult = world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this.enderman));
            boolean bl = blockHitResult.getBlockPos().equals(blockPos);
            if (blockState.isIn(BlockTags.ENDERMAN_HOLDABLE) && bl) {
                world.removeBlock(blockPos, false);
                world.emitGameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Emitter.of(this.enderman, blockState));
                this.enderman.setCarriedBlock(blockState.getBlock().getDefaultState());
            }
        }
    }

    static class TeleportTowardsPlayerGoal
    extends ActiveTargetGoal<PlayerEntity> {
        private final EndermanEntity enderman;
        private @Nullable PlayerEntity targetPlayer;
        private int lookAtPlayerWarmup;
        private int ticksSinceUnseenTeleport;
        private final TargetPredicate staringPlayerPredicate;
        private final TargetPredicate validTargetPredicate = TargetPredicate.createAttackable().ignoreVisibility();
        private final TargetPredicate.EntityPredicate angerPredicate;

        public TeleportTowardsPlayerGoal(EndermanEntity enderman, @Nullable TargetPredicate.EntityPredicate targetPredicate) {
            super(enderman, PlayerEntity.class, 10, false, false, targetPredicate);
            this.enderman = enderman;
            this.angerPredicate = (playerEntity, world) -> (enderman.isPlayerStaring((PlayerEntity)playerEntity) || enderman.shouldAngerAt(playerEntity, world)) && !enderman.hasPassengerDeep(playerEntity);
            this.staringPlayerPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(this.angerPredicate);
        }

        @Override
        public boolean canStart() {
            this.targetPlayer = TeleportTowardsPlayerGoal.getServerWorld(this.enderman).getClosestPlayer(this.staringPlayerPredicate.setBaseMaxDistance(this.getFollowRange()), this.enderman);
            return this.targetPlayer != null;
        }

        @Override
        public void start() {
            this.lookAtPlayerWarmup = this.getTickCount(5);
            this.ticksSinceUnseenTeleport = 0;
            this.enderman.setProvoked();
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            super.stop();
        }

        @Override
        public boolean shouldContinue() {
            if (this.targetPlayer != null) {
                if (!this.angerPredicate.test(this.targetPlayer, TeleportTowardsPlayerGoal.getServerWorld(this.enderman))) {
                    return false;
                }
                this.enderman.lookAtEntity(this.targetPlayer, 10.0f, 10.0f);
                return true;
            }
            if (this.targetEntity != null) {
                if (this.enderman.hasPassengerDeep(this.targetEntity)) {
                    return false;
                }
                if (this.validTargetPredicate.test(TeleportTowardsPlayerGoal.getServerWorld(this.enderman), this.enderman, this.targetEntity)) {
                    return true;
                }
            }
            return super.shouldContinue();
        }

        @Override
        public void tick() {
            if (this.enderman.getTarget() == null) {
                super.setTargetEntity(null);
            }
            if (this.targetPlayer != null) {
                if (--this.lookAtPlayerWarmup <= 0) {
                    this.targetEntity = this.targetPlayer;
                    this.targetPlayer = null;
                    super.start();
                }
            } else {
                if (this.targetEntity != null && !this.enderman.hasVehicle()) {
                    if (this.enderman.isPlayerStaring((PlayerEntity)this.targetEntity)) {
                        if (this.targetEntity.squaredDistanceTo(this.enderman) < 16.0) {
                            this.enderman.teleportRandomly();
                        }
                        this.ticksSinceUnseenTeleport = 0;
                    } else if (this.targetEntity.squaredDistanceTo(this.enderman) > 256.0 && this.ticksSinceUnseenTeleport++ >= this.getTickCount(30) && this.enderman.teleportTo(this.targetEntity)) {
                        this.ticksSinceUnseenTeleport = 0;
                    }
                }
                super.tick();
            }
        }
    }
}

