/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.data.tag.TagProvider;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public abstract class ValueLookupTagProvider<T>
extends TagProvider<T> {
    private final Function<T, RegistryKey<T>> valueToKey;

    public ValueLookupTagProvider(DataOutput output, RegistryKey<? extends Registry<T>> registryRef, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, Function<T, RegistryKey<T>> valueToKey) {
        super(output, registryRef, registriesFuture);
        this.valueToKey = valueToKey;
    }

    public ValueLookupTagProvider(DataOutput output, RegistryKey<? extends Registry<T>> registryRef, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, CompletableFuture<TagProvider.TagLookup<T>> parentTagLookupFuture, Function<T, RegistryKey<T>> valueToKey) {
        super(output, registryRef, registriesFuture, parentTagLookupFuture);
        this.valueToKey = valueToKey;
    }

    protected ProvidedTagBuilder<T, T> builder(TagKey<T> tag) {
        TagBuilder tagBuilder = this.getTagBuilder(tag);
        return ProvidedTagBuilder.of(tagBuilder).mapped(this.valueToKey);
    }
}

