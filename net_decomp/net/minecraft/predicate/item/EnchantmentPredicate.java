/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

public record EnchantmentPredicate(Optional<RegistryEntryList<Enchantment>> enchantments, NumberRange.IntRange levels) {
    public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.entryList(RegistryKeys.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(EnchantmentPredicate::enchantments), (App)NumberRange.IntRange.CODEC.optionalFieldOf("levels", (Object)NumberRange.IntRange.ANY).forGetter(EnchantmentPredicate::levels)).apply((Applicative)instance, EnchantmentPredicate::new));

    public EnchantmentPredicate(RegistryEntry<Enchantment> enchantment, NumberRange.IntRange levels) {
        this(Optional.of(RegistryEntryList.of(enchantment)), levels);
    }

    public EnchantmentPredicate(RegistryEntryList<Enchantment> enchantments, NumberRange.IntRange levels) {
        this(Optional.of(enchantments), levels);
    }

    public boolean test(ItemEnchantmentsComponent enchantmentsComponent) {
        if (this.enchantments.isPresent()) {
            for (RegistryEntry registryEntry : this.enchantments.get()) {
                if (!this.testLevel(enchantmentsComponent, registryEntry)) continue;
                return true;
            }
            return false;
        }
        if (this.levels != NumberRange.IntRange.ANY) {
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantmentsComponent.getEnchantmentEntries()) {
                if (!this.levels.test(entry.getIntValue())) continue;
                return true;
            }
            return false;
        }
        return !enchantmentsComponent.isEmpty();
    }

    private boolean testLevel(ItemEnchantmentsComponent enchantmentsComponent, RegistryEntry<Enchantment> enchantment) {
        int i = enchantmentsComponent.getLevel(enchantment);
        if (i == 0) {
            return false;
        }
        if (this.levels == NumberRange.IntRange.ANY) {
            return true;
        }
        return this.levels.test(i);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EnchantmentPredicate.class, "enchantments;level", "enchantments", "levels"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EnchantmentPredicate.class, "enchantments;level", "enchantments", "levels"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EnchantmentPredicate.class, "enchantments;level", "enchantments", "levels"}, this, object);
    }
}

