/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.context.ContextParameter;

public record RandomChanceWithEnchantedBonusLootCondition(float unenchantedChance, EnchantmentLevelBasedValue enchantedChance, RegistryEntry<Enchantment> enchantment) implements LootCondition
{
    public static final MapCodec<RandomChanceWithEnchantedBonusLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("unenchanted_chance").forGetter(RandomChanceWithEnchantedBonusLootCondition::unenchantedChance), (App)EnchantmentLevelBasedValue.CODEC.fieldOf("enchanted_chance").forGetter(RandomChanceWithEnchantedBonusLootCondition::enchantedChance), (App)Enchantment.ENTRY_CODEC.fieldOf("enchantment").forGetter(RandomChanceWithEnchantedBonusLootCondition::enchantment)).apply((Applicative)instance, RandomChanceWithEnchantedBonusLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.RANDOM_CHANCE_WITH_ENCHANTED_BONUS;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.ATTACKING_ENTITY);
    }

    @Override
    public boolean test(LootContext lootContext) {
        int n;
        Entity entity = lootContext.get(LootContextParameters.ATTACKING_ENTITY);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            n = EnchantmentHelper.getEquipmentLevel(this.enchantment, livingEntity);
        } else {
            n = 0;
        }
        int i = n;
        float f = i > 0 ? this.enchantedChance.getValue(i) : this.unenchantedChance;
        return lootContext.getRandom().nextFloat() < f;
    }

    public static LootCondition.Builder builder(RegistryWrapper.WrapperLookup registries, float base, float perLevelAboveFirst) {
        RegistryEntryLookup impl = registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return () -> RandomChanceWithEnchantedBonusLootCondition.method_952(base, perLevelAboveFirst, (RegistryWrapper.Impl)impl);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    private static /* synthetic */ LootCondition method_952(float f, float g, RegistryWrapper.Impl impl) {
        return new RandomChanceWithEnchantedBonusLootCondition(f, new EnchantmentLevelBasedValue.Linear(f + g, g), impl.getOrThrow(Enchantments.LOOTING));
    }
}

