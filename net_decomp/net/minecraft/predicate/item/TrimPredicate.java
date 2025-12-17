/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;

public record TrimPredicate(Optional<RegistryEntryList<ArmorTrimMaterial>> material, Optional<RegistryEntryList<ArmorTrimPattern>> pattern) implements ComponentSubPredicate<ArmorTrim>
{
    public static final Codec<TrimPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.entryList(RegistryKeys.TRIM_MATERIAL).optionalFieldOf("material").forGetter(TrimPredicate::material), (App)RegistryCodecs.entryList(RegistryKeys.TRIM_PATTERN).optionalFieldOf("pattern").forGetter(TrimPredicate::pattern)).apply((Applicative)instance, TrimPredicate::new));

    @Override
    public ComponentType<ArmorTrim> getComponentType() {
        return DataComponentTypes.TRIM;
    }

    @Override
    public boolean test(ArmorTrim armorTrim) {
        if (this.material.isPresent() && !this.material.get().contains(armorTrim.material())) {
            return false;
        }
        return !this.pattern.isPresent() || this.pattern.get().contains(armorTrim.pattern());
    }
}

