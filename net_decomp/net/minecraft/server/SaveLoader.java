/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.DataPackContents;
import net.minecraft.world.SaveProperties;

public record SaveLoader(LifecycledResourceManager resourceManager, DataPackContents dataPackContents, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, SaveProperties saveProperties) implements AutoCloseable
{
    @Override
    public void close() {
        this.resourceManager.close();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{SaveLoader.class, "resourceManager;dataPackResources;registries;worldData", "resourceManager", "dataPackContents", "combinedDynamicRegistries", "saveProperties"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SaveLoader.class, "resourceManager;dataPackResources;registries;worldData", "resourceManager", "dataPackContents", "combinedDynamicRegistries", "saveProperties"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SaveLoader.class, "resourceManager;dataPackResources;registries;worldData", "resourceManager", "dataPackContents", "combinedDynamicRegistries", "saveProperties"}, this, object);
    }
}

