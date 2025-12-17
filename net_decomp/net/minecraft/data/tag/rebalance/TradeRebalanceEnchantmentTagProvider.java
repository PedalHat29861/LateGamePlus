/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.rebalance;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.SimpleTagProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;

public class TradeRebalanceEnchantmentTagProvider
extends SimpleTagProvider<Enchantment> {
    public TradeRebalanceEnchantmentTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ENCHANTMENT, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(EnchantmentTags.DESERT_COMMON_TRADE).add(Enchantments.FIRE_PROTECTION, Enchantments.THORNS, Enchantments.INFINITY);
        this.builder(EnchantmentTags.JUNGLE_COMMON_TRADE).add(Enchantments.FEATHER_FALLING, Enchantments.PROJECTILE_PROTECTION, Enchantments.POWER);
        this.builder(EnchantmentTags.PLAINS_COMMON_TRADE).add(Enchantments.PUNCH, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS);
        this.builder(EnchantmentTags.SAVANNA_COMMON_TRADE).add(Enchantments.KNOCKBACK, Enchantments.BINDING_CURSE, Enchantments.SWEEPING_EDGE);
        this.builder(EnchantmentTags.SNOW_COMMON_TRADE).add(Enchantments.AQUA_AFFINITY, Enchantments.LOOTING, Enchantments.FROST_WALKER);
        this.builder(EnchantmentTags.SWAMP_COMMON_TRADE).add(Enchantments.DEPTH_STRIDER, Enchantments.RESPIRATION, Enchantments.VANISHING_CURSE);
        this.builder(EnchantmentTags.TAIGA_COMMON_TRADE).add(Enchantments.BLAST_PROTECTION, Enchantments.FIRE_ASPECT, Enchantments.FLAME);
        this.builder(EnchantmentTags.DESERT_SPECIAL_TRADE).add(Enchantments.EFFICIENCY);
        this.builder(EnchantmentTags.JUNGLE_SPECIAL_TRADE).add(Enchantments.UNBREAKING);
        this.builder(EnchantmentTags.PLAINS_SPECIAL_TRADE).add(Enchantments.PROTECTION);
        this.builder(EnchantmentTags.SAVANNA_SPECIAL_TRADE).add(Enchantments.SHARPNESS);
        this.builder(EnchantmentTags.SNOW_SPECIAL_TRADE).add(Enchantments.SILK_TOUCH);
        this.builder(EnchantmentTags.SWAMP_SPECIAL_TRADE).add(Enchantments.MENDING);
        this.builder(EnchantmentTags.TAIGA_SPECIAL_TRADE).add(Enchantments.FORTUNE);
    }
}

