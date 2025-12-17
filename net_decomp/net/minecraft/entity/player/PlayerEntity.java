/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.math.IntMath
 *  com.mojang.authlib.GameProfile
 *  com.mojang.datafixers.util.Either
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.player;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SculkShriekerWarningManager;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.entity.TestBlockEntity;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ContainerUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AbstractNautilusEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEquipment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public abstract class PlayerEntity
extends PlayerLikeEntity
implements ContainerUser {
    public static final int MAX_HEALTH = 20;
    public static final int SLEEP_DURATION = 100;
    public static final int WAKE_UP_DURATION = 10;
    public static final int ENDER_SLOT_OFFSET = 200;
    public static final int HELD_ITEM_SLOT_OFFSET = 499;
    public static final int CRAFTING_SLOT_OFFSET = 500;
    public static final float BLOCK_INTERACTION_RANGE = 4.5f;
    public static final float ENTITY_INTERACTION_RANGE = 3.0f;
    private static final int field_52222 = 40;
    private static final TrackedData<Float> ABSORPTION_AMOUNT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> SCORE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<OptionalInt> LEFT_SHOULDER_PARROT_VARIANT_ID = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
    private static final TrackedData<OptionalInt> RIGHT_SHOULDER_PARROT_VARIANT_ID = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
    private static final short field_57725 = 0;
    private static final float field_57726 = 0.0f;
    private static final int field_57727 = 0;
    private static final int field_57728 = 0;
    private static final int field_57729 = 0;
    private static final int field_57730 = 0;
    private static final int field_57731 = 0;
    private static final boolean field_57723 = false;
    private static final int field_57724 = 0;
    public static final float field_64686 = 2.0f;
    final PlayerInventory inventory;
    protected EnderChestInventory enderChestInventory = new EnderChestInventory();
    public final PlayerScreenHandler playerScreenHandler;
    public ScreenHandler currentScreenHandler;
    protected HungerManager hungerManager = new HungerManager();
    protected int abilityResyncCountdown;
    public int experiencePickUpDelay;
    private int sleepTimer = 0;
    protected boolean isSubmergedInWater;
    private final PlayerAbilities abilities = new PlayerAbilities();
    public int experienceLevel = 0;
    public int totalExperience = 0;
    public float experienceProgress = 0.0f;
    protected int enchantingTableSeed = 0;
    protected final float baseFlySpeed = 0.02f;
    private int lastPlayedLevelUpSoundTime;
    private final GameProfile gameProfile;
    private boolean reducedDebugInfo;
    private ItemStack selectedItem = ItemStack.EMPTY;
    private final ItemCooldownManager itemCooldownManager = this.createCooldownManager();
    private Optional<GlobalPos> lastDeathPos = Optional.empty();
    public @Nullable FishingBobberEntity fishHook;
    protected float damageTiltYaw;
    public @Nullable Vec3d currentExplosionImpactPos;
    public @Nullable Entity explodedBy;
    private boolean ignoreFallDamageFromCurrentExplosion = false;
    private int currentExplosionResetGraceTime = 0;

    public PlayerEntity(World world, GameProfile profile) {
        super((EntityType<? extends LivingEntity>)EntityType.PLAYER, world);
        this.setUuid(profile.id());
        this.gameProfile = profile;
        this.inventory = new PlayerInventory(this, this.equipment);
        this.playerScreenHandler = new PlayerScreenHandler(this.inventory, !world.isClient(), this);
        this.currentScreenHandler = this.playerScreenHandler;
    }

    @Override
    protected EntityEquipment createEquipment() {
        return new PlayerEquipment(this);
    }

    public boolean isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode) {
        if (!gameMode.isBlockBreakingRestricted()) {
            return false;
        }
        if (gameMode == GameMode.SPECTATOR) {
            return true;
        }
        if (this.canModifyBlocks()) {
            return false;
        }
        ItemStack itemStack = this.getMainHandStack();
        return itemStack.isEmpty() || !itemStack.canBreak(new CachedBlockPosition(world, pos, false));
    }

    public static DefaultAttributeContainer.Builder createPlayerAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.ATTACK_DAMAGE, 1.0).add(EntityAttributes.MOVEMENT_SPEED, 0.1f).add(EntityAttributes.ATTACK_SPEED).add(EntityAttributes.LUCK).add(EntityAttributes.BLOCK_INTERACTION_RANGE, 4.5).add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3.0).add(EntityAttributes.BLOCK_BREAK_SPEED).add(EntityAttributes.SUBMERGED_MINING_SPEED).add(EntityAttributes.SNEAKING_SPEED).add(EntityAttributes.MINING_EFFICIENCY).add(EntityAttributes.SWEEPING_DAMAGE_RATIO).add(EntityAttributes.WAYPOINT_TRANSMIT_RANGE, 6.0E7).add(EntityAttributes.WAYPOINT_RECEIVE_RANGE, 6.0E7);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ABSORPTION_AMOUNT, Float.valueOf(0.0f));
        builder.add(SCORE, 0);
        builder.add(LEFT_SHOULDER_PARROT_VARIANT_ID, OptionalInt.empty());
        builder.add(RIGHT_SHOULDER_PARROT_VARIANT_ID, OptionalInt.empty());
    }

    @Override
    public void tick() {
        this.noClip = this.isSpectator();
        if (this.isSpectator() || this.hasVehicle()) {
            this.setOnGround(false);
        }
        if (this.experiencePickUpDelay > 0) {
            --this.experiencePickUpDelay;
        }
        if (this.isSleeping()) {
            ++this.sleepTimer;
            if (this.sleepTimer > 100) {
                this.sleepTimer = 100;
            }
            if (!this.getEntityWorld().isClient() && !this.getEntityWorld().getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.BED_RULE_GAMEPLAY, this.getEntityPos()).canSleep(this.getEntityWorld())) {
                this.wakeUp(false, true);
            }
        } else if (this.sleepTimer > 0) {
            ++this.sleepTimer;
            if (this.sleepTimer >= 110) {
                this.sleepTimer = 0;
            }
        }
        this.updateWaterSubmersionState();
        super.tick();
        int i = 29999999;
        double d = MathHelper.clamp(this.getX(), -2.9999999E7, 2.9999999E7);
        double e = MathHelper.clamp(this.getZ(), -2.9999999E7, 2.9999999E7);
        if (d != this.getX() || e != this.getZ()) {
            this.setPosition(d, this.getY(), e);
        }
        ++this.ticksSinceLastAttack;
        ++this.ticksSinceHandEquipping;
        ItemStack itemStack = this.getMainHandStack();
        if (!ItemStack.areEqual(this.selectedItem, itemStack)) {
            if (!ItemStack.areItemsEqual(this.selectedItem, itemStack)) {
                this.resetTicksSince();
            }
            this.selectedItem = itemStack.copy();
        }
        if (!this.isSubmergedIn(FluidTags.WATER) && this.isEquipped(Items.TURTLE_HELMET)) {
            this.updateTurtleHelmet();
        }
        this.itemCooldownManager.update();
        this.updatePose();
        if (this.currentExplosionResetGraceTime > 0) {
            --this.currentExplosionResetGraceTime;
        }
    }

    @Override
    protected float getMaxRelativeHeadRotation() {
        if (this.isBlocking()) {
            return 15.0f;
        }
        return super.getMaxRelativeHeadRotation();
    }

    public boolean shouldCancelInteraction() {
        return this.isSneaking();
    }

    protected boolean shouldDismount() {
        return this.isSneaking();
    }

    protected boolean clipAtLedge() {
        return this.isSneaking();
    }

    protected boolean updateWaterSubmersionState() {
        this.isSubmergedInWater = this.isSubmergedIn(FluidTags.WATER);
        return this.isSubmergedInWater;
    }

    @Override
    public void onBubbleColumnSurfaceCollision(boolean drag, BlockPos pos) {
        if (!this.getAbilities().flying) {
            super.onBubbleColumnSurfaceCollision(drag, pos);
        }
    }

    @Override
    public void onBubbleColumnCollision(boolean drag) {
        if (!this.getAbilities().flying) {
            super.onBubbleColumnCollision(drag);
        }
    }

    private void updateTurtleHelmet() {
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 200, 0, false, false, true));
    }

    private boolean isEquipped(Item item) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            EquippableComponent equippableComponent = itemStack.get(DataComponentTypes.EQUIPPABLE);
            if (!itemStack.isOf(item) || equippableComponent == null || equippableComponent.slot() != equipmentSlot) continue;
            return true;
        }
        return false;
    }

    protected ItemCooldownManager createCooldownManager() {
        return new ItemCooldownManager();
    }

    protected void updatePose() {
        if (!this.canChangeIntoPose(EntityPose.SWIMMING)) {
            return;
        }
        EntityPose entityPose = this.getExpectedPose();
        EntityPose entityPose2 = this.isSpectator() || this.hasVehicle() || this.canChangeIntoPose(entityPose) ? entityPose : (this.canChangeIntoPose(EntityPose.CROUCHING) ? EntityPose.CROUCHING : EntityPose.SWIMMING);
        this.setPose(entityPose2);
    }

    private EntityPose getExpectedPose() {
        if (this.isSleeping()) {
            return EntityPose.SLEEPING;
        }
        if (this.isSwimming()) {
            return EntityPose.SWIMMING;
        }
        if (this.isGliding()) {
            return EntityPose.GLIDING;
        }
        if (this.isUsingRiptide()) {
            return EntityPose.SPIN_ATTACK;
        }
        if (this.isSneaking() && !this.abilities.flying) {
            return EntityPose.CROUCHING;
        }
        return EntityPose.STANDING;
    }

    protected boolean canChangeIntoPose(EntityPose pose) {
        return this.getEntityWorld().isSpaceEmpty(this, this.getDimensions(pose).getBoxAt(this.getEntityPos()).contract(1.0E-7));
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_PLAYER_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_PLAYER_SPLASH;
    }

    @Override
    protected SoundEvent getHighSpeedSplashSound() {
        return SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED;
    }

    @Override
    public int getDefaultPortalCooldown() {
        return 10;
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        this.getEntityWorld().playSound((Entity)this, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }

    @Override
    protected int getBurningDuration() {
        return 20;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 9) {
            this.consumeItem();
        } else if (status == 23) {
            this.setReducedDebugInfo(false);
        } else if (status == 22) {
            this.setReducedDebugInfo(true);
        } else {
            super.handleStatus(status);
        }
    }

    protected void closeHandledScreen() {
        this.currentScreenHandler = this.playerScreenHandler;
    }

    protected void onHandledScreenClosed() {
    }

    @Override
    public void tickRiding() {
        if (!this.getEntityWorld().isClient() && this.shouldDismount() && this.hasVehicle()) {
            this.stopRiding();
            this.setSneaking(false);
            return;
        }
        super.tickRiding();
    }

    @Override
    public void tickMovement() {
        if (this.abilityResyncCountdown > 0) {
            --this.abilityResyncCountdown;
        }
        this.tickHunger();
        this.inventory.updateItems();
        if (this.abilities.flying && !this.hasVehicle()) {
            this.onLanding();
        }
        super.tickMovement();
        this.tickHandSwing();
        this.headYaw = this.getYaw();
        this.setMovementSpeed((float)this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED));
        if (this.getHealth() > 0.0f && !this.isSpectator()) {
            Box box = this.hasVehicle() && !this.getVehicle().isRemoved() ? this.getBoundingBox().union(this.getVehicle().getBoundingBox()).expand(1.0, 0.0, 1.0) : this.getBoundingBox().expand(1.0, 0.5, 1.0);
            List<Entity> list = this.getEntityWorld().getOtherEntities(this, box);
            ArrayList list2 = Lists.newArrayList();
            for (Entity entity : list) {
                if (entity.getType() == EntityType.EXPERIENCE_ORB) {
                    list2.add(entity);
                    continue;
                }
                if (entity.isRemoved()) continue;
                this.collideWithEntity(entity);
            }
            if (!list2.isEmpty()) {
                this.collideWithEntity((Entity)Util.getRandom(list2, this.random));
            }
        }
        this.handleShoulderEntities();
    }

    protected void tickHunger() {
    }

    public void handleShoulderEntities() {
    }

    protected void dropShoulderEntities() {
    }

    private void collideWithEntity(Entity entity) {
        entity.onPlayerCollision(this);
    }

    public int getScore() {
        return this.dataTracker.get(SCORE);
    }

    public void setScore(int score) {
        this.dataTracker.set(SCORE, score);
    }

    public void addScore(int score) {
        int i = this.getScore();
        this.dataTracker.set(SCORE, i + score);
    }

    public void useRiptide(int riptideTicks, float riptideAttackDamage, ItemStack stack) {
        this.riptideTicks = riptideTicks;
        this.riptideAttackDamage = riptideAttackDamage;
        this.riptideStack = stack;
        if (!this.getEntityWorld().isClient()) {
            this.dropShoulderEntities();
            this.setLivingFlag(4, true);
        }
    }

    @Override
    public ItemStack getWeaponStack() {
        if (this.isUsingRiptide() && this.riptideStack != null) {
            return this.riptideStack;
        }
        return super.getWeaponStack();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        World world;
        super.onDeath(damageSource);
        this.refreshPosition();
        if (!this.isSpectator() && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.drop(serverWorld, damageSource);
        }
        if (damageSource != null) {
            this.setVelocity(-MathHelper.cos((this.getDamageTiltYaw() + this.getYaw()) * ((float)Math.PI / 180)) * 0.1f, 0.1f, -MathHelper.sin((this.getDamageTiltYaw() + this.getYaw()) * ((float)Math.PI / 180)) * 0.1f);
        } else {
            this.setVelocity(0.0, 0.1, 0.0);
        }
        this.incrementStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        this.extinguish();
        this.setOnFire(false);
        this.setLastDeathPos(Optional.of(GlobalPos.create(this.getEntityWorld().getRegistryKey(), this.getBlockPos())));
    }

    @Override
    protected void dropInventory(ServerWorld world) {
        super.dropInventory(world);
        if (!world.getGameRules().getValue(GameRules.KEEP_INVENTORY).booleanValue()) {
            this.vanishCursedItems();
            this.inventory.dropAll();
        }
    }

    protected void vanishCursedItems() {
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack itemStack = this.inventory.getStack(i);
            if (itemStack.isEmpty() || !EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) continue;
            this.inventory.removeStack(i);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return source.getType().effects().getSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    public void dropCreativeStack(ItemStack stack) {
    }

    public @Nullable ItemEntity dropItem(ItemStack stack, boolean retainOwnership) {
        return this.dropItem(stack, false, retainOwnership);
    }

    public float getBlockBreakingSpeed(BlockState block) {
        float f = this.inventory.getSelectedStack().getMiningSpeedMultiplier(block);
        if (f > 1.0f) {
            f += (float)this.getAttributeValue(EntityAttributes.MINING_EFFICIENCY);
        }
        if (StatusEffectUtil.hasHaste(this)) {
            f *= 1.0f + (float)(StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2f;
        }
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float g = switch (this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            };
            f *= g;
        }
        f *= (float)this.getAttributeValue(EntityAttributes.BLOCK_BREAK_SPEED);
        if (this.isSubmergedIn(FluidTags.WATER)) {
            f *= (float)this.getAttributeInstance(EntityAttributes.SUBMERGED_MINING_SPEED).getValue();
        }
        if (!this.isOnGround()) {
            f /= 5.0f;
        }
        return f;
    }

    public boolean canHarvest(BlockState state) {
        return !state.isToolRequired() || this.inventory.getSelectedStack().isSuitableFor(state);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setUuid(this.gameProfile.id());
        this.inventory.readData(view.getTypedListView("Inventory", StackWithSlot.CODEC));
        this.inventory.setSelectedSlot(view.getInt("SelectedItemSlot", 0));
        this.sleepTimer = view.getShort("SleepTimer", (short)0);
        this.experienceProgress = view.getFloat("XpP", 0.0f);
        this.experienceLevel = view.getInt("XpLevel", 0);
        this.totalExperience = view.getInt("XpTotal", 0);
        this.enchantingTableSeed = view.getInt("XpSeed", 0);
        if (this.enchantingTableSeed == 0) {
            this.enchantingTableSeed = this.random.nextInt();
        }
        this.setScore(view.getInt("Score", 0));
        this.hungerManager.readData(view);
        view.read("abilities", PlayerAbilities.Packed.CODEC).ifPresent(this.abilities::unpack);
        this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(this.abilities.getWalkSpeed());
        this.enderChestInventory.readData(view.getTypedListView("EnderItems", StackWithSlot.CODEC));
        this.setLastDeathPos(view.read("LastDeathLocation", GlobalPos.CODEC));
        this.currentExplosionImpactPos = view.read("current_explosion_impact_pos", Vec3d.CODEC).orElse(null);
        this.ignoreFallDamageFromCurrentExplosion = view.getBoolean("ignore_fall_damage_from_current_explosion", false);
        this.currentExplosionResetGraceTime = view.getInt("current_impulse_context_reset_grace_time", 0);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        NbtHelper.writeDataVersion(view);
        this.inventory.writeData(view.getListAppender("Inventory", StackWithSlot.CODEC));
        view.putInt("SelectedItemSlot", this.inventory.getSelectedSlot());
        view.putShort("SleepTimer", (short)this.sleepTimer);
        view.putFloat("XpP", this.experienceProgress);
        view.putInt("XpLevel", this.experienceLevel);
        view.putInt("XpTotal", this.totalExperience);
        view.putInt("XpSeed", this.enchantingTableSeed);
        view.putInt("Score", this.getScore());
        this.hungerManager.writeData(view);
        view.put("abilities", PlayerAbilities.Packed.CODEC, this.abilities.pack());
        this.enderChestInventory.writeData(view.getListAppender("EnderItems", StackWithSlot.CODEC));
        this.lastDeathPos.ifPresent(pos -> view.put("LastDeathLocation", GlobalPos.CODEC, pos));
        view.putNullable("current_explosion_impact_pos", Vec3d.CODEC, this.currentExplosionImpactPos);
        view.putBoolean("ignore_fall_damage_from_current_explosion", this.ignoreFallDamageFromCurrentExplosion);
        view.putInt("current_impulse_context_reset_grace_time", this.currentExplosionResetGraceTime);
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        if (super.isInvulnerableTo(world, source)) {
            return true;
        }
        if (source.isIn(DamageTypeTags.IS_DROWNING)) {
            return world.getGameRules().getValue(GameRules.DROWNING_DAMAGE) == false;
        }
        if (source.isIn(DamageTypeTags.IS_FALL)) {
            return world.getGameRules().getValue(GameRules.FALL_DAMAGE) == false;
        }
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            return world.getGameRules().getValue(GameRules.FIRE_DAMAGE) == false;
        }
        if (source.isIn(DamageTypeTags.IS_FREEZING)) {
            return world.getGameRules().getValue(GameRules.FREEZE_DAMAGE) == false;
        }
        return false;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        if (this.abilities.invulnerable && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        this.despawnCounter = 0;
        if (this.isDead()) {
            return false;
        }
        this.dropShoulderEntities();
        if (source.isScaledWithDifficulty()) {
            if (world.getDifficulty() == Difficulty.PEACEFUL) {
                amount = 0.0f;
            }
            if (world.getDifficulty() == Difficulty.EASY) {
                amount = Math.min(amount / 2.0f + 1.0f, amount);
            }
            if (world.getDifficulty() == Difficulty.HARD) {
                amount = amount * 3.0f / 2.0f;
            }
        }
        if (amount == 0.0f) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    protected void takeShieldHit(ServerWorld world, LivingEntity attacker) {
        super.takeShieldHit(world, attacker);
        ItemStack itemStack = this.getBlockingItem();
        BlocksAttacksComponent blocksAttacksComponent = itemStack != null ? itemStack.get(DataComponentTypes.BLOCKS_ATTACKS) : null;
        float f = attacker.getWeaponDisableBlockingForSeconds();
        if (f > 0.0f && blocksAttacksComponent != null) {
            blocksAttacksComponent.applyShieldCooldown(world, this, f, itemStack);
        }
    }

    @Override
    public boolean canTakeDamage() {
        return !this.getAbilities().invulnerable && super.canTakeDamage();
    }

    public boolean shouldDamagePlayer(PlayerEntity player) {
        Team abstractTeam = this.getScoreboardTeam();
        Team abstractTeam2 = player.getScoreboardTeam();
        if (abstractTeam == null) {
            return true;
        }
        if (!abstractTeam.isEqual(abstractTeam2)) {
            return true;
        }
        return ((AbstractTeam)abstractTeam).isFriendlyFireAllowed();
    }

    @Override
    protected void damageArmor(DamageSource source, float amount) {
        this.damageEquipment(source, amount, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    }

    @Override
    protected void damageHelmet(DamageSource source, float amount) {
        this.damageEquipment(source, amount, EquipmentSlot.HEAD);
    }

    @Override
    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return;
        }
        amount = this.applyArmorToDamage(source, amount);
        float f = amount = this.modifyAppliedDamage(source, amount);
        amount = Math.max(amount - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - amount));
        float g = f - amount;
        if (g > 0.0f && g < 3.4028235E37f) {
            this.increaseStat(Stats.DAMAGE_ABSORBED, Math.round(g * 10.0f));
        }
        if (amount == 0.0f) {
            return;
        }
        this.addExhaustion(source.getExhaustion());
        this.getDamageTracker().onDamage(source, amount);
        this.setHealth(this.getHealth() - amount);
        if (amount < 3.4028235E37f) {
            this.increaseStat(Stats.DAMAGE_TAKEN, Math.round(amount * 10.0f));
        }
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public boolean shouldFilterText() {
        return false;
    }

    public void openEditSignScreen(SignBlockEntity sign, boolean front) {
    }

    public void openCommandBlockMinecartScreen(CommandBlockMinecartEntity minecart) {
    }

    public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
    }

    public void openStructureBlockScreen(StructureBlockBlockEntity structureBlock) {
    }

    public void openTestBlockScreen(TestBlockEntity testBlock) {
    }

    public void openTestInstanceBlockScreen(TestInstanceBlockEntity testInstanceBlock) {
    }

    public void openJigsawScreen(JigsawBlockEntity jigsaw) {
    }

    public void openHorseInventory(AbstractHorseEntity horse, Inventory inventory) {
    }

    public void openNautilusInventory(AbstractNautilusEntity nautilus, Inventory inventory) {
    }

    public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory) {
        return OptionalInt.empty();
    }

    public void openDialog(RegistryEntry<Dialog> dialog) {
    }

    public void sendTradeOffers(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable) {
    }

    public void useBook(ItemStack book, Hand hand) {
    }

    public ActionResult interact(Entity entity, Hand hand) {
        if (this.isSpectator()) {
            if (entity instanceof NamedScreenHandlerFactory) {
                this.openHandledScreen((NamedScreenHandlerFactory)((Object)entity));
            }
            return ActionResult.PASS;
        }
        ItemStack itemStack = this.getStackInHand(hand);
        ItemStack itemStack2 = itemStack.copy();
        ActionResult actionResult = entity.interact(this, hand);
        if (actionResult.isAccepted()) {
            if (this.isInCreativeMode() && itemStack == this.getStackInHand(hand) && itemStack.getCount() < itemStack2.getCount()) {
                itemStack.setCount(itemStack2.getCount());
            }
            return actionResult;
        }
        if (!itemStack.isEmpty() && entity instanceof LivingEntity) {
            ActionResult actionResult2;
            if (this.isInCreativeMode()) {
                itemStack = itemStack2;
            }
            if ((actionResult2 = itemStack.useOnEntity(this, (LivingEntity)entity, hand)).isAccepted()) {
                this.getEntityWorld().emitGameEvent(GameEvent.ENTITY_INTERACT, entity.getEntityPos(), GameEvent.Emitter.of(this));
                if (itemStack.isEmpty() && !this.isInCreativeMode()) {
                    this.setStackInHand(hand, ItemStack.EMPTY);
                }
                return actionResult2;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void dismountVehicle() {
        super.dismountVehicle();
        this.ridingCooldown = 0;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isSleeping();
    }

    @Override
    public boolean shouldSwimInFluids() {
        return !this.abilities.flying;
    }

    @Override
    protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
        double d;
        float f = this.getStepHeight();
        if (this.abilities.flying || movement.y > 0.0 || type != MovementType.SELF && type != MovementType.PLAYER || !this.clipAtLedge() || !this.isStandingOnSurface(f)) {
            return movement;
        }
        double e = movement.z;
        double g = 0.05;
        double h = Math.signum(d) * 0.05;
        double i = Math.signum(e) * 0.05;
        for (d = movement.x; d != 0.0 && this.isSpaceAroundPlayerEmpty(d, 0.0, f); d -= h) {
            if (!(Math.abs(d) <= 0.05)) continue;
            d = 0.0;
            break;
        }
        while (e != 0.0 && this.isSpaceAroundPlayerEmpty(0.0, e, f)) {
            if (Math.abs(e) <= 0.05) {
                e = 0.0;
                break;
            }
            e -= i;
        }
        while (d != 0.0 && e != 0.0 && this.isSpaceAroundPlayerEmpty(d, e, f)) {
            d = Math.abs(d) <= 0.05 ? 0.0 : (d -= h);
            if (Math.abs(e) <= 0.05) {
                e = 0.0;
                continue;
            }
            e -= i;
        }
        return new Vec3d(d, movement.y, e);
    }

    private boolean isStandingOnSurface(float stepHeight) {
        return this.isOnGround() || this.fallDistance < (double)stepHeight && !this.isSpaceAroundPlayerEmpty(0.0, 0.0, (double)stepHeight - this.fallDistance);
    }

    private boolean isSpaceAroundPlayerEmpty(double offsetX, double offsetZ, double stepHeight) {
        Box box = this.getBoundingBox();
        return this.getEntityWorld().isSpaceEmpty(this, new Box(box.minX + 1.0E-7 + offsetX, box.minY - stepHeight - 1.0E-7, box.minZ + 1.0E-7 + offsetZ, box.maxX - 1.0E-7 + offsetX, box.minY, box.maxZ - 1.0E-7 + offsetZ));
    }

    public void attack(Entity target) {
        if (this.cannotAttack(target)) {
            return;
        }
        float f = this.isUsingRiptide() ? this.riptideAttackDamage : (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        ItemStack itemStack = this.getWeaponStack();
        DamageSource damageSource = this.getDamageSource(itemStack);
        float g = this.getAttackCooldownProgress(0.5f);
        float h = g * (this.getDamageAgainst(target, f, damageSource) - f);
        f *= this.getAttackCooldownDamageModifier();
        this.beforePlayerAttack();
        if (this.tryDeflect(target)) {
            return;
        }
        if (f > 0.0f || h > 0.0f) {
            boolean bl3;
            boolean bl2;
            boolean bl;
            boolean bl4 = bl = g > 0.9f;
            if (this.isSprinting() && bl) {
                this.playAttackSound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK);
                bl2 = true;
            } else {
                bl2 = false;
            }
            f += itemStack.getItem().getBonusAttackDamage(target, f, damageSource);
            boolean bl5 = bl3 = bl && this.isCriticalHit(target);
            if (bl3) {
                f *= 1.5f;
            }
            float i = f + h;
            boolean bl42 = this.canUseSweepAttack(bl, bl3, bl2);
            float j = 0.0f;
            if (target instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)target;
                j = livingEntity.getHealth();
            }
            Vec3d vec3d = target.getVelocity();
            boolean bl52 = target.sidedDamage(damageSource, i);
            if (bl52) {
                this.knockbackTarget(target, this.getAttackKnockbackAgainst(target, damageSource) + (bl2 ? 0.5f : 0.0f), vec3d);
                if (bl42) {
                    this.doSweepingAttack(target, f, damageSource, g);
                }
                this.addAttackParticlesAndSounds(target, bl3, bl42, bl, false, h);
                this.onAttacking(target);
                this.onTargetDamaged(target, itemStack, damageSource, true);
                this.handleAttackDamage(target, j);
                this.addExhaustion(0.1f);
            } else {
                this.playAttackSound(SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE);
            }
        }
        this.useAttackEnchantmentEffects();
    }

    private void playAttackSound(SoundEvent sound) {
        this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), 1.0f, 1.0f);
    }

    private DamageSource getDamageSource(ItemStack stack) {
        return stack.getDamageSource(this, () -> this.getDamageSources().playerAttack(this));
    }

    private boolean cannotAttack(Entity target) {
        if (!target.isAttackable()) {
            return true;
        }
        return target.handleAttack(this);
    }

    private boolean tryDeflect(Entity entity) {
        ProjectileEntity projectileEntity;
        if (entity.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof ProjectileEntity && (projectileEntity = (ProjectileEntity)entity).deflect(ProjectileDeflection.REDIRECTED, this, LazyEntityReference.of(this), true)) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory());
            return true;
        }
        return false;
    }

    private boolean isCriticalHit(Entity target) {
        return this.fallDistance > 0.0 && !this.isOnGround() && !this.isClimbing() && !this.isTouchingWater() && !this.hasBlindnessEffect() && !this.hasVehicle() && target instanceof LivingEntity && !this.isSprinting();
    }

    private boolean canUseSweepAttack(boolean cooldownPassed, boolean criticalHit, boolean knockback) {
        double e;
        double d;
        if (cooldownPassed && !criticalHit && !knockback && this.isOnGround() && (d = this.getMovement().horizontalLengthSquared()) < MathHelper.square(e = (double)this.getMovementSpeed() * 2.5)) {
            return this.getStackInHand(Hand.MAIN_HAND).isIn(ItemTags.SWORDS);
        }
        return false;
    }

    private void addAttackParticlesAndSounds(Entity target, boolean criticalHit, boolean sweeping, boolean cooldownPassed, boolean pierce, float enchantDamage) {
        if (criticalHit) {
            this.playAttackSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT);
            this.addCritParticles(target);
        }
        if (!(criticalHit || sweeping || pierce)) {
            this.playAttackSound(cooldownPassed ? SoundEvents.ENTITY_PLAYER_ATTACK_STRONG : SoundEvents.ENTITY_PLAYER_ATTACK_WEAK);
        }
        if (enchantDamage > 0.0f) {
            this.addEnchantedHitParticles(target);
        }
    }

    private void handleAttackDamage(Entity target, float healthBeforeAttack) {
        if (target instanceof LivingEntity) {
            float f = healthBeforeAttack - ((LivingEntity)target).getHealth();
            this.increaseStat(Stats.DAMAGE_DEALT, Math.round(f * 10.0f));
            if (this.getEntityWorld() instanceof ServerWorld && f > 2.0f) {
                int i = (int)((double)f * 0.5);
                ((ServerWorld)this.getEntityWorld()).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), i, 0.1, 0.0, 0.1, 0.2);
            }
        }
    }

    private void onTargetDamaged(Entity target, ItemStack stack, DamageSource damageSource, boolean runEnchantmentEffects) {
        Entity entity = target;
        if (target instanceof EnderDragonPart) {
            entity = ((EnderDragonPart)target).owner;
        }
        boolean bl = false;
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                bl = stack.postHit(livingEntity, this);
            }
            if (runEnchantmentEffects) {
                EnchantmentHelper.onTargetDamaged(serverWorld, target, damageSource, stack);
            }
        }
        if (!this.getEntityWorld().isClient() && !stack.isEmpty() && entity instanceof LivingEntity) {
            if (bl) {
                stack.postDamageEntity((LivingEntity)entity, this);
            }
            if (stack.isEmpty()) {
                if (stack == this.getMainHandStack()) {
                    this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                } else {
                    this.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void knockbackTarget(Entity target, float strength, Vec3d playerTargetVelocity) {
        if (strength > 0.0f) {
            if (target instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)target;
                livingEntity.takeKnockback(strength, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
            } else {
                target.addVelocity(-MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)) * strength, 0.1, MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)) * strength);
            }
            this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            this.setSprinting(false);
        }
        if (target instanceof ServerPlayerEntity && target.knockedBack) {
            ((ServerPlayerEntity)target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
            target.knockedBack = false;
            target.setVelocity(playerTargetVelocity);
        }
    }

    @Override
    public float getSoundPitch() {
        return 1.0f;
    }

    private void doSweepingAttack(Entity target, float damage, DamageSource damageSource, float cooldownProgress) {
        this.playAttackSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP);
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        float f = 1.0f + (float)this.getAttributeValue(EntityAttributes.SWEEPING_DAMAGE_RATIO) * damage;
        List<LivingEntity> list = this.getEntityWorld().getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0));
        for (LivingEntity livingEntity : list) {
            float g;
            ArmorStandEntity armorStandEntity;
            if (livingEntity == this || livingEntity == target || this.isTeammate(livingEntity) || livingEntity instanceof ArmorStandEntity && (armorStandEntity = (ArmorStandEntity)livingEntity).isMarker() || !(this.squaredDistanceTo(livingEntity) < 9.0) || !livingEntity.damage(serverWorld, damageSource, g = this.getDamageAgainst(livingEntity, f, damageSource) * cooldownProgress)) continue;
            livingEntity.takeKnockback(0.4f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
            EnchantmentHelper.onTargetDamaged(serverWorld, livingEntity, damageSource);
        }
        double d = -MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
        double e = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
        serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getBodyY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
    }

    protected float getDamageAgainst(Entity target, float baseDamage, DamageSource damageSource) {
        return baseDamage;
    }

    @Override
    protected void attackLivingEntity(LivingEntity target) {
        this.attack(target);
    }

    public void addCritParticles(Entity target) {
    }

    private float getAttackCooldownDamageModifier() {
        float f = this.getAttackCooldownProgress(0.5f);
        return 0.2f + f * f * 0.8f;
    }

    @Override
    public boolean pierce(EquipmentSlot slot, Entity target, float damage, boolean dealDamage, boolean knockback, boolean dismount) {
        boolean bl;
        if (this.cannotAttack(target)) {
            return false;
        }
        ItemStack itemStack = this.getEquippedStack(slot);
        DamageSource damageSource = this.getDamageSource(itemStack);
        float f = this.getDamageAgainst(target, damage, damageSource) - damage;
        if (!this.isUsingItem() || this.getActiveHand().getEquipmentSlot() != slot) {
            f *= this.getAttackCooldownProgress(0.5f);
            damage *= this.getAttackCooldownDamageModifier();
        }
        if (knockback && this.tryDeflect(target)) {
            return true;
        }
        float g = dealDamage ? damage + f : 0.0f;
        float h = 0.0f;
        if (target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)target;
            h = livingEntity.getHealth();
        }
        Vec3d vec3d = target.getVelocity();
        boolean bl2 = bl = dealDamage && target.sidedDamage(damageSource, g);
        if (knockback) {
            this.knockbackTarget(target, 0.4f + this.getAttackKnockbackAgainst(target, damageSource), vec3d);
        }
        boolean bl22 = false;
        if (dismount && target.hasVehicle()) {
            bl22 = true;
            target.stopRiding();
        }
        if (!(bl || knockback || bl22)) {
            return false;
        }
        this.addAttackParticlesAndSounds(target, false, false, dealDamage, true, f);
        this.onAttacking(target);
        this.onTargetDamaged(target, itemStack, damageSource, bl);
        this.handleAttackDamage(target, h);
        this.addExhaustion(0.1f);
        return true;
    }

    public void addEnchantedHitParticles(Entity target) {
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        this.playerScreenHandler.onClosed(this);
        if (this.shouldCloseHandledScreenOnRespawn()) {
            this.onHandledScreenClosed();
        }
    }

    @Override
    public boolean isControlledByPlayer() {
        return true;
    }

    @Override
    protected boolean isControlledByMainPlayer() {
        return this.isMainPlayer();
    }

    public boolean isMainPlayer() {
        return false;
    }

    @Override
    public boolean canMoveVoluntarily() {
        return !this.getEntityWorld().isClient() || this.isMainPlayer();
    }

    @Override
    public boolean canActVoluntarily() {
        return !this.getEntityWorld().isClient() || this.isMainPlayer();
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public PlayerConfigEntry getPlayerConfigEntry() {
        return new PlayerConfigEntry(this.gameProfile);
    }

    public PlayerInventory getInventory() {
        return this.inventory;
    }

    public PlayerAbilities getAbilities() {
        return this.abilities;
    }

    @Override
    public boolean isInCreativeMode() {
        return this.abilities.creativeMode;
    }

    public boolean shouldSkipBlockDrops() {
        return this.abilities.creativeMode;
    }

    public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
    }

    public boolean shouldCloseHandledScreenOnRespawn() {
        return this.currentScreenHandler != this.playerScreenHandler;
    }

    public boolean canDropItems() {
        return true;
    }

    public Either<SleepFailureReason, Unit> trySleep(BlockPos pos) {
        this.sleep(pos);
        this.sleepTimer = 0;
        return Either.right((Object)((Object)Unit.INSTANCE));
    }

    public void wakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers) {
        super.wakeUp();
        if (this.getEntityWorld() instanceof ServerWorld && updateSleepingPlayers) {
            ((ServerWorld)this.getEntityWorld()).updateSleepingPlayers();
        }
        this.sleepTimer = skipSleepTimer ? 0 : 100;
    }

    @Override
    public void wakeUp() {
        this.wakeUp(true, true);
    }

    public boolean canResetTimeBySleeping() {
        return this.isSleeping() && this.sleepTimer >= 100;
    }

    public int getSleepTimer() {
        return this.sleepTimer;
    }

    public void sendMessage(Text message, boolean overlay) {
    }

    public void incrementStat(Identifier stat) {
        this.incrementStat(Stats.CUSTOM.getOrCreateStat(stat));
    }

    public void increaseStat(Identifier stat, int amount) {
        this.increaseStat(Stats.CUSTOM.getOrCreateStat(stat), amount);
    }

    public void incrementStat(Stat<?> stat) {
        this.increaseStat(stat, 1);
    }

    public void increaseStat(Stat<?> stat, int amount) {
    }

    public void resetStat(Stat<?> stat) {
    }

    public int unlockRecipes(Collection<RecipeEntry<?>> recipes) {
        return 0;
    }

    public void onRecipeCrafted(RecipeEntry<?> recipe, List<ItemStack> ingredients) {
    }

    public void unlockRecipes(List<RegistryKey<Recipe<?>>> recipes) {
    }

    public int lockRecipes(Collection<RecipeEntry<?>> recipes) {
        return 0;
    }

    @Override
    public void travel(Vec3d movementInput) {
        double d;
        if (this.hasVehicle()) {
            super.travel(movementInput);
            return;
        }
        if (this.isSwimming()) {
            double e;
            d = this.getRotationVector().y;
            double d2 = e = d < -0.2 ? 0.085 : 0.06;
            if (d <= 0.0 || this.jumping || !this.getEntityWorld().getFluidState(BlockPos.ofFloored(this.getX(), this.getY() + 1.0 - 0.1, this.getZ())).isEmpty()) {
                Vec3d vec3d = this.getVelocity();
                this.setVelocity(vec3d.add(0.0, (d - vec3d.y) * e, 0.0));
            }
        }
        if (this.getAbilities().flying) {
            d = this.getVelocity().y;
            super.travel(movementInput);
            this.setVelocity(this.getVelocity().withAxis(Direction.Axis.Y, d * 0.6));
        } else {
            super.travel(movementInput);
        }
    }

    @Override
    protected boolean canGlide() {
        return !this.abilities.flying && super.canGlide();
    }

    @Override
    public void updateSwimming() {
        if (this.abilities.flying) {
            this.setSwimming(false);
        } else {
            super.updateSwimming();
        }
    }

    protected boolean doesNotSuffocate(BlockPos pos) {
        return !this.getEntityWorld().getBlockState(pos).shouldSuffocate(this.getEntityWorld(), pos);
    }

    @Override
    public float getMovementSpeed() {
        return (float)this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        double d;
        boolean bl;
        if (this.abilities.allowFlying) {
            return false;
        }
        if (fallDistance >= 2.0) {
            this.increaseStat(Stats.FALL_ONE_CM, (int)Math.round(fallDistance * 100.0));
        }
        boolean bl2 = bl = this.currentExplosionImpactPos != null && this.ignoreFallDamageFromCurrentExplosion;
        if (bl) {
            boolean bl22;
            d = Math.min(fallDistance, this.currentExplosionImpactPos.y - this.getY());
            boolean bl3 = bl22 = d <= 0.0;
            if (bl22) {
                this.clearCurrentExplosion();
            } else {
                this.tryClearCurrentExplosion();
            }
        } else {
            d = fallDistance;
        }
        if (d > 0.0 && super.handleFallDamage(d, damagePerDistance, damageSource)) {
            this.clearCurrentExplosion();
            return true;
        }
        this.handleFallDamageForPassengers(fallDistance, damagePerDistance, damageSource);
        return false;
    }

    public boolean checkGliding() {
        if (!this.isGliding() && this.canGlide() && !this.isTouchingWater()) {
            this.startGliding();
            return true;
        }
        return false;
    }

    public void startGliding() {
        this.setFlag(7, true);
    }

    @Override
    protected void onSwimmingStart() {
        if (!this.isSpectator()) {
            super.onSwimmingStart();
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isTouchingWater()) {
            this.playSwimSound();
            this.playSecondaryStepSound(state);
        } else {
            BlockPos blockPos = this.getStepSoundPos(pos);
            if (!pos.equals(blockPos)) {
                BlockState blockState = this.getEntityWorld().getBlockState(blockPos);
                if (blockState.isIn(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
                    this.playCombinationStepSounds(blockState, state);
                } else {
                    super.playStepSound(blockPos, blockState);
                }
            } else {
                super.playStepSound(pos, state);
            }
        }
    }

    @Override
    public LivingEntity.FallSounds getFallSounds() {
        return new LivingEntity.FallSounds(SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundEvents.ENTITY_PLAYER_BIG_FALL);
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other, DamageSource damageSource) {
        this.incrementStat(Stats.KILLED.getOrCreateStat(other.getType()));
        return true;
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
        if (!this.abilities.flying) {
            super.slowMovement(state, multiplier);
        }
        this.tryClearCurrentExplosion();
    }

    public void addExperience(int experience) {
        this.addScore(experience);
        this.experienceProgress += (float)experience / (float)this.getNextLevelExperience();
        this.totalExperience = MathHelper.clamp(this.totalExperience + experience, 0, Integer.MAX_VALUE);
        while (this.experienceProgress < 0.0f) {
            float f = this.experienceProgress * (float)this.getNextLevelExperience();
            if (this.experienceLevel > 0) {
                this.addExperienceLevels(-1);
                this.experienceProgress = 1.0f + f / (float)this.getNextLevelExperience();
                continue;
            }
            this.addExperienceLevels(-1);
            this.experienceProgress = 0.0f;
        }
        while (this.experienceProgress >= 1.0f) {
            this.experienceProgress = (this.experienceProgress - 1.0f) * (float)this.getNextLevelExperience();
            this.addExperienceLevels(1);
            this.experienceProgress /= (float)this.getNextLevelExperience();
        }
    }

    public int getEnchantingTableSeed() {
        return this.enchantingTableSeed;
    }

    public void applyEnchantmentCosts(ItemStack enchantedItem, int experienceLevels) {
        this.experienceLevel -= experienceLevels;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        this.enchantingTableSeed = this.random.nextInt();
    }

    public void addExperienceLevels(int levels) {
        this.experienceLevel = IntMath.saturatedAdd((int)this.experienceLevel, (int)levels);
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        if (levels > 0 && this.experienceLevel % 5 == 0 && (float)this.lastPlayedLevelUpSoundTime < (float)this.age - 100.0f) {
            float f = this.experienceLevel > 30 ? 1.0f : (float)this.experienceLevel / 30.0f;
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundCategory(), f * 0.75f, 1.0f);
            this.lastPlayedLevelUpSoundTime = this.age;
        }
    }

    public int getNextLevelExperience() {
        if (this.experienceLevel >= 30) {
            return 112 + (this.experienceLevel - 30) * 9;
        }
        if (this.experienceLevel >= 15) {
            return 37 + (this.experienceLevel - 15) * 5;
        }
        return 7 + this.experienceLevel * 2;
    }

    public void addExhaustion(float exhaustion) {
        if (this.abilities.invulnerable) {
            return;
        }
        if (!this.getEntityWorld().isClient()) {
            this.hungerManager.addExhaustion(exhaustion);
        }
    }

    @Override
    public void useAttackEnchantmentEffects() {
        if (this.canSprintOrFly()) {
            super.useAttackEnchantmentEffects();
        }
    }

    protected boolean canSprintOrFly() {
        return this.getHungerManager().canSprint() || this.getAbilities().allowFlying;
    }

    public Optional<SculkShriekerWarningManager> getSculkShriekerWarningManager() {
        return Optional.empty();
    }

    public HungerManager getHungerManager() {
        return this.hungerManager;
    }

    public boolean canConsume(boolean ignoreHunger) {
        return this.abilities.invulnerable || ignoreHunger || this.hungerManager.isNotFull();
    }

    public boolean canFoodHeal() {
        return this.getHealth() > 0.0f && this.getHealth() < this.getMaxHealth();
    }

    public boolean canModifyBlocks() {
        return this.abilities.allowModifyWorld;
    }

    public boolean canPlaceOn(BlockPos pos, Direction facing, ItemStack stack) {
        if (this.abilities.allowModifyWorld) {
            return true;
        }
        BlockPos blockPos = pos.offset(facing.getOpposite());
        CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(this.getEntityWorld(), blockPos, false);
        return stack.canPlaceOn(cachedBlockPosition);
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        if (world.getGameRules().getValue(GameRules.KEEP_INVENTORY).booleanValue() || this.isSpectator()) {
            return 0;
        }
        return Math.min(this.experienceLevel * 7, 100);
    }

    @Override
    protected boolean shouldAlwaysDropExperience() {
        return true;
    }

    @Override
    public boolean shouldRenderName() {
        return true;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return !this.abilities.flying && (!this.isOnGround() || !this.isSneaky()) ? Entity.MoveEffect.ALL : Entity.MoveEffect.NONE;
    }

    public void sendAbilitiesUpdate() {
    }

    @Override
    public Text getName() {
        return Text.literal(this.gameProfile.name());
    }

    @Override
    public String getStringifiedName() {
        return this.gameProfile.name();
    }

    public EnderChestInventory getEnderChestInventory() {
        return this.enderChestInventory;
    }

    @Override
    protected boolean isArmorSlot(EquipmentSlot slot) {
        return slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    public boolean giveItemStack(ItemStack stack) {
        return this.inventory.insertStack(stack);
    }

    public abstract @Nullable GameMode getGameMode();

    @Override
    public boolean isSpectator() {
        return this.getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return !this.isSpectator() && super.canBeHitByProjectile();
    }

    @Override
    public boolean isSwimming() {
        return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
    }

    public boolean isCreative() {
        return this.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public boolean isPushedByFluids() {
        return !this.abilities.flying;
    }

    @Override
    public Text getDisplayName() {
        MutableText mutableText = Team.decorateName(this.getScoreboardTeam(), this.getName());
        return this.addTellClickEvent(mutableText);
    }

    private MutableText addTellClickEvent(MutableText component) {
        String string = this.getGameProfile().name();
        return component.styled(style -> style.withClickEvent(new ClickEvent.SuggestCommand("/tell " + string + " ")).withHoverEvent(this.getHoverEvent()).withInsertion(string));
    }

    @Override
    public String getNameForScoreboard() {
        return this.getGameProfile().name();
    }

    @Override
    protected void setAbsorptionAmountUnclamped(float absorptionAmount) {
        this.getDataTracker().set(ABSORPTION_AMOUNT, Float.valueOf(absorptionAmount));
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getDataTracker().get(ABSORPTION_AMOUNT).floatValue();
    }

    @Override
    public @Nullable StackReference getStackReference(int slot) {
        if (slot == 499) {
            return new StackReference(){

                @Override
                public ItemStack get() {
                    return PlayerEntity.this.currentScreenHandler.getCursorStack();
                }

                @Override
                public boolean set(ItemStack stack) {
                    PlayerEntity.this.currentScreenHandler.setCursorStack(stack);
                    return true;
                }
            };
        }
        final int i = slot - 500;
        if (i >= 0 && i < 4) {
            return new StackReference(){

                @Override
                public ItemStack get() {
                    return PlayerEntity.this.playerScreenHandler.getCraftingInput().getStack(i);
                }

                @Override
                public boolean set(ItemStack stack) {
                    PlayerEntity.this.playerScreenHandler.getCraftingInput().setStack(i, stack);
                    PlayerEntity.this.playerScreenHandler.onContentChanged(PlayerEntity.this.inventory);
                    return true;
                }
            };
        }
        if (slot >= 0 && slot < this.inventory.getMainStacks().size()) {
            return this.inventory.getStackReference(slot);
        }
        int j = slot - 200;
        if (j >= 0 && j < this.enderChestInventory.size()) {
            return this.enderChestInventory.getStackReference(j);
        }
        return super.getStackReference(slot);
    }

    public boolean hasReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public void setReducedDebugInfo(boolean reducedDebugInfo) {
        this.reducedDebugInfo = reducedDebugInfo;
    }

    @Override
    public void setFireTicks(int fireTicks) {
        super.setFireTicks(this.abilities.invulnerable ? Math.min(fireTicks, 1) : fireTicks);
    }

    protected static Optional<ParrotEntity.Variant> readParrotVariant(NbtCompound nbt) {
        EntityType entityType;
        if (!nbt.isEmpty() && (entityType = (EntityType)nbt.get("id", EntityType.CODEC).orElse(null)) == EntityType.PARROT) {
            return nbt.get("Variant", ParrotEntity.Variant.INDEX_CODEC);
        }
        return Optional.empty();
    }

    protected static OptionalInt mapParrotVariant(Optional<ParrotEntity.Variant> variant2) {
        return variant2.map(variant -> OptionalInt.of(variant.getIndex())).orElse(OptionalInt.empty());
    }

    private static Optional<ParrotEntity.Variant> mapParrotVariantIfPresent(OptionalInt variantIndex) {
        if (variantIndex.isPresent()) {
            return Optional.of(ParrotEntity.Variant.byIndex(variantIndex.getAsInt()));
        }
        return Optional.empty();
    }

    public void setLeftShoulderParrotVariant(Optional<ParrotEntity.Variant> variant) {
        this.dataTracker.set(LEFT_SHOULDER_PARROT_VARIANT_ID, PlayerEntity.mapParrotVariant(variant));
    }

    public Optional<ParrotEntity.Variant> getLeftShoulderParrotVariant() {
        return PlayerEntity.mapParrotVariantIfPresent(this.dataTracker.get(LEFT_SHOULDER_PARROT_VARIANT_ID));
    }

    public void setRightShoulderParrotVariant(Optional<ParrotEntity.Variant> variant) {
        this.dataTracker.set(RIGHT_SHOULDER_PARROT_VARIANT_ID, PlayerEntity.mapParrotVariant(variant));
    }

    public Optional<ParrotEntity.Variant> getRightShoulderParrotVariant() {
        return PlayerEntity.mapParrotVariantIfPresent(this.dataTracker.get(RIGHT_SHOULDER_PARROT_VARIANT_ID));
    }

    public float getAttackCooldownProgressPerTick() {
        return (float)(1.0 / this.getAttributeValue(EntityAttributes.ATTACK_SPEED) * 20.0);
    }

    public boolean isBelowMinimumAttackCharge(ItemStack stack, int baseTime) {
        float f = stack.getOrDefault(DataComponentTypes.MINIMUM_ATTACK_CHARGE, Float.valueOf(0.0f)).floatValue();
        float g = (float)(this.ticksSinceLastAttack + baseTime) / this.getAttackCooldownProgressPerTick();
        return f > 0.0f && g < f;
    }

    public float getAttackCooldownProgress(float baseTime) {
        return MathHelper.clamp(((float)this.ticksSinceLastAttack + baseTime) / this.getAttackCooldownProgressPerTick(), 0.0f, 1.0f);
    }

    public float getHandEquippingProgress(float baseTime) {
        return MathHelper.clamp(((float)this.ticksSinceHandEquipping + baseTime) / this.getAttackCooldownProgressPerTick(), 0.0f, 1.0f);
    }

    public void resetTicksSince() {
        this.ticksSinceLastAttack = 0;
        this.ticksSinceHandEquipping = 0;
    }

    @Override
    public void beforePlayerAttack() {
        this.resetTicksSinceLastAttack();
        super.beforePlayerAttack();
    }

    public void resetTicksSinceLastAttack() {
        this.ticksSinceLastAttack = 0;
    }

    public ItemCooldownManager getItemCooldownManager() {
        return this.itemCooldownManager;
    }

    @Override
    protected float getVelocityMultiplier() {
        return this.abilities.flying || this.isGliding() ? 1.0f : super.getVelocityMultiplier();
    }

    @Override
    public float getLuck() {
        return (float)this.getAttributeValue(EntityAttributes.LUCK);
    }

    public boolean isCreativeLevelTwoOp() {
        return this.abilities.creativeMode && this.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS);
    }

    public PermissionPredicate getPermissions() {
        return PermissionPredicate.NONE;
    }

    @Override
    public ImmutableList<EntityPose> getPoses() {
        return ImmutableList.of((Object)EntityPose.STANDING, (Object)EntityPose.CROUCHING, (Object)EntityPose.SWIMMING);
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        if (!(stack.getItem() instanceof RangedWeaponItem)) {
            return ItemStack.EMPTY;
        }
        Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
        ItemStack itemStack = RangedWeaponItem.getHeldProjectile(this, predicate);
        if (!itemStack.isEmpty()) {
            return itemStack;
        }
        predicate = ((RangedWeaponItem)stack.getItem()).getProjectiles();
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack itemStack2 = this.inventory.getStack(i);
            if (!predicate.test(itemStack2)) continue;
            return itemStack2;
        }
        return this.isInCreativeMode() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    public Vec3d getLeashPos(float tickProgress) {
        double d = 0.22 * (this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0);
        float f = MathHelper.lerp(tickProgress * 0.5f, this.getPitch(), this.lastPitch) * ((float)Math.PI / 180);
        float g = MathHelper.lerp(tickProgress, this.lastBodyYaw, this.bodyYaw) * ((float)Math.PI / 180);
        if (this.isGliding() || this.isUsingRiptide()) {
            float k;
            Vec3d vec3d = this.getRotationVec(tickProgress);
            Vec3d vec3d2 = this.getVelocity();
            double e = vec3d2.horizontalLengthSquared();
            double h = vec3d.horizontalLengthSquared();
            if (e > 0.0 && h > 0.0) {
                double i = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(e * h);
                double j = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                k = (float)(Math.signum(j) * Math.acos(i));
            } else {
                k = 0.0f;
            }
            return this.getLerpedPos(tickProgress).add(new Vec3d(d, -0.11, 0.85).rotateZ(-k).rotateX(-f).rotateY(-g));
        }
        if (this.isInSwimmingPose()) {
            return this.getLerpedPos(tickProgress).add(new Vec3d(d, 0.2, -0.15).rotateX(-f).rotateY(-g));
        }
        double l = this.getBoundingBox().getLengthY() - 1.0;
        double e = this.isInSneakingPose() ? -0.2 : 0.07;
        return this.getLerpedPos(tickProgress).add(new Vec3d(d, l, e).rotateY(-g));
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    public boolean isUsingSpyglass() {
        return this.isUsingItem() && this.getActiveItem().isOf(Items.SPYGLASS);
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    public Optional<GlobalPos> getLastDeathPos() {
        return this.lastDeathPos;
    }

    public void setLastDeathPos(Optional<GlobalPos> lastDeathPos) {
        this.lastDeathPos = lastDeathPos;
    }

    @Override
    public float getDamageTiltYaw() {
        return this.damageTiltYaw;
    }

    @Override
    public void animateDamage(float yaw) {
        super.animateDamage(yaw);
        this.damageTiltYaw = yaw;
    }

    public boolean hasBlindnessEffect() {
        return this.hasStatusEffect(StatusEffects.BLINDNESS);
    }

    @Override
    public boolean canSprintAsVehicle() {
        return true;
    }

    @Override
    protected float getOffGroundSpeed() {
        if (this.abilities.flying && !this.hasVehicle()) {
            return this.isSprinting() ? this.abilities.getFlySpeed() * 2.0f : this.abilities.getFlySpeed();
        }
        return this.isSprinting() ? 0.025999999f : 0.02f;
    }

    @Override
    public boolean isViewingContainerAt(ViewerCountManager viewerCountManager, BlockPos pos) {
        return viewerCountManager.isPlayerViewing(this);
    }

    @Override
    public double getContainerInteractionRange() {
        return this.getBlockInteractionRange();
    }

    public double getBlockInteractionRange() {
        return this.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
    }

    public double getEntityInteractionRange() {
        return this.getAttributeValue(EntityAttributes.ENTITY_INTERACTION_RANGE);
    }

    public boolean canInteractWithEntity(Entity entity, double additionalRange) {
        if (entity.isRemoved()) {
            return false;
        }
        return this.canInteractWithEntityIn(entity.getBoundingBox(), additionalRange);
    }

    public boolean canInteractWithEntityIn(Box box, double additionalRange) {
        double d = this.getEntityInteractionRange() + additionalRange;
        double e = box.squaredMagnitude(this.getEyePos());
        return e < d * d;
    }

    public boolean canAttackEntityIn(Box box, double additionalRange) {
        return this.getAttackRange().isWithinRange((LivingEntity)this, box, additionalRange);
    }

    public boolean canInteractWithBlockAt(BlockPos pos, double additionalRange) {
        double d = this.getBlockInteractionRange() + additionalRange;
        return new Box(pos).squaredMagnitude(this.getEyePos()) < d * d;
    }

    public void setIgnoreFallDamageFromCurrentExplosion(boolean ignoreFallDamageFromCurrentExplosion) {
        this.ignoreFallDamageFromCurrentExplosion = ignoreFallDamageFromCurrentExplosion;
        if (ignoreFallDamageFromCurrentExplosion) {
            this.setCurrentExplosionResetGraceTime(40);
        } else {
            this.currentExplosionResetGraceTime = 0;
        }
    }

    public void setCurrentExplosionResetGraceTime(int currentExplosionResetGraceTime) {
        this.currentExplosionResetGraceTime = Math.max(this.currentExplosionResetGraceTime, currentExplosionResetGraceTime);
    }

    public boolean shouldIgnoreFallDamageFromCurrentExplosion() {
        return this.ignoreFallDamageFromCurrentExplosion;
    }

    public void tryClearCurrentExplosion() {
        if (this.currentExplosionResetGraceTime == 0) {
            this.clearCurrentExplosion();
        }
    }

    public boolean isInCurrentExplosionResetGraceTime() {
        return this.currentExplosionResetGraceTime > 0;
    }

    public void clearCurrentExplosion() {
        this.currentExplosionResetGraceTime = 0;
        this.explodedBy = null;
        this.currentExplosionImpactPos = null;
        this.ignoreFallDamageFromCurrentExplosion = false;
    }

    public boolean shouldRotateWithMinecart() {
        return false;
    }

    @Override
    public boolean isClimbing() {
        if (this.abilities.flying) {
            return false;
        }
        return super.isClimbing();
    }

    public String asString() {
        return MoreObjects.toStringHelper((Object)this).add("name", (Object)this.getStringifiedName()).add("id", this.getId()).add("pos", (Object)this.getEntityPos()).add("mode", (Object)this.getGameMode()).add("permission", (Object)this.getPermissions()).toString();
    }

    public record SleepFailureReason(@Nullable Text message) {
        public static final SleepFailureReason TOO_FAR_AWAY = new SleepFailureReason(Text.translatable("block.minecraft.bed.too_far_away"));
        public static final SleepFailureReason OBSTRUCTED = new SleepFailureReason(Text.translatable("block.minecraft.bed.obstructed"));
        public static final SleepFailureReason OTHER = new SleepFailureReason(null);
        public static final SleepFailureReason NOT_SAFE = new SleepFailureReason(Text.translatable("block.minecraft.bed.not_safe"));
    }
}

