/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.registry;

import io.netty.buffer.ByteBuf;
import net.minecraft.SharedConstants;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record VersionedIdentifier(String namespace, String id, String version) {
    public static final PacketCodec<ByteBuf, VersionedIdentifier> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.STRING, VersionedIdentifier::namespace, PacketCodecs.STRING, VersionedIdentifier::id, PacketCodecs.STRING, VersionedIdentifier::version, VersionedIdentifier::new);
    public static final String DEFAULT_NAMESPACE = "minecraft";

    public static VersionedIdentifier createVanilla(String path) {
        return new VersionedIdentifier(DEFAULT_NAMESPACE, path, SharedConstants.getGameVersion().id());
    }

    public boolean isVanilla() {
        return this.namespace.equals(DEFAULT_NAMESPACE);
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.id + ":" + this.version;
    }
}

