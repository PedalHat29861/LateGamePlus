/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public interface ProvidedTagBuilder<E, T> {
    public ProvidedTagBuilder<E, T> add(E var1);

    default public ProvidedTagBuilder<E, T> add(E ... values) {
        return this.add(Arrays.stream(values));
    }

    default public ProvidedTagBuilder<E, T> add(Collection<E> values) {
        values.forEach(this::add);
        return this;
    }

    default public ProvidedTagBuilder<E, T> add(Stream<E> values) {
        values.forEach(this::add);
        return this;
    }

    public ProvidedTagBuilder<E, T> addOptional(E var1);

    public ProvidedTagBuilder<E, T> addTag(TagKey<T> var1);

    public ProvidedTagBuilder<E, T> addOptionalTag(TagKey<T> var1);

    public static <T> ProvidedTagBuilder<RegistryKey<T>, T> of(final TagBuilder builder) {
        return new ProvidedTagBuilder<RegistryKey<T>, T>(){

            @Override
            public ProvidedTagBuilder<RegistryKey<T>, T> add(RegistryKey<T> registryKey) {
                builder.add(registryKey.getValue());
                return this;
            }

            @Override
            public ProvidedTagBuilder<RegistryKey<T>, T> addOptional(RegistryKey<T> registryKey) {
                builder.addOptional(registryKey.getValue());
                return this;
            }

            @Override
            public ProvidedTagBuilder<RegistryKey<T>, T> addTag(TagKey<T> tag) {
                builder.addTag(tag.id());
                return this;
            }

            @Override
            public ProvidedTagBuilder<RegistryKey<T>, T> addOptionalTag(TagKey<T> tag) {
                builder.addOptionalTag(tag.id());
                return this;
            }
        };
    }

    default public <U> ProvidedTagBuilder<U, T> mapped(final Function<U, E> mapper) {
        final ProvidedTagBuilder providedTagBuilder = this;
        return new ProvidedTagBuilder<U, T>(this){

            @Override
            public ProvidedTagBuilder<U, T> add(U value) {
                providedTagBuilder.add(mapper.apply(value));
                return this;
            }

            @Override
            public ProvidedTagBuilder<U, T> addOptional(U value) {
                providedTagBuilder.add(mapper.apply(value));
                return this;
            }

            @Override
            public ProvidedTagBuilder<U, T> addTag(TagKey<T> tag) {
                providedTagBuilder.addTag(tag);
                return this;
            }

            @Override
            public ProvidedTagBuilder<U, T> addOptionalTag(TagKey<T> tag) {
                providedTagBuilder.addOptionalTag(tag);
                return this;
            }
        };
    }
}

