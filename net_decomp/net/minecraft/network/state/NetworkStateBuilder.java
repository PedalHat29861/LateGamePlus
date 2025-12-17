/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.state;

import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.handler.SideValidatingDispatchingCodecBuilder;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketCodecModifier;
import net.minecraft.network.state.ContextAwareNetworkStateFactory;
import net.minecraft.network.state.NetworkState;
import net.minecraft.network.state.NetworkStateFactory;
import net.minecraft.util.Unit;
import org.jspecify.annotations.Nullable;

public class NetworkStateBuilder<T extends PacketListener, B extends ByteBuf, C> {
    final NetworkPhase phase;
    final NetworkSide side;
    private final List<PacketType<T, ?, B, C>> packetTypes = new ArrayList();
    private @Nullable PacketBundleHandler bundleHandler;

    public NetworkStateBuilder(NetworkPhase phase, NetworkSide side) {
        this.phase = phase;
        this.side = side;
    }

    public <P extends Packet<? super T>> NetworkStateBuilder<T, B, C> add(net.minecraft.network.packet.PacketType<P> type, PacketCodec<? super B, P> codec) {
        this.packetTypes.add(new PacketType(type, codec, null));
        return this;
    }

    public <P extends Packet<? super T>> NetworkStateBuilder<T, B, C> add(net.minecraft.network.packet.PacketType<P> type, PacketCodec<? super B, P> codec, PacketCodecModifier<B, P, C> modifier) {
        this.packetTypes.add(new PacketType(type, codec, modifier));
        return this;
    }

    public <P extends BundlePacket<? super T>, D extends BundleSplitterPacket<? super T>> NetworkStateBuilder<T, B, C> addBundle(net.minecraft.network.packet.PacketType<P> id, Function<Iterable<Packet<? super T>>, P> bundler, D splitter) {
        PacketCodec packetCodec = PacketCodec.unit(splitter);
        net.minecraft.network.packet.PacketType<BundleSplitterPacket<? super T>> packetType = splitter.getPacketType();
        this.packetTypes.add(new PacketType(packetType, packetCodec, null));
        this.bundleHandler = PacketBundleHandler.create(id, bundler, splitter);
        return this;
    }

    PacketCodec<ByteBuf, Packet<? super T>> createCodec(Function<ByteBuf, B> bufUpgrader, List<PacketType<T, ?, B, C>> packetTypes, C context) {
        SideValidatingDispatchingCodecBuilder sideValidatingDispatchingCodecBuilder = new SideValidatingDispatchingCodecBuilder(this.side);
        for (PacketType packetType : packetTypes) {
            packetType.add(sideValidatingDispatchingCodecBuilder, bufUpgrader, context);
        }
        return sideValidatingDispatchingCodecBuilder.build();
    }

    private static NetworkState.Unbound createState(final NetworkPhase phase, final NetworkSide side, final List<? extends PacketType<?, ?, ?, ?>> types) {
        return new NetworkState.Unbound(){

            @Override
            public NetworkPhase phase() {
                return phase;
            }

            @Override
            public NetworkSide side() {
                return side;
            }

            @Override
            public void forEachPacketType(NetworkState.Unbound.PacketTypeConsumer callback) {
                for (int i = 0; i < types.size(); ++i) {
                    PacketType packetType = (PacketType)types.get(i);
                    callback.accept(packetType.type, i);
                }
            }
        };
    }

    public NetworkStateFactory<T, B> buildFactory(final C context) {
        final List<PacketType<T, ?, B, C>> list = List.copyOf(this.packetTypes);
        final PacketBundleHandler packetBundleHandler = this.bundleHandler;
        final NetworkState.Unbound unbound = NetworkStateBuilder.createState(this.phase, this.side, list);
        return new NetworkStateFactory<T, B>(){

            @Override
            public NetworkState<T> bind(Function<ByteBuf, B> registryBinder) {
                return new NetworkStateImpl(NetworkStateBuilder.this.phase, NetworkStateBuilder.this.side, NetworkStateBuilder.this.createCodec(registryBinder, list, context), packetBundleHandler);
            }

            @Override
            public NetworkState.Unbound buildUnbound() {
                return unbound;
            }
        };
    }

    public ContextAwareNetworkStateFactory<T, B, C> buildContextAwareFactory() {
        final List<PacketType<T, ?, B, C>> list = List.copyOf(this.packetTypes);
        final PacketBundleHandler packetBundleHandler = this.bundleHandler;
        final NetworkState.Unbound unbound = NetworkStateBuilder.createState(this.phase, this.side, list);
        return new ContextAwareNetworkStateFactory<T, B, C>(){

            @Override
            public NetworkState<T> bind(Function<ByteBuf, B> registryBinder, C context) {
                return new NetworkStateImpl(NetworkStateBuilder.this.phase, NetworkStateBuilder.this.side, NetworkStateBuilder.this.createCodec(registryBinder, list, context), packetBundleHandler);
            }

            @Override
            public NetworkState.Unbound buildUnbound() {
                return unbound;
            }
        };
    }

