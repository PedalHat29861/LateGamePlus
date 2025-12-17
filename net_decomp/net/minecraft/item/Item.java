/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.item;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.DamageResistantComponent;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.component.type.KineticWeaponComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.PiercingWeaponComponent;
import net.minecraft.component.type.ProvidesTrimMaterialComponent;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.component.type.SwingAnimationComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.component.type.UseEffectsComponent;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.component.type.WeaponComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.map.MapState;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeyedValue;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.LazyRegistryEntryReference;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.SwingAnimationType;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Item
implements ToggleableFeature,
ItemConvertible {
    public static final Codec<RegistryEntry<Item>> ENTRY_CODEC = Registries.ITEM.getEntryCodec().validate(entry -> entry.matches(Items.AIR.getRegistryEntry()) ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success((Object)entry));
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<Item>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.ITEM);
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Block, Item> BLOCK_ITEMS = Maps.newHashMap();
    public static final Identifier BASE_ATTACK_DAMAGE_MODIFIER_ID = Identifier.ofVanilla("base_attack_damage");
    public static final Identifier BASE_ATTACK_SPEED_MODIFIER_ID = Identifier.ofVanilla("base_attack_speed");
    public static final int DEFAULT_MAX_COUNT = 64;
    public static final int MAX_MAX_COUNT = 99;
    public static final int ITEM_BAR_STEPS = 13;
    protected static final int DEFAULT_BLOCKS_ATTACKS_MAX_USE_TIME = 72000;
    private final RegistryEntry.Reference<Item> registryEntry = Registries.ITEM.createEntry(this);
    private final ComponentMap components;
    private final @Nullable Item recipeRemainder;
    protected final String translationKey;
    private final FeatureSet requiredFeatures;

    public static int getRawId(Item item) {
        return item == null ? 0 : Registries.ITEM.getRawId(item);
    }

    public static Item byRawId(int id) {
        return Registries.ITEM.get(id);
    }

    @Deprecated
    public static Item fromBlock(Block block) {
        return BLOCK_ITEMS.getOrDefault(block, Items.AIR);
    }

    public Item(Settings settings) {
        String string;
        this.translationKey = settings.getTranslationKey();
        this.components = settings.getValidatedComponents(Text.translatable(this.translationKey), settings.getModelId());
        this.recipeRemainder = settings.recipeRemainder;
        this.requiredFeatures = settings.requiredFeatures;
        if (SharedConstants.isDevelopment && !(string = this.getClass().getSimpleName()).endsWith("Item")) {
            LOGGER.error("Item classes should end with Item and {} doesn't.", (Object)string);
        }
    }

    @Deprecated
    public RegistryEntry.Reference<Item> getRegistryEntry() {
        return this.registryEntry;
    }

    public ComponentMap getComponents() {
        return this.components;
    }

    public int getMaxCount() {
        return this.components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
    }

    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
    }

    public void onItemEntityDestroyed(ItemEntity entity) {
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean canMine(ItemStack stack, BlockState state, World world, BlockPos pos, LivingEntity user) {
        ToolComponent toolComponent = stack.get(DataComponentTypes.TOOL);
        if (toolComponent == null) return true;
        if (toolComponent.canDestroyBlocksInCreative()) return true;
        if (!(user instanceof PlayerEntity)) return true;
        PlayerEntity playerEntity = (PlayerEntity)user;
        if (playerEntity.getAbilities().creativeMode) return false;
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    public float getMiningSpeed(ItemStack stack, BlockState state) {
        ToolComponent toolComponent = stack.get(DataComponentTypes.TOOL);
        return toolComponent != null ? toolComponent.getSpeed(state) : 1.0f;
    }

    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        ConsumableComponent consumableComponent = itemStack.get(DataComponentTypes.CONSUMABLE);
        if (consumableComponent != null) {
            return consumableComponent.consume(user, itemStack, hand);
        }
        EquippableComponent equippableComponent = itemStack.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent != null && equippableComponent.swappable()) {
            return equippableComponent.equip(itemStack, user);
        }
        if (itemStack.contains(DataComponentTypes.BLOCKS_ATTACKS)) {
            user.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }
        KineticWeaponComponent kineticWeaponComponent = itemStack.get(DataComponentTypes.KINETIC_WEAPON);
        if (kineticWeaponComponent != null) {
            user.setCurrentHand(hand);
            kineticWeaponComponent.playSound(user);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ConsumableComponent consumableComponent = stack.get(DataComponentTypes.CONSUMABLE);
        if (consumableComponent != null) {
            return consumableComponent.finishConsumption(world, user, stack);
        }
        return stack;
    }

    public boolean isItemBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    public int getItemBarStep(ItemStack stack) {
        return MathHelper.clamp(Math.round(13.0f - (float)stack.getDamage() * 13.0f / (float)stack.getMaxDamage()), 0, 13);
    }

    public int getItemBarColor(ItemStack stack) {
        int i = stack.getMaxDamage();
        float f = Math.max(0.0f, ((float)i - (float)stack.getDamage()) / (float)i);
        return MathHelper.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
    }

    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        return false;
    }

    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        return false;
    }

    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        return 0.0f;
    }

    @Deprecated
    public @Nullable DamageSource getDamageSource(LivingEntity user) {
        return null;
    }

    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    }

    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    }

    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        ToolComponent toolComponent = stack.get(DataComponentTypes.TOOL);
        if (toolComponent == null) {
            return false;
        }
        if (!world.isClient() && state.getHardness(world, pos) != 0.0f && toolComponent.damagePerBlock() > 0) {
            stack.damage(toolComponent.damagePerBlock(), miner, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    public boolean isCorrectForDrops(ItemStack stack, BlockState state) {
        ToolComponent toolComponent = stack.get(DataComponentTypes.TOOL);
        return toolComponent != null && toolComponent.isCorrectForDrops(state);
    }

    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return ActionResult.PASS;
    }

    public String toString() {
        return Registries.ITEM.getEntry(this).getIdAsString();
    }

    public final ItemStack getRecipeRemainder() {
        return this.recipeRemainder == null ? ItemStack.EMPTY : new ItemStack(this.recipeRemainder);
    }

    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
    }

    public void onCraftByPlayer(ItemStack stack, PlayerEntity player) {
        this.onCraft(stack, player.getEntityWorld());
    }

    public void onCraft(ItemStack stack, World world) {
    }

    public UseAction getUseAction(ItemStack stack) {
        ConsumableComponent consumableComponent = stack.get(DataComponentTypes.CONSUMABLE);
        if (consumableComponent != null) {
            return consumableComponent.useAction();
        }
        if (stack.contains(DataComponentTypes.BLOCKS_ATTACKS)) {
            return UseAction.BLOCK;
        }
        if (stack.contains(DataComponentTypes.KINETIC_WEAPON)) {
            return UseAction.SPEAR;
        }
        return UseAction.NONE;
    }

    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        ConsumableComponent consumableComponent = stack.get(DataComponentTypes.CONSUMABLE);
        if (consumableComponent != null) {
            return consumableComponent.getConsumeTicks();
        }
        if (stack.contains(DataComponentTypes.BLOCKS_ATTACKS) || stack.contains(DataComponentTypes.KINETIC_WEAPON)) {
            return 72000;
        }
        return 0;
    }

    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        return false;
    }

    @Deprecated
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
    }

    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.empty();
    }

    @VisibleForTesting
    public final String getTranslationKey() {
        return this.translationKey;
    }

    public final Text getName() {
        return this.components.getOrDefault(DataComponentTypes.ITEM_NAME, ScreenTexts.EMPTY);
    }

    public Text getName(ItemStack stack) {
        return stack.getComponents().getOrDefault(DataComponentTypes.ITEM_NAME, ScreenTexts.EMPTY);
    }

    public boolean hasGlint(ItemStack stack) {
        return stack.hasEnchantments();
    }

    protected static BlockHitResult raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        Vec3d vec3d = player.getEyePos();
        Vec3d vec3d2 = vec3d.add(player.getRotationVector(player.getPitch(), player.getYaw()).multiply(player.getBlockInteractionRange()));
        return world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));
    }

    public boolean isUsedOnRelease(ItemStack stack) {
        return false;
    }

    public ItemStack getDefaultStack() {
        return new ItemStack(this);
    }

    public boolean canBeNested() {
        return true;
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.requiredFeatures;
    }

    public boolean shouldShowOperatorBlockWarnings(ItemStack stack, @Nullable PlayerEntity player) {
        return false;
    }

    public static class Settings {
        private static final RegistryKeyedValue<Item, String> BLOCK_PREFIXED_TRANSLATION_KEY = key -> Util.createTranslationKey("block", key.getValue());
        private static final RegistryKeyedValue<Item, String> ITEM_PREFIXED_TRANSLATION_KEY = key -> Util.createTranslationKey("item", key.getValue());
        private final ComponentMap.Builder components = ComponentMap.builder().addAll(DataComponentTypes.DEFAULT_ITEM_COMPONENTS);
        @Nullable Item recipeRemainder;
        FeatureSet requiredFeatures = FeatureFlags.VANILLA_FEATURES;
        private @Nullable RegistryKey<Item> registryKey;
        private RegistryKeyedValue<Item, String> translationKey = ITEM_PREFIXED_TRANSLATION_KEY;
        private final RegistryKeyedValue<Item, Identifier> modelId = RegistryKey::getValue;

        public Settings food(FoodComponent foodComponent) {
            return this.food(foodComponent, ConsumableComponents.FOOD);
        }

        public Settings food(FoodComponent foodComponent, ConsumableComponent consumableComponent) {
            return this.component(DataComponentTypes.FOOD, foodComponent).component(DataComponentTypes.CONSUMABLE, consumableComponent);
        }

        public Settings useRemainder(Item convertInto) {
            return this.component(DataComponentTypes.USE_REMAINDER, new UseRemainderComponent(new ItemStack(convertInto)));
        }

        public Settings useCooldown(float seconds) {
            return this.component(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(seconds));
        }

        public Settings maxCount(int maxCount) {
            return this.component(DataComponentTypes.MAX_STACK_SIZE, maxCount);
        }

        public Settings maxDamage(int maxDamage) {
            this.component(DataComponentTypes.MAX_DAMAGE, maxDamage);
            this.component(DataComponentTypes.MAX_STACK_SIZE, 1);
            this.component(DataComponentTypes.DAMAGE, 0);
            return this;
        }

        public Settings recipeRemainder(Item recipeRemainder) {
            this.recipeRemainder = recipeRemainder;
            return this;
        }

        public Settings rarity(Rarity rarity) {
            return this.component(DataComponentTypes.RARITY, rarity);
        }

        public Settings fireproof() {
            return this.component(DataComponentTypes.DAMAGE_RESISTANT, new DamageResistantComponent(DamageTypeTags.IS_FIRE));
        }

        public Settings jukeboxPlayable(RegistryKey<JukeboxSong> songKey) {
            return this.component(DataComponentTypes.JUKEBOX_PLAYABLE, new JukeboxPlayableComponent(new LazyRegistryEntryReference<JukeboxSong>(songKey)));
        }

        public Settings enchantable(int enchantability) {
            return this.component(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(enchantability));
        }

        public Settings repairable(Item repairIngredient) {
            return this.component(DataComponentTypes.REPAIRABLE, new RepairableComponent(RegistryEntryList.of(repairIngredient.getRegistryEntry())));
        }

        public Settings repairable(TagKey<Item> repairIngredientsTag) {
            RegistryEntryLookup<Item> registryEntryLookup = Registries.createEntryLookup(Registries.ITEM);
            return this.component(DataComponentTypes.REPAIRABLE, new RepairableComponent(registryEntryLookup.getOrThrow(repairIngredientsTag)));
        }

        public Settings equippable(EquipmentSlot slot) {
            return this.component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(slot).build());
        }

        public Settings equippableUnswappable(EquipmentSlot slot) {
            return this.component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(slot).swappable(false).build());
        }

        public Settings tool(ToolMaterial material, TagKey<Block> effectiveBlocks, float attackDamage, float attackSpeed, float disableBlockingForSeconds) {
            return material.applyToolSettings(this, effectiveBlocks, attackDamage, attackSpeed, disableBlockingForSeconds);
        }

        public Settings pickaxe(ToolMaterial material, float attackDamage, float attackSpeed) {
            return this.tool(material, BlockTags.PICKAXE_MINEABLE, attackDamage, attackSpeed, 0.0f);
        }

        public Settings axe(ToolMaterial material, float attackDamage, float attackSpeed) {
            return this.tool(material, BlockTags.AXE_MINEABLE, attackDamage, attackSpeed, 5.0f);
        }

        public Settings hoe(ToolMaterial material, float attackDamage, float attackSpeed) {
            return this.tool(material, BlockTags.HOE_MINEABLE, attackDamage, attackSpeed, 0.0f);
        }

        public Settings shovel(ToolMaterial material, float attackDamage, float attackSpeed) {
            return this.tool(material, BlockTags.SHOVEL_MINEABLE, attackDamage, attackSpeed, 0.0f);
        }

        public Settings sword(ToolMaterial material, float attackDamage, float attackSpeed) {
            return material.applySwordSettings(this, attackDamage, attackSpeed);
        }

        public Settings spear(ToolMaterial material, float swingAnimationSeconds, float chargeDamageMultiplier, float chargeDelaySeconds, float maxDurationForDismountSeconds, float minSpeedForDismount, float maxDurationForChargeKnockbackInSeconds, float minSpeedForChargeKnockback, float maxDurationForChargeDamageInSeconds, float minRelativeSpeedForChargeDamage) {
            return this.maxDamage(material.durability()).repairable(material.repairItems()).enchantable(material.enchantmentValue()).component(DataComponentTypes.DAMAGE_TYPE, new LazyRegistryEntryReference<DamageType>(DamageTypes.SPEAR)).component(DataComponentTypes.KINETIC_WEAPON, new KineticWeaponComponent(10, (int)(chargeDelaySeconds * 20.0f), KineticWeaponComponent.Condition.ofMinSpeed((int)(maxDurationForDismountSeconds * 20.0f), minSpeedForDismount), KineticWeaponComponent.Condition.ofMinSpeed((int)(maxDurationForChargeKnockbackInSeconds * 20.0f), minSpeedForChargeKnockback), KineticWeaponComponent.Condition.ofMinRelativeSpeed((int)(maxDurationForChargeDamageInSeconds * 20.0f), minRelativeSpeedForChargeDamage), 0.38f, chargeDamageMultiplier, Optional.of(material == ToolMaterial.WOOD ? SoundEvents.ITEM_SPEAR_WOOD_USE : SoundEvents.ITEM_SPEAR_USE), Optional.of(material == ToolMaterial.WOOD ? SoundEvents.ITEM_SPEAR_WOOD_HIT : SoundEvents.ITEM_SPEAR_HIT))).component(DataComponentTypes.PIERCING_WEAPON, new PiercingWeaponComponent(true, false, Optional.of(material == ToolMaterial.WOOD ? SoundEvents.ITEM_SPEAR_WOOD_ATTACK : SoundEvents.ITEM_SPEAR_ATTACK), Optional.of(material == ToolMaterial.WOOD ? SoundEvents.ITEM_SPEAR_WOOD_HIT : SoundEvents.ITEM_SPEAR_HIT))).component(DataComponentTypes.ATTACK_RANGE, new AttackRangeComponent(2.0f, 4.5f, 2.0f, 6.5f, 0.125f, 0.5f)).component(DataComponentTypes.MINIMUM_ATTACK_CHARGE, Float.valueOf(1.0f)).component(DataComponentTypes.SWING_ANIMATION, new SwingAnimationComponent(SwingAnimationType.STAB, (int)(swingAnimationSeconds * 20.0f))).attributeModifiers(AttributeModifiersComponent.builder().add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 0.0f + material.attackDamageBonus(), EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, (double)(1.0f / swingAnimationSeconds) - 4.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build()).component(DataComponentTypes.USE_EFFECTS, new UseEffectsComponent(true, false, 1.0f)).component(DataComponentTypes.WEAPON, new WeaponComponent(1));
        }

        public Settings spawnEgg(EntityType<?> entityType) {
            return this.component(DataComponentTypes.ENTITY_DATA, TypedEntityData.create(entityType, new NbtCompound()));
        }

        public Settings armor(ArmorMaterial material, EquipmentType type) {
            return this.maxDamage(type.getMaxDamage(material.durability())).attributeModifiers(material.createAttributeModifiers(type)).enchantable(material.enchantmentValue()).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(type.getEquipmentSlot()).equipSound(material.equipSound()).model(material.assetId()).build()).repairable(material.repairIngredient());
        }

        public Settings wolfArmor(ArmorMaterial material) {
            return this.maxDamage(EquipmentType.BODY.getMaxDamage(material.durability())).attributeModifiers(material.createAttributeModifiers(EquipmentType.BODY)).repairable(material.repairIngredient()).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.BODY).equipSound(material.equipSound()).model(material.assetId()).allowedEntities(RegistryEntryList.of(EntityType.WOLF.getRegistryEntry())).canBeSheared(true).shearingSound(Registries.SOUND_EVENT.getEntry(SoundEvents.ITEM_ARMOR_UNEQUIP_WOLF)).build()).component(DataComponentTypes.BREAK_SOUND, SoundEvents.ITEM_WOLF_ARMOR_BREAK).maxCount(1);
        }

        public Settings horseArmor(ArmorMaterial material) {
            RegistryEntryLookup<EntityType<?>> registryEntryLookup = Registries.createEntryLookup(Registries.ENTITY_TYPE);
            return this.attributeModifiers(material.createAttributeModifiers(EquipmentType.BODY)).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.BODY).equipSound(SoundEvents.ENTITY_HORSE_ARMOR).model(material.assetId()).allowedEntities(registryEntryLookup.getOrThrow(EntityTypeTags.CAN_WEAR_HORSE_ARMOR)).damageOnHurt(false).canBeSheared(true).shearingSound(SoundEvents.ITEM_HORSE_ARMOR_UNEQUIP).build()).maxCount(1);
        }

        public Settings nautilusArmor(ArmorMaterial material) {
            RegistryEntryLookup<EntityType<?>> registryEntryLookup = Registries.createEntryLookup(Registries.ENTITY_TYPE);
            return this.attributeModifiers(material.createAttributeModifiers(EquipmentType.BODY)).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.BODY).equipSound(SoundEvents.ITEM_ARMOR_EQUIP_NAUTILUS).model(material.assetId()).allowedEntities(registryEntryLookup.getOrThrow(EntityTypeTags.CAN_WEAR_NAUTILUS_ARMOR)).damageOnHurt(false).equipOnInteract(true).canBeSheared(true).shearingSound(SoundEvents.ITEM_ARMOR_UNEQUIP_NAUTILUS).build()).maxCount(1);
        }

        public Settings trimMaterial(RegistryKey<ArmorTrimMaterial> trimMaterial) {
            return this.component(DataComponentTypes.PROVIDES_TRIM_MATERIAL, new ProvidesTrimMaterialComponent(trimMaterial));
        }

        public Settings requires(FeatureFlag ... features) {
            this.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(features);
            return this;
        }

        public Settings registryKey(RegistryKey<Item> registryKey) {
            this.registryKey = registryKey;
            return this;
        }

        public Settings translationKey(String translationKey) {
            this.translationKey = RegistryKeyedValue.fixed(translationKey);
            return this;
        }

        public Settings useBlockPrefixedTranslationKey() {
            this.translationKey = BLOCK_PREFIXED_TRANSLATION_KEY;
            return this;
        }

        public Settings useItemPrefixedTranslationKey() {
            this.translationKey = ITEM_PREFIXED_TRANSLATION_KEY;
            return this;
        }

        protected String getTranslationKey() {
            return this.translationKey.get(Objects.requireNonNull(this.registryKey, "Item id not set"));
        }

        public Identifier getModelId() {
            return this.modelId.get(Objects.requireNonNull(this.registryKey, "Item id not set"));
        }

        public <T> Settings component(ComponentType<T> type, T value) {
            this.components.add(type, value);
            return this;
        }

        public Settings attributeModifiers(AttributeModifiersComponent attributeModifiersComponent) {
            return this.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributeModifiersComponent);
        }

        ComponentMap getValidatedComponents(Text name, Identifier modelId) {
            ComponentMap componentMap = this.components.add(DataComponentTypes.ITEM_NAME, name).add(DataComponentTypes.ITEM_MODEL, modelId).build();
            if (componentMap.contains(DataComponentTypes.DAMAGE) && componentMap.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1) {
                throw new IllegalStateException("Item cannot have both durability and be stackable");
            }
            return componentMap;
        }
    }

    public static interface TooltipContext {
        public static final TooltipContext DEFAULT = new TooltipContext(){

            @Override
            public  @Nullable RegistryWrapper.WrapperLookup getRegistryLookup() {
                return null;
            }

            @Override
            public float getUpdateTickRate() {
                return 20.0f;
            }

            @Override
            public @Nullable MapState getMapState(MapIdComponent mapIdComponent) {
                return null;
            }

            @Override
            public boolean isDifficultyPeaceful() {
                return false;
            }
        };

        public  @Nullable RegistryWrapper.WrapperLookup getRegistryLookup();

        public float getUpdateTickRate();

        public @Nullable MapState getMapState(MapIdComponent var1);

        public boolean isDifficultyPeaceful();

        public static TooltipContext create(final @Nullable World world) {
            if (world == null) {
                return DEFAULT;
            }
            return new TooltipContext(){

                @Override
                public RegistryWrapper.WrapperLookup getRegistryLookup() {
                    return world.getRegistryManager();
                }

                @Override
                public float getUpdateTickRate() {
                    return world.getTickManager().getTickRate();
                }

                @Override
                public MapState getMapState(MapIdComponent mapIdComponent) {
                    return world.getMapState(mapIdComponent);
                }

                @Override
                public boolean isDifficultyPeaceful() {
                    return world.getDifficulty() == Difficulty.PEACEFUL;
                }
            };
        }

        public static TooltipContext create(final RegistryWrapper.WrapperLookup registries) {
            return new TooltipContext(){

                @Override
                public RegistryWrapper.WrapperLookup getRegistryLookup() {
                    return registries;
                }

                @Override
                public float getUpdateTickRate() {
                    return 20.0f;
                }

                @Override
                public @Nullable MapState getMapState(MapIdComponent mapIdComponent) {
                    return null;
                }

                @Override
                public boolean isDifficultyPeaceful() {
                    return false;
                }
            };
        }
    }
}

