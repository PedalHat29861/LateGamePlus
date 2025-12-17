/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.slot;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.slot.ItemStream;
import net.minecraft.loot.slot.SlotSource;
import net.minecraft.loot.slot.TransformSlotSource;
import net.minecraft.predicate.item.ItemPredicate;

public class FilteredSlotSource
extends TransformSlotSource {
    public static final MapCodec<FilteredSlotSource> CODEC = RecordCodecBuilder.mapCodec(instance -> FilteredSlotSource.addSlotSourceField(instance).and((App)ItemPredicate.CODEC.fieldOf("item_filter").forGetter(source -> source.itemFilter)).apply((Applicative)instance, FilteredSlotSource::new));
    private final ItemPredicate itemFilter;

    private FilteredSlotSource(SlotSource slotSource, ItemPredicate itemFilter) {
        super(slotSource);
        this.itemFilter = itemFilter;
    }

    public MapCodec<FilteredSlotSource> getCodec() {
        return CODEC;
    }

    @Override
    protected ItemStream transform(ItemStream stream) {
        return stream.filter(this.itemFilter);
    }
}

