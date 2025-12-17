/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Lifecycle
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.registry;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

public class SimpleDefaultedRegistry<T>
extends SimpleRegistry<T>
implements DefaultedRegistry<T> {
    private final Identifier defaultId;
    private RegistryEntry.Reference<T> defaultEntry;

    public SimpleDefaultedRegistry(String defaultId, RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, boolean intrusive) {
        super(key, lifecycle, intrusive);
        this.defaultId = Identifier.of(defaultId);
    }

    @Override
    public RegistryEntry.Reference<T> add(RegistryKey<T> key, T value, RegistryEntryInfo info) {
        RegistryEntry.Reference<T> reference = super.add(key, value, info);
        if (this.defaultId.equals(key.getValue())) {
            this.defaultEntry = reference;
        }
        return reference;
    }

    @Override
    public int getRawId(@Nullable T value) {
        int i = super.getRawId(value);
        return i == -1 ? super.getRawId(this.defaultEntry.value()) : i;
    }

    @Override
    public Identifier getId(T value) {
        Identifier identifier = super.getId(value);
        return identifier == null ? this.defaultId : identifier;
    }

    @Override
    public T get(@Nullable Identifier id) {
        Object object = super.get(id);
        return object == null ? this.defaultEntry.value() : object;
    }

    @Override
    public Optional<T> getOptionalValue(@Nullable Identifier id) {
        return Optional.ofNullable(super.get(id));
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getDefaultEntry() {
        return Optional.ofNullable(this.defaultEntry);
    }

    @Override
    public T get(int index) {
        Object object = super.get(index);
        return object == null ? this.defaultEntry.value() : object;
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
        return super.getRandom(random).or(() -> Optional.of(this.defaultEntry));
    }

    @Override
    public Identifier getDefaultId() {
        return this.defaultId;
    }
}

