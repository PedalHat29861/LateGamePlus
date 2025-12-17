/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.slot.ContentsSlotSource;
import net.minecraft.loot.slot.EmptySlotSourceType;
import net.minecraft.loot.slot.FilteredSlotSource;
import net.minecraft.loot.slot.GroupSlotSource;
import net.minecraft.loot.slot.ItemStream;
import net.minecraft.loot.slot.LimitSlotsSlotSource;
import net.minecraft.loot.slot.SlotRangeSlotSource;
import net.minecraft.loot.slot.SlotSource;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface SlotSources {
    public static final Codec<SlotSource> BASE_CODEC = Registries.SLOT_SOURCE_TYPE.getCodec().dispatch(SlotSource::getCodec, codec -> codec);
    public static final Codec<SlotSource> CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(BASE_CODEC, GroupSlotSource.INLINE_CODEC));

    public static MapCodec<? extends SlotSource> registerAndGetDefault(Registry<MapCodec<? extends SlotSource>> registry) {
        Registry.register(registry, "group", GroupSlotSource.CODEC);
        Registry.register(registry, "filtered", FilteredSlotSource.CODEC);
        Registry.register(registry, "limit_slots", LimitSlotsSlotSource.CODEC);
        Registry.register(registry, "slot_range", SlotRangeSlotSource.CODEC);
        Registry.register(registry, "contents", ContentsSlotSource.CODEC);
        return Registry.register(registry, "empty", EmptySlotSourceType.CODEC);
    }

    public static Function<LootContext, ItemStream> concat(Collection<? extends SlotSource> sources) {
        List<? extends SlotSource> list = List.copyOf(sources);
        return switch (list.size()) {
            case 0 -> context -> ItemStream.EMPTY;
            case 1 -> list.getFirst()::stream;
            case 2 -> {
                SlotSource slotSource = list.get(0);
                SlotSource slotSource2 = list.get(1);
                yield context -> ItemStream.concat(slotSource.stream((LootContext)context), slotSource2.stream((LootContext)context));
            }
            default -> context -> {
                ArrayList<ItemStream> list2 = new ArrayList<ItemStream>();
                for (SlotSource slotSource : list) {
                    list2.add(slotSource.stream((LootContext)context));
                }
                return ItemStream.concat(list2);
            };
        };
    }
}

