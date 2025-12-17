/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.registry.entry;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.registry.VersionedIdentifier;

public record RegistryEntryInfo(Optional<VersionedIdentifier> knownPackInfo, Lifecycle lifecycle) {
    public static final RegistryEntryInfo DEFAULT = new RegistryEntryInfo(Optional.empty(), Lifecycle.stable());
}

