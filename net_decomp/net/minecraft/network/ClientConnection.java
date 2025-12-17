/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  io.netty.bootstrap.Bootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelOutboundHandler
 *  io.netty.channel.ChannelOutboundHandlerAdapter
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.ChannelPromise
 *  io.netty.channel.SimpleChannelInboundHandler
 *  io.netty.channel.local.LocalChannel
 *  io.netty.channel.local.LocalServerChannel
 *  io.netty.handler.flow.FlowControlHandler
 *  io.netty.handler.timeout.ReadTimeoutHandler
 *  io.netty.handler.timeout.TimeoutException
 *  io.netty.util.concurrent.GenericFutureListener
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.Marker
 *  org.slf4j.MarkerFactory
 */
package net.minecraft.network;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import javax.crypto.Cipher;
import net.minecraft.SharedConstants;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkingBackend;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.PacketDecryptor;
import net.minecraft.network.encryption.PacketEncryptor;
import net.minecraft.network.handler.DecoderHandler;
import net.minecraft.network.handler.EncoderHandler;
import net.minecraft.network.handler.LocalBufPacker;
import net.minecraft.network.handler.LocalBufUnpacker;
import net.minecraft.network.handler.NetworkStateTransitions;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.handler.PacketBundler;
import net.minecraft.network.handler.PacketDeflater;
import net.minecraft.network.handler.PacketException;
import net.minecraft.network.handler.PacketInflater;
import net.minecraft.network.handler.PacketSizeLogHandler;
import net.minecraft.network.handler.PacketSizeLogger;
import net.minecraft.network.handler.PacketUnbundler;
import net.minecraft.network.handler.SizePrepender;
import net.minecraft.network.handler.SplitterHandler;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.state.HandshakeStates;
import net.minecraft.network.state.LoginStates;
import net.minecraft.network.state.NetworkState;
import net.minecraft.network.state.QueryStates;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ClientConnection
extends SimpleChannelInboundHandler<Packet<?>> {
    private static final float CURRENT_PACKET_COUNTER_WEIGHT = 0.75f;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker NETWORK_MARKER = MarkerFactory.getMarker((String)"NETWORK");
    public static final Marker NETWORK_PACKETS_MARKER = Util.make(MarkerFactory.getMarker((String)"NETWORK_PACKETS"), marker -> marker.add(NETWORK_MARKER));
    public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker((String)"PACKET_RECEIVED"), marker -> marker.add(NETWORK_PACKETS_MARKER));
    public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker((String)"PACKET_SENT"), marker -> marker.add(NETWORK_PACKETS_MARKER));
    private static final NetworkState<ServerHandshakePacketListener> C2S_HANDSHAKE_STATE = HandshakeStates.C2S;
    private final NetworkSide side;
    private volatile boolean duringLogin = true;
    private final Queue<Consumer<ClientConnection>> queuedTasks = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private SocketAddress address;
    private volatile @Nullable PacketListener prePlayStateListener;
    private volatile @Nullable PacketListener packetListener;
    private @Nullable DisconnectionInfo disconnectionInfo;
    private boolean encrypted;
    private boolean disconnected;
    private int packetsReceivedCounter;
    private int packetsSentCounter;
    private float averagePacketsReceived;
    private float averagePacketsSent;
    private int ticks;
    private boolean errored;
    private volatile @Nullable DisconnectionInfo pendingDisconnectionInfo;
    @Nullable PacketSizeLogger packetSizeLogger;

    public ClientConnection(NetworkSide side) {
        this.side = side;
    }

    public void channelActive(ChannelHandlerContext context) throws Exception {
        super.channelActive(context);
        this.channel = context.channel();
        this.address = this.channel.remoteAddress();
        if (this.pendingDisconnectionInfo != null) {
            this.disconnect(this.pendingDisconnectionInfo);
        }
    }

    public void channelInactive(ChannelHandlerContext context) {
        this.disconnect(Text.translatable("disconnect.endOfStream"));
    }

    public void exceptionCaught(ChannelHandlerContext context, Throwable ex) {
        if (ex instanceof PacketException) {
            LOGGER.debug("Skipping packet due to errors", ex.getCause());
            return;
        }
        boolean bl = !this.errored;
        this.errored = true;
        if (!this.channel.isOpen()) {
            return;
        }
        if (ex instanceof TimeoutException) {
            LOGGER.debug("Timeout", ex);
            this.disconnect(Text.translatable("disconnect.timeout"));
        } else {
            MutableText text = Text.translatable("disconnect.genericReason", "Internal Exception: " + String.valueOf(ex));
            PacketListener packetListener = this.packetListener;
            DisconnectionInfo disconnectionInfo = packetListener != null ? packetListener.createDisconnectionInfo(text, ex) : new DisconnectionInfo(text);
            if (bl) {
                LOGGER.debug("Failed to sent packet", ex);
                if (this.getOppositeSide() == NetworkSide.CLIENTBOUND) {
                    Record packet = this.duringLogin ? new LoginDisconnectS2CPacket(text) : new DisconnectS2CPacket(text);
                    this.send((Packet<?>)((Object)packet), PacketCallbacks.always(() -> this.disconnect(disconnectionInfo)));
                } else {
                    this.disconnect(disconnectionInfo);
                }
                this.tryDisableAutoRead();
            } else {
                LOGGER.debug("Double fault", ex);
                this.disconnect(disconnectionInfo);
            }
        }
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
        if (!this.channel.isOpen()) {
            return;
        }
        PacketListener packetListener = this.packetListener;
        if (packetListener == null) {
            throw new IllegalStateException("Received a packet before the packet listener was initialized");
        }
        if (packetListener.accepts(packet)) {
            try {
                ClientConnection.handlePacket(packet, packetListener);
            }
            catch (OffThreadException offThreadException) {
            }
            catch (RejectedExecutionException rejectedExecutionException) {
                this.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
            }
            catch (ClassCastException classCastException) {
                LOGGER.error("Received {} that couldn't be processed", packet.getClass(), (Object)classCastException);
                this.disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
            }
            ++this.packetsReceivedCounter;
        }
    }

    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
        packet.apply(listener);
    }

    private void setPacketListener(NetworkState<?> state, PacketListener listener) {
        Objects.requireNonNull(listener, "packetListener");
        NetworkSide networkSide = listener.getSide();
        if (networkSide != this.side) {
            throw new IllegalStateException("Trying to set listener for wrong side: connection is " + String.valueOf((Object)this.side) + ", but listener is " + String.valueOf((Object)networkSide));
        }
        NetworkPhase networkPhase = listener.getPhase();
        if (state.id() != networkPhase) {
            throw new IllegalStateException("Listener protocol (" + String.valueOf((Object)networkPhase) + ") does not match requested one " + String.valueOf(state));
        }
    }

    private static void syncUninterruptibly(ChannelFuture future) {
        try {
            future.syncUninterruptibly();
        }
        catch (Exception exception) {
            if (exception instanceof ClosedChannelException) {
                LOGGER.info("Connection closed during protocol change");
                return;
            }
            throw exception;
        }
    }

    public <T extends PacketListener> void transitionInbound(NetworkState<T> state, T packetListener) {
        this.setPacketListener(state, packetListener);
        if (state.side() != this.getSide()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf((Object)state.id()));
        }
        this.packetListener = packetListener;
        this.prePlayStateListener = null;
        NetworkStateTransitions.DecoderTransitioner decoderTransitioner = NetworkStateTransitions.decoderTransitioner(state);
        PacketBundleHandler packetBundleHandler = state.bundleHandler();
        if (packetBundleHandler != null) {
            PacketBundler packetBundler = new PacketBundler(packetBundleHandler);
            decoderTransitioner = decoderTransitioner.andThen(context -> context.pipeline().addAfter("decoder", "bundler", (ChannelHandler)packetBundler));
        }
        ClientConnection.syncUninterruptibly(this.channel.writeAndFlush((Object)decoderTransitioner));
    }

    public void transitionOutbound(NetworkState<?> newState) {
        if (newState.side() != this.getOppositeSide()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf((Object)newState.id()));
        }
        NetworkStateTransitions.EncoderTransitioner encoderTransitioner = NetworkStateTransitions.encoderTransitioner(newState);
        PacketBundleHandler packetBundleHandler = newState.bundleHandler();
        if (packetBundleHandler != null) {
            PacketUnbundler packetUnbundler = new PacketUnbundler(packetBundleHandler);
            encoderTransitioner = encoderTransitioner.andThen(context -> context.pipeline().addAfter("encoder", "unbundler", (ChannelHandler)packetUnbundler));
        }
        boolean bl = newState.id() == NetworkPhase.LOGIN;
        ClientConnection.syncUninterruptibly(this.channel.writeAndFlush((Object)encoderTransitioner.andThen(context -> {
            this.duringLogin = bl;
        })));
    }

    public void setInitialPacketListener(PacketListener packetListener) {
        if (this.packetListener != null) {
            throw new IllegalStateException("Listener already set");
        }
        if (this.side != NetworkSide.SERVERBOUND || packetListener.getSide() != NetworkSide.SERVERBOUND || packetListener.getPhase() != C2S_HANDSHAKE_STATE.id()) {
            throw new IllegalStateException("Invalid initial listener");
        }
        this.packetListener = packetListener;
    }

    public void connect(String address, int port, ClientQueryPacketListener listener) {
        this.connect(address, port, QueryStates.C2S, QueryStates.S2C, listener, ConnectionIntent.STATUS);
    }

    public void connect(String address, int port, ClientLoginPacketListener listener) {
        this.connect(address, port, LoginStates.C2S, LoginStates.S2C, listener, ConnectionIntent.LOGIN);
    }

    public <S extends ServerPacketListener, C extends ClientPacketListener> void connect(String address, int port, NetworkState<S> outboundState, NetworkState<C> inboundState, C prePlayStateListener, boolean transfer) {
        this.connect(address, port, outboundState, inboundState, prePlayStateListener, transfer ? ConnectionIntent.TRANSFER : ConnectionIntent.LOGIN);
    }

    private <S extends ServerPacketListener, C extends ClientPacketListener> void connect(String address, int port, NetworkState<S> outboundState, NetworkState<C> inboundState, C prePlayStateListener, ConnectionIntent intent) {
        if (outboundState.id() != inboundState.id()) {
            throw new IllegalStateException("Mismatched initial protocols");
        }
        this.prePlayStateListener = prePlayStateListener;
        this.submit(connection -> {
            this.transitionInbound(inboundState, prePlayStateListener);
            connection.sendImmediately(new HandshakeC2SPacket(SharedConstants.getGameVersion().protocolVersion(), address, port, intent), null, true);
            this.transitionOutbound(outboundState);
        });
    }

    public void send(Packet<?> packet) {
        this.send(packet, null);
    }

    public void send(Packet<?> packet, @Nullable ChannelFutureListener listener) {
        this.send(packet, listener, true);
    }

    public void send(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush) {
        if (this.isOpen()) {
            this.handleQueuedTasks();
            this.sendImmediately(packet, listener, flush);
        } else {
            this.queuedTasks.add(connection -> connection.sendImmediately(packet, listener, flush));
        }
    }

    public void submit(Consumer<ClientConnection> task) {
        if (this.isOpen()) {
            this.handleQueuedTasks();
            task.accept(this);
        } else {
            this.queuedTasks.add(task);
        }
    }

    private void sendImmediately(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush) {
        ++this.packetsSentCounter;
        if (this.channel.eventLoop().inEventLoop()) {
            this.sendInternal(packet, listener, flush);
        } else {
            this.channel.eventLoop().execute(() -> this.sendInternal(packet, listener, flush));
        }
    }

    private void sendInternal(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush) {
        if (listener != null) {
            ChannelFuture channelFuture = flush ? this.channel.writeAndFlush(packet) : this.channel.write(packet);
            channelFuture.addListener((GenericFutureListener)listener);
        } else if (flush) {
            this.channel.writeAndFlush(packet, this.channel.voidPromise());
        } else {
            this.channel.write(packet, this.channel.voidPromise());
        }
    }

    public void flush() {
        if (this.isOpen()) {
            this.flushInternal();
        } else {
            this.queuedTasks.add(ClientConnection::flushInternal);
        }
    }

    private void flushInternal() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> this.channel.flush());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleQueuedTasks() {
        if (this.channel == null || !this.channel.isOpen()) {
            return;
        }
        Queue<Consumer<ClientConnection>> queue = this.queuedTasks;
        synchronized (queue) {
            Consumer<ClientConnection> consumer;
            while ((consumer = this.queuedTasks.poll()) != null) {
                consumer.accept(this);
            }
        }
    }

    public void tick() {
        this.handleQueuedTasks();
        PacketListener packetListener = this.packetListener;
        if (packetListener instanceof TickablePacketListener) {
            TickablePacketListener tickablePacketListener = (TickablePacketListener)packetListener;
            tickablePacketListener.tick();
        }
        if (!this.isOpen() && !this.disconnected) {
            this.handleDisconnection();
        }
        if (this.channel != null) {
            this.channel.flush();
        }
        if (this.ticks++ % 20 == 0) {
            this.updateStats();
        }
        if (this.packetSizeLogger != null) {
            this.packetSizeLogger.push();
        }
    }

    protected void updateStats() {
        this.averagePacketsSent = MathHelper.lerp(0.75f, (float)this.packetsSentCounter, this.averagePacketsSent);
        this.averagePacketsReceived = MathHelper.lerp(0.75f, (float)this.packetsReceivedCounter, this.averagePacketsReceived);
        this.packetsSentCounter = 0;
        this.packetsReceivedCounter = 0;
    }

    public SocketAddress getAddress() {
        return this.address;
    }

    public String getAddressAsString(boolean logIps) {
        if (this.address == null) {
            return "local";
        }
        if (logIps) {
            return this.address.toString();
        }
        return "IP hidden";
    }

    public void disconnect(Text disconnectReason) {
        this.disconnect(new DisconnectionInfo(disconnectReason));
    }

    public void disconnect(DisconnectionInfo disconnectionInfo) {
        if (this.channel == null) {
            this.pendingDisconnectionInfo = disconnectionInfo;
        }
        if (this.isOpen()) {
            this.channel.close().awaitUninterruptibly();
            this.disconnectionInfo = disconnectionInfo;
        }
    }

    public boolean isLocal() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public NetworkSide getSide() {
        return this.side;
    }

    public NetworkSide getOppositeSide() {
        return this.side.getOpposite();
    }

    public static ClientConnection connect(InetSocketAddress address, NetworkingBackend backend, @Nullable MultiValueDebugSampleLogImpl packetSizeLog) {
        ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
        if (packetSizeLog != null) {
            clientConnection.resetPacketSizeLog(packetSizeLog);
        }
        ChannelFuture channelFuture = ClientConnection.connect(address, backend, clientConnection);
        channelFuture.syncUninterruptibly();
        return clientConnection;
    }

    public static ChannelFuture connect(InetSocketAddress address, NetworkingBackend backend, final ClientConnection connection) {
        return ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(backend.getEventLoopGroup())).handler((ChannelHandler)new ChannelInitializer<Channel>(){

            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, (Object)true);
                }
                catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                ClientConnection.addHandlers(channelPipeline, NetworkSide.CLIENTBOUND, false, connection.packetSizeLogger);
                connection.addFlowControlHandler(channelPipeline);
            }
        })).channel(backend.getChannelClass())).connect(address.getAddress(), address.getPort());
    }

    private static String getOutboundHandlerName(boolean sendingSide) {
        return sendingSide ? "encoder" : "outbound_config";
    }

    private static String getInboundHandlerName(boolean receivingSide) {
        return receivingSide ? "decoder" : "inbound_config";
    }

    public void addFlowControlHandler(ChannelPipeline pipeline) {
        pipeline.addLast("hackfix", (ChannelHandler)new ChannelOutboundHandlerAdapter(this){

            public void write(ChannelHandlerContext context, Object value, ChannelPromise promise) throws Exception {
                super.write(context, value, promise);
            }
        }).addLast("packet_handler", (ChannelHandler)this);
    }

    public static void addHandlers(ChannelPipeline pipeline, NetworkSide side, boolean local, @Nullable PacketSizeLogger packetSizeLogger) {
        NetworkSide networkSide = side.getOpposite();
        boolean bl = side == NetworkSide.SERVERBOUND;
        boolean bl2 = networkSide == NetworkSide.SERVERBOUND;
        pipeline.addLast("splitter", (ChannelHandler)ClientConnection.getSplitter(packetSizeLogger, local)).addLast(new ChannelHandler[]{new FlowControlHandler()}).addLast(ClientConnection.getInboundHandlerName(bl), bl ? new DecoderHandler<ServerHandshakePacketListener>(C2S_HANDSHAKE_STATE) : new NetworkStateTransitions.InboundConfigurer()).addLast("prepender", (ChannelHandler)ClientConnection.getPrepender(local)).addLast(ClientConnection.getOutboundHandlerName(bl2), bl2 ? new EncoderHandler<ServerHandshakePacketListener>(C2S_HANDSHAKE_STATE) : new NetworkStateTransitions.OutboundConfigurer());
    }

    private static ChannelOutboundHandler getPrepender(boolean local) {
        return local ? new LocalBufPacker() : new SizePrepender();
    }

    private static ChannelInboundHandler getSplitter(@Nullable PacketSizeLogger packetSizeLogger, boolean local) {
        if (!local) {
            return new SplitterHandler(packetSizeLogger);
        }
        if (packetSizeLogger != null) {
            return new PacketSizeLogHandler(packetSizeLogger);
        }
        return new LocalBufUnpacker();
    }

    public static void addLocalValidator(ChannelPipeline pipeline, NetworkSide side) {
        ClientConnection.addHandlers(pipeline, side, true, null);
    }

    public static ClientConnection connectLocal(SocketAddress address) {
        final ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(NetworkingBackend.local().getEventLoopGroup())).handler((ChannelHandler)new ChannelInitializer<Channel>(){

            protected void initChannel(Channel channel) {
                ChannelPipeline channelPipeline = channel.pipeline();
                ClientConnection.addLocalValidator(channelPipeline, NetworkSide.CLIENTBOUND);
                clientConnection.addFlowControlHandler(channelPipeline);
            }
        })).channel(NetworkingBackend.local().getChannelClass())).connect(address).syncUninterruptibly();
        return clientConnection;
    }

    public void setupEncryption(Cipher decryptionCipher, Cipher encryptionCipher) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", (ChannelHandler)new PacketDecryptor(decryptionCipher));
        this.channel.pipeline().addBefore("prepender", "encrypt", (ChannelHandler)new PacketEncryptor(encryptionCipher));
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean isOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isChannelAbsent() {
        return this.channel == null;
    }

    public @Nullable PacketListener getPacketListener() {
        return this.packetListener;
    }

    public @Nullable DisconnectionInfo getDisconnectionInfo() {
        return this.disconnectionInfo;
    }

    public void tryDisableAutoRead() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }
    }

    public void setCompressionThreshold(int compressionThreshold, boolean rejectsBadPackets) {
        if (compressionThreshold >= 0) {
            ChannelHandler channelHandler = this.channel.pipeline().get("decompress");
            if (channelHandler instanceof PacketInflater) {
                PacketInflater packetInflater = (PacketInflater)channelHandler;
                packetInflater.setCompressionThreshold(compressionThreshold, rejectsBadPackets);
            } else {
                this.channel.pipeline().addAfter("splitter", "decompress", (ChannelHandler)new PacketInflater(compressionThreshold, rejectsBadPackets));
            }
            channelHandler = this.channel.pipeline().get("compress");
            if (channelHandler instanceof PacketDeflater) {
                PacketDeflater packetDeflater = (PacketDeflater)channelHandler;
                packetDeflater.setCompressionThreshold(compressionThreshold);
            } else {
                this.channel.pipeline().addAfter("prepender", "compress", (ChannelHandler)new PacketDeflater(compressionThreshold));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof PacketInflater) {
                this.channel.pipeline().remove("decompress");
            }
            if (this.channel.pipeline().get("compress") instanceof PacketDeflater) {
                this.channel.pipeline().remove("compress");
            }
        }
    }

    public void handleDisconnection() {
        PacketListener packetListener2;
        if (this.channel == null || this.channel.isOpen()) {
            return;
        }
        if (this.disconnected) {
            LOGGER.warn("handleDisconnection() called twice");
            return;
        }
        this.disconnected = true;
        PacketListener packetListener = this.getPacketListener();
        PacketListener packetListener3 = packetListener2 = packetListener != null ? packetListener : this.prePlayStateListener;
        if (packetListener2 != null) {
            DisconnectionInfo disconnectionInfo = Objects.requireNonNullElseGet(this.getDisconnectionInfo(), () -> new DisconnectionInfo(Text.translatable("multiplayer.disconnect.generic")));
            packetListener2.onDisconnected(disconnectionInfo);
        }
    }

    public float getAveragePacketsReceived() {
        return this.averagePacketsReceived;
    }

    public float getAveragePacketsSent() {
        return this.averagePacketsSent;
    }

    public void resetPacketSizeLog(MultiValueDebugSampleLogImpl log) {
        this.packetSizeLogger = new PacketSizeLogger(log);
    }

    protected /* synthetic */ void channelRead0(ChannelHandlerContext context, Object packet) throws Exception {
        this.channelRead0(context, (Packet)packet);
    }
}

