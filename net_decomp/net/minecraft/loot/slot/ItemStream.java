/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.loot.slot;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;

public interface ItemStream {
    public static final ItemStream EMPTY = Stream::empty;

    public Stream<ItemStack> itemCopies();

    default public ItemStream filter(Predicate<ItemStack> predicate) {
        return new Filter(this, predicate);
    }

    default public ItemStream map(Function<ItemStack, ? extends ItemStream> function) {
        return new Map(this, function);
    }

    default public ItemStream limit(int count) {
        return new Limit(this, count);
    }

    public static ItemStream of(StackReference stackReference) {
        return () -> Stream.of(stackReference.get().copy());
    }

    public static ItemStream of(Collection<? extends StackReference> stackReferences) {
        return switch (stackReferences.size()) {
            case 0 -> EMPTY;
            case 1 -> ItemStream.of(stackReferences.iterator().next());
            default -> () -> stackReferences.stream().map(StackReference::get).map(ItemStack::copy);
        };
    }

    public static ItemStream concat(ItemStream first, ItemStream second) {
        return () -> Stream.concat(first.itemCopies(), second.itemCopies());
    }

    public static ItemStream concat(List<? extends ItemStream> streams) {
        return switch (streams.size()) {
            case 0 -> EMPTY;
            case 1 -> streams.getFirst();
            case 2 -> ItemStream.concat(streams.get(0), streams.get(1));
            default -> () -> streams.stream().flatMap(ItemStream::itemCopies);
        };
    }

    public record Filter(ItemStream slots, Predicate<ItemStack> filter) implements ItemStream
    {
        @Override
        public Stream<ItemStack> itemCopies() {
            return this.slots.itemCopies().filter(this.filter);
        }

        @Override
        public ItemStream filter(Predicate<ItemStack> predicate) {
            return new Filter(this.slots, this.filter.and(predicate));
        }
    }

    public record Map(ItemStream slots, Function<ItemStack, ? extends ItemStream> mapper) implements ItemStream
    {
        @Override
        public Stream<ItemStack> itemCopies() {
            return this.slots.itemCopies().map(this.mapper).flatMap(ItemStream::itemCopies);
        }
    }

    public record Limit(ItemStream slots, int limit) implements ItemStream
    {
        @Override
        public Stream<ItemStack> itemCopies() {
            return this.slots.itemCopies().limit(this.limit);
        }

        @Override
        public ItemStream limit(int count) {
            return new Limit(this.slots, Math.min(this.limit, count));
        }
    }
}

