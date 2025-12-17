/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public interface AssetInfo {
    public Identifier id();

    public record SkinAssetInfo(Identifier texturePath, String url) implements TextureAsset
    {
        @Override
        public Identifier id() {
            return this.texturePath;
        }
    }

    public record TextureAssetInfo(Identifier id, Identifier texturePath) implements TextureAsset
    {
        public static final Codec<TextureAssetInfo> CODEC = Identifier.CODEC.xmap(TextureAssetInfo::new, TextureAssetInfo::id);
        public static final MapCodec<TextureAssetInfo> MAP_CODEC = CODEC.fieldOf("asset_id");
        public static final PacketCodec<ByteBuf, TextureAssetInfo> PACKET_CODEC = Identifier.PACKET_CODEC.xmap(TextureAssetInfo::new, TextureAssetInfo::id);

        public TextureAssetInfo(Identifier id) {
            this(id, id.withPath(path -> "textures/" + path + ".png"));
        }
    }

    public static interface TextureAsset
    extends AssetInfo {
        public Identifier texturePath();
    }
}

