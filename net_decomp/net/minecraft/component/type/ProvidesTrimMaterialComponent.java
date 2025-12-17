/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.LazyRegistryEntryReference;
import net.minecraft.registry.entry.RegistryEntry;

public record ProvidesTrimMaterialComponent(LazyRegistryEntryReference<ArmorTrimMaterial> material) {
    public static final Codec<ProvidesTrimMaterialComponent> CODEC = LazyRegistryEntryReference.createCodec(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.ENTRY_CODEC).xmap(ProvidesTrimMaterialComponent::new, ProvidesTrimMaterialComponent::material);
    public static final PacketCodec<RegistryByteBuf, ProvidesTrimMaterialComponent> PACKET_CODEC = LazyRegistryEntryReference.createPacketCodec(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.ENTRY_PACKET_CODEC).xmap(ProvidesTrimMaterialComponent::new, ProvidesTrimMaterialComponent::material);

    public ProvidesTrimMaterialComponent(RegistryEntry<ArmorTrimMaterial> material) {
        this(new LazyRegistryEntryReference<ArmorTrimMaterial>(material));
    }

    @Deprecated
    public ProvidesTrimMaterialComponent(RegistryKey<ArmorTrimMaterial> material) {
        this(new LazyRegistryEntryReference<ArmorTrimMaterial>(material));
    }

    public Optional<RegistryEntry<ArmorTrimMaterial>> getMaterial(RegistryWrapper.WrapperLookup registries) {
        return this.material.resolveEntry(registries);
    }
}

