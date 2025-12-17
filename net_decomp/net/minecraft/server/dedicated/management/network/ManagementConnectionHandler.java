/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.SimpleChannelInboundHandler
 *  io.netty.handler.timeout.ReadTimeoutException
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated.management.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.dedicated.management.IncomingRpcMethod;
import net.minecraft.server.dedicated.management.InvalidRpcRequestException;
import net.minecraft.server.dedicated.management.JsonRpc;
import net.minecraft.server.dedicated.management.ManagementError;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.ManagementServer;
import net.minecraft.server.dedicated.management.OutgoingRpcMethod;
import net.minecraft.server.dedicated.management.PendingResponse;
import net.minecraft.server.dedicated.management.RpcEncodingException;
import net.minecraft.server.dedicated.management.RpcException;
import net.minecraft.server.dedicated.management.RpcMethodNotFoundException;
import net.minecraft.server.dedicated.management.RpcRemoteErrorException;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ManagementConnectionHandler
extends SimpleChannelInboundHandler<JsonElement> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private final ManagementLogger managementLogger;
    private final ManagementConnectionId remote;
    private final ManagementServer managementServer;
    private final Channel channel;
    private final ManagementHandlerDispatcher handlerDispatcher;
    private final AtomicInteger OUTGOING_REQUEST_ID = new AtomicInteger();
    private final Int2ObjectMap<PendingResponse<?>> pendingResponses = Int2ObjectMaps.synchronize((Int2ObjectMap)new Int2ObjectOpenHashMap());

    public ManagementConnectionHandler(Channel channel, ManagementServer managementServer, ManagementHandlerDispatcher handlerDispatcher, ManagementLogger managementLogger) {
        this.remote = ManagementConnectionId.of(CONNECTION_ID.incrementAndGet());
        this.managementServer = managementServer;
        this.handlerDispatcher = handlerDispatcher;
        this.channel = channel;
        this.managementLogger = managementLogger;
    }

    public void processTimeouts() {
        long l = Util.getMeasuringTimeMs();
        this.pendingResponses.int2ObjectEntrySet().removeIf(responseEntry -> {
            boolean bl = ((PendingResponse)responseEntry.getValue()).shouldTimeout(l);
            if (bl) {
                ((PendingResponse)responseEntry.getValue()).resultFuture().completeExceptionally((Throwable)new ReadTimeoutException("RPC method " + String.valueOf(((PendingResponse)responseEntry.getValue()).method().registryKey().getValue()) + " timed out waiting for response"));
            }
            return bl;
        });
    }

    public void channelActive(ChannelHandlerContext context) throws Exception {
        this.managementLogger.logAction(this.remote, "Management connection opened for {}", this.channel.remoteAddress());
        super.channelActive(context);
        this.managementServer.onConnectionOpen(this);
    }

    public void channelInactive(ChannelHandlerContext context) throws Exception {
        this.managementLogger.logAction(this.remote, "Management connection closed for {}", this.channel.remoteAddress());
        super.channelInactive(context);
        this.managementServer.onConnectionClose(this);
    }

    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) throws Exception {
        if (throwable.getCause() instanceof JsonParseException) {
            this.channel.writeAndFlush((Object)ManagementError.PARSE_ERROR.encode(throwable.getMessage()));
            return;
        }
        super.exceptionCaught(context, throwable);
        this.channel.close().awaitUninterruptibly();
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = this.handleMessage(jsonElement.getAsJsonObject());
            if (jsonObject != null) {
                this.channel.writeAndFlush((Object)jsonObject);
            }
        } else if (jsonElement.isJsonArray()) {
            this.channel.writeAndFlush((Object)this.handleEach(jsonElement.getAsJsonArray().asList()));
        } else {
            this.channel.writeAndFlush((Object)ManagementError.INVALID_REQUEST.encode((String)null));
        }
    }

    private JsonArray handleEach(List<JsonElement> messages) {
        JsonArray jsonArray = new JsonArray();
        messages.stream().map(message -> this.handleMessage(message.getAsJsonObject())).filter(Objects::nonNull).forEach(arg_0 -> ((JsonArray)jsonArray).add(arg_0));
        return jsonArray;
    }

    public void sendNotification(RegistryEntry.Reference<? extends OutgoingRpcMethod<Void, ?>> method) {
        this.sendRequest(method, null, false);
    }

    public <Params> void sendNotification(RegistryEntry.Reference<? extends OutgoingRpcMethod<Params, ?>> method, Params params) {
        this.sendRequest(method, params, false);
    }

    public <Result> CompletableFuture<Result> sendRequest(RegistryEntry.Reference<? extends OutgoingRpcMethod<Void, Result>> method) {
        return this.sendRequest(method, null, true);
    }

    public <Params, Result> CompletableFuture<Result> sendRequest(RegistryEntry.Reference<? extends OutgoingRpcMethod<Params, Result>> method, Params params) {
        return this.sendRequest(method, params, true);
    }

    @Contract(value="_,_,false->null;_,_,true->!null")
    private <Params, Result> @Nullable CompletableFuture<Result> sendRequest(RegistryEntry.Reference<? extends OutgoingRpcMethod<Params, ? extends Result>> method, @Nullable Params params, boolean expectResponse) {
        List<JsonElement> list;
        List<JsonElement> list2 = list = params != null ? List.of(Objects.requireNonNull(method.value().encodeParams(params))) : List.of();
        if (expectResponse) {
            CompletableFuture completableFuture = new CompletableFuture();
            int i = this.OUTGOING_REQUEST_ID.incrementAndGet();
            long l = Util.nanoTimeSupplier.get(TimeUnit.MILLISECONDS);
            this.pendingResponses.put(i, new PendingResponse(method, completableFuture, l + 5000L));
            this.channel.writeAndFlush((Object)JsonRpc.encodeRequest(i, method.registryKey().getValue(), list));
            return completableFuture;
        }
        this.channel.writeAndFlush((Object)JsonRpc.encodeRequest(null, method.registryKey().getValue(), list));
        return null;
    }

    @VisibleForTesting
    @Nullable JsonObject handleMessage(JsonObject request) {
        try {
            JsonElement jsonElement = JsonRpc.getId(request);
            String string = JsonRpc.getMethod(request);
            JsonElement jsonElement2 = JsonRpc.getResult(request);
            JsonElement jsonElement3 = JsonRpc.getParameters(request);
            JsonObject jsonObject = JsonRpc.getError(request);
            if (string != null && jsonElement2 == null && jsonObject == null) {
                if (jsonElement != null && !ManagementConnectionHandler.isValidRequestId(jsonElement)) {
                    return ManagementError.INVALID_REQUEST.encode("Invalid request id - only String, Number and NULL supported");
                }
                return this.handleRequest(jsonElement, string, jsonElement3);
            }
            if (string == null && jsonElement2 != null && jsonObject == null && jsonElement != null) {
                if (ManagementConnectionHandler.isValidResponseId(jsonElement)) {
                    this.handleResponse(jsonElement.getAsInt(), jsonElement2);
                } else {
                    LOGGER.warn("Received respose {} with id {} we did not request", (Object)jsonElement2, (Object)jsonElement);
                }
                return null;
            }
            if (string == null && jsonElement2 == null && jsonObject != null) {
                return this.handleError(jsonElement, jsonObject);
            }
            return ManagementError.INVALID_REQUEST.encode((JsonElement)Objects.requireNonNullElse(jsonElement, JsonNull.INSTANCE));
        }
        catch (Exception exception) {
            LOGGER.error("Error while handling rpc request", (Throwable)exception);
            return ManagementError.INTERNAL_ERROR.encode("Unknown error handling request - check server logs for stack trace");
        }
    }

    private static boolean isValidRequestId(JsonElement json) {
        return json.isJsonNull() || JsonHelper.isNumber(json) || JsonHelper.isString(json);
    }

    private static boolean isValidResponseId(JsonElement json) {
        return JsonHelper.isNumber(json);
    }

    private @Nullable JsonObject handleRequest(@Nullable JsonElement json, String method, @Nullable JsonElement parameters) {
        boolean bl = json != null;
        try {
            JsonElement jsonElement = this.processRequest(method, parameters);
            if (jsonElement == null || !bl) {
                return null;
            }
            return JsonRpc.encodeResult(json, jsonElement);
        }
        catch (RpcException rpcException) {
            LOGGER.debug("Invalid parameter invocation {}: {}, {}", new Object[]{method, parameters, rpcException.getMessage()});
            return bl ? ManagementError.INVALID_PARAMS.encode(json, rpcException.getMessage()) : null;
        }
        catch (RpcEncodingException rpcEncodingException) {
            LOGGER.error("Failed to encode json rpc response {}: {}", (Object)method, (Object)rpcEncodingException.getMessage());
            return bl ? ManagementError.INTERNAL_ERROR.encode(json, rpcEncodingException.getMessage()) : null;
        }
        catch (InvalidRpcRequestException invalidRpcRequestException) {
            return bl ? ManagementError.INVALID_REQUEST.encode(json, invalidRpcRequestException.getMessage()) : null;
        }
        catch (RpcMethodNotFoundException rpcMethodNotFoundException) {
            return bl ? ManagementError.METHOD_NOT_FOUND.encode(json, rpcMethodNotFoundException.getMessage()) : null;
        }
        catch (Exception exception) {
            LOGGER.error("Error while dispatching rpc method {}", (Object)method, (Object)exception);
            return bl ? ManagementError.INTERNAL_ERROR.encode(json) : null;
        }
    }

    public @Nullable JsonElement processRequest(String method, @Nullable JsonElement json) {
        Identifier identifier = Identifier.tryParse(method);
        if (identifier == null) {
            throw new InvalidRpcRequestException("Failed to parse method value: " + method);
        }
        Optional<IncomingRpcMethod<?, ?>> optional = Registries.INCOMING_RPC_METHOD.getOptionalValue(identifier);
        if (optional.isEmpty()) {
            throw new RpcMethodNotFoundException("Method not found: " + method);
        }
        if (optional.get().attributes().runOnMainThread()) {
            try {
                return this.handlerDispatcher.submit(() -> ((IncomingRpcMethod)optional.get()).handle(this.handlerDispatcher, json, this.remote)).join();
            }
            catch (CompletionException completionException) {
                Throwable throwable = completionException.getCause();
                if (throwable instanceof RuntimeException) {
                    RuntimeException runtimeException = (RuntimeException)throwable;
                    throw runtimeException;
                }
                throw completionException;
            }
        }
        return optional.get().handle(this.handlerDispatcher, json, this.remote);
    }

    private void handleResponse(int id, JsonElement result) {
        PendingResponse pendingResponse = (PendingResponse)this.pendingResponses.remove(id);
        if (pendingResponse == null) {
            LOGGER.warn("Received unknown response (id: {}): {}", (Object)id, (Object)result);
        } else {
            pendingResponse.handleResponse(result);
        }
    }

    private @Nullable JsonObject handleError(@Nullable JsonElement json, JsonObject error) {
        PendingResponse pendingResponse;
        if (json != null && ManagementConnectionHandler.isValidResponseId(json) && (pendingResponse = (PendingResponse)this.pendingResponses.remove(json.getAsInt())) != null) {
            pendingResponse.resultFuture().completeExceptionally(new RpcRemoteErrorException(json, error));
        }
        LOGGER.error("Received error (id: {}): {}", (Object)json, (Object)error);
        return null;
    }

    protected /* synthetic */ void channelRead0(ChannelHandlerContext context, Object in) throws Exception {
        this.channelRead0(context, (JsonElement)in);
    }
}

