/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;

public record ParsedSaveProperties(SaveProperties properties, DimensionOptionsRegistryHolder.DimensionsConfig dimensions) {
    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ParsedSaveProperties.class, "worldData;dimensions", "properties", "dimensions"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ParsedSaveProperties.class, "worldData;dimensions", "properties", "dimensions"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ParsedSaveProperties.class, "worldData;dimensions", "properties", "dimensions"}, this, object);
    }
}

