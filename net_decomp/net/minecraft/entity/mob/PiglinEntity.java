/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PiglinActivity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class PiglinEntity
extends AbstractPiglinEntity
implements CrossbowUser,
InventoryOwner {
    private static final TrackedData<Boolean> BABY = DataTracker.registerData(PiglinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CHARGING = DataTracker.registerData(PiglinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DANCING = DataTracker.registerData(PiglinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final Identifier BABY_SPEED_BOOST_ID = Identifier.ofVanilla("baby");
    private static final EntityAttributeModifier BABY_SPEED_BOOST = new EntityAttributeModifier(BABY_SPEED_BOOST_ID, 0.2f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final int field_30548 = 16;
    private static final float field_30549 = 0.35f;
    private static final int field_30550 = 5;
    private static final float field_30552 = 0.1f;
    private static final int field_30553 = 3;
    private static final float field_30554 = 0.2f;
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.PIGLIN.getDimensions().scaled(0.5f).withEyeHeight(0.97f);
    private static final double field_30556 = 0.5;
    private static final boolean DEFAULT_IS_BABY = false;
    private static final boolean DEFAULT_CANNOT_HUNT = false;
    private final SimpleInventory inventory = new SimpleInventory(8);
    private boolean cannotHunt = false;
    protected static final ImmutableList<SensorType<? extends Sensor<? super PiglinEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULE_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, (Object[])new MemoryModuleType[]{MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.SPEAR_FLEEING_TIME, MemoryModuleType.SPEAR_FLEEING_POSITION, MemoryModuleType.SPEAR_CHARGE_POSITION, MemoryModuleType.SPEAR_ENGAGE_TIME, MemoryModuleType.SPEAR_STATUS});

    public PiglinEntity(EntityType<? extends AbstractPiglinEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 5;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("IsBaby", this.isBaby());
        view.putBoolean("CannotHunt", this.cannotHunt);
        this.writeInventory(view);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setBaby(view.getBoolean("IsBaby", false));
        this.setCannotHunt(view.getBoolean("CannotHunt", false));
        this.readInventory(view);
    }

    @Override
    @Debug
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        this.inventory.clearToList().forEach(stack -> this.dropStack(world, (ItemStack)stack));
    }

    protected ItemStack addItem(ItemStack stack) {
        return this.inventory.addStack(stack);
    }

    protected boolean canInsertIntoInventory(ItemStack stack) {
        return this.inventory.canInsert(stack);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(BABY, false);
        builder.add(CHARGING, false);
        builder.add(DANCING, false);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (BABY.equals(data)) {
            this.calculateDimensions();
        }
    }

    public static DefaultAttributeContainer.Builder createPiglinAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.MAX_HEALTH, 16.0).add(EntityAttributes.MOVEMENT_SPEED, 0.35f).add(EntityAttributes.ATTACK_DAMAGE, 5.0);
    }

    public static boolean canSpawn(EntityType<PiglinEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return !world.getBlockState(pos.down()).isOf(Blocks.NETHER_WART_BLOCK);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        if (spawnReason != SpawnReason.STRUCTURE) {
            if (random.nextFloat() < 0.2f) {
                this.setBaby(true);
            } else if (this.isAdult()) {
                this.equipStack(EquipmentSlot.MAINHAND, this.makeInitialWeapon());
            }
        }
        PiglinBrain.setHuntedRecently(this, world.getRandom());
        this.initEquipment(random, difficulty);
        this.updateEnchantments(world, random, difficulty);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.isPersistent();
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (this.isAdult()) {
            this.equipAtChance(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET), random);
            this.equipAtChance(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE), random);
            this.equipAtChance(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS), random);
            this.equipAtChance(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS), random);
        }
    }

    private void equipAtChance(EquipmentSlot slot, ItemStack stack, Random random) {
        if (random.nextFloat() < 0.1f) {
            this.equipStack(slot, stack);
        }
    }

    protected Brain.Profile<PiglinEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULE_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return PiglinBrain.create(this, this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<PiglinEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ActionResult actionResult = super.interactMob(player, hand);
        if (actionResult.isAccepted()) {
            return actionResult;
        }
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            return PiglinBrain.playerInteract(serverWorld, this, player, hand);
        }
        boolean bl = PiglinBrain.isWillingToTrade(this, player.getStackInHand(hand)) && this.getActivity() != PiglinActivity.ADMIRING_ITEM;
        return bl ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    @Override
    public void setBaby(boolean baby) {
        this.getDataTracker().set(BABY, baby);
        if (!this.getEntityWorld().isClient()) {
            EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            entityAttributeInstance.removeModifier(BABY_SPEED_BOOST.id());
            if (baby) {
                entityAttributeInstance.addTemporaryModifier(BABY_SPEED_BOOST);
            }
        }
    }

    @Override
    public boolean isBaby() {
        return this.getDataTracker().get(BABY);
    }

    private void setCannotHunt(boolean cannotHunt) {
        this.cannotHunt = cannotHunt;
    }

    @Override
    protected boolean canHunt() {
        return !this.cannotHunt;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler profiler = Profilers.get();
        profiler.push("piglinBrain");
        this.getBrain().tick(world, this);
        profiler.pop();
        PiglinBrain.tickActivities(this);
        super.mobTick(world);
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        return this.experiencePoints;
    }

    @Override
    protected void zombify(ServerWorld world) {
        PiglinBrain.pickupItemWithOffHand(world, this);
        this.inventory.clearToList().forEach(stack -> this.dropStack(world, (ItemStack)stack));
        super.zombify(world);
    }

    private ItemStack makeInitialWeapon() {
        if ((double)this.random.nextFloat() < 0.5) {
            return new ItemStack(Items.CROSSBOW);
        }
        return new ItemStack(this.random.nextInt(10) == 0 ? Items.GOLDEN_SPEAR : Items.GOLDEN_SWORD);
    }

    @Override
    public @Nullable TagKey<Item> getPreferredWeapons() {
        if (this.isBaby()) {
            return null;
        }
        return ItemTags.PIGLIN_PREFERRED_WEAPONS;
    }

    private boolean isCharging() {
        return this.dataTracker.get(CHARGING);
    }

    @Override
    public void setCharging(boolean charging) {
        this.dataTracker.set(CHARGING, charging);
    }

    @Override
    public void postShoot() {
        this.despawnCounter = 0;
    }

    @Override
    public PiglinActivity getActivity() {
        if (this.isDancing()) {
            return PiglinActivity.DANCING;
        }
        if (PiglinBrain.isGoldenItem(this.getOffHandStack())) {
            return PiglinActivity.ADMIRING_ITEM;
        }
        if (this.isAttacking() && this.isHoldingTool()) {
            return PiglinActivity.ATTACKING_WITH_MELEE_WEAPON;
        }
        if (this.isCharging()) {
            return PiglinActivity.CROSSBOW_CHARGE;
        }
        if (this.isHolding(Items.CROSSBOW) && CrossbowItem.isCharged(this.getWeaponStack())) {
            return PiglinActivity.CROSSBOW_HOLD;
        }
        return PiglinActivity.DEFAULT;
    }

    public boolean isDancing() {
        return this.dataTracker.get(DANCING);
    }

    public void setDancing(boolean dancing) {
        this.dataTracker.set(DANCING, dancing);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity entity;
        boolean bl = super.damage(world, source, amount);
        if (bl && (entity = source.getAttacker()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            PiglinBrain.onAttacked(world, this, livingEntity);
        }
        return bl;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        this.shoot(this, 1.6f);
    }

    @Override
    public boolean canUseRangedWeapon(ItemStack stack) {
        return stack.getItem() == Items.CROSSBOW || stack.contains(DataComponentTypes.KINETIC_WEAPON);
    }

    protected void equipToMainHand(ItemStack stack) {
        this.equipLootStack(EquipmentSlot.MAINHAND, stack);
    }

    protected void equipToOffHand(ItemStack stack) {
        if (stack.isOf(PiglinBrain.BARTERING_ITEM)) {
            this.equipStack(EquipmentSlot.OFFHAND, stack);
            this.setDropGuaranteed(EquipmentSlot.OFFHAND);
        } else {
            this.equipLootStack(EquipmentSlot.OFFHAND, stack);
        }
    }

    @Override
    public boolean canGather(ServerWorld world, ItemStack stack) {
        return world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING) != false && this.canPickUpLoot() && PiglinBrain.canGather(this, stack);
    }

    protected boolean canEquipStack(ItemStack stack) {
        EquipmentSlot equipmentSlot = this.getPreferredEquipmentSlot(stack);
        ItemStack itemStack = this.getEquippedStack(equipmentSlot);
        return this.prefersNewEquipment(stack, itemStack, equipmentSlot);
    }

    @Override
    protected boolean prefersNewEquipment(ItemStack newStack, ItemStack currentStack, EquipmentSlot slot) {
        boolean bl2;
        if (EnchantmentHelper.hasAnyEnchantmentsWith(currentStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        TagKey<Item> tagKey = this.getPreferredWeapons();
        boolean bl = PiglinBrain.isGoldenItem(newStack) || tagKey != null && newStack.isIn(tagKey);
        boolean bl3 = bl2 = PiglinBrain.isGoldenItem(currentStack) || tagKey != null && currentStack.isIn(tagKey);
        if (bl && !bl2) {
            return true;
        }
        if (!bl && bl2) {
            return false;
        }
        return super.prefersNewEquipment(newStack, currentStack, slot);
    }

    @Override
    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        this.triggerItemPickedUpByEntityCriteria(itemEntity);
        PiglinBrain.loot(world, this, itemEntity);
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        if (this.isBaby() && entity.getType() == EntityType.HOGLIN) {
            entity = this.getTopMostPassenger(entity, 3);
        }
        return super.startRiding(entity, force, emitEvent);
    }

    private Entity getTopMostPassenger(Entity entity, int maxLevel) {
        List<Entity> list = entity.getPassengerList();
        if (maxLevel == 1 || list.isEmpty()) {
            return entity;
        }
        return this.getTopMostPassenger(list.getFirst(), maxLevel - 1);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.getEntityWorld().isClient()) {
            return null;
        }
        return PiglinBrain.getCurrentActivitySound(this).orElse(null);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PIGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_PIGLIN_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playZombificationSound() {
        this.playSound(SoundEvents.ENTITY_PIGLIN_CONVERTED_TO_ZOMBIFIED);
    }
}

