/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

public interface VariantSelectorProvider<Context, Condition extends SelectorCondition<Context>> {
    public List<Selector<Context, Condition>> getSelectors();

    public static <C, T> Stream<T> select(Stream<T> entries, Function<T, VariantSelectorProvider<C, ?>> providerGetter, C context) {
        ArrayList list = new ArrayList();
        entries.forEach(entry -> {
            VariantSelectorProvider variantSelectorProvider = (VariantSelectorProvider)providerGetter.apply(entry);
            for (Selector selector : variantSelectorProvider.getSelectors()) {
                list.add(new UnwrappedSelector(entry, selector.priority(), (SelectorCondition)DataFixUtils.orElseGet(selector.condition(), SelectorCondition::alwaysTrue)));
            }
        });
        list.sort(UnwrappedSelector.PRIORITY_COMPARATOR);
        Iterator iterator = list.iterator();
        int i = Integer.MIN_VALUE;
        while (iterator.hasNext()) {
            UnwrappedSelector unwrappedSelector = (UnwrappedSelector)iterator.next();
            if (unwrappedSelector.priority < i) {
                iterator.remove();
                continue;
            }
            if (unwrappedSelector.condition.test(context)) {
                i = unwrappedSelector.priority;
                continue;
            }
            iterator.remove();
        }
        return list.stream().map(UnwrappedSelector::entry);
    }

    public static <C, T> Optional<T> select(Stream<T> entries, Function<T, VariantSelectorProvider<C, ?>> providerGetter, Random random, C context) {
        List<T> list = VariantSelectorProvider.select(entries, providerGetter, context).toList();
        return Util.getRandomOrEmpty(list, random);
    }

    public static <Context, Condition extends SelectorCondition<Context>> List<Selector<Context, Condition>> createSingle(Condition condition, int priority) {
        return List.of(new Selector(condition, priority));
    }

    public static <Context, Condition extends SelectorCondition<Context>> List<Selector<Context, Condition>> createFallback(int priority) {
        return List.of(new Selector(Optional.empty(), priority));
    }

    public static final class UnwrappedSelector<C, T>
    extends Record {
        private final T entry;
        final int priority;
        final SelectorCondition<C> condition;
        public static final Comparator<UnwrappedSelector<?, ?>> PRIORITY_COMPARATOR = Comparator.comparingInt(UnwrappedSelector::priority).reversed();

        public UnwrappedSelector(T entry, int priority, SelectorCondition<C> condition) {
            this.entry = entry;
            this.priority = priority;
            this.condition = condition;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{UnwrappedSelector.class, "entry;priority;condition", "entry", "priority", "condition"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{UnwrappedSelector.class, "entry;priority;condition", "entry", "priority", "condition"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{UnwrappedSelector.class, "entry;priority;condition", "entry", "priority", "condition"}, this, object);
        }

        public T entry() {
            return this.entry;
        }

        public int priority() {
            return this.priority;
        }

        public SelectorCondition<C> condition() {
            return this.condition;
        }
    }

    @FunctionalInterface
    public static interface SelectorCondition<C>
    extends Predicate<C> {
        public static <C> SelectorCondition<C> alwaysTrue() {
            return context -> true;
        }
    }

    public record Selector<Context, Condition extends SelectorCondition<Context>>(Optional<Condition> condition, int priority) {
        public Selector(Condition condition, int priority) {
            this(Optional.of(condition), priority);
        }

        public Selector(int priority) {
            this(Optional.empty(), priority);
        }

        public static <Context, Condition extends SelectorCondition<Context>> Codec<Selector<Context, Condition>> createCodec(Codec<Condition> conditionCodec) {
            return RecordCodecBuilder.create(instance -> instance.group((App)conditionCodec.optionalFieldOf("condition").forGetter(Selector::condition), (App)Codec.INT.fieldOf("priority").forGetter(Selector::priority)).apply((Applicative)instance, Selector::new));
        }
    }
}

