/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  org.apache.commons.lang3.function.TriConsumer
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Spawner;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.BlockPredicatesComponent;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.DamageResistantComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.KineticWeaponComponent;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.component.type.SwingAnimationComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.component.type.UseEffectsComponent;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.component.type.WeaponComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.dynamic.NullOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class ItemStack
implements ComponentHolder {
    private static final List<Text> OPERATOR_WARNINGS = List.of(Text.translatable("item.op_warning.line1").formatted(Formatting.RED, Formatting.BOLD), Text.translatable("item.op_warning.line2").formatted(Formatting.RED), Text.translatable("item.op_warning.line3").formatted(Formatting.RED));
    private static final Text UNBREAKABLE_TEXT = Text.translatable("item.unbreakable").formatted(Formatting.BLUE);
    private static final Text INTANGIBLE_TEXT = Text.translatable("item.intangible").formatted(Formatting.GRAY);
    public static final MapCodec<ItemStack> MAP_CODEC = MapCodec.recursive((String)"ItemStack", codec -> RecordCodecBuilder.mapCodec(instance -> instance.group((App)Item.ENTRY_CODEC.fieldOf("id").forGetter(ItemStack::getRegistryEntry), (App)Codecs.rangedInt(1, 99).fieldOf("count").orElse((Object)1).forGetter(ItemStack::getCount), (App)ComponentChanges.CODEC.optionalFieldOf("components", (Object)ComponentChanges.EMPTY).forGetter(stack -> stack.components.getChanges())).apply((Applicative)instance, ItemStack::new)));
    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(() -> MAP_CODEC.codec());
    public static final Codec<ItemStack> UNCOUNTED_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group((App)Item.ENTRY_CODEC.fieldOf("id").forGetter(ItemStack::getRegistryEntry), (App)ComponentChanges.CODEC.optionalFieldOf("components", (Object)ComponentChanges.EMPTY).forGetter(stack -> stack.components.getChanges())).apply((Applicative)instance, (item, components) -> new ItemStack((RegistryEntry<Item>)item, 1, (ComponentChanges)components))));
    public static final Codec<ItemStack> VALIDATED_CODEC = CODEC.validate(ItemStack::validate);
    public static final Codec<ItemStack> VALIDATED_UNCOUNTED_CODEC = UNCOUNTED_CODEC.validate(ItemStack::validate);
    public static final Codec<ItemStack> OPTIONAL_CODEC = Codecs.optional(CODEC).xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    public static final Codec<ItemStack> REGISTRY_ENTRY_CODEC = Item.ENTRY_CODEC.xmap(ItemStack::new, ItemStack::getRegistryEntry);
    public static final PacketCodec<RegistryByteBuf, ItemStack> OPTIONAL_PACKET_CODEC = ItemStack.createOptionalPacketCodec(ComponentChanges.PACKET_CODEC);
    public static final PacketCodec<RegistryByteBuf, ItemStack> LENGTH_PREPENDED_OPTIONAL_PACKET_CODEC = ItemStack.createOptionalPacketCodec(ComponentChanges.LENGTH_PREPENDED_PACKET_CODEC);
    public static final PacketCodec<RegistryByteBuf, ItemStack> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ItemStack>(){

        @Override
        public ItemStack decode(RegistryByteBuf registryByteBuf) {
            ItemStack itemStack = (ItemStack)OPTIONAL_PACKET_CODEC.decode(registryByteBuf);
            if (itemStack.isEmpty()) {
                throw new DecoderException("Empty ItemStack not allowed");
            }
            return itemStack;
        }

        @Override
        public void encode(RegistryByteBuf registryByteBuf, ItemStack itemStack) {
            if (itemStack.isEmpty()) {
                throw new EncoderException("Empty ItemStack not allowed");
            }
            OPTIONAL_PACKET_CODEC.encode(registryByteBuf, itemStack);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryByteBuf)((Object)object), (ItemStack)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryByteBuf)((Object)object));
        }
    };
    public static final PacketCodec<RegistryByteBuf, List<ItemStack>> OPTIONAL_LIST_PACKET_CODEC = OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toCollection(DefaultedList::ofSize));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Void)null);
    private static final Text DISABLED_TEXT = Text.translatable("item.disabled").formatted(Formatting.RED);
    private int count;
    private int bobbingAnimationTime;
    @Deprecated
    private final @Nullable Item item;
    final MergedComponentMap components;
    private @Nullable Entity holder;

    public static DataResult<ItemStack> validate(ItemStack stack) {
        DataResult<Unit> dataResult = ItemStack.validateComponents(stack.getComponents());
        if (dataResult.isError()) {
            return dataResult.map(v -> stack);
        }
        if (stack.getCount() > stack.getMaxCount()) {
            return DataResult.error(() -> "Item stack with stack size of " + stack.getCount() + " was larger than maximum: " + stack.getMaxCount());
        }
        return DataResult.success((Object)stack);
    }

    private static PacketCodec<RegistryByteBuf, ItemStack> createOptionalPacketCodec(final PacketCodec<RegistryByteBuf, ComponentChanges> componentsPacketCodec) {
        return new PacketCodec<RegistryByteBuf, ItemStack>(){

            @Override
            public ItemStack decode(RegistryByteBuf registryByteBuf) {
                int i = registryByteBuf.readVarInt();
                if (i <= 0) {
                    return EMPTY;
                }
                RegistryEntry registryEntry = (RegistryEntry)Item.ENTRY_PACKET_CODEC.decode(registryByteBuf);
                ComponentChanges componentChanges = (ComponentChanges)componentsPacketCodec.decode(registryByteBuf);
                return new ItemStack(registryEntry, i, componentChanges);
            }

            @Override
            public void encode(RegistryByteBuf registryByteBuf, ItemStack itemStack) {
                if (itemStack.isEmpty()) {
                    registryByteBuf.writeVarInt(0);
                    return;
                }
                registryByteBuf.writeVarInt(itemStack.getCount());
                Item.ENTRY_PACKET_CODEC.encode(registryByteBuf, itemStack.getRegistryEntry());
                componentsPacketCodec.encode(registryByteBuf, itemStack.components.getChanges());
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)((Object)object), (ItemStack)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)((Object)object));
            }
        };
    }

    public static PacketCodec<RegistryByteBuf, ItemStack> createExtraValidatingPacketCodec(final PacketCodec<RegistryByteBuf, ItemStack> basePacketCodec) {
        return new PacketCodec<RegistryByteBuf, ItemStack>(){

            @Override
            public ItemStack decode(RegistryByteBuf registryByteBuf) {
                ItemStack itemStack = (ItemStack)basePacketCodec.decode(registryByteBuf);
                if (!itemStack.isEmpty()) {
                    RegistryOps<Unit> registryOps = registryByteBuf.getRegistryManager().getOps(NullOps.INSTANCE);
                    CODEC.encodeStart(registryOps, (Object)itemStack).getOrThrow(DecoderException::new);
                }
                return itemStack;
            }

            @Override
            public void encode(RegistryByteBuf registryByteBuf, ItemStack itemStack) {
                basePacketCodec.encode(registryByteBuf, itemStack);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)((Object)object), (ItemStack)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)((Object)object));
            }
        };
    }

    public Optional<TooltipData> getTooltipData() {
        return this.getItem().getTooltipData(this);
    }

    @Override
    public ComponentMap getComponents() {
        return !this.isEmpty() ? this.components : ComponentMap.EMPTY;
    }

    public ComponentMap getDefaultComponents() {
        return !this.isEmpty() ? this.getItem().getComponents() : ComponentMap.EMPTY;
    }

    public ComponentChanges getComponentChanges() {
        return !this.isEmpty() ? this.components.getChanges() : ComponentChanges.EMPTY;
    }

    public ComponentMap getImmutableComponents() {
        return !this.isEmpty() ? this.components.immutableCopy() : ComponentMap.EMPTY;
    }

    public boolean hasChangedComponent(ComponentType<?> type) {
        return !this.isEmpty() && this.components.hasChanged(type);
    }

    public ItemStack(ItemConvertible item) {
        this(item, 1);
    }

    public ItemStack(RegistryEntry<Item> entry) {
        this(entry.value(), 1);
    }

    public ItemStack(RegistryEntry<Item> item, int count, ComponentChanges changes) {
        this(item.value(), count, MergedComponentMap.create(item.value().getComponents(), changes));
    }

    public ItemStack(RegistryEntry<Item> itemEntry, int count) {
        this(itemEntry.value(), count);
    }

    public ItemStack(ItemConvertible item, int count) {
        this(item, count, new MergedComponentMap(item.asItem().getComponents()));
    }

    private ItemStack(ItemConvertible item, int count, MergedComponentMap components) {
        this.item = item.asItem();
        this.count = count;
        this.components = components;
    }

    private ItemStack(@Nullable Void v) {
        this.item = null;
        this.components = new MergedComponentMap(ComponentMap.EMPTY);
    }

    public static DataResult<Unit> validateComponents(ComponentMap components) {
        if (components.contains(DataComponentTypes.MAX_DAMAGE) && components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1) {
            return DataResult.error(() -> "Item cannot be both damageable and stackable");
        }
        ContainerComponent containerComponent = components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        for (ItemStack itemStack : containerComponent.iterateNonEmpty()) {
            int j;
            int i = itemStack.getCount();
            if (i <= (j = itemStack.getMaxCount())) continue;
            return DataResult.error(() -> "Item stack with count of " + i + " was larger than maximum: " + j);
        }
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public boolean isEmpty() {
        return this == EMPTY || this.item == Items.AIR || this.count <= 0;
    }

    public boolean isItemEnabled(FeatureSet enabledFeatures) {
        return this.isEmpty() || this.getItem().isEnabled(enabledFeatures);
    }

    public ItemStack split(int amount) {
        int i = Math.min(amount, this.getCount());
        ItemStack itemStack = this.copyWithCount(i);
        this.decrement(i);
        return itemStack;
    }

    public ItemStack copyAndEmpty() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = this.copy();
        this.setCount(0);
        return itemStack;
    }

    public Item getItem() {
        return this.isEmpty() ? Items.AIR : this.item;
    }

    public RegistryEntry<Item> getRegistryEntry() {
        return this.getItem().getRegistryEntry();
    }

    public boolean isIn(TagKey<Item> tag) {
        return this.getItem().getRegistryEntry().isIn(tag);
    }

    public boolean isOf(Item item) {
        return this.getItem() == item;
    }

    public boolean itemMatches(Predicate<RegistryEntry<Item>> predicate) {
        return predicate.test(this.getItem().getRegistryEntry());
    }

    public boolean itemMatches(RegistryEntry<Item> itemEntry) {
        return this.getItem().getRegistryEntry() == itemEntry;
    }

    public boolean isIn(RegistryEntryList<Item> registryEntryList) {
        return registryEntryList.contains(this.getRegistryEntry());
    }

    public Stream<TagKey<Item>> streamTags() {
        return this.getItem().getRegistryEntry().streamTags();
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        ActionResult.Success success;
        PlayerEntity playerEntity = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        if (playerEntity != null && !playerEntity.getAbilities().allowModifyWorld && !this.canPlaceOn(new CachedBlockPosition(context.getWorld(), blockPos, false))) {
            return ActionResult.PASS;
        }
        Item item = this.getItem();
        ActionResult actionResult = item.useOnBlock(context);
        if (playerEntity != null && actionResult instanceof ActionResult.Success && (success = (ActionResult.Success)actionResult).shouldIncrementStat()) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(item));
        }
        return actionResult;
    }

    public float getMiningSpeedMultiplier(BlockState state) {
        return this.getItem().getMiningSpeed(this, state);
    }

    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = this.copy();
        boolean bl = this.getMaxUseTime(user) <= 0;
        ActionResult actionResult = this.getItem().use(world, user, hand);
        if (bl && actionResult instanceof ActionResult.Success) {
            ActionResult.Success success;
            return success.withNewHandStack((success = (ActionResult.Success)actionResult).getNewHandStack() == null ? this.applyRemainderAndCooldown(user, itemStack) : success.getNewHandStack().applyRemainderAndCooldown(user, itemStack));
        }
        return actionResult;
    }

    public ItemStack finishUsing(World world, LivingEntity user) {
        ItemStack itemStack = this.copy();
        ItemStack itemStack2 = this.getItem().finishUsing(this, world, user);
        return itemStack2.applyRemainderAndCooldown(user, itemStack);
    }

    private ItemStack applyRemainderAndCooldown(LivingEntity user, ItemStack stack) {
        UseRemainderComponent useRemainderComponent = stack.get(DataComponentTypes.USE_REMAINDER);
        UseCooldownComponent useCooldownComponent = stack.get(DataComponentTypes.USE_COOLDOWN);
        int i = stack.getCount();
        ItemStack itemStack = this;
        if (useRemainderComponent != null) {
            itemStack = useRemainderComponent.convert(itemStack, i, user.isInCreativeMode(), user::giveOrDropStack);
        }
        if (useCooldownComponent != null) {
            useCooldownComponent.set(stack, user);
        }
        return itemStack;
    }

    public int getMaxCount() {
        return this.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
    }

    public boolean isStackable() {
        return this.getMaxCount() > 1 && (!this.isDamageable() || !this.isDamaged());
    }

    public boolean isDamageable() {
        return this.contains(DataComponentTypes.MAX_DAMAGE) && !this.contains(DataComponentTypes.UNBREAKABLE) && this.contains(DataComponentTypes.DAMAGE);
    }

    public boolean isDamaged() {
        return this.isDamageable() && this.getDamage() > 0;
    }

    public int getDamage() {
        return MathHelper.clamp(this.getOrDefault(DataComponentTypes.DAMAGE, 0), 0, this.getMaxDamage());
    }

    public void setDamage(int damage) {
        this.set(DataComponentTypes.DAMAGE, MathHelper.clamp(damage, 0, this.getMaxDamage()));
    }

    public int getMaxDamage() {
        return this.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0);
    }

    public boolean shouldBreak() {
        return this.isDamageable() && this.getDamage() >= this.getMaxDamage();
    }

    public boolean willBreakNextUse() {
        return this.isDamageable() && this.getDamage() >= this.getMaxDamage() - 1;
    }

    public void damage(int amount, ServerWorld world, @Nullable ServerPlayerEntity player, Consumer<Item> breakCallback) {
        int i = this.calculateDamage(amount, world, player);
        if (i != 0) {
            this.onDurabilityChange(this.getDamage() + i, player, breakCallback);
        }
    }

    private int calculateDamage(int baseDamage, ServerWorld world, @Nullable ServerPlayerEntity player) {
        if (!this.isDamageable()) {
            return 0;
        }
        if (player != null && player.isInCreativeMode()) {
            return 0;
        }
        if (baseDamage > 0) {
            return EnchantmentHelper.getItemDamage(world, this, baseDamage);
        }
        return baseDamage;
    }

    private void onDurabilityChange(int damage, @Nullable ServerPlayerEntity player, Consumer<Item> breakCallback) {
        if (player != null) {
            Criteria.ITEM_DURABILITY_CHANGED.trigger(player, this, damage);
        }
        this.setDamage(damage);
        if (this.shouldBreak()) {
            Item item = this.getItem();
            this.decrement(1);
            breakCallback.accept(item);
        }
    }

    public void damage(int amount, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
            int i = this.calculateDamage(amount, serverPlayerEntity.getEntityWorld(), serverPlayerEntity);
            if (i == 0) {
                return;
            }
            int j = Math.min(this.getDamage() + i, this.getMaxDamage() - 1);
            this.onDurabilityChange(j, serverPlayerEntity, item -> {});
        }
    }

    public void damage(int amount, LivingEntity entity, Hand hand) {
        this.damage(amount, entity, hand.getEquipmentSlot());
    }

    public void damage(int amount, LivingEntity entity, EquipmentSlot slot) {
        World world = entity.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerPlayerEntity serverPlayerEntity;
            ServerWorld serverWorld = (ServerWorld)world;
            this.damage(amount, serverWorld, entity instanceof ServerPlayerEntity ? (serverPlayerEntity = (ServerPlayerEntity)entity) : null, (Item item) -> entity.sendEquipmentBreakStatus((Item)item, slot));
        }
    }

    public ItemStack damage(int amount, ItemConvertible itemAfterBreaking, LivingEntity entity, EquipmentSlot slot) {
        this.damage(amount, entity, slot);
        if (this.isEmpty()) {
            ItemStack itemStack = this.copyComponentsToNewStackIgnoreEmpty(itemAfterBreaking, 1);
            if (itemStack.isDamageable()) {
                itemStack.setDamage(0);
            }
            return itemStack;
        }
        return this;
    }

    public boolean isItemBarVisible() {
        return this.getItem().isItemBarVisible(this);
    }

    public int getItemBarStep() {
        return this.getItem().getItemBarStep(this);
    }

    public int getItemBarColor() {
        return this.getItem().getItemBarColor(this);
    }

    public boolean onStackClicked(Slot slot, ClickType clickType, PlayerEntity player) {
        return this.getItem().onStackClicked(this, slot, clickType, player);
    }

    public boolean onClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        return this.getItem().onClicked(this, stack, slot, clickType, player, cursorStackReference);
    }

    public boolean postHit(LivingEntity target, LivingEntity user) {
        Item item = this.getItem();
        item.postHit(this, target, user);
        if (this.contains(DataComponentTypes.WEAPON)) {
            if (user instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity)user;
                playerEntity.incrementStat(Stats.USED.getOrCreateStat(item));
            }
            return true;
        }
        return false;
    }

    public void postDamageEntity(LivingEntity target, LivingEntity user) {
        this.getItem().postDamageEntity(this, target, user);
        WeaponComponent weaponComponent = this.get(DataComponentTypes.WEAPON);
        if (weaponComponent != null) {
            this.damage(weaponComponent.itemDamagePerAttack(), user, EquipmentSlot.MAINHAND);
        }
    }

    public void postMine(World world, BlockState state, BlockPos pos, PlayerEntity miner) {
        Item item = this.getItem();
        if (item.postMine(this, world, state, pos, miner)) {
            miner.incrementStat(Stats.USED.getOrCreateStat(item));
        }
    }

    public boolean isSuitableFor(BlockState state) {
        return this.getItem().isCorrectForDrops(this, state);
    }

    public ActionResult useOnEntity(PlayerEntity user, LivingEntity entity, Hand hand) {
        ActionResult actionResult;
        EquippableComponent equippableComponent = this.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent != null && equippableComponent.equipOnInteract() && (actionResult = equippableComponent.equipOnInteract(user, entity, this)) != ActionResult.PASS) {
            return actionResult;
        }
        return this.getItem().useOnEntity(this, user, entity, hand);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = new ItemStack(this.getItem(), this.count, this.components.copy());
        itemStack.setBobbingAnimationTime(this.getBobbingAnimationTime());
        return itemStack;
    }

    public ItemStack copyWithCount(int count) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = this.copy();
        itemStack.setCount(count);
        return itemStack;
    }

    public ItemStack withItem(ItemConvertible item) {
        return this.copyComponentsToNewStack(item, this.getCount());
    }

    public ItemStack copyComponentsToNewStack(ItemConvertible item, int count) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        return this.copyComponentsToNewStackIgnoreEmpty(item, count);
    }

    private ItemStack copyComponentsToNewStackIgnoreEmpty(ItemConvertible item, int count) {
        return new ItemStack(item.asItem().getRegistryEntry(), count, this.components.getChanges());
    }

    public static boolean areEqual(ItemStack left, ItemStack right) {
        if (left == right) {
            return true;
        }
        if (left.getCount() != right.getCount()) {
            return false;
        }
        return ItemStack.areItemsAndComponentsEqual(left, right);
    }

    @Deprecated
    public static boolean stacksEqual(List<ItemStack> left, List<ItemStack> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); ++i) {
            if (ItemStack.areEqual(left.get(i), right.get(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean areItemsEqual(ItemStack left, ItemStack right) {
        return left.isOf(right.getItem());
    }

    public static boolean areItemsAndComponentsEqual(ItemStack stack, ItemStack otherStack) {
        if (!stack.isOf(otherStack.getItem())) {
            return false;
        }
        if (stack.isEmpty() && otherStack.isEmpty()) {
            return true;
        }
        return Objects.equals(stack.components, otherStack.components);
    }

    public static boolean shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to, Predicate<ComponentType<?>> skippedComponent) {
        if (from == to) {
            return true;
        }
        if (from.getCount() != to.getCount()) {
            return false;
        }
        if (!from.isOf(to.getItem())) {
            return false;
        }
        if (from.isEmpty() && to.isEmpty()) {
            return true;
        }
        if (from.components.size() != to.components.size()) {
            return false;
        }
        for (ComponentType<?> componentType : from.components.getTypes()) {
            Object object = from.components.get(componentType);
            Object object2 = to.components.get(componentType);
            if (object == null || object2 == null) {
                return false;
            }
            if (Objects.equals(object, object2) || skippedComponent.test(componentType)) continue;
            return false;
        }
        return true;
    }

    public static MapCodec<ItemStack> createOptionalCodec(String fieldName) {
        return CODEC.lenientOptionalFieldOf(fieldName).xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    public static int hashCode(@Nullable ItemStack stack) {
        if (stack != null) {
            int i = 31 + stack.getItem().hashCode();
            return 31 * i + stack.getComponents().hashCode();
        }
        return 0;
    }

    @Deprecated
    public static int listHashCode(List<ItemStack> stacks) {
        int i = 0;
        for (ItemStack itemStack : stacks) {
            i = i * 31 + ItemStack.hashCode(itemStack);
        }
        return i;
    }

    public String toString() {
        return this.getCount() + " " + String.valueOf(this.getItem());
    }

    public void inventoryTick(World world, Entity entity, @Nullable EquipmentSlot slot) {
        if (this.bobbingAnimationTime > 0) {
            --this.bobbingAnimationTime;
        }
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.getItem().inventoryTick(this, serverWorld, entity, slot);
        }
    }

    public void onCraftByPlayer(PlayerEntity player, int amount) {
        player.increaseStat(Stats.CRAFTED.getOrCreateStat(this.getItem()), amount);
        this.getItem().onCraftByPlayer(this, player);
    }

    public void onCraftByCrafter(World world) {
        this.getItem().onCraft(this, world);
    }

    public int getMaxUseTime(LivingEntity user) {
        return this.getItem().getMaxUseTime(this, user);
    }

    public UseAction getUseAction() {
        return this.getItem().getUseAction(this);
    }

    public void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks) {
        ItemStack itemStack2;
        ItemStack itemStack = this.copy();
        if (this.getItem().onStoppedUsing(this, world, user, remainingUseTicks) && (itemStack2 = this.applyRemainderAndCooldown(user, itemStack)) != this) {
            user.setStackInHand(user.getActiveHand(), itemStack2);
        }
    }

    public void emitUseGameEvent(Entity user, RegistryEntry.Reference<GameEvent> gameEvent) {
        UseEffectsComponent useEffectsComponent = this.get(DataComponentTypes.USE_EFFECTS);
        if (useEffectsComponent != null && useEffectsComponent.interactVibrations()) {
            user.emitGameEvent(gameEvent);
        }
    }

    public boolean isUsedOnRelease() {
        return this.getItem().isUsedOnRelease(this);
    }

    public <T> @Nullable T set(ComponentType<T> type, @Nullable T value) {
        return this.components.set(type, value);
    }

    public <T> @Nullable T set(Component<T> component) {
        return this.components.set(component);
    }

    public <T> void copy(ComponentType<T> type, ComponentsAccess from) {
        this.set(type, from.get(type));
    }

    public <T, U> @Nullable T apply(ComponentType<T> type, T defaultValue, U change, BiFunction<T, U, T> applier) {
        return this.set(type, applier.apply(this.getOrDefault(type, defaultValue), change));
    }

    public <T> @Nullable T apply(ComponentType<T> type, T defaultValue, UnaryOperator<T> applier) {
        T object = this.getOrDefault(type, defaultValue);
        return this.set(type, applier.apply(object));
    }

    public <T> @Nullable T remove(ComponentType<? extends T> type) {
        return this.components.remove(type);
    }

    public void applyChanges(ComponentChanges changes) {
        ComponentChanges componentChanges = this.components.getChanges();
        this.components.applyChanges(changes);
        Optional optional = ItemStack.validate(this).error();
        if (optional.isPresent()) {
            LOGGER.error("Failed to apply component patch '{}' to item: '{}'", (Object)changes, (Object)((DataResult.Error)optional.get()).message());
            this.components.setChanges(componentChanges);
        }
    }

    public void applyUnvalidatedChanges(ComponentChanges changes) {
        this.components.applyChanges(changes);
    }

    public void applyComponentsFrom(ComponentMap components) {
        this.components.setAll(components);
    }

    public Text getName() {
        Text text = this.getCustomName();
        if (text != null) {
            return text;
        }
        return this.getItemName();
    }

    public @Nullable Text getCustomName() {
        String string;
        Text text = this.get(DataComponentTypes.CUSTOM_NAME);
        if (text != null) {
            return text;
        }
        WrittenBookContentComponent writtenBookContentComponent = this.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (writtenBookContentComponent != null && !StringHelper.isBlank(string = writtenBookContentComponent.title().raw())) {
            return Text.literal(string);
        }
        return null;
    }

    public Text getItemName() {
        return this.getItem().getName(this);
    }

    public Text getFormattedName() {
        MutableText mutableText = Text.empty().append(this.getName()).formatted(this.getRarity().getFormatting());
        if (this.contains(DataComponentTypes.CUSTOM_NAME)) {
            mutableText.formatted(Formatting.ITALIC);
        }
        return mutableText;
    }

    public <T extends TooltipAppender> void appendComponentTooltip(ComponentType<T> componentType, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        TooltipAppender tooltipAppender = (TooltipAppender)this.get(componentType);
        if (tooltipAppender != null && displayComponent.shouldDisplay(componentType)) {
            tooltipAppender.appendTooltip(context, textConsumer, type, this.components);
        }
    }

    public List<Text> getTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type) {
        TooltipDisplayComponent tooltipDisplayComponent = this.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
        if (!type.isCreative() && tooltipDisplayComponent.hideTooltip()) {
            boolean bl = this.getItem().shouldShowOperatorBlockWarnings(this, player);
            return bl ? OPERATOR_WARNINGS : List.of();
        }
        ArrayList list = Lists.newArrayList();
        list.add(this.getFormattedName());
        this.appendTooltip(context, tooltipDisplayComponent, player, type, list::add);
        return list;
    }

    public void appendTooltip(Item.TooltipContext context, TooltipDisplayComponent displayComponent, @Nullable PlayerEntity player, TooltipType type, Consumer<Text> textConsumer) {
        boolean bl;
        BlockPredicatesComponent blockPredicatesComponent2;
        BlockPredicatesComponent blockPredicatesComponent;
        this.getItem().appendTooltip(this, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.TROPICAL_FISH_PATTERN, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.INSTRUMENT, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.MAP_ID, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.BEES, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.CONTAINER_LOOT, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.CONTAINER, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.BANNER_PATTERNS, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.POT_DECORATIONS, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.WRITTEN_BOOK_CONTENT, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.CHARGED_PROJECTILES, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.FIREWORKS, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.FIREWORK_EXPLOSION, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.POTION_CONTENTS, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.JUKEBOX_PLAYABLE, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.TRIM, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.STORED_ENCHANTMENTS, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.ENCHANTMENTS, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.DYED_COLOR, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.PROFILE, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.LORE, context, displayComponent, textConsumer, type);
        this.appendAttributeModifiersTooltip(textConsumer, displayComponent, player);
        this.appendTooltipIfComponentExists(DataComponentTypes.INTANGIBLE_PROJECTILE, INTANGIBLE_TEXT, displayComponent, textConsumer);
        this.appendTooltipIfComponentExists(DataComponentTypes.UNBREAKABLE, UNBREAKABLE_TEXT, displayComponent, textConsumer);
        this.appendComponentTooltip(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.BLOCK_STATE, context, displayComponent, textConsumer, type);
        this.appendComponentTooltip(DataComponentTypes.ENTITY_DATA, context, displayComponent, textConsumer, type);
        if ((this.isOf(Items.SPAWNER) || this.isOf(Items.TRIAL_SPAWNER)) && displayComponent.shouldDisplay(DataComponentTypes.BLOCK_ENTITY_DATA)) {
            TypedEntityData<BlockEntityType<?>> typedEntityData = this.get(DataComponentTypes.BLOCK_ENTITY_DATA);
            Spawner.appendSpawnDataToTooltip(typedEntityData, textConsumer, "SpawnData");
        }
        if ((blockPredicatesComponent = this.get(DataComponentTypes.CAN_BREAK)) != null && displayComponent.shouldDisplay(DataComponentTypes.CAN_BREAK)) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(BlockPredicatesComponent.CAN_BREAK_TEXT);
            blockPredicatesComponent.addTooltips(textConsumer);
        }
        if ((blockPredicatesComponent2 = this.get(DataComponentTypes.CAN_PLACE_ON)) != null && displayComponent.shouldDisplay(DataComponentTypes.CAN_PLACE_ON)) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(BlockPredicatesComponent.CAN_PLACE_TEXT);
            blockPredicatesComponent2.addTooltips(textConsumer);
        }
        if (type.isAdvanced()) {
            if (this.isDamaged() && displayComponent.shouldDisplay(DataComponentTypes.DAMAGE)) {
                textConsumer.accept(Text.translatable("item.durability", this.getMaxDamage() - this.getDamage(), this.getMaxDamage()));
            }
            textConsumer.accept(Text.literal(Registries.ITEM.getId(this.getItem()).toString()).formatted(Formatting.DARK_GRAY));
            int i = this.components.size();
            if (i > 0) {
                textConsumer.accept(Text.translatable("item.components", i).formatted(Formatting.DARK_GRAY));
            }
        }
        if (player != null && !this.getItem().isEnabled(player.getEntityWorld().getEnabledFeatures())) {
            textConsumer.accept(DISABLED_TEXT);
        }
        if (bl = this.getItem().shouldShowOperatorBlockWarnings(this, player)) {
            OPERATOR_WARNINGS.forEach(textConsumer);
        }
    }

    private void appendTooltipIfComponentExists(ComponentType<?> type, Text tooltip, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer) {
        if (this.contains(type) && displayComponent.shouldDisplay(type)) {
            textConsumer.accept(tooltip);
        }
    }

    private void appendAttributeModifiersTooltip(Consumer<Text> textConsumer, TooltipDisplayComponent displayComponent, @Nullable PlayerEntity player) {
        if (!displayComponent.shouldDisplay(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
            return;
        }
        for (AttributeModifierSlot attributeModifierSlot : AttributeModifierSlot.values()) {
            MutableBoolean mutableBoolean = new MutableBoolean(true);
            this.applyAttributeModifier(attributeModifierSlot, (TriConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier, AttributeModifiersComponent.Display>)((TriConsumer)(attribute, modifier, display) -> {
                if (display == AttributeModifiersComponent.Display.getHidden()) {
                    return;
                }
                if (mutableBoolean.isTrue()) {
                    textConsumer.accept(ScreenTexts.EMPTY);
                    textConsumer.accept(Text.translatable("item.modifiers." + attributeModifierSlot.asString()).formatted(Formatting.GRAY));
                    mutableBoolean.setFalse();
                }
                display.addTooltip(textConsumer, player, (RegistryEntry<EntityAttribute>)attribute, (EntityAttributeModifier)modifier);
            }));
        }
    }

    public boolean hasGlint() {
        Boolean boolean_ = this.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        if (boolean_ != null) {
            return boolean_;
        }
        return this.getItem().hasGlint(this);
    }

    public Rarity getRarity() {
        Rarity rarity = this.getOrDefault(DataComponentTypes.RARITY, Rarity.COMMON);
        if (!this.hasEnchantments()) {
            return rarity;
        }
        return switch (rarity) {
            case Rarity.COMMON, Rarity.UNCOMMON -> Rarity.RARE;
            case Rarity.RARE -> Rarity.EPIC;
            default -> rarity;
        };
    }

    public boolean isEnchantable() {
        if (!this.contains(DataComponentTypes.ENCHANTABLE)) {
            return false;
        }
        ItemEnchantmentsComponent itemEnchantmentsComponent = this.get(DataComponentTypes.ENCHANTMENTS);
        return itemEnchantmentsComponent != null && itemEnchantmentsComponent.isEmpty();
    }

    public void addEnchantment(RegistryEntry<Enchantment> enchantment, int level) {
        EnchantmentHelper.apply(this, builder -> builder.add(enchantment, level));
    }

    public boolean hasEnchantments() {
        return !this.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).isEmpty();
    }

    public ItemEnchantmentsComponent getEnchantments() {
        return this.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
    }

    public boolean isInFrame() {
        return this.holder instanceof ItemFrameEntity;
    }

    public void setHolder(@Nullable Entity holder) {
        if (!this.isEmpty()) {
            this.holder = holder;
        }
    }

    public @Nullable ItemFrameEntity getFrame() {
        return this.holder instanceof ItemFrameEntity ? (ItemFrameEntity)this.getHolder() : null;
    }

    public @Nullable Entity getHolder() {
        return !this.isEmpty() ? this.holder : null;
    }

    public void applyAttributeModifier(AttributeModifierSlot slot, TriConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier, AttributeModifiersComponent.Display> attributeModifierConsumer) {
        AttributeModifiersComponent attributeModifiersComponent = this.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        attributeModifiersComponent.applyModifiers(slot, attributeModifierConsumer);
        EnchantmentHelper.applyAttributeModifiers(this, slot, (attribute, modifier) -> attributeModifierConsumer.accept(attribute, modifier, (Object)AttributeModifiersComponent.Display.getDefault()));
    }

    public void applyAttributeModifiers(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        AttributeModifiersComponent attributeModifiersComponent = this.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        attributeModifiersComponent.applyModifiers(slot, attributeModifierConsumer);
        EnchantmentHelper.applyAttributeModifiers(this, slot, attributeModifierConsumer);
    }

    public Text toHoverableText() {
        MutableText mutableText = Text.empty().append(this.getName());
        if (this.contains(DataComponentTypes.CUSTOM_NAME)) {
            mutableText.formatted(Formatting.ITALIC);
        }
        MutableText mutableText2 = Texts.bracketed(mutableText);
        if (!this.isEmpty()) {
            mutableText2.formatted(this.getRarity().getFormatting()).styled(style -> style.withHoverEvent(new HoverEvent.ShowItem(this)));
        }
        return mutableText2;
    }

    public SwingAnimationComponent getSwingAnimation() {
        return this.getOrDefault(DataComponentTypes.SWING_ANIMATION, SwingAnimationComponent.DEFAULT);
    }

    public boolean canPlaceOn(CachedBlockPosition pos) {
        BlockPredicatesComponent blockPredicatesComponent = this.get(DataComponentTypes.CAN_PLACE_ON);
        return blockPredicatesComponent != null && blockPredicatesComponent.check(pos);
    }

    public boolean canBreak(CachedBlockPosition pos) {
        BlockPredicatesComponent blockPredicatesComponent = this.get(DataComponentTypes.CAN_BREAK);
        return blockPredicatesComponent != null && blockPredicatesComponent.check(pos);
    }

    public int getBobbingAnimationTime() {
        return this.bobbingAnimationTime;
    }

    public void setBobbingAnimationTime(int bobbingAnimationTime) {
        this.bobbingAnimationTime = bobbingAnimationTime;
    }

    public int getCount() {
        return this.isEmpty() ? 0 : this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void capCount(int maxCount) {
        if (!this.isEmpty() && this.getCount() > maxCount) {
            this.setCount(maxCount);
        }
    }

    public void increment(int amount) {
        this.setCount(this.getCount() + amount);
    }

    public void decrement(int amount) {
        this.increment(-amount);
    }

    public void decrementUnlessCreative(int amount, @Nullable LivingEntity entity) {
        if (entity == null || !entity.isInCreativeMode()) {
            this.decrement(amount);
        }
    }

    public ItemStack splitUnlessCreative(int amount, @Nullable LivingEntity entity) {
        ItemStack itemStack = this.copyWithCount(amount);
        this.decrementUnlessCreative(amount, entity);
        return itemStack;
    }

    public void usageTick(World world, LivingEntity user, int remainingUseTicks) {
        KineticWeaponComponent kineticWeaponComponent;
        ConsumableComponent consumableComponent = this.get(DataComponentTypes.CONSUMABLE);
        if (consumableComponent != null && consumableComponent.shouldSpawnParticlesAndPlaySounds(remainingUseTicks)) {
            consumableComponent.spawnParticlesAndPlaySound(user.getRandom(), user, this, 5);
        }
        if ((kineticWeaponComponent = this.get(DataComponentTypes.KINETIC_WEAPON)) != null && !world.isClient()) {
            kineticWeaponComponent.usageTick(this, remainingUseTicks, user, user.getActiveHand().getEquipmentSlot());
            return;
        }
        this.getItem().usageTick(world, user, this, remainingUseTicks);
    }

    public void onItemEntityDestroyed(ItemEntity entity) {
        this.getItem().onItemEntityDestroyed(entity);
    }

    public boolean takesDamageFrom(DamageSource source) {
        DamageResistantComponent damageResistantComponent = this.get(DataComponentTypes.DAMAGE_RESISTANT);
        return damageResistantComponent == null || !damageResistantComponent.resists(source);
    }

    public boolean canRepairWith(ItemStack ingredient) {
        RepairableComponent repairableComponent = this.get(DataComponentTypes.REPAIRABLE);
        return repairableComponent != null && repairableComponent.matches(ingredient);
    }

    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        return this.getItem().canMine(this, state, world, pos, player);
    }

    public DamageSource getDamageSource(LivingEntity attacker, Supplier<DamageSource> fallbackSupplier) {
        return Optional.ofNullable(this.get(DataComponentTypes.DAMAGE_TYPE)).flatMap(ref -> ref.resolveEntry(attacker.getRegistryManager())).map(typeEntry -> new DamageSource((RegistryEntry<DamageType>)typeEntry, attacker)).or(() -> Optional.ofNullable(this.getItem().getDamageSource(attacker))).orElseGet(fallbackSupplier);
    }
}

