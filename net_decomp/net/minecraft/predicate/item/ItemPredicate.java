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
import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

public record ItemPredicate(Optional<RegistryEntryList<Item>> items, NumberRange.IntRange count, ComponentsPredicate components) implements Predicate<ItemStack>
{
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.entryList(RegistryKeys.ITEM).optionalFieldOf("items").forGetter(ItemPredicate::items), (App)NumberRange.IntRange.CODEC.optionalFieldOf("count", (Object)NumberRange.IntRange.ANY).forGetter(ItemPredicate::count), (App)ComponentsPredicate.CODEC.forGetter(ItemPredicate::components)).apply((Applicative)instance, ItemPredicate::new));

    @Override
    public boolean test(ItemStack stack) {
        if (this.items.isPresent() && !stack.isIn(this.items.get())) {
            return false;
        }
        if (!this.count.test(stack.getCount())) {
            return false;
        }
        return this.components.test(stack);
    }

    @Override
    public /* synthetic */ boolean test(Object stack) {
        return this.test((ItemStack)stack);
    }

    public static class Builder {
        private Optional<RegistryEntryList<Item>> item = Optional.empty();
        private NumberRange.IntRange count = NumberRange.IntRange.ANY;
        private ComponentsPredicate components = ComponentsPredicate.EMPTY;

        public static Builder create() {
            return new Builder();
        }

        public Builder items(RegistryEntryLookup<Item> itemRegistry, ItemConvertible ... items) {
            this.item = Optional.of(RegistryEntryList.of(item -> item.asItem().getRegistryEntry(), items));
            return this;
        }

        public Builder tag(RegistryEntryLookup<Item> itemRegistry, TagKey<Item> tag) {
            this.item = Optional.of(itemRegistry.getOrThrow(tag));
            return this;
        }

        public Builder count(NumberRange.IntRange count) {
            this.count = count;
            return this;
        }

        public Builder components(ComponentsPredicate components) {
            this.components = components;
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.item, this.count, this.components);
        }
    }
}

