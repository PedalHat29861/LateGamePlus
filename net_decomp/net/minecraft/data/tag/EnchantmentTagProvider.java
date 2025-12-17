/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.SimpleTagProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;

public abstract class EnchantmentTagProvider
extends SimpleTagProvider<Enchantment> {
    public EnchantmentTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ENCHANTMENT, registriesFuture);
    }

    protected void createTooltipOrderTag(RegistryWrapper.WrapperLookup registries, RegistryKey<Enchantment> ... enchantments) {
        this.builder(EnchantmentTags.TOOLTIP_ORDER).add(enchantments);
        Set<RegistryKey<Enchantment>> set = Set.of(enchantments);
        List list = registries.getOrThrow(RegistryKeys.ENCHANTMENT).streamEntries().filter(entry -> !set.contains(entry.getKey().get())).map(RegistryEntry::getIdAsString).collect(Collectors.toList());
        if (!list.isEmpty()) {
            throw new IllegalStateException("Not all enchantments were registered for tooltip ordering. Missing: " + String.join((CharSequence)", ", list));
        }
    }
}

