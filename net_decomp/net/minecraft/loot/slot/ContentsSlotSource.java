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
import net.minecraft.loot.ContainerComponentModifier;
import net.minecraft.loot.ContainerComponentModifiers;
import net.minecraft.loot.slot.ItemStream;
import net.minecraft.loot.slot.SlotSource;
import net.minecraft.loot.slot.TransformSlotSource;

public class ContentsSlotSource
extends TransformSlotSource {
    public static final MapCodec<ContentsSlotSource> CODEC = RecordCodecBuilder.mapCodec(instance -> ContentsSlotSource.addSlotSourceField(instance).and((App)ContainerComponentModifiers.MODIFIER_CODEC.fieldOf("component").forGetter(source -> source.component)).apply((Applicative)instance, ContentsSlotSource::new));
    private final ContainerComponentModifier<?> component;

    private ContentsSlotSource(SlotSource slotSource, ContainerComponentModifier<?> component) {
        super(slotSource);
        this.component = component;
    }

    public MapCodec<ContentsSlotSource> getCodec() {
        return CODEC;
    }

    @Override
    protected ItemStream transform(ItemStream stream) {
        return stream.map(this.component::stream);
    }
}

