/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.debug;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class DebugSubscriptionType<T> {
    public static final int DEFAULT_EXPIRY = 0;
    final @Nullable PacketCodec<? super RegistryByteBuf, T> packetCodec;
    private final int expiry;

    public DebugSubscriptionType(@Nullable PacketCodec<? super RegistryByteBuf, T> packetCodec, int expiry) {
        this.packetCodec = packetCodec;
        this.expiry = expiry;
    }

    public DebugSubscriptionType(@Nullable PacketCodec<? super RegistryByteBuf, T> packetCodec) {
        this(packetCodec, 0);
    }

    public OptionalValue<T> optionalValueFor(@Nullable T value) {
        return new OptionalValue<T>(this, Optional.ofNullable(value));
    }

    public OptionalValue<T> optionalValueFor() {
        return new OptionalValue(this, Optional.empty());
    }

    public Value<T> valueFor(T value) {
        return new Value<T>(this, value);
    }

    public String toString() {
        return Util.registryValueToString(Registries.DEBUG_SUBSCRIPTION, this);
    }

    public @Nullable PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
        return this.packetCodec;
    }

    public int getExpiry() {
        return this.expiry;
    }

    public record OptionalValue<T>(DebugSubscriptionType<T> subscription, Optional<T> value) {
        public static final PacketCodec<RegistryByteBuf, OptionalValue<?>> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.DEBUG_SUBSCRIPTION).dispatch(OptionalValue::subscription, OptionalValue::createPacketCodec);

        private static <T> PacketCodec<? super RegistryByteBuf, OptionalValue<T>> createPacketCodec(DebugSubscriptionType<T> type) {
            return PacketCodecs.optional(Objects.requireNonNull(type.packetCodec)).xmap(value -> new OptionalValue(type, value), OptionalValue::value);
        }
    }

    public record Value<T>(DebugSubscriptionType<T> subscription, T value) {
        public static final PacketCodec<RegistryByteBuf, Value<?>> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.DEBUG_SUBSCRIPTION).dispatch(Value::subscription, Value::createPacketCodec);

        private static <T> PacketCodec<? super RegistryByteBuf, Value<T>> createPacketCodec(DebugSubscriptionType<T> type) {
            return Objects.requireNonNull(type.packetCodec).xmap(value -> new Value<Object>(type, value), Value::value);
        }
    }
}

