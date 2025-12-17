/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentDropChances;
import net.minecraft.entity.EquipmentHolder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentTable;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobVisibilityCache;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.BrainDebugData;
import net.minecraft.world.debug.data.EntityPathDebugData;
import net.minecraft.world.debug.data.GoalSelectorDebugData;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public abstract class MobEntity
extends LivingEntity
implements EquipmentHolder,
Leashable,
Targeter {
    private static final TrackedData<Byte> MOB_FLAGS = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int AI_DISABLED_FLAG = 1;
    private static final int LEFT_HANDED_FLAG = 2;
    private static final int ATTACKING_FLAG = 4;
    protected static final int MINIMUM_DROPPED_EXPERIENCE_PER_EQUIPMENT = 1;
    private static final Vec3i ITEM_PICK_UP_RANGE_EXPANDER = new Vec3i(1, 0, 1);
    private static final List<EquipmentSlot> EQUIPMENT_INIT_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    public static final float BASE_SPAWN_EQUIPMENT_CHANCE = 0.15f;
    public static final float ARMOR_UPGRADE_CHANCE = 0.1087f;
    public static final float ARMOR_UPGRADE_ROLLS = 3.0f;
    public static final float DEFAULT_CAN_PICKUP_LOOT_CHANCE = 0.55f;
    public static final float BASE_ENCHANTED_ARMOR_CHANCE = 0.5f;
    public static final float BASE_ENCHANTED_MAIN_HAND_EQUIPMENT_CHANCE = 0.25f;
    public static final int field_35039 = 2;
    private static final double ATTACK_RANGE = Math.sqrt(2.04f) - (double)0.6f;
    private static final boolean DEFAULT_CAN_PICK_UP_LOOT = false;
    private static final boolean DEFAULT_PERSISTENCE_REQUIRED = false;
    private static final boolean DEFAULT_LEFT_HANDED = false;
    private static final boolean DEFAULT_NO_AI = false;
    protected static final Identifier RANDOM_SPAWN_BONUS_MODIFIER_ID = Identifier.ofVanilla("random_spawn_bonus");
    public static final String DROP_CHANCES_KEY = "drop_chances";
    public static final String LEFT_HANDED_KEY = "LeftHanded";
    public static final String CAN_PICK_UP_LOOT_KEY = "CanPickUpLoot";
    public static final String NO_AI_KEY = "NoAI";
    public int ambientSoundChance;
    protected int experiencePoints;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyControl bodyControl;
    protected EntityNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    private @Nullable LivingEntity target;
    private final MobVisibilityCache visibilityCache;
    private EquipmentDropChances equipmentDropChances = EquipmentDropChances.DEFAULT;
    private boolean canPickUpLoot = false;
    private boolean persistent = false;
    private final Map<PathNodeType, Float> pathfindingPenalties = Maps.newEnumMap(PathNodeType.class);
    private Optional<RegistryKey<LootTable>> lootTable = Optional.empty();
    private long lootTableSeed;
    private @Nullable Leashable.LeashData leashData;
    private BlockPos positionTarget = BlockPos.ORIGIN;
    private int positionTargetRange = -1;

    protected MobEntity(EntityType<? extends MobEntity> entityType, World world) {
        super((EntityType<? extends LivingEntity>)entityType, world);
        this.goalSelector = new GoalSelector();
        this.targetSelector = new GoalSelector();
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyControl = this.createBodyControl();
        this.navigation = this.createNavigation(world);
        this.visibilityCache = new MobVisibilityCache(this);
        if (world instanceof ServerWorld) {
            this.initGoals();
        }
    }

    protected void initGoals() {
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.FOLLOW_RANGE, 16.0);
    }

    protected EntityNavigation createNavigation(World world) {
        return new MobNavigation(this, world);
    }

    protected boolean movesIndependently() {
        return false;
    }

    public float getPathfindingPenalty(PathNodeType nodeType) {
        MobEntity mobEntity;
        Entity entity = this.getControllingVehicle();
        MobEntity mobEntity2 = entity instanceof MobEntity && (mobEntity = (MobEntity)entity).movesIndependently() ? mobEntity : this;
        Float float_ = mobEntity2.pathfindingPenalties.get((Object)nodeType);
        return float_ == null ? nodeType.getDefaultPenalty() : float_.floatValue();
    }

    public void setPathfindingPenalty(PathNodeType nodeType, float penalty) {
        this.pathfindingPenalties.put(nodeType, Float.valueOf(penalty));
    }

    public void onStartPathfinding() {
    }

    public void onFinishPathfinding() {
    }

    protected BodyControl createBodyControl() {
        return new BodyControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        Entity entity = this.getControllingVehicle();
        if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity;
            return mobEntity.getMoveControl();
        }
        return this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public EntityNavigation getNavigation() {
        Entity entity = this.getControllingVehicle();
        if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity;
            return mobEntity.getNavigation();
        }
        return this.navigation;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (this.isAiDisabled()) return null;
        if (!(entity instanceof MobEntity)) return null;
        MobEntity mobEntity = (MobEntity)entity;
        if (!entity.shouldControlVehicles()) return null;
        MobEntity mobEntity2 = mobEntity;
        return mobEntity2;
    }

    public MobVisibilityCache getVisibilityCache() {
        return this.visibilityCache;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.target;
    }

    protected final @Nullable LivingEntity getTargetInBrain() {
        return this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return type != EntityType.GHAST;
    }

    public boolean canUseRangedWeapon(ItemStack stack) {
        return false;
    }

    public void onEatingGrass() {
        this.emitGameEvent(GameEvent.EAT);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MOB_FLAGS, (byte)0);
    }

    public int getMinAmbientSoundDelay() {
        return 80;
    }

    public void playAmbientSound() {
        this.playSound(this.getAmbientSound());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        Profiler profiler = Profilers.get();
        profiler.push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundChance++) {
            this.resetSoundDelay();
            this.playAmbientSound();
        }
        profiler.pop();
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        this.resetSoundDelay();
        super.playHurtSound(damageSource);
    }

    private void resetSoundDelay() {
        this.ambientSoundChance = -this.getMinAmbientSoundDelay();
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        if (this.experiencePoints > 0) {
            int i = this.experiencePoints;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                ItemStack itemStack;
                if (!equipmentSlot.increasesDroppedExperience() || (itemStack = this.getEquippedStack(equipmentSlot)).isEmpty() || !(this.equipmentDropChances.get(equipmentSlot) <= 1.0f)) continue;
                i += 1 + this.random.nextInt(3);
            }
            return i;
        }
        return this.experiencePoints;
    }

    public void playSpawnEffects() {
        if (this.getEntityWorld().isClient()) {
            this.addDeathParticles();
        } else {
            this.getEntityWorld().sendEntityStatus(this, (byte)20);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 20) {
            this.playSpawnEffects();
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getEntityWorld().isClient() && this.age % 5 == 0) {
            this.updateGoalControls();
        }
    }

    protected void updateGoalControls() {
        boolean bl = !(this.getControllingPassenger() instanceof MobEntity);
        boolean bl2 = !(this.getVehicle() instanceof AbstractBoatEntity);
        this.goalSelector.setControlEnabled(Goal.Control.MOVE, bl);
        this.goalSelector.setControlEnabled(Goal.Control.JUMP, bl && bl2);
        this.goalSelector.setControlEnabled(Goal.Control.LOOK, bl);
    }

    @Override
    protected void turnHead(float bodyRotation) {
        this.bodyControl.tick();
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean(CAN_PICK_UP_LOOT_KEY, this.canPickUpLoot());
        view.putBoolean("PersistenceRequired", this.persistent);
        if (!this.equipmentDropChances.equals(EquipmentDropChances.DEFAULT)) {
            view.put(DROP_CHANCES_KEY, EquipmentDropChances.CODEC, this.equipmentDropChances);
        }
        this.writeLeashData(view, this.leashData);
        if (this.hasPositionTarget()) {
            view.putInt("home_radius", this.positionTargetRange);
            view.put("home_pos", BlockPos.CODEC, this.positionTarget);
        }
        view.putBoolean(LEFT_HANDED_KEY, this.isLeftHanded());
        this.lootTable.ifPresent(lootTableKey -> view.put("DeathLootTable", LootTable.TABLE_KEY, lootTableKey));
        if (this.lootTableSeed != 0L) {
            view.putLong("DeathLootTableSeed", this.lootTableSeed);
        }
        if (this.isAiDisabled()) {
            view.putBoolean(NO_AI_KEY, this.isAiDisabled());
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setCanPickUpLoot(view.getBoolean(CAN_PICK_UP_LOOT_KEY, false));
        this.persistent = view.getBoolean("PersistenceRequired", false);
        this.equipmentDropChances = view.read(DROP_CHANCES_KEY, EquipmentDropChances.CODEC).orElse(EquipmentDropChances.DEFAULT);
        this.readLeashData(view);
        this.positionTargetRange = view.getInt("home_radius", -1);
        if (this.positionTargetRange >= 0) {
            this.positionTarget = view.read("home_pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        }
        this.setLeftHanded(view.getBoolean(LEFT_HANDED_KEY, false));
        this.lootTable = view.read("DeathLootTable", LootTable.TABLE_KEY);
        this.lootTableSeed = view.getLong("DeathLootTableSeed", 0L);
        this.setAiDisabled(view.getBoolean(NO_AI_KEY, false));
    }

    @Override
    protected void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(world, damageSource, causedByPlayer);
        this.lootTable = Optional.empty();
    }

    @Override
    public final Optional<RegistryKey<LootTable>> getLootTableKey() {
        if (this.lootTable.isPresent()) {
            return this.lootTable;
        }
        return super.getLootTableKey();
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setForwardSpeed(float forwardSpeed) {
        this.forwardSpeed = forwardSpeed;
    }

    public void setUpwardSpeed(float upwardSpeed) {
        this.upwardSpeed = upwardSpeed;
    }

    public void setSidewaysSpeed(float sidewaysSpeed) {
        this.sidewaysSpeed = sidewaysSpeed;
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        super.setMovementSpeed(movementSpeed);
        this.setForwardSpeed(movementSpeed);
    }

    public void stopMovement() {
        this.getNavigation().stop();
        this.setSidewaysSpeed(0.0f);
        this.setUpwardSpeed(0.0f);
        this.setMovementSpeed(0.0f);
        this.setVelocity(0.0, 0.0, 0.0);
        this.resetLeashMomentum();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getType().isIn(EntityTypeTags.BURN_IN_DAYLIGHT)) {
            this.tickBurnInDaylight();
        }
        Profiler profiler = Profilers.get();
        profiler.push("looting");
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (this.canPickUpLoot() && this.isAlive() && !this.dead && serverWorld.getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                Vec3i vec3i = this.getItemPickUpRangeExpander();
                List<ItemEntity> list = this.getEntityWorld().getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
                for (ItemEntity itemEntity : list) {
                    if (itemEntity.isRemoved() || itemEntity.getStack().isEmpty() || itemEntity.cannotPickup() || !this.canGather(serverWorld, itemEntity.getStack())) continue;
                    this.loot(serverWorld, itemEntity);
                }
            }
        }
        profiler.pop();
    }

    protected EquipmentSlot getDaylightProtectionSlot() {
        return EquipmentSlot.HEAD;
    }

    private void tickBurnInDaylight() {
        if (!this.isAlive() || !this.isAffectedByDaylight()) {
            return;
        }
        EquipmentSlot equipmentSlot = this.getDaylightProtectionSlot();
        ItemStack itemStack = this.getEquippedStack(equipmentSlot);
        if (!itemStack.isEmpty()) {
            if (itemStack.isDamageable()) {
                Item item = itemStack.getItem();
                itemStack.setDamage(itemStack.getDamage() + this.random.nextInt(2));
                if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
                    this.sendEquipmentBreakStatus(item, equipmentSlot);
                    this.equipStack(equipmentSlot, ItemStack.EMPTY);
                }
            }
            return;
        }
        this.setOnFireFor(8.0f);
    }

    private boolean isAffectedByDaylight() {
        if (!this.getEntityWorld().isClient() && this.getEntityWorld().getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.MONSTERS_BURN_GAMEPLAY, this.getEntityPos()).booleanValue()) {
            boolean bl;
            float f = this.getBrightnessAtEyes();
            BlockPos blockPos = BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ());
            boolean bl2 = bl = this.isTouchingWaterOrRain() || this.inPowderSnow || this.wasInPowderSnow;
            if (f > 0.5f && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && !bl && this.getEntityWorld().isSkyVisible(blockPos)) {
                return true;
            }
        }
        return false;
    }

    protected Vec3i getItemPickUpRangeExpander() {
        return ITEM_PICK_UP_RANGE_EXPANDER;
    }

    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getStack();
        ItemStack itemStack2 = this.tryEquip(world, itemStack.copy());
        if (!itemStack2.isEmpty()) {
            this.triggerItemPickedUpByEntityCriteria(itemEntity);
            this.sendPickup(itemEntity, itemStack2.getCount());
            itemStack.decrement(itemStack2.getCount());
            if (itemStack.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    public ItemStack tryEquip(ServerWorld world, ItemStack stack) {
        EquipmentSlot equipmentSlot = this.getPreferredEquipmentSlot(stack);
        if (!this.canEquip(stack, equipmentSlot)) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = this.getEquippedStack(equipmentSlot);
        boolean bl = this.prefersNewEquipment(stack, itemStack, equipmentSlot);
        if (equipmentSlot.isArmorSlot() && !bl) {
            equipmentSlot = EquipmentSlot.MAINHAND;
            itemStack = this.getEquippedStack(equipmentSlot);
            bl = itemStack.isEmpty();
        }
        if (bl && this.canPickupItem(stack)) {
            double d = this.equipmentDropChances.get(equipmentSlot);
            if (!itemStack.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.dropStack(world, itemStack);
            }
            ItemStack itemStack2 = equipmentSlot.split(stack);
            this.equipLootStack(equipmentSlot, itemStack2);
            return itemStack2;
        }
        return ItemStack.EMPTY;
    }

    protected void equipLootStack(EquipmentSlot slot, ItemStack stack) {
        this.equipStack(slot, stack);
        this.setDropGuaranteed(slot);
        this.persistent = true;
    }

    protected boolean canRemoveSaddle(PlayerEntity player) {
        return !this.hasPassengers();
    }

    public void setDropGuaranteed(EquipmentSlot slot) {
        this.equipmentDropChances = this.equipmentDropChances.withGuaranteed(slot);
    }

    protected boolean prefersNewEquipment(ItemStack newStack, ItemStack currentStack, EquipmentSlot slot) {
        if (currentStack.isEmpty()) {
            return true;
        }
        if (slot.isArmorSlot()) {
            return this.prefersNewArmor(newStack, currentStack, slot);
        }
        if (slot == EquipmentSlot.MAINHAND) {
            return this.prefersNewWeapon(newStack, currentStack, slot);
        }
        return false;
    }

    private boolean prefersNewArmor(ItemStack newStack, ItemStack currentStack, EquipmentSlot slot) {
        if (EnchantmentHelper.hasAnyEnchantmentsWith(currentStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        double d = this.getAttributeValueWithStack(newStack, EntityAttributes.ARMOR, slot);
        double e = this.getAttributeValueWithStack(currentStack, EntityAttributes.ARMOR, slot);
        double f = this.getAttributeValueWithStack(newStack, EntityAttributes.ARMOR_TOUGHNESS, slot);
        double g = this.getAttributeValueWithStack(currentStack, EntityAttributes.ARMOR_TOUGHNESS, slot);
        if (d != e) {
            return d > e;
        }
        if (f != g) {
            return f > g;
        }
        return this.prefersNewDamageableItem(newStack, currentStack);
    }

    private boolean prefersNewWeapon(ItemStack newStack, ItemStack currentStack, EquipmentSlot slot) {
        double e;
        double d;
        TagKey<Item> tagKey = this.getPreferredWeapons();
        if (tagKey != null) {
            if (currentStack.isIn(tagKey) && !newStack.isIn(tagKey)) {
                return false;
            }
            if (!currentStack.isIn(tagKey) && newStack.isIn(tagKey)) {
                return true;
            }
        }
        if ((d = this.getAttributeValueWithStack(newStack, EntityAttributes.ATTACK_DAMAGE, slot)) != (e = this.getAttributeValueWithStack(currentStack, EntityAttributes.ATTACK_DAMAGE, slot))) {
            return d > e;
        }
        return this.prefersNewDamageableItem(newStack, currentStack);
    }

    private double getAttributeValueWithStack(ItemStack stack, RegistryEntry<EntityAttribute> attribute, EquipmentSlot slot) {
        double d = this.getAttributes().hasAttribute(attribute) ? this.getAttributeBaseValue(attribute) : 0.0;
        AttributeModifiersComponent attributeModifiersComponent = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        return attributeModifiersComponent.applyOperations(attribute, d, slot);
    }

    public boolean prefersNewDamageableItem(ItemStack newStack, ItemStack oldStack) {
        int j;
        Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> set = oldStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getEnchantmentEntries();
        Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> set2 = newStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getEnchantmentEntries();
        if (set2.size() != set.size()) {
            return set2.size() > set.size();
        }
        int i = newStack.getDamage();
        if (i != (j = oldStack.getDamage())) {
            return i < j;
        }
        return newStack.contains(DataComponentTypes.CUSTOM_NAME) && !oldStack.contains(DataComponentTypes.CUSTOM_NAME);
    }

    public boolean canPickupItem(ItemStack stack) {
        return true;
    }

    public boolean canGather(ServerWorld world, ItemStack stack) {
        return this.canPickupItem(stack);
    }

    public @Nullable TagKey<Item> getPreferredWeapons() {
        return null;
    }

    public boolean canImmediatelyDespawn(double distanceSquared) {
        return true;
    }

    public boolean cannotDespawn() {
        return this.hasVehicle();
    }

    @Override
    public void checkDespawn() {
        if (this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL && !this.getType().isAllowedInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistent() || this.cannotDespawn()) {
            this.despawnCounter = 0;
            return;
        }
        PlayerEntity entity = this.getEntityWorld().getClosestPlayer(this, -1.0);
        if (entity != null) {
            int i;
            int j;
            double d = entity.squaredDistanceTo(this);
            if (d > (double)(j = (i = this.getType().getSpawnGroup().getImmediateDespawnRange()) * i) && this.canImmediatelyDespawn(d)) {
                this.discard();
            }
            int k = this.getType().getSpawnGroup().getDespawnStartRange();
            int l = k * k;
            if (this.despawnCounter > 600 && this.random.nextInt(800) == 0 && d > (double)l && this.canImmediatelyDespawn(d)) {
                this.discard();
            } else if (d < (double)l) {
                this.despawnCounter = 0;
            }
        }
    }

    @Override
    protected final void tickNewAi() {
        ++this.despawnCounter;
        Profiler profiler = Profilers.get();
        profiler.push("sensing");
        this.visibilityCache.clear();
        profiler.pop();
        int i = this.age + this.getId();
        if (i % 2 == 0 || this.age <= 1) {
            profiler.push("targetSelector");
            this.targetSelector.tick();
            profiler.pop();
            profiler.push("goalSelector");
            this.goalSelector.tick();
            profiler.pop();
        } else {
            profiler.push("targetSelector");
            this.targetSelector.tickGoals(false);
            profiler.pop();
            profiler.push("goalSelector");
            this.goalSelector.tickGoals(false);
            profiler.pop();
        }
        profiler.push("navigation");
        this.navigation.tick();
        profiler.pop();
        profiler.push("mob tick");
        this.mobTick((ServerWorld)this.getEntityWorld());
        profiler.pop();
        profiler.push("controls");
        profiler.push("move");
        this.moveControl.tick();
        profiler.swap("look");
        this.lookControl.tick();
        profiler.swap("jump");
        this.jumpControl.tick();
        profiler.pop();
        profiler.pop();
    }

    protected void mobTick(ServerWorld world) {
    }

    public int getMaxLookPitchChange() {
        return 40;
    }

    public int getMaxHeadRotation() {
        return 75;
    }

    protected void clampHeadYaw() {
        float f = this.getMaxHeadRotation();
        float g = this.getHeadYaw();
        float h = MathHelper.wrapDegrees(this.bodyYaw - g);
        float i = MathHelper.clamp(MathHelper.wrapDegrees(this.bodyYaw - g), -f, f);
        float j = g + h - i;
        this.setHeadYaw(j);
    }

    public int getMaxLookYawChange() {
        return 10;
    }

    public void lookAtEntity(Entity targetEntity, float maxYawChange, float maxPitchChange) {
        double f;
        double d = targetEntity.getX() - this.getX();
        double e = targetEntity.getZ() - this.getZ();
        if (targetEntity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)targetEntity;
            f = livingEntity.getEyeY() - this.getEyeY();
        } else {
            f = (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double g = Math.sqrt(d * d + e * e);
        float h = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0f;
        float i = (float)(-(MathHelper.atan2(f, g) * 57.2957763671875));
        this.setPitch(this.changeAngle(this.getPitch(), i, maxPitchChange));
        this.setYaw(this.changeAngle(this.getYaw(), h, maxYawChange));
    }

    private float changeAngle(float from, float to, float max) {
        float f = MathHelper.wrapDegrees(to - from);
        if (f > max) {
            f = max;
        }
        if (f < -max) {
            f = -max;
        }
        return from + f;
    }

    public static boolean canMobSpawn(EntityType<? extends MobEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        BlockPos blockPos = pos.down();
        return SpawnReason.isAnySpawner(spawnReason) || world.getBlockState(blockPos).allowsSpawning(world, blockPos, type);
    }

    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return true;
    }

    public boolean canSpawn(WorldView world) {
        return !world.containsFluid(this.getBoundingBox()) && world.doesNotIntersectEntities(this);
    }

    public int getLimitPerChunk() {
        return 4;
    }

    public boolean spawnsTooManyForEachTry(int count) {
        return false;
    }

    @Override
    public int getSafeFallDistance() {
        if (this.getTarget() == null) {
            return this.getSafeFallDistance(0.0f);
        }
        int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33f);
        if ((i -= (3 - this.getEntityWorld().getDifficulty().getId()) * 4) < 0) {
            i = 0;
        }
        return this.getSafeFallDistance(i);
    }

    public ItemStack getBodyArmor() {
        return this.getEquippedStack(EquipmentSlot.BODY);
    }

    public boolean hasSaddleEquipped() {
        return this.isWearing(EquipmentSlot.SADDLE);
    }

    public boolean isWearingBodyArmor() {
        return this.isWearing(EquipmentSlot.BODY);
    }

    private boolean isWearing(EquipmentSlot slot) {
        return this.hasStackEquipped(slot) && this.canEquip(this.getEquippedStack(slot), slot);
    }

    public void equipBodyArmor(ItemStack stack) {
        this.equipLootStack(EquipmentSlot.BODY, stack);
    }

    public Inventory createEquipmentInventory(final EquipmentSlot slot) {
        return new SingleStackInventory(){

            @Override
            public ItemStack getStack() {
                return MobEntity.this.getEquippedStack(slot);
            }

            @Override
            public void setStack(ItemStack stack) {
                MobEntity.this.equipStack(slot, stack);
                if (!stack.isEmpty()) {
                    MobEntity.this.setDropGuaranteed(slot);
                    MobEntity.this.setPersistent();
                }
            }

            @Override
            public void markDirty() {
            }

            @Override
            public boolean canPlayerUse(PlayerEntity player) {
                return player.getVehicle() == MobEntity.this || player.canInteractWithEntity(MobEntity.this, 4.0);
            }
        };
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            float f = this.equipmentDropChances.get(equipmentSlot);
            if (f == 0.0f) continue;
            boolean bl = this.equipmentDropChances.dropsExactly(equipmentSlot);
            Object object = source.getAttacker();
            if (object instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)object;
                object = this.getEntityWorld();
                if (object instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld)object;
                    f = EnchantmentHelper.getEquipmentDropChance(serverWorld, livingEntity, source, f);
                }
            }
            if (itemStack.isEmpty() || EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP) || !causedByPlayer && !bl || !(this.random.nextFloat() < f)) continue;
            if (!bl && itemStack.isDamageable()) {
                itemStack.setDamage(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
            }
            this.dropStack(world, itemStack);
            this.equipStack(equipmentSlot, ItemStack.EMPTY);
        }
    }

    public EquipmentDropChances getEquipmentDropChances() {
        return this.equipmentDropChances;
    }

    public void dropAllForeignEquipment(ServerWorld world) {
        this.dropForeignEquipment(world, stack -> true);
    }

    public Set<EquipmentSlot> dropForeignEquipment(ServerWorld world, Predicate<ItemStack> dropPredicate) {
        HashSet<EquipmentSlot> set = new HashSet<EquipmentSlot>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            if (itemStack.isEmpty()) continue;
            if (!dropPredicate.test(itemStack)) {
                set.add(equipmentSlot);
                continue;
            }
            if (!this.equipmentDropChances.dropsExactly(equipmentSlot)) continue;
            this.equipStack(equipmentSlot, ItemStack.EMPTY);
            this.dropStack(world, itemStack);
        }
        return set;
    }

    private LootWorldContext createEquipmentLootParameters(ServerWorld world) {
        return new LootWorldContext.Builder(world).add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.EQUIPMENT);
    }

    public void setEquipmentFromTable(EquipmentTable equipmentTable) {
        this.setEquipmentFromTable(equipmentTable.lootTable(), equipmentTable.slotDropChances());
    }

    public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.setEquipmentFromTable(lootTable, this.createEquipmentLootParameters(serverWorld), slotDropChances);
        }
    }

    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < 0.15f * localDifficulty.getClampedLocalDifficulty()) {
            int i = random.nextInt(3);
            int j = 1;
            while ((float)j <= 3.0f) {
                if (random.nextFloat() < 0.1087f) {
                    ++i;
                }
                ++j;
            }
            float f = this.getEntityWorld().getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            boolean bl = true;
            for (EquipmentSlot equipmentSlot : EQUIPMENT_INIT_ORDER) {
                Item item;
                ItemStack itemStack = this.getEquippedStack(equipmentSlot);
                if (!bl && random.nextFloat() < f) break;
                bl = false;
                if (!itemStack.isEmpty() || (item = MobEntity.getEquipmentForSlot(equipmentSlot, i)) == null) continue;
                this.equipStack(equipmentSlot, new ItemStack(item));
            }
        }
    }

    public static @Nullable Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int equipmentLevel) {
        switch (equipmentSlot) {
            case HEAD: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_HELMET;
                }
                if (equipmentLevel == 1) {
                    return Items.COPPER_HELMET;
                }
                if (equipmentLevel == 2) {
                    return Items.GOLDEN_HELMET;
                }
                if (equipmentLevel == 3) {
                    return Items.CHAINMAIL_HELMET;
                }
                if (equipmentLevel == 4) {
                    return Items.IRON_HELMET;
                }
                if (equipmentLevel == 5) {
                    return Items.DIAMOND_HELMET;
                }
            }
            case CHEST: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_CHESTPLATE;
                }
                if (equipmentLevel == 1) {
                    return Items.COPPER_CHESTPLATE;
                }
                if (equipmentLevel == 2) {
                    return Items.GOLDEN_CHESTPLATE;
                }
                if (equipmentLevel == 3) {
                    return Items.CHAINMAIL_CHESTPLATE;
                }
                if (equipmentLevel == 4) {
                    return Items.IRON_CHESTPLATE;
                }
                if (equipmentLevel == 5) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            }
            case LEGS: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_LEGGINGS;
                }
                if (equipmentLevel == 1) {
                    return Items.COPPER_LEGGINGS;
                }
                if (equipmentLevel == 2) {
                    return Items.GOLDEN_LEGGINGS;
                }
                if (equipmentLevel == 3) {
                    return Items.CHAINMAIL_LEGGINGS;
                }
                if (equipmentLevel == 4) {
                    return Items.IRON_LEGGINGS;
                }
                if (equipmentLevel == 5) {
                    return Items.DIAMOND_LEGGINGS;
                }
            }
            case FEET: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_BOOTS;
                }
                if (equipmentLevel == 1) {
                    return Items.COPPER_BOOTS;
                }
                if (equipmentLevel == 2) {
                    return Items.GOLDEN_BOOTS;
                }
                if (equipmentLevel == 3) {
                    return Items.CHAINMAIL_BOOTS;
                }
                if (equipmentLevel == 4) {
                    return Items.IRON_BOOTS;
                }
                if (equipmentLevel != 5) break;
                return Items.DIAMOND_BOOTS;
            }
        }
        return null;
    }

    protected void updateEnchantments(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty) {
        this.enchantMainHandItem(world, random, localDifficulty);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.enchantEquipment(world, random, equipmentSlot, localDifficulty);
        }
    }

    protected void enchantMainHandItem(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty) {
        this.enchantEquipment(world, EquipmentSlot.MAINHAND, random, 0.25f, localDifficulty);
    }

    protected void enchantEquipment(ServerWorldAccess world, Random random, EquipmentSlot slot, LocalDifficulty localDifficulty) {
        this.enchantEquipment(world, slot, random, 0.5f, localDifficulty);
    }

    private void enchantEquipment(ServerWorldAccess world, EquipmentSlot slot, Random random, float power, LocalDifficulty localDifficulty) {
        ItemStack itemStack = this.getEquippedStack(slot);
        if (!itemStack.isEmpty() && random.nextFloat() < power * localDifficulty.getClampedLocalDifficulty()) {
            EnchantmentHelper.applyEnchantmentProvider(itemStack, world.getRegistryManager(), EnchantmentProviders.MOB_SPAWN_EQUIPMENT, localDifficulty, random);
            this.equipStack(slot, itemStack);
        }
    }

    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        EntityAttributeInstance entityAttributeInstance = Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE));
        if (!entityAttributeInstance.hasModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID)) {
            entityAttributeInstance.addPersistentModifier(new EntityAttributeModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID, random.nextTriangular(0.0, 0.11485000000000001), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.setLeftHanded(random.nextFloat() < 0.05f);
        return entityData;
    }

    public void setPersistent() {
        this.persistent = true;
    }

    @Override
    public void setEquipmentDropChance(EquipmentSlot slot, float dropChance) {
        this.equipmentDropChances = this.equipmentDropChances.withChance(slot, dropChance);
    }

    @Override
    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
    }

    @Override
    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return this.canPickUpLoot();
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.isAlive()) {
            return ActionResult.PASS;
        }
        ActionResult actionResult = this.interactWithItem(player, hand);
        if (actionResult.isAccepted()) {
            this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            return actionResult;
        }
        ActionResult actionResult2 = super.interact(player, hand);
        if (actionResult2 != ActionResult.PASS) {
            return actionResult2;
        }
        actionResult = this.interactMob(player, hand);
        if (actionResult.isAccepted()) {
            this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            return actionResult;
        }
        return ActionResult.PASS;
    }

    private ActionResult interactWithItem(PlayerEntity player, Hand hand) {
        ActionResult actionResult;
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.NAME_TAG) && (actionResult = itemStack.useOnEntity(player, this, hand)).isAccepted()) {
            return actionResult;
        }
        Item item = itemStack.getItem();
        if (item instanceof SpawnEggItem) {
            SpawnEggItem spawnEggItem = (SpawnEggItem)item;
            if (this.getEntityWorld() instanceof ServerWorld) {
                Optional<MobEntity> optional = spawnEggItem.spawnBaby(player, this, this.getType(), (ServerWorld)this.getEntityWorld(), this.getEntityPos(), itemStack);
                optional.ifPresent(entity -> this.onPlayerSpawnedChild(player, (MobEntity)entity));
                if (optional.isEmpty()) {
                    return ActionResult.PASS;
                }
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.PASS;
    }

    protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
    }

    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        return ActionResult.PASS;
    }

    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        int i = stack.getCount();
        UseRemainderComponent useRemainderComponent = stack.get(DataComponentTypes.USE_REMAINDER);
        stack.decrementUnlessCreative(1, player);
        if (useRemainderComponent != null) {
            ItemStack itemStack = useRemainderComponent.convert(stack, i, player.isInCreativeMode(), player::giveOrDropStack);
            player.setStackInHand(hand, itemStack);
        }
    }

    public boolean isInPositionTargetRange() {
        return this.isInPositionTargetRange(this.getBlockPos());
    }

    public boolean isInPositionTargetRange(BlockPos pos) {
        if (this.positionTargetRange == -1) {
            return true;
        }
        return this.positionTarget.getSquaredDistance(pos) < (double)(this.positionTargetRange * this.positionTargetRange);
    }

    public boolean isInPositionTargetRange(Vec3d pos) {
        if (this.positionTargetRange == -1) {
            return true;
        }
        return this.positionTarget.getSquaredDistance(pos) < (double)(this.positionTargetRange * this.positionTargetRange);
    }

    public void setPositionTarget(BlockPos target, int range) {
        this.positionTarget = target;
        this.positionTargetRange = range;
    }

    public BlockPos getPositionTarget() {
        return this.positionTarget;
    }

    public int getPositionTargetRange() {
        return this.positionTargetRange;
    }

    public void clearPositionTarget() {
        this.positionTargetRange = -1;
    }

    public boolean hasPositionTarget() {
        return this.positionTargetRange != -1;
    }

    public <T extends MobEntity> @Nullable T convertTo(EntityType<T> entityType, EntityConversionContext context, SpawnReason reason, EntityConversionContext.Finalizer<T> finalizer) {
        if (this.isRemoved()) {
            return null;
        }
        MobEntity mobEntity = (MobEntity)entityType.create(this.getEntityWorld(), reason);
        if (mobEntity == null) {
            return null;
        }
        context.type().setUpNewEntity(this, mobEntity, context);
        finalizer.finalizeConversion(mobEntity);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            serverWorld.spawnEntity(mobEntity);
        }
        if (context.type().shouldDiscardOldEntity()) {
            this.discard();
        }
        return (T)mobEntity;
    }

    public <T extends MobEntity> @Nullable T convertTo(EntityType<T> entityType, EntityConversionContext context, EntityConversionContext.Finalizer<T> finalizer) {
        return this.convertTo(entityType, context, SpawnReason.CONVERSION, finalizer);
    }

    @Override
    public @Nullable Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    private void resetLeashMomentum() {
        if (this.leashData != null) {
            this.leashData.momentum = 0.0;
        }
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public void onLeashRemoved() {
        if (this.getLeashData() == null) {
            this.clearPositionTarget();
        }
    }

    @Override
    public void snapLongLeash() {
        Leashable.super.snapLongLeash();
        this.goalSelector.disableControl(Goal.Control.MOVE);
    }

    @Override
    public boolean canBeLeashed() {
        return !(this instanceof Monster);
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        boolean bl = super.startRiding(entity, force, emitEvent);
        if (bl && this.isLeashed()) {
            this.detachLeash();
        }
        return bl;
    }

    @Override
    public boolean canActVoluntarily() {
        return super.canActVoluntarily() && !this.isAiDisabled();
    }

    public void setAiDisabled(boolean aiDisabled) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, aiDisabled ? (byte)(b | 1) : (byte)(b & 0xFFFFFFFE));
    }

    public void setLeftHanded(boolean leftHanded) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, leftHanded ? (byte)(b | 2) : (byte)(b & 0xFFFFFFFD));
    }

    public void setAttacking(boolean attacking) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, attacking ? (byte)(b | 4) : (byte)(b & 0xFFFFFFFB));
    }

    public boolean isAiDisabled() {
        return (this.dataTracker.get(MOB_FLAGS) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.dataTracker.get(MOB_FLAGS) & 2) != 0;
    }

    public boolean isAttacking() {
        return (this.dataTracker.get(MOB_FLAGS) & 4) != 0;
    }

    public void setBaby(boolean baby) {
    }

    @Override
    public Arm getMainArm() {
        return this.isLeftHanded() ? Arm.LEFT : Arm.RIGHT;
    }

    public boolean isInAttackRange(LivingEntity entity) {
        double e;
        double d;
        AttackRangeComponent attackRangeComponent = this.getActiveOrMainHandStack().get(DataComponentTypes.ATTACK_RANGE);
        if (attackRangeComponent == null) {
            d = ATTACK_RANGE;
            e = 0.0;
        } else {
            d = attackRangeComponent.getEffectiveMaxRange(this);
            e = attackRangeComponent.getEffectiveMinRange(this);
        }
        Box box = entity.getHitbox();
        return this.getAttackBox(d).intersects(box) && (e <= 0.0 || !this.getAttackBox(e).intersects(box));
    }

    protected Box getAttackBox(double attackRange) {
        Box box3;
        Entity entity = this.getVehicle();
        if (entity != null) {
            Box box = entity.getBoundingBox();
            Box box2 = this.getBoundingBox();
            box3 = new Box(Math.min(box2.minX, box.minX), box2.minY, Math.min(box2.minZ, box.minZ), Math.max(box2.maxX, box.maxX), box2.maxY, Math.max(box2.maxZ, box.maxZ));
        } else {
            box3 = this.getBoundingBox();
        }
        return box3.expand(attackRange, 0.0, attackRange);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        ItemStack itemStack = this.getWeaponStack();
        DamageSource damageSource = itemStack.getDamageSource(this, () -> this.getDamageSources().mobAttack(this));
        f = EnchantmentHelper.getDamage(world, itemStack, target, damageSource, f);
        f += itemStack.getItem().getBonusAttackDamage(target, f, damageSource);
        Vec3d vec3d = target.getVelocity();
        boolean bl = target.damage(world, damageSource, f);
        if (bl) {
            this.knockbackTarget(target, this.getAttackKnockbackAgainst(target, damageSource), vec3d);
            if (target instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)target;
                itemStack.postHit(livingEntity, this);
            }
            EnchantmentHelper.onTargetDamaged(world, target, damageSource);
            this.onAttacking(target);
            this.playAttackSound();
        }
        this.useAttackEnchantmentEffects();
        return bl;
    }

    @Override
    protected void swimUpward(TagKey<Fluid> fluid) {
        if (this.getNavigation().canSwim()) {
            super.swimUpward(fluid);
        } else {
            this.setVelocity(this.getVelocity().add(0.0, 0.3, 0.0));
        }
    }

    @VisibleForTesting
    public void clearGoalsAndTasks() {
        this.clearGoals(goal -> true);
        this.getBrain().clear();
    }

    public void clearGoals(Predicate<Goal> predicate) {
        this.goalSelector.clear(predicate);
    }

    @Override
    protected void removeFromDimension() {
        super.removeFromDimension();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            if (itemStack.isEmpty()) continue;
            itemStack.setCount(0);
        }
    }

    @Override
    public @Nullable ItemStack getPickBlockStack() {
        SpawnEggItem spawnEggItem = SpawnEggItem.forEntity(this.getType());
        if (spawnEggItem == null) {
            return null;
        }
        return new ItemStack(spawnEggItem);
    }

    @Override
    protected void updateAttribute(RegistryEntry<EntityAttribute> attribute) {
        super.updateAttribute(attribute);
        if (attribute.matches(EntityAttributes.FOLLOW_RANGE) || attribute.matches(EntityAttributes.TEMPT_RANGE)) {
            this.getNavigation().updateRange();
        }
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
        tracker.track(DebugSubscriptionTypes.ENTITY_PATHS, () -> {
            Path path = this.getNavigation().getCurrentPath();
            if (path != null && path.getDebugNodeInfos() != null) {
                return new EntityPathDebugData(path.copy(), this.getNavigation().getNodeReachProximity());
            }
            return null;
        });
        tracker.track(DebugSubscriptionTypes.GOAL_SELECTORS, () -> {
            Set<PrioritizedGoal> set = this.goalSelector.getGoals();
            ArrayList<GoalSelectorDebugData.Goal> list = new ArrayList<GoalSelectorDebugData.Goal>(set.size());
            set.forEach(goal -> list.add(new GoalSelectorDebugData.Goal(goal.getPriority(), goal.isRunning(), goal.getGoal().getClass().getSimpleName())));
            return new GoalSelectorDebugData(list);
        });
        if (!this.brain.isEmpty()) {
            tracker.track(DebugSubscriptionTypes.BRAINS, () -> BrainDebugData.fromEntity(world, this));
        }
    }

    public float getRiderChargingSpeedMultiplier() {
        return 1.0f;
    }
}

