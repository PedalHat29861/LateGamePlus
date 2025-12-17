/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.player;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import org.jspecify.annotations.Nullable;

public final class PlayerSkinType
extends Enum<PlayerSkinType>
implements StringIdentifiable {
    public static final /* enum */ PlayerSkinType SLIM = new PlayerSkinType("slim", "slim");
    public static final /* enum */ PlayerSkinType WIDE = new PlayerSkinType("wide", "default");
    public static final Codec<PlayerSkinType> CODEC;
    private static final Function<String, PlayerSkinType> BY_MODEL_METADATA;
    public static final PacketCodec<ByteBuf, PlayerSkinType> PACKET_CODEC;
    private final String name;
    private final String modelMetadata;
    private static final /* synthetic */ PlayerSkinType[] field_41125;

    public static PlayerSkinType[] values() {
        return (PlayerSkinType[])field_41125.clone();
    }

    public static PlayerSkinType valueOf(String string) {
        return Enum.valueOf(PlayerSkinType.class, string);
    }

    private PlayerSkinType(String name, String modelMetadata) {
        this.name = name;
        this.modelMetadata = modelMetadata;
    }

    public static PlayerSkinType byModelMetadata(@Nullable String modelMetadata) {
        return Objects.requireNonNullElse(BY_MODEL_METADATA.apply(modelMetadata), WIDE);
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ PlayerSkinType[] method_47439() {
        return new PlayerSkinType[]{SLIM, WIDE};
    }

    static {
        field_41125 = PlayerSkinType.method_47439();
        CODEC = StringIdentifiable.createCodec(PlayerSkinType::values);
        BY_MODEL_METADATA = StringIdentifiable.createMapper(PlayerSkinType.values(), playerSkinType -> playerSkinType.modelMetadata);
        PACKET_CODEC = PacketCodecs.BOOLEAN.xmap(slim -> slim != false ? SLIM : WIDE, model -> model == SLIM);
    }
}

