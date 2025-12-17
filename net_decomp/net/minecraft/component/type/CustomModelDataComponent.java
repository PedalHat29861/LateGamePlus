/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.Nullable;

public record CustomModelDataComponent(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
    public static final CustomModelDataComponent DEFAULT = new CustomModelDataComponent(List.of(), List.of(), List.of(), List.of());
    public static final Codec<CustomModelDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.listOf().optionalFieldOf("floats", List.of()).forGetter(CustomModelDataComponent::floats), (App)Codec.BOOL.listOf().optionalFieldOf("flags", List.of()).forGetter(CustomModelDataComponent::flags), (App)Codec.STRING.listOf().optionalFieldOf("strings", List.of()).forGetter(CustomModelDataComponent::strings), (App)Codecs.RGB.listOf().optionalFieldOf("colors", List.of()).forGetter(CustomModelDataComponent::colors)).apply((Applicative)instance, CustomModelDataComponent::new));
    public static final PacketCodec<ByteBuf, CustomModelDataComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.FLOAT.collect(PacketCodecs.toList()), CustomModelDataComponent::floats, PacketCodecs.BOOLEAN.collect(PacketCodecs.toList()), CustomModelDataComponent::flags, PacketCodecs.STRING.collect(PacketCodecs.toList()), CustomModelDataComponent::strings, PacketCodecs.INTEGER.collect(PacketCodecs.toList()), CustomModelDataComponent::colors, CustomModelDataComponent::new);

    private static <T> @Nullable T getValue(List<T> values, int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    public @Nullable Float getFloat(int index) {
        return CustomModelDataComponent.getValue(this.floats, index);
    }

    public @Nullable Boolean getFlag(int index) {
        return CustomModelDataComponent.getValue(this.flags, index);
    }

    public @Nullable String getString(int index) {
        return CustomModelDataComponent.getValue(this.strings, index);
    }

    public @Nullable Integer getColor(int index) {
        return CustomModelDataComponent.getValue(this.colors, index);
    }
}

