/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.loot;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.slot.ItemStream;

public interface ContainerComponentModifier<T> {
    public ComponentType<T> getComponentType();

    public T getDefault();

    public T apply(T var1, Stream<ItemStack> var2);

    public Stream<ItemStack> stream(T var1);

    default public void apply(ItemStack stack, T component, Stream<ItemStack> contents) {
        T object = stack.getOrDefault(this.getComponentType(), component);
        T object2 = this.apply(object, contents);
        stack.set(this.getComponentType(), object2);
    }

    default public void apply(ItemStack stack, Stream<ItemStack> contents) {
        this.apply(stack, this.getDefault(), contents);
    }

    default public void apply(ItemStack stack, UnaryOperator<ItemStack> contentsOperator) {
        T object = stack.get(this.getComponentType());
        if (object != null) {
            UnaryOperator unaryOperator = contentStack -> {
                if (contentStack.isEmpty()) {
                    return contentStack;
                }
                ItemStack itemStack = (ItemStack)contentsOperator.apply((ItemStack)contentStack);
                itemStack.capCount(itemStack.getMaxCount());
                return itemStack;
            };
            this.apply(stack, this.stream(object).map(unaryOperator));
        }
    }

    default public ItemStream stream(ItemStack stack) {
        return () -> {
            T object = stack.get(this.getComponentType());
            if (object != null) {
                return this.stream(object).filter(s -> !s.isEmpty());
            }
            return Stream.empty();
        };
    }
}

