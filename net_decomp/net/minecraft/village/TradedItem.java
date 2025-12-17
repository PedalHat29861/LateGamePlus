/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.village;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.component.ComponentMapPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Codecs;

public record TradedItem(RegistryEntry<Item> item, int count, ComponentMapPredicate components, ItemStack itemStack) {
    public static final Codec<TradedItem> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Item.ENTRY_CODEC.fieldOf("id").forGetter(TradedItem::item), (App)Codecs.POSITIVE_INT.fieldOf("count").orElse((Object)1).forGetter(TradedItem::count), (App)ComponentMapPredicate.CODEC.optionalFieldOf("components", (Object)ComponentMapPredicate.EMPTY).forGetter(TradedItem::components)).apply((Applicative)instance, TradedItem::new));
    public static final PacketCodec<RegistryByteBuf, TradedItem> PACKET_CODEC = PacketCodec.tuple(Item.ENTRY_PACKET_CODEC, TradedItem::item, PacketCodecs.VAR_INT, TradedItem::count, ComponentMapPredicate.PACKET_CODEC, TradedItem::components, TradedItem::new);
    public static final PacketCodec<RegistryByteBuf, Optional<TradedItem>> OPTIONAL_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs::optional);

    public TradedItem(ItemConvertible item) {
        this(item, 1);
    }

    public TradedItem(ItemConvertible item, int count) {
        this(item.asItem().getRegistryEntry(), count, ComponentMapPredicate.EMPTY);
    }

    public TradedItem(RegistryEntry<Item> item, int count, ComponentMapPredicate components) {
        this(item, count, components, TradedItem.createDisplayStack(item, count, components));
    }

    public TradedItem withComponents(UnaryOperator<ComponentMapPredicate.Builder> builderCallback) {
        return new TradedItem(this.item, this.count, ((ComponentMapPredicate.Builder)builderCallback.apply(ComponentMapPredicate.builder())).build());
    }

    private static ItemStack createDisplayStack(RegistryEntry<Item> item, int count, ComponentMapPredicate components) {
        return new ItemStack(item, count, components.toChanges());
    }

    public boolean matches(ItemStack stack) {
        return stack.itemMatches(this.item) && this.components.test(stack);
    }
}

