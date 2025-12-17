/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P1
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.loot.slot;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.slot.ItemStream;
import net.minecraft.loot.slot.SlotSource;
import net.minecraft.loot.slot.SlotSources;
import net.minecraft.util.ErrorReporter;

public abstract class TransformSlotSource
implements SlotSource {
    protected final SlotSource slotSource;

    protected TransformSlotSource(SlotSource slotSource) {
        this.slotSource = slotSource;
    }

    public abstract MapCodec<? extends TransformSlotSource> getCodec();

    protected static <T extends TransformSlotSource> Products.P1<RecordCodecBuilder.Mu<T>, SlotSource> addSlotSourceField(RecordCodecBuilder.Instance<T> instance) {
        return instance.group((App)SlotSources.CODEC.fieldOf("slot_source").forGetter(source -> source.slotSource));
    }

    protected abstract ItemStream transform(ItemStream var1);

    @Override
    public final ItemStream stream(LootContext context) {
        return this.transform(this.slotSource.stream(context));
    }

    @Override
    public void validate(LootTableReporter reporter) {
        SlotSource.super.validate(reporter);
        this.slotSource.validate(reporter.makeChild(new ErrorReporter.MapElementContext("slot_source")));
    }
}

