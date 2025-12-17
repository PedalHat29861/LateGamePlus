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
import java.util.Set;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.SlotRanges;
import net.minecraft.inventory.StackReferenceGetter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootEntityValueSource;
import net.minecraft.loot.slot.ItemStream;
import net.minecraft.loot.slot.SlotSource;
import net.minecraft.util.context.ContextParameter;

public class SlotRangeSlotSource
implements SlotSource {
    public static final MapCodec<SlotRangeSlotSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootEntityValueSource.ENTITY_OR_BLOCK_ENTITY_CODEC.fieldOf("source").forGetter(slotRangeSlotSource -> slotRangeSlotSource.field_64159), (App)SlotRanges.CODEC.fieldOf("slots").forGetter(slotRangeSlotSource -> slotRangeSlotSource.field_64160)).apply((Applicative)instance, SlotRangeSlotSource::new));
    private final LootEntityValueSource<Object> field_64159;
    private final SlotRange field_64160;

    private SlotRangeSlotSource(LootEntityValueSource<Object> lootEntityValueSource, SlotRange slotRange) {
        this.field_64159 = lootEntityValueSource;
        this.field_64160 = slotRange;
    }

    public MapCodec<SlotRangeSlotSource> getCodec() {
        return CODEC;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(this.field_64159.contextParam());
    }

    @Override
    public final ItemStream stream(LootContext context) {
        Object object = this.field_64159.get(context);
        if (object instanceof StackReferenceGetter) {
            StackReferenceGetter stackReferenceGetter = (StackReferenceGetter)object;
            return stackReferenceGetter.getStackReferences(this.field_64160.getSlotIds());
        }
        return ItemStream.EMPTY;
    }
}

