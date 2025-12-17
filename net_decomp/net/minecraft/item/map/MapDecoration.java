/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item.map;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record MapDecoration(RegistryEntry<MapDecorationType> type, byte x, byte z, byte rotation, Optional<Text> name) {
    public static final PacketCodec<RegistryByteBuf, MapDecoration> CODEC = PacketCodec.tuple(MapDecorationType.PACKET_CODEC, MapDecoration::type, PacketCodecs.BYTE, MapDecoration::x, PacketCodecs.BYTE, MapDecoration::z, PacketCodecs.BYTE, MapDecoration::rotation, TextCodecs.OPTIONAL_PACKET_CODEC, MapDecoration::name, MapDecoration::new);

    public MapDecoration {
        rotation = (byte)(rotation & 0xF);
    }

    public Identifier getAssetId() {
        return this.type.value().assetId();
    }

    public boolean isAlwaysRendered() {
        return this.type.value().showOnItemFrame();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{MapDecoration.class, "type;x;y;rot;name", "type", "x", "z", "rotation", "name"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MapDecoration.class, "type;x;y;rot;name", "type", "x", "z", "rotation", "name"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MapDecoration.class, "type;x;y;rot;name", "type", "x", "z", "rotation", "name"}, this, o);
    }
}

