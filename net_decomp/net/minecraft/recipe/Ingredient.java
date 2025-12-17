/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.recipe;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraft.util.dynamic.Codecs;

public final class Ingredient
implements RecipeMatcher.RawIngredient<RegistryEntry<Item>>,
Predicate<ItemStack> {
    public static final PacketCodec<RegistryByteBuf, Ingredient> PACKET_CODEC = PacketCodecs.registryEntryList(RegistryKeys.ITEM).xmap(Ingredient::new, ingredient -> ingredient.entries);
    public static final PacketCodec<RegistryByteBuf, Optional<Ingredient>> OPTIONAL_PACKET_CODEC = PacketCodecs.registryEntryList(RegistryKeys.ITEM).xmap(entries -> entries.size() == 0 ? Optional.empty() : Optional.of(new Ingredient((RegistryEntryList<Item>)entries)), optional -> optional.map(ingredient -> ingredient.entries).orElse(RegistryEntryList.of(new RegistryEntry[0])));
    public static final Codec<RegistryEntryList<Item>> ENTRIES_CODEC = RegistryEntryListCodec.create(RegistryKeys.ITEM, Item.ENTRY_CODEC, false);
    public static final Codec<Ingredient> CODEC = Codecs.nonEmptyEntryList(ENTRIES_CODEC).xmap(Ingredient::new, ingredient -> ingredient.entries);
    private final RegistryEntryList<Item> entries;

    private Ingredient(RegistryEntryList<Item> entries) {
        entries.getStorage().ifRight(list -> {
            if (list.isEmpty()) {
                throw new UnsupportedOperationException("Ingredients can't be empty");
            }
            if (list.contains(Items.AIR.getRegistryEntry())) {
                throw new UnsupportedOperationException("Ingredient can't contain air");
            }
        });
        this.entries = entries;
    }

    public static boolean matches(Optional<Ingredient> ingredient, ItemStack stack) {
        return ingredient.map(ingredient2 -> ingredient2.test(stack)).orElseGet(stack::isEmpty);
    }

    @Deprecated
    public Stream<RegistryEntry<Item>> getMatchingItems() {
        return this.entries.stream();
    }

    public boolean isEmpty() {
        return this.entries.size() == 0;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return itemStack.isIn(this.entries);
    }

    @Override
    public boolean acceptsItem(RegistryEntry<Item> registryEntry) {
        return this.entries.contains(registryEntry);
    }

    public boolean equals(Object o) {
        if (o instanceof Ingredient) {
            Ingredient ingredient = (Ingredient)o;
            return Objects.equals(this.entries, ingredient.entries);
        }
        return false;
    }

    public static Ingredient ofItem(ItemConvertible item) {
        return new Ingredient(RegistryEntryList.of(item.asItem().getRegistryEntry()));
    }

    public static Ingredient ofItems(ItemConvertible ... items) {
        return Ingredient.ofItems(Arrays.stream(items));
    }

    public static Ingredient ofItems(Stream<? extends ItemConvertible> stacks) {
        return new Ingredient(RegistryEntryList.of(stacks.map(item -> item.asItem().getRegistryEntry()).toList()));
    }

    public static Ingredient ofTag(RegistryEntryList<Item> tag) {
        return new Ingredient(tag);
    }

    public SlotDisplay toDisplay() {
        return (SlotDisplay)this.entries.getStorage().map(SlotDisplay.TagSlotDisplay::new, items -> new SlotDisplay.CompositeSlotDisplay(items.stream().map(Ingredient::createDisplayWithRemainder).toList()));
    }

    public static SlotDisplay toDisplay(Optional<Ingredient> ingredient) {
        return ingredient.map(Ingredient::toDisplay).orElse(SlotDisplay.EmptySlotDisplay.INSTANCE);
    }

    private static SlotDisplay createDisplayWithRemainder(RegistryEntry<Item> displayedItem) {
        SlotDisplay.ItemSlotDisplay slotDisplay = new SlotDisplay.ItemSlotDisplay(displayedItem);
        ItemStack itemStack = displayedItem.value().getRecipeRemainder();
        if (!itemStack.isEmpty()) {
            SlotDisplay.StackSlotDisplay slotDisplay2 = new SlotDisplay.StackSlotDisplay(itemStack);
            return new SlotDisplay.WithRemainderSlotDisplay(slotDisplay, slotDisplay2);
        }
        return slotDisplay;
    }

    @Override
    public /* synthetic */ boolean test(Object stack) {
        return this.test((ItemStack)stack);
    }

    @Override
    public /* synthetic */ boolean acceptsItem(Object object) {
        return this.acceptsItem((RegistryEntry)object);
    }
}

