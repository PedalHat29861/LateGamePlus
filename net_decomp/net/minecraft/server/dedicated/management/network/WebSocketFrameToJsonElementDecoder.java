/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToMessageDecoder
 *  io.netty.handler.codec.http.websocketx.TextWebSocketFrame
 */
package net.minecraft.server.dedicated.management.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;

public class WebSocketFrameToJsonElementDecoder
extends MessageToMessageDecoder<TextWebSocketFrame> {
    protected void decode(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame, List<Object> list) {
        JsonElement jsonElement = JsonParser.parseString((String)textWebSocketFrame.text());
        list.add(jsonElement);
    }

    protected /* synthetic */ void decode(ChannelHandlerContext context, Object frame, List out) throws Exception {
        this.decode(context, (TextWebSocketFrame)frame, (List<Object>)out);
    }
}

