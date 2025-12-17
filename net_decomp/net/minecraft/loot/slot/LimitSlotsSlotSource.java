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
import net.minecraft.util.dynamic.Codecs;

public class LimitSlotsSlotSource
extends TransformSlotSource {
    public static final MapCodec<LimitSlotsSlotSource> CODEC = RecordCodecBuilder.mapCodec(instance -> LimitSlotsSlotSource.addSlotSourceField(instance).and((App)Codecs.POSITIVE_INT.fieldOf("limit").forGetter(source -> source.limit)).apply((Applicative)instance, LimitSlotsSlotSource::new));
    private final int limit;

    private LimitSlotsSlotSource(SlotSource slotSource, int limit) {
        super(slotSource);
        this.limit = limit;
    }

    public MapCodec<LimitSlotsSlotSource> getCodec() {
        return CODEC;
    }

    @Override
    protected ItemStream transform(ItemStream stream) {
        return stream.limit(this.limit);
    }
}

