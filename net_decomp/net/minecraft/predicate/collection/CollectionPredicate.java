/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.collection;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.collection.CollectionContainsPredicate;
import net.minecraft.predicate.collection.CollectionCountsPredicate;

public record CollectionPredicate<T, P extends Predicate<T>>(Optional<CollectionContainsPredicate<T, P>> contains, Optional<CollectionCountsPredicate<T, P>> counts, Optional<NumberRange.IntRange> size) implements Predicate<Iterable<T>>
{
    public static <T, P extends Predicate<T>> Codec<CollectionPredicate<T, P>> createCodec(Codec<P> predicateCodec) {
        return RecordCodecBuilder.create(instance -> instance.group((App)CollectionContainsPredicate.createCodec(predicateCodec).optionalFieldOf("contains").forGetter(CollectionPredicate::contains), (App)CollectionCountsPredicate.createCodec(predicateCodec).optionalFieldOf("count").forGetter(CollectionPredicate::counts), (App)NumberRange.IntRange.CODEC.optionalFieldOf("size").forGetter(CollectionPredicate::size)).apply((Applicative)instance, CollectionPredicate::new));
    }

    @Override
    public boolean test(Iterable<T> iterable) {
        if (this.contains.isPresent() && !this.contains.get().test(iterable)) {
            return false;
        }
        if (this.counts.isPresent() && !this.counts.get().test(iterable)) {
            return false;
        }
        return !this.size.isPresent() || this.size.get().test(Iterables.size(iterable));
    }

    @Override
    public /* synthetic */ boolean test(Object collection) {
        return this.test((Iterable)collection);
    }
}

