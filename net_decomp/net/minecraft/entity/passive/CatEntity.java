/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Variants;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.GoToBedAndSleepGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.UntamedActiveTargetGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.CatVariants;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.jspecify.annotations.Nullable;

public class CatEntity
extends TameableEntity {
    public static final double CROUCHING_SPEED = 0.6;
    public static final double NORMAL_SPEED = 0.8;
    public static final double SPRINTING_SPEED = 1.33;
    private static final TrackedData<RegistryEntry<CatVariant>> CAT_VARIANT = DataTracker.registerData(CatEntity.class, TrackedDataHandlerRegistry.CAT_VARIANT);
    private static final TrackedData<Boolean> IN_SLEEPING_POSE = DataTracker.registerData(CatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HEAD_DOWN = DataTracker.registerData(CatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> COLLAR_COLOR = DataTracker.registerData(CatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final RegistryKey<CatVariant> DEFAULT_VARIANT = CatVariants.BLACK;
    private static final DyeColor DEFAULT_COLLAR_COLOR = DyeColor.RED;
    private @Nullable CatFleeGoal<PlayerEntity> fleeGoal;
    private @Nullable net.minecraft.entity.ai.goal.TemptGoal temptGoal;
    private float sleepAnimation;
    private float lastSleepAnimation;
    private float tailCurlAnimation;
    private float lastTailCurlAnimation;
    private boolean nearSleepingPlayer;
    private float headDownAnimation;
    private float lastHeadDownAnimation;

    public CatEntity(EntityType<? extends CatEntity> entityType, World world) {
        super((EntityType<? extends TameableEntity>)entityType, world);
        this.onTamedChanged();
    }

    @Override
    protected void initGoals() {
        this.temptGoal = new TemptGoal(this, 0.6, stack -> stack.isIn(ItemTags.CAT_FOOD), true);
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new TameableEntity.TameableEscapeDangerGoal(1.5));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new SleepWithOwnerGoal(this));
        this.goalSelector.add(4, this.temptGoal);
        this.goalSelector.add(5, new GoToBedAndSleepGoal(this, 1.1, 8));
        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0f, 5.0f));
        this.goalSelector.add(7, new CatSitOnBlockGoal(this, 0.8));
        this.goalSelector.add(8, new PounceAtTargetGoal(this, 0.3f));
        this.goalSelector.add(9, new AttackGoal(this));
        this.goalSelector.add(10, new AnimalMateGoal(this, 0.8));
        this.goalSelector.add(11, new WanderAroundFarGoal((PathAwareEntity)this, 0.8, 1.0000001E-5f));
        this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        this.targetSelector.add(1, new UntamedActiveTargetGoal<RabbitEntity>(this, RabbitEntity.class, false, null));
        this.targetSelector.add(1, new UntamedActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public RegistryEntry<CatVariant> getVariant() {
        return this.dataTracker.get(CAT_VARIANT);
    }

    private void setVariant(RegistryEntry<CatVariant> variant) {
        this.dataTracker.set(CAT_VARIANT, variant);
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.CAT_VARIANT) {
            return CatEntity.castComponentValue(type, this.getVariant());
        }
        if (type == DataComponentTypes.CAT_COLLAR) {
            return CatEntity.castComponentValue(type, this.getCollarColor());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.CAT_VARIANT);
        this.copyComponentFrom(from, DataComponentTypes.CAT_COLLAR);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.CAT_VARIANT) {
            this.setVariant(CatEntity.castComponentValue(DataComponentTypes.CAT_VARIANT, value));
            return true;
        }
        if (type == DataComponentTypes.CAT_COLLAR) {
            this.setCollarColor(CatEntity.castComponentValue(DataComponentTypes.CAT_COLLAR, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    public void setInSleepingPose(boolean sleeping) {
        this.dataTracker.set(IN_SLEEPING_POSE, sleeping);
    }

    public boolean isInSleepingPose() {
        return this.dataTracker.get(IN_SLEEPING_POSE);
    }

    void setHeadDown(boolean headDown) {
        this.dataTracker.set(HEAD_DOWN, headDown);
    }

    boolean isHeadDown() {
        return this.dataTracker.get(HEAD_DOWN);
    }

    public DyeColor getCollarColor() {
        return DyeColor.byIndex(this.dataTracker.get(COLLAR_COLOR));
    }

    private void setCollarColor(DyeColor color) {
        this.dataTracker.set(COLLAR_COLOR, color.getIndex());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CAT_VARIANT, Variants.getOrDefaultOrThrow(this.getRegistryManager(), DEFAULT_VARIANT));
        builder.add(IN_SLEEPING_POSE, false);
        builder.add(HEAD_DOWN, false);
        builder.add(COLLAR_COLOR, DEFAULT_COLLAR_COLOR.getIndex());
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        Variants.writeData(view, this.getVariant());
        view.put("CollarColor", DyeColor.INDEX_CODEC, this.getCollarColor());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        Variants.fromData(view, RegistryKeys.CAT_VARIANT).ifPresent(this::setVariant);
        this.setCollarColor(view.read("CollarColor", DyeColor.INDEX_CODEC).orElse(DEFAULT_COLLAR_COLOR));
    }

    @Override
    public void mobTick(ServerWorld world) {
        if (this.getMoveControl().isMoving()) {
            double d = this.getMoveControl().getSpeed();
            if (d == 0.6) {
                this.setPose(EntityPose.CROUCHING);
                this.setSprinting(false);
            } else if (d == 1.33) {
                this.setPose(EntityPose.STANDING);
                this.setSprinting(true);
            } else {
                this.setPose(EntityPose.STANDING);
                this.setSprinting(false);
            }
        } else {
            this.setPose(EntityPose.STANDING);
            this.setSprinting(false);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.isTamed()) {
            if (this.isInLove()) {
                return SoundEvents.ENTITY_CAT_PURR;
            }
            if (this.random.nextInt(4) == 0) {
                return SoundEvents.ENTITY_CAT_PURREOW;
            }
            return SoundEvents.ENTITY_CAT_AMBIENT;
        }
        return SoundEvents.ENTITY_CAT_STRAY_AMBIENT;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 120;
    }

    public void hiss() {
        this.playSound(SoundEvents.ENTITY_CAT_HISS);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CAT_DEATH;
    }

    public static DefaultAttributeContainer.Builder createCatAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 10.0).add(EntityAttributes.MOVEMENT_SPEED, 0.3f).add(EntityAttributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void playEatSound() {
        this.playSound(SoundEvents.ENTITY_CAT_EAT, 1.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.temptGoal != null && this.temptGoal.isActive() && !this.isTamed() && this.age % 100 == 0) {
            this.playSound(SoundEvents.ENTITY_CAT_BEG_FOR_FOOD, 1.0f, 1.0f);
        }
        this.updateAnimations();
    }

    private void updateAnimations() {
        if ((this.isInSleepingPose() || this.isHeadDown()) && this.age % 5 == 0) {
            this.playSound(SoundEvents.ENTITY_CAT_PURR, 0.6f + 0.4f * (this.random.nextFloat() - this.random.nextFloat()), 1.0f);
        }
        this.updateSleepAnimation();
        this.updateHeadDownAnimation();
        this.nearSleepingPlayer = false;
        if (this.isInSleepingPose()) {
            BlockPos blockPos = this.getBlockPos();
            List<PlayerEntity> list = this.getEntityWorld().getNonSpectatingEntities(PlayerEntity.class, new Box(blockPos).expand(2.0, 2.0, 2.0));
            for (PlayerEntity playerEntity : list) {
                if (!playerEntity.isSleeping()) continue;
                this.nearSleepingPlayer = true;
                break;
            }
        }
    }

    public boolean isNearSleepingPlayer() {
        return this.nearSleepingPlayer;
    }

    private void updateSleepAnimation() {
        this.lastSleepAnimation = this.sleepAnimation;
        this.lastTailCurlAnimation = this.tailCurlAnimation;
        if (this.isInSleepingPose()) {
            this.sleepAnimation = Math.min(1.0f, this.sleepAnimation + 0.15f);
            this.tailCurlAnimation = Math.min(1.0f, this.tailCurlAnimation + 0.08f);
        } else {
            this.sleepAnimation = Math.max(0.0f, this.sleepAnimation - 0.22f);
            this.tailCurlAnimation = Math.max(0.0f, this.tailCurlAnimation - 0.13f);
        }
    }

    private void updateHeadDownAnimation() {
        this.lastHeadDownAnimation = this.headDownAnimation;
        this.headDownAnimation = this.isHeadDown() ? Math.min(1.0f, this.headDownAnimation + 0.1f) : Math.max(0.0f, this.headDownAnimation - 0.13f);
    }

    public float getSleepAnimationProgress(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastSleepAnimation, this.sleepAnimation);
    }

    public float getTailCurlAnimationProgress(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastTailCurlAnimation, this.tailCurlAnimation);
    }

    public float getHeadDownAnimationProgress(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastHeadDownAnimation, this.headDownAnimation);
    }

    @Override
    public @Nullable CatEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        CatEntity catEntity = EntityType.CAT.create(serverWorld, SpawnReason.BREEDING);
        if (catEntity != null && passiveEntity instanceof CatEntity) {
            CatEntity catEntity2 = (CatEntity)passiveEntity;
            if (this.random.nextBoolean()) {
                catEntity.setVariant(this.getVariant());
            } else {
                catEntity.setVariant(catEntity2.getVariant());
            }
            if (this.isTamed()) {
                catEntity.setOwner(this.getOwnerReference());
                catEntity.setTamed(true, true);
                DyeColor dyeColor = this.getCollarColor();
                DyeColor dyeColor2 = catEntity2.getCollarColor();
                catEntity.setCollarColor(DyeColor.mixColors(serverWorld, dyeColor, dyeColor2));
            }
        }
        return catEntity;
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (!this.isTamed()) {
            return false;
        }
        if (!(other instanceof CatEntity)) {
            return false;
        }
        CatEntity catEntity = (CatEntity)other;
        return catEntity.isTamed() && super.canBreedWith(other);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        Variants.select(SpawnContext.of(world, this.getBlockPos()), RegistryKeys.CAT_VARIANT).ifPresent(this::setVariant);
        return entityData;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ActionResult actionResult;
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (this.isTamed()) {
            if (this.isOwner(player)) {
                ActionResult actionResult2;
                if (item instanceof DyeItem) {
                    DyeItem dyeItem = (DyeItem)item;
                    DyeColor dyeColor = dyeItem.getColor();
                    if (dyeColor != this.getCollarColor()) {
                        if (!this.getEntityWorld().isClient()) {
                            this.setCollarColor(dyeColor);
                            itemStack.decrementUnlessCreative(1, player);
                            this.setPersistent();
                        }
                        return ActionResult.SUCCESS;
                    }
                } else if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                    if (!this.getEntityWorld().isClient()) {
                        this.eat(player, hand, itemStack);
                        FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
                        this.heal(foodComponent != null ? (float)foodComponent.nutrition() : 1.0f);
                        this.playEatSound();
                    }
                    return ActionResult.SUCCESS;
                }
                if (!(actionResult2 = super.interactMob(player, hand)).isAccepted()) {
                    this.setSitting(!this.isSitting());
                    return ActionResult.SUCCESS;
                }
                return actionResult2;
            }
        } else if (this.isBreedingItem(itemStack)) {
            if (!this.getEntityWorld().isClient()) {
                this.eat(player, hand, itemStack);
                this.tryTame(player);
                this.setPersistent();
                this.playEatSound();
            }
            return ActionResult.SUCCESS;
        }
        if ((actionResult = super.interactMob(player, hand)).isAccepted()) {
            this.setPersistent();
        }
        return actionResult;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.CAT_FOOD);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.isTamed() && this.age > 2400;
    }

    @Override
    public void setTamed(boolean tamed, boolean updateAttributes) {
        super.setTamed(tamed, updateAttributes);
        this.onTamedChanged();
    }

    protected void onTamedChanged() {
        if (this.fleeGoal == null) {
            this.fleeGoal = new CatFleeGoal<PlayerEntity>(this, PlayerEntity.class, 16.0f, 0.8, 1.33);
        }
        this.goalSelector.remove(this.fleeGoal);
        if (!this.isTamed()) {
            this.goalSelector.add(4, this.fleeGoal);
        }
    }

    private void tryTame(PlayerEntity player) {
        if (this.random.nextInt(3) == 0) {
            this.setTamedBy(player);
            this.setSitting(true);
            this.getEntityWorld().sendEntityStatus(this, (byte)7);
        } else {
            this.getEntityWorld().sendEntityStatus(this, (byte)6);
        }
    }

    @Override
    public boolean bypassesSteppingEffects() {
        return this.isInSneakingPose() || super.bypassesSteppingEffects();
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    static class TemptGoal
    extends net.minecraft.entity.ai.goal.TemptGoal {
        private @Nullable PlayerEntity player;
        private final CatEntity cat;

        public TemptGoal(CatEntity cat, double speed, Predicate<ItemStack> foodPredicate, boolean canBeScared) {
            super(cat, speed, foodPredicate, canBeScared);
            this.cat = cat;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.player == null && this.mob.getRandom().nextInt(this.getTickCount(600)) == 0) {
                this.player = this.closestPlayer;
            } else if (this.mob.getRandom().nextInt(this.getTickCount(500)) == 0) {
                this.player = null;
            }
        }

        @Override
        protected boolean canBeScared() {
            if (this.player != null && this.player.equals(this.closestPlayer)) {
                return false;
            }
            return super.canBeScared();
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !this.cat.isTamed();
        }
    }

    static class SleepWithOwnerGoal
    extends Goal {
        private final CatEntity cat;
        private @Nullable PlayerEntity owner;
        private @Nullable BlockPos bedPos;
        private int ticksOnBed;

        public SleepWithOwnerGoal(CatEntity cat) {
            this.cat = cat;
        }

        @Override
        public boolean canStart() {
            if (!this.cat.isTamed()) {
                return false;
            }
            if (this.cat.isSitting()) {
                return false;
            }
            LivingEntity livingEntity = this.cat.getOwner();
            if (livingEntity instanceof PlayerEntity) {
                PlayerEntity playerEntity;
                this.owner = playerEntity = (PlayerEntity)livingEntity;
                if (!livingEntity.isSleeping()) {
                    return false;
                }
                if (this.cat.squaredDistanceTo(this.owner) > 100.0) {
                    return false;
                }
                BlockPos blockPos = this.owner.getBlockPos();
                BlockState blockState = this.cat.getEntityWorld().getBlockState(blockPos);
                if (blockState.isIn(BlockTags.BEDS)) {
                    this.bedPos = blockState.getOrEmpty(BedBlock.FACING).map(direction -> blockPos.offset(direction.getOpposite())).orElseGet(() -> new BlockPos(blockPos));
                    return !this.cannotSleep();
                }
            }
            return false;
        }

        private boolean cannotSleep() {
            List<CatEntity> list = this.cat.getEntityWorld().getNonSpectatingEntities(CatEntity.class, new Box(this.bedPos).expand(2.0));
            for (CatEntity catEntity : list) {
                if (catEntity == this.cat || !catEntity.isInSleepingPose() && !catEntity.isHeadDown()) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinue() {
            return this.cat.isTamed() && !this.cat.isSitting() && this.owner != null && this.owner.isSleeping() && this.bedPos != null && !this.cannotSleep();
        }

        @Override
        public void start() {
            if (this.bedPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().startMovingTo(this.bedPos.getX(), this.bedPos.getY(), this.bedPos.getZ(), 1.1f);
            }
        }

        @Override
        public void stop() {
            this.cat.setInSleepingPose(false);
            if (this.owner.getSleepTimer() >= 100 && this.cat.getEntityWorld().getRandom().nextFloat() < this.cat.getEntityWorld().getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.CAT_WAKING_UP_GIFT_CHANCE_GAMEPLAY, this.cat.getEntityPos()).floatValue()) {
                this.dropMorningGifts();
            }
            this.ticksOnBed = 0;
            this.cat.setHeadDown(false);
            this.cat.getNavigation().stop();
        }

        private void dropMorningGifts() {
            Random random = this.cat.getRandom();
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            mutable.set(this.cat.isLeashed() ? this.cat.getLeashHolder().getBlockPos() : this.cat.getBlockPos());
            this.cat.teleport(mutable.getX() + random.nextInt(11) - 5, mutable.getY() + random.nextInt(5) - 2, mutable.getZ() + random.nextInt(11) - 5, false);
            mutable.set(this.cat.getBlockPos());
            this.cat.forEachGiftedItem(SleepWithOwnerGoal.getServerWorld(this.cat), LootTables.CAT_MORNING_GIFT_GAMEPLAY, (world, stack) -> world.spawnEntity(new ItemEntity((World)world, (double)mutable.getX() - (double)MathHelper.sin(this.cat.bodyYaw * ((float)Math.PI / 180)), mutable.getY(), (double)mutable.getZ() + (double)MathHelper.cos(this.cat.bodyYaw * ((float)Math.PI / 180)), (ItemStack)stack)));
        }

        @Override
        public void tick() {
            if (this.owner != null && this.bedPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().startMovingTo(this.bedPos.getX(), this.bedPos.getY(), this.bedPos.getZ(), 1.1f);
                if (this.cat.squaredDistanceTo(this.owner) < 2.5) {
                    ++this.ticksOnBed;
                    if (this.ticksOnBed > this.getTickCount(16)) {
                        this.cat.setInSleepingPose(true);
                        this.cat.setHeadDown(false);
                    } else {
                        this.cat.lookAtEntity(this.owner, 45.0f, 45.0f);
                        this.cat.setHeadDown(true);
                    }
                } else {
                    this.cat.setInSleepingPose(false);
                }
            }
        }
    }

    static class CatFleeGoal<T extends LivingEntity>
    extends FleeEntityGoal<T> {
        private final CatEntity cat;

        public CatFleeGoal(CatEntity cat, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(cat, fleeFromType, distance, slowSpeed, fastSpeed, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
            this.cat = cat;
        }

        @Override
        public boolean canStart() {
            return !this.cat.isTamed() && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return !this.cat.isTamed() && super.shouldContinue();
        }
    }
}

