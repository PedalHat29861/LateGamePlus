/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.commons.lang3.mutable.MutableFloat
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class EnchantmentHelper {
    public static int getLevel(RegistryEntry<Enchantment> enchantment, ItemStack stack) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        return itemEnchantmentsComponent.getLevel(enchantment);
    }

    public static ItemEnchantmentsComponent apply(ItemStack stack, java.util.function.Consumer<ItemEnchantmentsComponent.Builder> applier) {
        ComponentType<ItemEnchantmentsComponent> componentType = EnchantmentHelper.getEnchantmentsComponentType(stack);
        ItemEnchantmentsComponent itemEnchantmentsComponent = stack.get(componentType);
        if (itemEnchantmentsComponent == null) {
            return ItemEnchantmentsComponent.DEFAULT;
        }
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(itemEnchantmentsComponent);
        applier.accept(builder);
        ItemEnchantmentsComponent itemEnchantmentsComponent2 = builder.build();
        stack.set(componentType, itemEnchantmentsComponent2);
        return itemEnchantmentsComponent2;
    }

    public static boolean canHaveEnchantments(ItemStack stack) {
        return stack.contains(EnchantmentHelper.getEnchantmentsComponentType(stack));
    }

    public static void set(ItemStack stack, ItemEnchantmentsComponent enchantments) {
        stack.set(EnchantmentHelper.getEnchantmentsComponentType(stack), enchantments);
    }

    public static ItemEnchantmentsComponent getEnchantments(ItemStack stack) {
        return stack.getOrDefault(EnchantmentHelper.getEnchantmentsComponentType(stack), ItemEnchantmentsComponent.DEFAULT);
    }

    private static ComponentType<ItemEnchantmentsComponent> getEnchantmentsComponentType(ItemStack stack) {
        return stack.isOf(Items.ENCHANTED_BOOK) ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
    }

    public static boolean hasEnchantments(ItemStack stack) {
        return !stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).isEmpty() || !stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).isEmpty();
    }

    public static int getItemDamage(ServerWorld world, ItemStack stack, int baseItemDamage) {
        MutableFloat mutableFloat = new MutableFloat((float)baseItemDamage);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyItemDamage(world, level, stack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int getAmmoUse(ServerWorld world, ItemStack rangedWeaponStack, ItemStack projectileStack, int baseAmmoUse) {
        MutableFloat mutableFloat = new MutableFloat((float)baseAmmoUse);
        EnchantmentHelper.forEachEnchantment(rangedWeaponStack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyAmmoUse(world, level, projectileStack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int getBlockExperience(ServerWorld world, ItemStack stack, int baseBlockExperience) {
        MutableFloat mutableFloat = new MutableFloat((float)baseBlockExperience);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyBlockExperience(world, level, stack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int getMobExperience(ServerWorld world, @Nullable Entity attacker, Entity mob, int baseMobExperience) {
        if (attacker instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)attacker;
            MutableFloat mutableFloat = new MutableFloat((float)baseMobExperience);
            EnchantmentHelper.forEachEnchantment(livingEntity, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).modifyMobExperience(world, level, context.stack(), mob, mutableFloat));
            return mutableFloat.intValue();
        }
        return baseMobExperience;
    }

    public static ItemStack getEnchantedBookWith(EnchantmentLevelEntry entry) {
        ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        itemStack.addEnchantment(entry.enchantment(), entry.level());
        return itemStack;
    }

    private static void forEachEnchantment(ItemStack stack, Consumer consumer) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
            consumer.accept((RegistryEntry)entry.getKey(), entry.getIntValue());
        }
    }

    private static void forEachEnchantment(ItemStack stack, EquipmentSlot slot, LivingEntity entity, ContextAwareConsumer contextAwareConsumer) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEnchantmentsComponent itemEnchantmentsComponent = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (itemEnchantmentsComponent == null || itemEnchantmentsComponent.isEmpty()) {
            return;
        }
        EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(stack, slot, entity);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
            RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
            if (!((Enchantment)registryEntry.value()).slotMatches(slot)) continue;
            contextAwareConsumer.accept(registryEntry, entry.getIntValue(), enchantmentEffectContext);
        }
    }

    private static void forEachEnchantment(LivingEntity entity, ContextAwareConsumer contextAwareConsumer) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            EnchantmentHelper.forEachEnchantment(entity.getEquippedStack(equipmentSlot), equipmentSlot, entity, contextAwareConsumer);
        }
    }

    public static boolean isInvulnerableTo(ServerWorld world, LivingEntity user, DamageSource damageSource) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> mutableBoolean.setValue(mutableBoolean.isTrue() || ((Enchantment)enchantment.value()).hasDamageImmunityTo(world, level, user, damageSource)));
        return mutableBoolean.isTrue();
    }

    public static float getProtectionAmount(ServerWorld world, LivingEntity user, DamageSource damageSource) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).modifyDamageProtection(world, level, context.stack(), user, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float getDamage(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseDamage) {
        MutableFloat mutableFloat = new MutableFloat(baseDamage);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyDamage(world, level, stack, target, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float getSmashDamagePerFallenBlock(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseSmashDamagePerFallenBlock) {
        MutableFloat mutableFloat = new MutableFloat(baseSmashDamagePerFallenBlock);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifySmashDamagePerFallenBlock(world, level, stack, target, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float getArmorEffectiveness(ServerWorld world, ItemStack stack, Entity user, DamageSource damageSource, float baseArmorEffectiveness) {
        MutableFloat mutableFloat = new MutableFloat(baseArmorEffectiveness);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyArmorEffectiveness(world, level, stack, user, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyKnockback(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseKnockback) {
        MutableFloat mutableFloat = new MutableFloat(baseKnockback);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyKnockback(world, level, stack, target, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static void onTargetDamaged(ServerWorld world, Entity target, DamageSource damageSource) {
        Entity entity = damageSource.getAttacker();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            EnchantmentHelper.onTargetDamaged(world, target, damageSource, livingEntity.getWeaponStack());
        } else {
            EnchantmentHelper.onTargetDamaged(world, target, damageSource, null);
        }
    }

    public static void onAttack(ServerWorld world, Entity attacker) {
        if (attacker instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)attacker;
            EnchantmentHelper.forEachEnchantment(attacker.getWeaponStack(), EquipmentSlot.MAINHAND, livingEntity, (enchantment, level, context) -> ((Enchantment)enchantment.value()).onPiercingAttack(world, level, context, attacker));
        }
    }

    public static void onTargetDamaged(ServerWorld world, Entity target, DamageSource damageSource, @Nullable ItemStack weapon) {
        EnchantmentHelper.onTargetDamaged(world, target, damageSource, weapon, null);
    }

    public static void onTargetDamaged(ServerWorld world, Entity target, DamageSource damageSource, @Nullable ItemStack weapon, @Nullable java.util.function.Consumer<Item> breakCallback) {
        LivingEntity livingEntity;
        if (target instanceof LivingEntity) {
            livingEntity = (LivingEntity)target;
            EnchantmentHelper.forEachEnchantment(livingEntity, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).onTargetDamaged(world, level, context, EnchantmentEffectTarget.VICTIM, target, damageSource));
        }
        if (weapon != null) {
            Entity entity = damageSource.getAttacker();
            if (entity instanceof LivingEntity) {
                livingEntity = (LivingEntity)entity;
                EnchantmentHelper.forEachEnchantment(weapon, EquipmentSlot.MAINHAND, livingEntity, (enchantment, level, context) -> ((Enchantment)enchantment.value()).onTargetDamaged(world, level, context, EnchantmentEffectTarget.ATTACKER, target, damageSource));
            } else if (breakCallback != null) {
                EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(weapon, null, null, breakCallback);
                EnchantmentHelper.forEachEnchantment(weapon, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).onTargetDamaged(world, level, enchantmentEffectContext, EnchantmentEffectTarget.ATTACKER, target, damageSource));
            }
        }
    }

    public static void applyLocationBasedEffects(ServerWorld world, LivingEntity user) {
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).applyLocationBasedEffects(world, level, context, user));
    }

    public static void applyLocationBasedEffects(ServerWorld world, ItemStack stack, LivingEntity user, EquipmentSlot slot) {
        EnchantmentHelper.forEachEnchantment(stack, slot, user, (enchantment, level, context) -> ((Enchantment)enchantment.value()).applyLocationBasedEffects(world, level, context, user));
    }

    public static void removeLocationBasedEffects(LivingEntity user) {
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).removeLocationBasedEffects(level, context, user));
    }

    public static void removeLocationBasedEffects(ItemStack stack, LivingEntity user, EquipmentSlot slot) {
        EnchantmentHelper.forEachEnchantment(stack, slot, user, (enchantment, level, context) -> ((Enchantment)enchantment.value()).removeLocationBasedEffects(level, context, user));
    }

    public static void onTick(ServerWorld world, LivingEntity user) {
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).onTick(world, level, context, user));
    }

    public static int getEquipmentLevel(RegistryEntry<Enchantment> enchantment, LivingEntity entity) {
        Collection<ItemStack> iterable = enchantment.value().getEquipment(entity).values();
        int i = 0;
        for (ItemStack itemStack : iterable) {
            int j = EnchantmentHelper.getLevel(enchantment, itemStack);
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    public static int getProjectileCount(ServerWorld world, ItemStack stack, Entity user, int baseProjectileCount) {
        MutableFloat mutableFloat = new MutableFloat((float)baseProjectileCount);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyProjectileCount(world, level, stack, user, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getProjectileSpread(ServerWorld world, ItemStack stack, Entity user, float baseProjectileSpread) {
        MutableFloat mutableFloat = new MutableFloat(baseProjectileSpread);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyProjectileSpread(world, level, stack, user, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static int getProjectilePiercing(ServerWorld world, ItemStack weaponStack, ItemStack projectileStack) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(weaponStack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyProjectilePiercing(world, level, projectileStack, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static void onProjectileSpawned(ServerWorld world, ItemStack weaponStack, ProjectileEntity projectile, java.util.function.Consumer<Item> onBreak) {
        LivingEntity livingEntity;
        Entity entity = projectile.getOwner();
        LivingEntity livingEntity2 = entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : null;
        EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(weaponStack, null, livingEntity2, onBreak);
        EnchantmentHelper.forEachEnchantment(weaponStack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).onProjectileSpawned(world, level, enchantmentEffectContext, projectile));
    }

    public static void onHitBlock(ServerWorld world, ItemStack stack, @Nullable LivingEntity user, Entity enchantedEntity, @Nullable EquipmentSlot slot, Vec3d pos, BlockState state, java.util.function.Consumer<Item> onBreak) {
        EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(stack, slot, user, onBreak);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).onHitBlock(world, level, enchantmentEffectContext, enchantedEntity, pos, state));
    }

    public static int getRepairWithExperience(ServerWorld world, ItemStack stack, int baseRepairWithExperience) {
        MutableFloat mutableFloat = new MutableFloat((float)baseRepairWithExperience);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyRepairWithExperience(world, level, stack, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getEquipmentDropChance(ServerWorld world, LivingEntity attacker, DamageSource damageSource, float baseEquipmentDropChance) {
        MutableFloat mutableFloat = new MutableFloat(baseEquipmentDropChance);
        Random random = attacker.getRandom();
        EnchantmentHelper.forEachEnchantment(attacker, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> {
            LootContext lootContext = Enchantment.createEnchantedDamageLootContext(world, level, attacker, damageSource);
            ((Enchantment)enchantment.value()).getEffect(EnchantmentEffectComponentTypes.EQUIPMENT_DROPS).forEach(effect -> {
                if (effect.enchanted() == EnchantmentEffectTarget.VICTIM && effect.affected() == EnchantmentEffectTarget.VICTIM && effect.test(lootContext)) {
                    mutableFloat.setValue(((EnchantmentValueEffect)effect.effect()).apply(level, random, mutableFloat.floatValue()));
                }
            });
        });
        Entity entity = damageSource.getAttacker();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            EnchantmentHelper.forEachEnchantment(livingEntity, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> {
                LootContext lootContext = Enchantment.createEnchantedDamageLootContext(world, level, attacker, damageSource);
                ((Enchantment)enchantment.value()).getEffect(EnchantmentEffectComponentTypes.EQUIPMENT_DROPS).forEach(effect -> {
                    if (effect.enchanted() == EnchantmentEffectTarget.ATTACKER && effect.affected() == EnchantmentEffectTarget.VICTIM && effect.test(lootContext)) {
                        mutableFloat.setValue(((EnchantmentValueEffect)effect.effect()).apply(level, random, mutableFloat.floatValue()));
                    }
                });
            });
        }
        return mutableFloat.floatValue();
    }

    public static void applyAttributeModifiers(ItemStack stack, AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).getEffect(EnchantmentEffectComponentTypes.ATTRIBUTES).forEach(effect -> {
            if (((Enchantment)enchantment.value()).definition().slots().contains(slot)) {
                attributeModifierConsumer.accept(effect.attribute(), effect.createAttributeModifier(level, slot));
            }
        }));
    }

    public static void applyAttributeModifiers(ItemStack stack, EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).getEffect(EnchantmentEffectComponentTypes.ATTRIBUTES).forEach(effect -> {
            if (((Enchantment)enchantment.value()).slotMatches(slot)) {
                attributeModifierConsumer.accept(effect.attribute(), effect.createAttributeModifier(level, slot));
            }
        }));
    }

    public static int getFishingLuckBonus(ServerWorld world, ItemStack stack, Entity user) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyFishingLuckBonus(world, level, stack, user, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getFishingTimeReduction(ServerWorld world, ItemStack stack, Entity user) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyFishingTimeReduction(world, level, stack, user, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static int getTridentReturnAcceleration(ServerWorld world, ItemStack stack, Entity user) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyTridentReturnAcceleration(world, level, stack, user, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getCrossbowChargeTime(ItemStack stack, LivingEntity user, float baseCrossbowChargeTime) {
        MutableFloat mutableFloat = new MutableFloat(baseCrossbowChargeTime);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyCrossbowChargeTime(user.getRandom(), level, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static float getTridentSpinAttackStrength(ItemStack stack, LivingEntity user) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyTridentSpinAttackStrength(user.getRandom(), level, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static boolean hasAnyEnchantmentsIn(ItemStack stack, TagKey<Enchantment> tag) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
            RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
            if (!registryEntry.isIn(tag)) continue;
            return true;
        }
        return false;
    }

    public static boolean hasAnyEnchantmentsWith(ItemStack stack, ComponentType<?> componentType) {
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> {
            if (((Enchantment)enchantment.value()).effects().contains(componentType)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.booleanValue();
    }

    public static <T> Optional<T> getEffect(ItemStack stack, ComponentType<List<T>> componentType) {
        Pair<List<T>, Integer> pair = EnchantmentHelper.getHighestLevelEffect(stack, componentType);
        if (pair != null) {
            List list = (List)pair.getFirst();
            int i = (Integer)pair.getSecond();
            return Optional.of(list.get(Math.min(i, list.size()) - 1));
        }
        return Optional.empty();
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public static <T> @Nullable Pair<T, Integer> getHighestLevelEffect(ItemStack stack, ComponentType<T> componentType) {
        @Nullable MutableObject mutableObject = new MutableObject();
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> {
            Object object;
            if ((mutableObject.get() == null || (Integer)((Pair)mutableObject.get()).getSecond() < level) && (object = ((Enchantment)enchantment.value()).effects().get(componentType)) != null) {
                mutableObject.setValue((Object)Pair.of(object, (Object)level));
            }
        });
        return (Pair)mutableObject.get();
    }

    public static Optional<EnchantmentEffectContext> chooseEquipmentWith(ComponentType<?> componentType, LivingEntity entity, Predicate<ItemStack> stackPredicate) {
        ArrayList<EnchantmentEffectContext> list = new ArrayList<EnchantmentEffectContext>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = entity.getEquippedStack(equipmentSlot);
            if (!stackPredicate.test(itemStack)) continue;
            ItemEnchantmentsComponent itemEnchantmentsComponent = itemStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
                if (!((Enchantment)registryEntry.value()).effects().contains(componentType) || !((Enchantment)registryEntry.value()).slotMatches(equipmentSlot)) continue;
                list.add(new EnchantmentEffectContext(itemStack, equipmentSlot, entity));
            }
        }
        return Util.getRandomOrEmpty(list, entity.getRandom());
    }

    public static int calculateRequiredExperienceLevel(Random random, int slotIndex, int bookshelfCount, ItemStack stack) {
        EnchantableComponent enchantableComponent = stack.get(DataComponentTypes.ENCHANTABLE);
        if (enchantableComponent == null) {
            return 0;
        }
        if (bookshelfCount > 15) {
            bookshelfCount = 15;
        }
        int i = random.nextInt(8) + 1 + (bookshelfCount >> 1) + random.nextInt(bookshelfCount + 1);
        if (slotIndex == 0) {
            return Math.max(i / 3, 1);
        }
        if (slotIndex == 1) {
            return i * 2 / 3 + 1;
        }
        return Math.max(i, bookshelfCount * 2);
    }

    public static ItemStack enchant(Random random, ItemStack stack, int level, DynamicRegistryManager dynamicRegistryManager, Optional<? extends RegistryEntryList<Enchantment>> enchantments) {
        return EnchantmentHelper.enchant(random, stack, level, enchantments.map(RegistryEntryList::stream).orElseGet(() -> dynamicRegistryManager.getOrThrow(RegistryKeys.ENCHANTMENT).streamEntries().map(reference -> reference)));
    }

    public static ItemStack enchant(Random random, ItemStack stack, int level, Stream<RegistryEntry<Enchantment>> possibleEnchantments) {
        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(random, stack, level, possibleEnchantments);
        if (stack.isOf(Items.BOOK)) {
            stack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        for (EnchantmentLevelEntry enchantmentLevelEntry : list) {
            stack.addEnchantment(enchantmentLevelEntry.enchantment(), enchantmentLevelEntry.level());
        }
        return stack;
    }

    public static List<EnchantmentLevelEntry> generateEnchantments(Random random, ItemStack stack, int level, Stream<RegistryEntry<Enchantment>> possibleEnchantments) {
        ArrayList list = Lists.newArrayList();
        EnchantableComponent enchantableComponent = stack.get(DataComponentTypes.ENCHANTABLE);
        if (enchantableComponent == null) {
            return list;
        }
        level += 1 + random.nextInt(enchantableComponent.value() / 4 + 1) + random.nextInt(enchantableComponent.value() / 4 + 1);
        float f = (random.nextFloat() + random.nextFloat() - 1.0f) * 0.15f;
        List<EnchantmentLevelEntry> list2 = EnchantmentHelper.getPossibleEntries(level = MathHelper.clamp(Math.round((float)level + (float)level * f), 1, Integer.MAX_VALUE), stack, possibleEnchantments);
        if (!list2.isEmpty()) {
            Weighting.getRandom(random, list2, EnchantmentLevelEntry::getWeight).ifPresent(list::add);
            while (random.nextInt(50) <= level) {
                if (!list.isEmpty()) {
                    EnchantmentHelper.removeConflicts(list2, (EnchantmentLevelEntry)list.getLast());
                }
                if (list2.isEmpty()) break;
                Weighting.getRandom(random, list2, EnchantmentLevelEntry::getWeight).ifPresent(list::add);
                level /= 2;
            }
        }
        return list;
    }

    public static void removeConflicts(List<EnchantmentLevelEntry> possibleEntries, EnchantmentLevelEntry pickedEntry) {
        possibleEntries.removeIf(entry -> !Enchantment.canBeCombined(pickedEntry.enchantment(), entry.enchantment()));
    }

    public static boolean isCompatible(Collection<RegistryEntry<Enchantment>> existing, RegistryEntry<Enchantment> candidate) {
        for (RegistryEntry<Enchantment> registryEntry : existing) {
            if (Enchantment.canBeCombined(registryEntry, candidate)) continue;
            return false;
        }
        return true;
    }

    public static List<EnchantmentLevelEntry> getPossibleEntries(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments) {
        ArrayList list = Lists.newArrayList();
        boolean bl = stack.isOf(Items.BOOK);
        possibleEnchantments.filter(enchantment -> ((Enchantment)enchantment.value()).isPrimaryItem(stack) || bl).forEach(enchantmentx -> {
            Enchantment enchantment = (Enchantment)enchantmentx.value();
            for (int j = enchantment.getMaxLevel(); j >= enchantment.getMinLevel(); --j) {
                if (level < enchantment.getMinPower(j) || level > enchantment.getMaxPower(j)) continue;
                list.add(new EnchantmentLevelEntry((RegistryEntry<Enchantment>)enchantmentx, j));
                break;
            }
        });
        return list;
    }

    public static void applyEnchantmentProvider(ItemStack stack, DynamicRegistryManager registryManager, RegistryKey<EnchantmentProvider> providerKey, LocalDifficulty localDifficulty, Random random) {
        EnchantmentProvider enchantmentProvider = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT_PROVIDER).get(providerKey);
        if (enchantmentProvider != null) {
            EnchantmentHelper.apply(stack, componentBuilder -> enchantmentProvider.provideEnchantments(stack, (ItemEnchantmentsComponent.Builder)componentBuilder, random, localDifficulty));
        }
    }

    @FunctionalInterface
    static interface Consumer {
        public void accept(RegistryEntry<Enchantment> var1, int var2);
    }

    @FunctionalInterface
    static interface ContextAwareConsumer {
        public void accept(RegistryEntry<Enchantment> var1, int var2, EnchantmentEffectContext var3);
    }
}

