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
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.predicate.item.ItemPredicate;

public record ContainerPredicate(Optional<CollectionPredicate<ItemStack, ItemPredicate>> items) implements ComponentSubPredicate<ContainerComponent>
{
    public static final Codec<ContainerPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CollectionPredicate.createCodec(ItemPredicate.CODEC).optionalFieldOf("items").forGetter(ContainerPredicate::items)).apply((Applicative)instance, ContainerPredicate::new));

    @Override
    public ComponentType<ContainerComponent> getComponentType() {
        return DataComponentTypes.CONTAINER;
    }

    @Override
    public boolean test(ContainerComponent containerComponent) {
        return !this.items.isPresent() || this.items.get().test(containerComponent.iterateNonEmpty());
    }
}

