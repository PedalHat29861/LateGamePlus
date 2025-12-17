/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource;

import java.util.function.Consumer;
import net.minecraft.resource.ResourcePackProfile;

@FunctionalInterface
public interface ResourcePackProvider {
    public void register(Consumer<ResourcePackProfile> var1);
}

