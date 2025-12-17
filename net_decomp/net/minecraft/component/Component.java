/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import net.minecraft.component.ComponentType;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record Component<T>(ComponentType<T> type, T value) {
    public static final PacketCodec<RegistryByteBuf, Component<?>> PACKET_CODEC = new PacketCodec<RegistryByteBuf, Component<?>>(){

        @Override
        public Component<?> decode(RegistryByteBuf registryByteBuf) {
            ComponentType componentType = (ComponentType)ComponentType.PACKET_CODEC.decode(registryByteBuf);
            return 1.read(registryByteBuf, componentType);
        }

        private static <T> Component<T> read(RegistryByteBuf buf, ComponentType<T> type) {
            return new Component<T>(type, type.getPacketCodec().decode(buf));
        }

        @Override
        public void encode(RegistryByteBuf registryByteBuf, Component<?> component) {
            1.write(registryByteBuf, component);
        }

        private static <T> void write(RegistryByteBuf buf, Component<T> component) {
            ComponentType.PACKET_CODEC.encode(buf, component.type());
            component.type().getPacketCodec().encode(buf, component.value());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryByteBuf)((Object)object), (Component)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryByteBuf)((Object)object));
        }
    };

    static Component<?> of(Map.Entry<ComponentType<?>, Object> entry) {
        return Component.of(entry.getKey(), entry.getValue());
    }

    public static <T> Component<T> of(ComponentType<T> type, Object value) {
        return new Component<Object>(type, value);
    }

    public void apply(MergedComponentMap components) {
        components.set(this.type, this.value);
    }

    public <D> DataResult<D> encode(DynamicOps<D> ops) {
        Codec<T> codec = this.type.getCodec();
        if (codec == null) {
            return DataResult.error(() -> "Component of type " + String.valueOf(this.type) + " is not encodable");
        }
        return codec.encodeStart(ops, this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.type) + "=>" + String.valueOf(this.value);
    }
}

