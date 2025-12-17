/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.Unpooled
 *  io.netty.channel.ChannelDuplexHandler
 *  io.netty.channel.ChannelHandler$Sharable
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelPromise
 *  io.netty.handler.codec.http.DefaultFullHttpResponse
 *  io.netty.handler.codec.http.HttpHeaderNames
 *  io.netty.handler.codec.http.HttpRequest
 *  io.netty.handler.codec.http.HttpResponse
 *  io.netty.handler.codec.http.HttpResponseStatus
 *  io.netty.handler.codec.http.HttpVersion
 *  io.netty.util.AttributeKey
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated.management.network;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import net.minecraft.network.encryption.BearerToken;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@ChannelHandler.Sharable
public class BearerAuthenticationHandler
extends ChannelDuplexHandler {
    private final Logger LOGGER = LogUtils.getLogger();
    private static final AttributeKey<Boolean> AUTHENTICATED_KEY = AttributeKey.valueOf((String)"authenticated");
    private static final AttributeKey<Boolean> WEBSOCKET_AUTH_ALLOWED_KEY = AttributeKey.valueOf((String)"websocket_auth_allowed");
    private static final String PROTOCOL = "minecraft-v1";
    private static final String PROTOCOL_PREFIX = "minecraft-v1,";
    public static final String BEARER_PREFIX = "Bearer ";
    private final BearerToken token;
    private final Set<String> allowedOrigins;

    public BearerAuthenticationHandler(BearerToken token, String allowedOrigins) {
        this.token = token;
        this.allowedOrigins = Sets.newHashSet((Object[])allowedOrigins.split(","));
    }

    public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
        Boolean boolean_;
        String string = this.getHostAddress(context);
        if (object instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest)object;
            Result result = this.authenticate(httpRequest);
            if (result.isSuccessful()) {
                context.channel().attr(AUTHENTICATED_KEY).set((Object)true);
                if (result.mustReturnProtocol()) {
                    context.channel().attr(WEBSOCKET_AUTH_ALLOWED_KEY).set((Object)Boolean.TRUE);
                }
            } else {
                this.LOGGER.debug("Authentication rejected for connection with ip {}: {}", (Object)string, (Object)result.getMessage());
                context.channel().attr(AUTHENTICATED_KEY).set((Object)false);
                this.sendUnauthorizedError(context, result.getMessage());
                return;
            }
        }
        if (Boolean.TRUE.equals(boolean_ = (Boolean)context.channel().attr(AUTHENTICATED_KEY).get())) {
            super.channelRead(context, object);
        } else {
            this.LOGGER.debug("Dropping unauthenticated connection with ip {}", (Object)string);
            context.close();
        }
    }

    public void write(ChannelHandlerContext context, Object value, ChannelPromise promise) throws Exception {
        HttpResponse httpResponse;
        if (value instanceof HttpResponse && (httpResponse = (HttpResponse)value).status().code() == HttpResponseStatus.SWITCHING_PROTOCOLS.code() && context.channel().attr(WEBSOCKET_AUTH_ALLOWED_KEY).get() != null && ((Boolean)context.channel().attr(WEBSOCKET_AUTH_ALLOWED_KEY).get()).equals(Boolean.TRUE)) {
            httpResponse.headers().set((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, (Object)PROTOCOL);
        }
        super.write(context, value, promise);
    }

    private Result authenticate(HttpRequest request) {
        String string = this.getBearerToken(request);
        if (string != null) {
            if (this.tokenMatches(string)) {
                return Result.success();
            }
            return Result.failure("Invalid API key");
        }
        String string2 = this.getProtocolToken(request);
        if (string2 != null) {
            if (!this.isOriginAllowed(request)) {
                return Result.failure("Origin Not Allowed");
            }
            if (this.tokenMatches(string2)) {
                return Result.success(true);
            }
            return Result.failure("Invalid API key");
        }
        return Result.failure("Missing API key");
    }

    private boolean isOriginAllowed(HttpRequest request) {
        String string = request.headers().get((CharSequence)HttpHeaderNames.ORIGIN);
        if (string == null || string.isEmpty()) {
            return false;
        }
        return this.allowedOrigins.contains(string);
    }

    private @Nullable String getBearerToken(HttpRequest request) {
        String string = request.headers().get((CharSequence)HttpHeaderNames.AUTHORIZATION);
        if (string != null && string.startsWith(BEARER_PREFIX)) {
            return string.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    private @Nullable String getProtocolToken(HttpRequest request) {
        String string = request.headers().get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
        if (string != null && string.startsWith(PROTOCOL_PREFIX)) {
            return string.substring(PROTOCOL_PREFIX.length()).trim();
        }
        return null;
    }

    public boolean tokenMatches(String requestToken) {
        if (requestToken.isEmpty()) {
            return false;
        }
        byte[] bs = requestToken.getBytes(StandardCharsets.UTF_8);
        byte[] cs = this.token.secretKey().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(bs, cs);
    }

    private String getHostAddress(ChannelHandlerContext context) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress)context.channel().remoteAddress();
        return inetSocketAddress.getAddress().getHostAddress();
    }

    private void sendUnauthorizedError(ChannelHandlerContext context, String message) {
        String string = "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED, Unpooled.wrappedBuffer((byte[])bs));
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONTENT_TYPE, (Object)"application/json");
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONTENT_LENGTH, (Object)bs.length);
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONNECTION, (Object)"close");
        context.writeAndFlush((Object)defaultFullHttpResponse).addListener(future -> context.close());
    }

    static class Result {
        private final boolean successful;
        private final String message;
        private final boolean mustReturnProtocol;

        private Result(boolean successful, String message, boolean mustReturnProtocol) {
            this.successful = successful;
            this.message = message;
            this.mustReturnProtocol = mustReturnProtocol;
        }

        public static Result success() {
            return new Result(true, null, false);
        }

        public static Result success(boolean mustReturnProtocol) {
            return new Result(true, null, mustReturnProtocol);
        }

        public static Result failure(String message) {
            return new Result(false, message, false);
        }

        public boolean isSuccessful() {
            return this.successful;
        }

        public String getMessage() {
            return this.message;
        }

        public boolean mustReturnProtocol() {
            return this.mustReturnProtocol;
        }
    }
}

