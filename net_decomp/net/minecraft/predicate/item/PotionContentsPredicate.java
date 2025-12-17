/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.predicate.item;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potion;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

public record PotionContentsPredicate(RegistryEntryList<Potion> potions) implements ComponentSubPredicate<PotionContentsComponent>
{
    public static final Codec<PotionContentsPredicate> CODEC = RegistryCodecs.entryList(RegistryKeys.POTION).xmap(PotionContentsPredicate::new, PotionContentsPredicate::potions);

    @Override
    public ComponentType<PotionContentsComponent> getComponentType() {
        return DataComponentTypes.POTION_CONTENTS;
    }

    @Override
    public boolean test(PotionContentsComponent potionContentsComponent) {
        Optional<RegistryEntry<Potion>> optional = potionContentsComponent.potion();
        return !optional.isEmpty() && this.potions.contains(optional.get());
    }

    public static ComponentPredicate potionContents(RegistryEntryList<Potion> potions) {
        return new PotionContentsPredicate(potions);
    }
}

