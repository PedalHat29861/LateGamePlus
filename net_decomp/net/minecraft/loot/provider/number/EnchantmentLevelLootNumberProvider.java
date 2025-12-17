/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.provider.number;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;

public record EnchantmentLevelLootNumberProvider(EnchantmentLevelBasedValue amount) implements LootNumberProvider
{
    public static final MapCodec<EnchantmentLevelLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)EnchantmentLevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentLevelLootNumberProvider::amount)).apply((Applicative)instance, EnchantmentLevelLootNumberProvider::new));

    @Override
    public float nextFloat(LootContext context) {
        int i = context.getOrThrow(LootContextParameters.ENCHANTMENT_LEVEL);
        return this.amount.getValue(i);
    }

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.ENCHANTMENT_LEVEL;
    }

    public static EnchantmentLevelLootNumberProvider create(EnchantmentLevelBasedValue amount) {
        return new EnchantmentLevelLootNumberProvider(amount);
    }
}

