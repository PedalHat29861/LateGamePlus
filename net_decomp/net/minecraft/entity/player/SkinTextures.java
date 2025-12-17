/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.player;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.AssetInfo;
import org.jspecify.annotations.Nullable;

public record SkinTextures(AssetInfo.TextureAsset body,  @Nullable AssetInfo.TextureAsset cape,  @Nullable AssetInfo.TextureAsset elytra, PlayerSkinType model, boolean secure) {
    public static SkinTextures create(AssetInfo.TextureAsset body,  @Nullable AssetInfo.TextureAsset cape,  @Nullable AssetInfo.TextureAsset elytra, PlayerSkinType model) {
        return new SkinTextures(body, cape, elytra, model, false);
    }

    public SkinTextures withOverride(SkinOverride override) {
        if (override.equals(SkinOverride.EMPTY)) {
            return this;
        }
        return SkinTextures.create((AssetInfo.TextureAsset)DataFixUtils.orElse(override.body, (Object)this.body), (AssetInfo.TextureAsset)DataFixUtils.orElse(override.cape, (Object)this.cape), (AssetInfo.TextureAsset)DataFixUtils.orElse(override.elytra, (Object)this.elytra), override.model.orElse(this.model));
    }

    public static final class SkinOverride
    extends Record {
        final Optional<AssetInfo.TextureAssetInfo> body;
        final Optional<AssetInfo.TextureAssetInfo> cape;
        final Optional<AssetInfo.TextureAssetInfo> elytra;
        final Optional<PlayerSkinType> model;
        public static final SkinOverride EMPTY = new SkinOverride(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        public static final MapCodec<SkinOverride> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)AssetInfo.TextureAssetInfo.CODEC.optionalFieldOf("texture").forGetter(SkinOverride::body), (App)AssetInfo.TextureAssetInfo.CODEC.optionalFieldOf("cape").forGetter(SkinOverride::cape), (App)AssetInfo.TextureAssetInfo.CODEC.optionalFieldOf("elytra").forGetter(SkinOverride::elytra), (App)PlayerSkinType.CODEC.optionalFieldOf("model").forGetter(SkinOverride::model)).apply((Applicative)instance, SkinOverride::create));
        public static final PacketCodec<ByteBuf, SkinOverride> PACKET_CODEC = PacketCodec.tuple(AssetInfo.TextureAssetInfo.PACKET_CODEC.collect(PacketCodecs::optional), SkinOverride::body, AssetInfo.TextureAssetInfo.PACKET_CODEC.collect(PacketCodecs::optional), SkinOverride::cape, AssetInfo.TextureAssetInfo.PACKET_CODEC.collect(PacketCodecs::optional), SkinOverride::elytra, PlayerSkinType.PACKET_CODEC.collect(PacketCodecs::optional), SkinOverride::model, SkinOverride::create);

        public SkinOverride(Optional<AssetInfo.TextureAssetInfo> body, Optional<AssetInfo.TextureAssetInfo> cape, Optional<AssetInfo.TextureAssetInfo> elytra, Optional<PlayerSkinType> model) {
            this.body = body;
            this.cape = cape;
            this.elytra = elytra;
            this.model = model;
        }

        public static SkinOverride create(Optional<AssetInfo.TextureAssetInfo> texture, Optional<AssetInfo.TextureAssetInfo> cape, Optional<AssetInfo.TextureAssetInfo> elytra, Optional<PlayerSkinType> model) {
            if (texture.isEmpty() && cape.isEmpty() && elytra.isEmpty() && model.isEmpty()) {
                return EMPTY;
            }
            return new SkinOverride(texture, cape, elytra, model);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SkinOverride.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SkinOverride.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SkinOverride.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this, object);
        }

        public Optional<AssetInfo.TextureAssetInfo> body() {
            return this.body;
        }

        public Optional<AssetInfo.TextureAssetInfo> cape() {
            return this.cape;
        }

        public Optional<AssetInfo.TextureAssetInfo> elytra() {
            return this.elytra;
        }

        public Optional<PlayerSkinType> model() {
            return this.model;
        }
    }
}

