/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import org.jspecify.annotations.Nullable;

public interface PacketBundleHandler {
    public static final int MAX_PACKETS = 4096;

    public static <T extends PacketListener, P extends BundlePacket<? super T>> PacketBundleHandler create(final PacketType<P> id, final Function<Iterable<Packet<? super T>>, P> bundleFunction, final BundleSplitterPacket<? super T> splitter) {
        return new PacketBundleHandler(){

            @Override
            public void forEachPacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
                if (packet.getPacketType() == id) {
                    BundlePacket bundlePacket = (BundlePacket)packet;
                    consumer.accept(splitter);
                    bundlePacket.getPackets().forEach(consumer);
                    consumer.accept(splitter);
                } else {
                    consumer.accept(packet);
                }
            }

            @Override
            public @Nullable Bundler createBundler(Packet<?> splitter2) {
                if (splitter2 == splitter) {
                    return new Bundler(){
                        private final List<Packet<? super T>> packets = new ArrayList();

                        @Override
                        public @Nullable Packet<?> add(Packet<?> packet) {
                            if (packet == splitter) {
                                return (Packet)bundleFunction.apply(this.packets);
                            }
                            Packet<?> packet2 = packet;
                            if (this.packets.size() >= 4096) {
                                throw new IllegalStateException("Too many packets in a bundle");
                            }
                            this.packets.add(packet2);
                            return null;
                        }
                    };
                }
                return null;
            }
        };
    }

    public void forEachPacket(Packet<?> var1, Consumer<Packet<?>> var2);

    public @Nullable Bundler createBundler(Packet<?> var1);

    public static interface Bundler {
        public @Nullable Packet<?> add(Packet<?> var1);
    }
}

