/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.block.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public record BannerPattern(Identifier assetId, String translationKey) {
    public static final Codec<BannerPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Identifier.CODEC.fieldOf("asset_id").forGetter(BannerPattern::assetId), (App)Codec.STRING.fieldOf("translation_key").forGetter(BannerPattern::translationKey)).apply((Applicative)instance, BannerPattern::new));
    public static final PacketCodec<RegistryByteBuf, BannerPattern> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, BannerPattern::assetId, PacketCodecs.STRING, BannerPattern::translationKey, BannerPattern::new);
    public static final Codec<RegistryEntry<BannerPattern>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.BANNER_PATTERN, CODEC);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<BannerPattern>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.BANNER_PATTERN, PACKET_CODEC);
}

