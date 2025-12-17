/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.recipe.display;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.SmithingTrimRecipe;
import net.minecraft.recipe.display.DisplayedItemFactory;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.util.math.random.Random;

public interface SlotDisplay {
    public static final Codec<SlotDisplay> CODEC = Registries.SLOT_DISPLAY.getCodec().dispatch(SlotDisplay::serializer, Serializer::codec);
    public static final PacketCodec<RegistryByteBuf, SlotDisplay> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.SLOT_DISPLAY).dispatch(SlotDisplay::serializer, Serializer::streamCodec);

    public <T> Stream<T> appendStacks(ContextParameterMap var1, DisplayedItemFactory<T> var2);

    public Serializer<? extends SlotDisplay> serializer();

    default public boolean isEnabled(FeatureSet features) {
        return true;
    }

    default public List<ItemStack> getStacks(ContextParameterMap parameters) {
        return this.appendStacks(parameters, NoopDisplayedItemFactory.INSTANCE).toList();
    }

    default public ItemStack getFirst(ContextParameterMap context) {
        return this.appendStacks(context, NoopDisplayedItemFactory.INSTANCE).findFirst().orElse(ItemStack.EMPTY);
    }

    public static class NoopDisplayedItemFactory
    implements DisplayedItemFactory.FromStack<ItemStack> {
        public static final NoopDisplayedItemFactory INSTANCE = new NoopDisplayedItemFactory();

        @Override
        public ItemStack toDisplayed(ItemStack itemStack) {
            return itemStack;
        }

        @Override
        public /* synthetic */ Object toDisplayed(ItemStack stack) {
            return this.toDisplayed(stack);
        }
    }

    public record WithRemainderSlotDisplay(SlotDisplay input, SlotDisplay remainder) implements SlotDisplay
    {
        public static final MapCodec<WithRemainderSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.fieldOf("input").forGetter(WithRemainderSlotDisplay::input), (App)CODEC.fieldOf("remainder").forGetter(WithRemainderSlotDisplay::remainder)).apply((Applicative)instance, WithRemainderSlotDisplay::new));
        public static final PacketCodec<RegistryByteBuf, WithRemainderSlotDisplay> PACKET_CODEC = PacketCodec.tuple(PACKET_CODEC, WithRemainderSlotDisplay::input, PACKET_CODEC, WithRemainderSlotDisplay::remainder, WithRemainderSlotDisplay::new);
        public static final Serializer<WithRemainderSlotDisplay> SERIALIZER = new Serializer<WithRemainderSlotDisplay>(CODEC, PACKET_CODEC);

        public Serializer<WithRemainderSlotDisplay> serializer() {
            return SERIALIZER;
        }

        @Override
        public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
            if (factory instanceof DisplayedItemFactory.FromRemainder) {
                DisplayedItemFactory.FromRemainder fromRemainder = (DisplayedItemFactory.FromRemainder)factory;
                List list = this.remainder.appendStacks(parameters, factory).toList();
                return this.input.appendStacks(parameters, factory).map(input -> fromRemainder.toDisplayed(input, list));
            }
            return this.input.appendStacks(parameters, factory);
        }

        @Override
        public boolean isEnabled(FeatureSet features) {
            return this.input.isEnabled(features) && this.remainder.isEnabled(features);
        }
    }

    public record CompositeSlotDisplay(List<SlotDisplay> contents) implements SlotDisplay
    {
        public static final MapCodec<CompositeSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.listOf().fieldOf("contents").forGetter(CompositeSlotDisplay::contents)).apply((Applicative)instance, CompositeSlotDisplay::new));
        public static final PacketCodec<RegistryByteBuf, CompositeSlotDisplay> PACKET_CODEC = PacketCodec.tuple(PACKET_CODEC.collect(PacketCodecs.toList()), CompositeSlotDisplay::contents, CompositeSlotDisplay::new);
        public static final Serializer<CompositeSlotDisplay> SERIALIZER = new Serializer<CompositeSlotDisplay>(CODEC, PACKET_CODEC);

        public Serializer<CompositeSlotDisplay> serializer() {
            return SERIALIZER;
        }

        @Override
        public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
            return this.contents.stream().flatMap(display -> display.appendStacks(parameters, factory));
        }

        @Override
        public boolean isEnabled(FeatureSet features) {
            return this.contents.stream().allMatch(child -> child.isEnabled(features));
        }
    }

    public record TagSlotDisplay(TagKey<Item> tag) implements SlotDisplay
    {
        public static final MapCodec<TagSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TagKey.unprefixedCodec(RegistryKeys.ITEM).fieldOf("tag").forGetter(TagSlotDisplay::tag)).apply((Applicative)instance, TagSlotDisplay::new));
        public static final PacketCodec<RegistryByteBuf, TagSlotDisplay> PACKET_CODEC = PacketCodec.tuple(TagKey.packetCodec(RegistryKeys.ITEM), TagSlotDisplay::tag, TagSlotDisplay::new);
        public static final Serializer<TagSlotDisplay> SERIALIZER = new Serializer<TagSlotDisplay>(CODEC, PACKET_CODEC);

        public Serializer<TagSlotDisplay> serializer() {
            return SERIALIZER;
        }

        @Override
        public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
            if (factory instanceof DisplayedItemFactory.FromStack) {
                DisplayedItemFactory.FromStack fromStack = (DisplayedItemFactory.FromStack)factory;
                RegistryWrapper.WrapperLookup wrapperLookup = parameters.getNullable(SlotDisplayContexts.REGISTRIES);
                if (wrapperLookup != null) {
                    return wrapperLookup.getOrThrow(RegistryKeys.ITEM).getOptional(this.tag).map(tag -> tag.stream().map(fromStack::toDisplayed)).stream().flatMap(values -> values);
                }
            }
            return Stream.empty();
        }
    }

    public record StackSlotDisplay(ItemStack stack) implements SlotDisplay
    {
        public static final MapCodec<StackSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ItemStack.VALIDATED_CODEC.fieldOf("item").forGetter(StackSlotDisplay::stack)).apply((Applicative)instance, StackSlotDisplay::new));
        public static final PacketCodec<RegistryByteBuf, StackSlotDisplay> PACKET_CODEC = PacketCodec.tuple(ItemStack.PACKET_CODEC, StackSlotDisplay::stack, StackSlotDisplay::new);
        public static final Serializer<StackSlotDisplay> SERIALIZER = new Serializer<StackSlotDisplay>(CODEC, PACKET_CODEC);

        public Serializer<StackSlotDisplay> serializer() {
            return SERIALIZER;
        }

        @Override
        public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
            if (factory instanceof DisplayedItemFactory.FromStack) {
                DisplayedItemFactory.FromStack fromStack = (DisplayedItemFactory.FromStack)factory;
                return Stream.of(fromStack.toDisplayed(this.stack));
            }
            return Stream.empty();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StackSlotDisplay)) return false;
            StackSlotDisplay stackSlotDisplay = (StackSlotDisplay)o;
            if (!ItemStack.areEqual(this.stack, stackSlotDisplay.stack)) return false;
            return true;
        }

        @Override
        public boolean isEnabled(FeatureSet features) {
            return this.stack.getItem().isEnabled(features);
        }
    }

    public record ItemSlotDisplay(RegistryEntry<Item> item) implements SlotDisplay
    {
        public static final MapCodec<ItemSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Item.ENTRY_CODEC.fieldOf("item").forGetter(ItemSlotDisplay::item)).apply((Applicative)instance, ItemSlotDisplay::new));
        public static final PacketCodec<RegistryByteBuf, ItemSlotDisplay> PACKET_CODEC = PacketCodec.tuple(Item.ENTRY_PACKET_CODEC, ItemSlotDisplay::item, ItemSlotDisplay::new);
        public static final Serializer<ItemSlotDisplay> SERIALIZER = new Serializer<ItemSlotDisplay>(CODEC, PACKET_CODEC);

        public ItemSlotDisplay(Item item) {
            this(item.getRegistryEntry());
        }

        public Serializer<ItemSlotDisplay> serializer() {
            return SERIALIZER;
        }

        @Override
        public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
            if (factory instanceof DisplayedItemFactory.FromStack) {
                DisplayedItemFactory.FromStack fromStack = (DisplayedItemFactory.FromStack)factory;
                return Stream.of(fromStack.toDisplayed(this.item));
            }
            return Stream.empty();
        }

        @Override
        public boolean isEnabled(FeatureSet features) {
            return this.item.value().isEnabled(features);
        }
    }

    public record SmithingTrimSlotDisplay(SlotDisplay base, SlotDisplay material, RegistryEntry<ArmorTrimPattern> pattern) implements SlotDisplay
    {
        public static final MapCodec<SmithingTrimSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.fieldOf("base").forGetter(SmithingTrimSlotDisplay::base), (App)CODEC.fieldOf("material").forGetter(SmithingTrimSlotDisplay::material), (App)ArmorTrimPattern.ENTRY_CODEC.fieldOf("pattern").forGetter(SmithingTrimSlotDisplay::pattern)).apply((Applicative)instance, SmithingTrimSlotDisplay::new));
        public static final PacketCodec<RegistryByteBuf, SmithingTrimSlotDisplay> PACKET_CODEC = PacketCodec.tuple(PACKET_CODEC, SmithingTrimSlotDisplay::base, PACKET_CODEC, SmithingTrimSlotDisplay::material, ArmorTrimPattern.ENTRY_PACKET_CODEC, SmithingTrimSlotDisplay::pattern, SmithingTrimSlotDisplay::new);
        public static final Serializer<SmithingTrimSlotDisplay> SERIALIZER = new Serializer<SmithingTrimSlotDisplay>(CODEC, PACKET_CODEC);

        public Serializer<SmithingTrimSlotDisplay> serializer() {
            return SERIALIZER;
        }

        @Override
        public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
            if (factory instanceof DisplayedItemFactory.FromStack) {
                DisplayedItemFactory.FromStack fromStack = (DisplayedItemFactory.FromStack)factory;
                RegistryWrapper.WrapperLookup wrapperLookup = parameters.getNullable(SlotDisplayContexts.REGISTRIES);
                if (wrapperLookup != null) {
                    Random random = Random.create(System.identityHashCode(this));
                    List<ItemStack> list = this.base.getStacks(parameters);
                    if (list.isEmpty()) {
                        return Stream.empty();
                    }
                    List<ItemStack> list2 = this.material.getStacks(parameters);
                    if (list2.isEmpty()) {
                        return Stream.empty();
                    }
                    return Stream.generate(() -> {
                        ItemStack itemStack = (ItemStack)Util.getRandom(list, random);
                        ItemStack itemStack2 = (ItemStack)Util.getRandom(list2, random);
                        return SmithingTrimRecipe.craft(wrapperLookup, itemStack, itemStack2, this.pattern);
                    }).limit(256L).filter(stack -> !stack.isEmpty()).limit(16L).map(fromStack::toDisplayed);
                }
            }
            return Stream.empty();
        }
    }

    public static class AnyFuelSlotDisplay
    implements SlotDisplay {
        public static final AnyFuelSlotDisplay INSTANCE = new AnyFuelSlotDisplay();
        public static final MapCodec<AnyFuelSlotDisplay> CODEC = MapCodec.unit((Object)INSTANCE);
        public static final PacketCodec<RegistryByteBuf, AnyFuelSlotDisplay> PACKET_CODEC = PacketCodec.unit(INSTANCE);
        public static final Serializer<AnyFuelSlotDisplay> SERIALIZER = new Serializer<AnyFuelSlotDisplay>(CODEC, PACKET_CODEC);

        private AnyFuelSlotDisplay() {
        }

        public Serializer<AnyFuelSlotDisplay> serializer() {
            return SERIALIZER;
        }

        public String toString() {
            return "<any fuel>";
        }

        @Override
        public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
            if (factory instanceof DisplayedItemFactory.FromStack) {
                DisplayedItemFactory.FromStack fromStack = (DisplayedItemFactory.FromStack)factory;
                FuelRegistry fuelRegistry = parameters.getNullable(SlotDisplayContexts.FUEL_REGISTRY);
                if (fuelRegistry != null) {
                    return fuelRegistry.getFuelItems().stream().map(fromStack::toDisplayed);
                }
            }
            return Stream.empty();
        }
    }

    public static class EmptySlotDisplay
    implements SlotDisplay {
        public static final EmptySlotDisplay INSTANCE = new EmptySlotDisplay();
        public static final MapCodec<EmptySlotDisplay> CODEC = MapCodec.unit((Object)INSTANCE);
        public static final PacketCodec<RegistryByteBuf, EmptySlotDisplay> PACKET_CODEC = PacketCodec.unit(INSTANCE);
        public static final Serializer<EmptySlotDisplay> SERIALIZER = new Serializer<EmptySlotDisplay>(CODEC, PACKET_CODEC);

        private EmptySlotDisplay() {
        }

        public Serializer<EmptySlotDisplay> serializer() {
            return SERIALIZER;
        }

        public String toString() {
            return "<empty>";
        }

        @Override
        public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
            return Stream.empty();
        }
    }

    public record Serializer<T extends SlotDisplay>(MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> streamCodec) {
    }
}

