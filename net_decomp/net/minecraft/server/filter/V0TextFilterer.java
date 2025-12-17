/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.mojang.authlib.GameProfile
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import net.minecraft.network.message.FilterMask;
import net.minecraft.server.filter.AbstractTextFilterer;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.filter.TextStream;
import net.minecraft.util.JsonHelper;
import org.jspecify.annotations.Nullable;

public class V0TextFilterer
extends AbstractTextFilterer {
    private static final String CHAT_ENDPOINT = "v1/chat";
    final URL joinEndpoint;
    final ProfileEncoder joinEncoder;
    final URL leaveEndpoint;
    final ProfileEncoder leaveEncoder;
    private final String apiKey;

    private V0TextFilterer(URL chatEndpoint, AbstractTextFilterer.MessageEncoder messageEncoder, URL joinEndpoint, ProfileEncoder joinEncoder, URL leaveEndpoint, ProfileEncoder leaveEncoder, String apiKey, AbstractTextFilterer.HashIgnorer ignorer, ExecutorService threadPool) {
        super(chatEndpoint, messageEncoder, ignorer, threadPool);
        this.joinEndpoint = joinEndpoint;
        this.joinEncoder = joinEncoder;
        this.leaveEndpoint = leaveEndpoint;
        this.leaveEncoder = leaveEncoder;
        this.apiKey = apiKey;
    }

    public static @Nullable AbstractTextFilterer load(String config) {
        try {
            AbstractTextFilterer.MessageEncoder messageEncoder;
            JsonObject jsonObject = JsonHelper.deserialize(config);
            URI uRI = new URI(JsonHelper.getString(jsonObject, "apiServer"));
            String string = JsonHelper.getString(jsonObject, "apiKey");
            if (string.isEmpty()) {
                throw new IllegalArgumentException("Missing API key");
            }
            int i = JsonHelper.getInt(jsonObject, "ruleId", 1);
            String string2 = JsonHelper.getString(jsonObject, "serverId", "");
            String string3 = JsonHelper.getString(jsonObject, "roomId", "Java:Chat");
            int j = JsonHelper.getInt(jsonObject, "hashesToDrop", -1);
            int k = JsonHelper.getInt(jsonObject, "maxConcurrentRequests", 7);
            JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "endpoints", null);
            String string4 = V0TextFilterer.getEndpointPath(jsonObject2, "chat", CHAT_ENDPOINT);
            boolean bl = string4.equals(CHAT_ENDPOINT);
            URL uRL = uRI.resolve("/" + string4).toURL();
            URL uRL2 = V0TextFilterer.resolveEndpoint(uRI, jsonObject2, "join", "v1/join");
            URL uRL3 = V0TextFilterer.resolveEndpoint(uRI, jsonObject2, "leave", "v1/leave");
            ProfileEncoder profileEncoder = profile -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("server", string2);
                jsonObject.addProperty("room", string3);
                jsonObject.addProperty("user_id", profile.id().toString());
                jsonObject.addProperty("user_display_name", profile.name());
                return jsonObject;
            };
            if (bl) {
                messageEncoder = (profile, message) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("rule", (Number)i);
                    jsonObject.addProperty("server", string2);
                    jsonObject.addProperty("room", string3);
                    jsonObject.addProperty("player", profile.id().toString());
                    jsonObject.addProperty("player_display_name", profile.name());
                    jsonObject.addProperty("text", message);
                    jsonObject.addProperty("language", "*");
                    return jsonObject;
                };
            } else {
                String string5 = String.valueOf(i);
                messageEncoder = (profile, message) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("rule_id", string5);
                    jsonObject.addProperty("category", string2);
                    jsonObject.addProperty("subcategory", string3);
                    jsonObject.addProperty("user_id", profile.id().toString());
                    jsonObject.addProperty("user_display_name", profile.name());
                    jsonObject.addProperty("text", message);
                    jsonObject.addProperty("language", "*");
                    return jsonObject;
                };
            }
            AbstractTextFilterer.HashIgnorer hashIgnorer = AbstractTextFilterer.HashIgnorer.dropHashes(j);
            ExecutorService executorService = V0TextFilterer.newThreadPool(k);
            String string6 = Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.US_ASCII));
            return new V0TextFilterer(uRL, messageEncoder, uRL2, profileEncoder, uRL3, profileEncoder, string6, hashIgnorer, executorService);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse chat filter config {}", (Object)config, (Object)exception);
            return null;
        }
    }

    @Override
    public TextStream createFilterer(GameProfile profile) {
        return new AbstractTextFilterer.StreamImpl(profile){

            @Override
            public void onConnect() {
                V0TextFilterer.this.sendJoinOrLeaveRequest(this.gameProfile, V0TextFilterer.this.joinEndpoint, V0TextFilterer.this.joinEncoder, this.executor);
            }

            @Override
            public void onDisconnect() {
                V0TextFilterer.this.sendJoinOrLeaveRequest(this.gameProfile, V0TextFilterer.this.leaveEndpoint, V0TextFilterer.this.leaveEncoder, this.executor);
            }
        };
    }

    void sendJoinOrLeaveRequest(GameProfile gameProfile, URL endpoint, ProfileEncoder profileEncoder, Executor executor) {
        executor.execute(() -> {
            JsonObject jsonObject = profileEncoder.encode(gameProfile);
            try {
                this.sendRequest(jsonObject, endpoint);
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to send join/leave packet to {} for player {}", new Object[]{endpoint, gameProfile, exception});
            }
        });
    }

    private void sendRequest(JsonObject payload, URL endpoint) throws IOException {
        HttpURLConnection httpURLConnection = this.openConnection(payload, endpoint);
        try (InputStream inputStream = httpURLConnection.getInputStream();){
            this.discardRestOfInput(inputStream);
        }
    }

    @Override
    protected void addAuthentication(HttpURLConnection connection) {
        connection.setRequestProperty("Authorization", "Basic " + this.apiKey);
    }

    @Override
    protected FilteredMessage filter(String raw, AbstractTextFilterer.HashIgnorer hashIgnorer, JsonObject response) {
        boolean bl = JsonHelper.getBoolean(response, "response", false);
        if (bl) {
            return FilteredMessage.permitted(raw);
        }
        String string = JsonHelper.getString(response, "hashed", null);
        if (string == null) {
            return FilteredMessage.censored(raw);
        }
        JsonArray jsonArray = JsonHelper.getArray(response, "hashes");
        FilterMask filterMask = this.createFilterMask(raw, jsonArray, hashIgnorer);
        return new FilteredMessage(raw, filterMask);
    }

    @FunctionalInterface
    static interface ProfileEncoder {
        public JsonObject encode(GameProfile var1);
    }
}

