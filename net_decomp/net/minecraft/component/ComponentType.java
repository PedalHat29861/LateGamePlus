/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public interface ComponentType<T> {
    public static final Codec<ComponentType<?>> CODEC = Codec.lazyInitialized(() -> Registries.DATA_COMPONENT_TYPE.getCodec());
    public static final PacketCodec<RegistryByteBuf, ComponentType<?>> PACKET_CODEC = PacketCodec.recursive(packetCodec -> PacketCodecs.registryValue(RegistryKeys.DATA_COMPONENT_TYPE));
    public static final Codec<ComponentType<?>> PERSISTENT_CODEC = CODEC.validate(componentType -> componentType.shouldSkipSerialization() ? DataResult.error(() -> "Encountered transient component " + String.valueOf(Registries.DATA_COMPONENT_TYPE.getId((ComponentType<?>)componentType))) : DataResult.success((Object)componentType));
    public static final Codec<Map<ComponentType<?>, Object>> TYPE_TO_VALUE_MAP_CODEC = Codec.dispatchedMap(PERSISTENT_CODEC, ComponentType::getCodecOrThrow);

    public static <T> Builder<T> builder() {
        return new Builder();
    }

    public @Nullable Codec<T> getCodec();

    default public Codec<T> getCodecOrThrow() {
        Codec<T> codec = this.getCodec();
        if (codec == null) {
            throw new IllegalStateException(String.valueOf(this) + " is not a persistent component");
        }
        return codec;
    }

    default public boolean shouldSkipSerialization() {
        return this.getCodec() == null;
    }

    public boolean skipsHandAnimation();

    public PacketCodec<? super RegistryByteBuf, T> getPacketCodec();

    public static class Builder<T> {
        private @Nullable Codec<T> codec;
        private @Nullable PacketCodec<? super RegistryByteBuf, T> packetCodec;
        private boolean cache;
        private boolean skipsHandAnimation;

        public Builder<T> codec(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> packetCodec(PacketCodec<? super RegistryByteBuf, T> packetCodec) {
            this.packetCodec = packetCodec;
            return this;
        }

        public Builder<T> cache() {
            this.cache = true;
            return this;
        }

        public ComponentType<T> build() {
            PacketCodec packetCodec = Objects.requireNonNullElseGet(this.packetCodec, () -> PacketCodecs.registryCodec(Objects.requireNonNull(this.codec, "Missing Codec for component")));
            Codec<T> codec = this.cache && this.codec != null ? DataComponentTypes.CACHE.wrap(this.codec) : this.codec;
            return new SimpleDataComponentType<T>(codec, packetCodec, this.skipsHandAnimation);
        }

        public Builder<T> skipsHandAnimation() {
            this.skipsHandAnimation = true;
            return this;
        }

        static class SimpleDataComponentType<T>
        implements ComponentType<T> {
            private final @Nullable Codec<T> codec;
            private final PacketCodec<? super RegistryByteBuf, T> packetCodec;
            private final boolean skipsHandAnimation;

            SimpleDataComponentType(@Nullable Codec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec, boolean skipsHandAnimation) {
                this.codec = codec;
                this.packetCodec = packetCodec;
                this.skipsHandAnimation = skipsHandAnimation;
            }

            @Override
            public boolean skipsHandAnimation() {
                return this.skipsHandAnimation;
            }

            @Override
            public @Nullable Codec<T> getCodec() {
                return this.codec;
            }

            @Override
            public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
                return this.packetCodec;
            }

            public String toString() {
                return Util.registryValueToString(Registries.DATA_COMPONENT_TYPE, this);
            }
        }
    }
}

