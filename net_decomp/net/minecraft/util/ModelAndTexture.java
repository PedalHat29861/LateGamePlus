/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;

public record ModelAndTexture<T>(T model, AssetInfo.TextureAssetInfo asset) {
    public ModelAndTexture(T model, Identifier assetId) {
        this(model, new AssetInfo.TextureAssetInfo(assetId));
    }

    public static <T> MapCodec<ModelAndTexture<T>> createMapCodec(Codec<T> modelCodec, T model) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)modelCodec.optionalFieldOf("model", model).forGetter(ModelAndTexture::model), (App)AssetInfo.TextureAssetInfo.MAP_CODEC.forGetter(ModelAndTexture::asset)).apply((Applicative)instance, ModelAndTexture::new));
    }

    public static <T> PacketCodec<RegistryByteBuf, ModelAndTexture<T>> createPacketCodec(PacketCodec<? super RegistryByteBuf, T> modelPacketCodec) {
        return PacketCodec.tuple(modelPacketCodec, ModelAndTexture::model, AssetInfo.TextureAssetInfo.PACKET_CODEC, ModelAndTexture::asset, ModelAndTexture::new);
    }
}