    private static <L extends PacketListener, B extends ByteBuf> NetworkStateFactory<L, B> build(NetworkPhase type, NetworkSide side, Consumer<NetworkStateBuilder<L, B, Unit>> registrar) {
        NetworkStateBuilder networkStateBuilder = new NetworkStateBuilder(type, side);
        registrar.accept(networkStateBuilder);
        return networkStateBuilder.buildFactory(Unit.INSTANCE);
    }

    public static <T extends ServerPacketListener, B extends ByteBuf> NetworkStateFactory<T, B> c2s(NetworkPhase type, Consumer<NetworkStateBuilder<T, B, Unit>> registrar) {
        return NetworkStateBuilder.build(type, NetworkSide.SERVERBOUND, registrar);
    }

    public static <T extends ClientPacketListener, B extends ByteBuf> NetworkStateFactory<T, B> s2c(NetworkPhase type, Consumer<NetworkStateBuilder<T, B, Unit>> registrar) {
        return NetworkStateBuilder.build(type, NetworkSide.CLIENTBOUND, registrar);
    }

    private static <L extends PacketListener, B extends ByteBuf, C> ContextAwareNetworkStateFactory<L, B, C> buildContextAware(NetworkPhase type, NetworkSide side, Consumer<NetworkStateBuilder<L, B, C>> registrar) {
        NetworkStateBuilder networkStateBuilder = new NetworkStateBuilder(type, side);
        registrar.accept(networkStateBuilder);
        return networkStateBuilder.buildContextAwareFactory();
    }

    public static <T extends ServerPacketListener, B extends ByteBuf, C> ContextAwareNetworkStateFactory<T, B, C> contextAwareC2S(NetworkPhase type, Consumer<NetworkStateBuilder<T, B, C>> registrar) {
        return NetworkStateBuilder.buildContextAware(type, NetworkSide.SERVERBOUND, registrar);
    }

    public static <T extends ClientPacketListener, B extends ByteBuf, C> ContextAwareNetworkStateFactory<T, B, C> contextAwareS2C(NetworkPhase type, Consumer<NetworkStateBuilder<T, B, C>> registrar) {
        return NetworkStateBuilder.buildContextAware(type, NetworkSide.CLIENTBOUND, registrar);
    }

    static final class PacketType<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf, C>
    extends Record {
        final net.minecraft.network.packet.PacketType<P> type;
        private final PacketCodec<? super B, P> codec;
        private final @Nullable PacketCodecModifier<B, P, C> modifier;

        PacketType(net.minecraft.network.packet.PacketType<P> type, PacketCodec<? super B, P> codec, @Nullable PacketCodecModifier<B, P, C> modifier) {
            this.type = type;
            this.codec = codec;
            this.modifier = modifier;
        }

        public void add(SideValidatingDispatchingCodecBuilder<ByteBuf, T> builder, Function<ByteBuf, B> bufUpgrader, C context) {
            PacketCodec<Object, P> packetCodec = this.modifier != null ? this.modifier.apply(this.codec, context) : this.codec;
            PacketCodec<ByteBuf, P> packetCodec2 = packetCodec.mapBuf(bufUpgrader);
            builder.add(this.type, packetCodec2);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PacketType.class, "type;serializer;modifier", "type", "codec", "modifier"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PacketType.class, "type;serializer;modifier", "type", "codec", "modifier"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PacketType.class, "type;serializer;modifier", "type", "codec", "modifier"}, this, object);
        }

        public net.minecraft.network.packet.PacketType<P> type() {
            return this.type;
        }

        public PacketCodec<? super B, P> codec() {
            return this.codec;
        }

        public @Nullable PacketCodecModifier<B, P, C> modifier() {
            return this.modifier;
        }
    }

    record NetworkStateImpl<L extends PacketListener>(NetworkPhase id, NetworkSide side, PacketCodec<ByteBuf, Packet<? super L>> codec, @Nullable PacketBundleHandler bundleHandler) implements NetworkState<L>
    {
        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{NetworkStateImpl.class, "id;flow;codec;bundlerInfo", "id", "side", "codec", "bundleHandler"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NetworkStateImpl.class, "id;flow;codec;bundlerInfo", "id", "side", "codec", "bundleHandler"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NetworkStateImpl.class, "id;flow;codec;bundlerInfo", "id", "side", "codec", "bundleHandler"}, this, object);
        }
    }
}

