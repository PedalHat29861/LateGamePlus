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
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.predicate.item.ItemPredicate;

public record BundleContentsPredicate(Optional<CollectionPredicate<ItemStack, ItemPredicate>> items) implements ComponentSubPredicate<BundleContentsComponent>
{
    public static final Codec<BundleContentsPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CollectionPredicate.createCodec(ItemPredicate.CODEC).optionalFieldOf("items").forGetter(BundleContentsPredicate::items)).apply((Applicative)instance, BundleContentsPredicate::new));

    @Override
    public ComponentType<BundleContentsComponent> getComponentType() {
        return DataComponentTypes.BUNDLE_CONTENTS;
    }

    @Override
    public boolean test(BundleContentsComponent bundleContentsComponent) {
        return !this.items.isPresent() || this.items.get().test(bundleContentsComponent.iterate());
    }
}

