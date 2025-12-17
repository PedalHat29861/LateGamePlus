/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.data.tag.TagProvider;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public abstract class SimpleTagProvider<T>
extends TagProvider<T> {
    protected SimpleTagProvider(DataOutput dataOutput, RegistryKey<? extends Registry<T>> registryKey, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(dataOutput, registryKey, completableFuture);
    }

    protected ProvidedTagBuilder<RegistryKey<T>, T> builder(TagKey<T> tag) {
        TagBuilder tagBuilder = this.getTagBuilder(tag);
        return ProvidedTagBuilder.of(tagBuilder);
    }
}

