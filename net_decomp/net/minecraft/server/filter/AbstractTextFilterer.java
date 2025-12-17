/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.internal.Streams
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.filter;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import net.minecraft.network.message.FilterMask;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.filter.V0TextFilterer;
import net.minecraft.server.filter.V1TextFilterer;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.thread.SimpleConsecutiveExecutor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractTextFilterer
implements AutoCloseable {
    protected static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger WORKER_ID = new AtomicInteger(1);
    private static final ThreadFactory THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Chat-Filter-Worker-" + WORKER_ID.getAndIncrement());
        return thread;
    };
    private final URL url;
    private final MessageEncoder messageEncoder;
    final HashIgnorer hashIgnorer;
    final ExecutorService threadPool;

    protected static ExecutorService newThreadPool(int threadCount) {
        return Executors.newFixedThreadPool(threadCount, THREAD_FACTORY);
    }

    protected AbstractTextFilterer(URL url, MessageEncoder messageEncoder, HashIgnorer hashIgnorer, ExecutorService threadPool) {
        this.hashIgnorer = hashIgnorer;
        this.threadPool = threadPool;
        this.url = url;
        this.messageEncoder = messageEncoder;
    }

    protected static URL resolveEndpoint(URI uri, @Nullable JsonObject endpoints, String key, String defaultPath) throws MalformedURLException {
        String string = AbstractTextFilterer.getEndpointPath(endpoints, key, defaultPath);
        return uri.resolve("/" + string).toURL();
    }

    protected static String getEndpointPath(@Nullable JsonObject endpoints, String key, String defaultPath) {
        return endpoints != null ? JsonHelper.getString(endpoints, key, defaultPath) : defaultPath;
    }

    public static @Nullable AbstractTextFilterer createTextFilter(ServerPropertiesHandler properties) {
        String string = properties.textFilteringConfig;
        if (StringHelper.isBlank(string)) {
            return null;
        }
        return switch (properties.textFilteringVersion) {
            case 0 -> V0TextFilterer.load(string);
            case 1 -> V1TextFilterer.load(string);
            default -> {
                LOGGER.warn("Could not create text filter - unsupported text filtering version used");
                yield null;
            }
        };
    }

    protected CompletableFuture<FilteredMessage> filter(GameProfile profile, String raw, HashIgnorer hashIgnorer, Executor executor) {
        if (raw.isEmpty()) {
            return CompletableFuture.completedFuture(FilteredMessage.EMPTY);
        }
        return CompletableFuture.supplyAsync(() -> {
            JsonObject jsonObject = this.messageEncoder.encode(profile, raw);
            try {
                JsonObject jsonObject2 = this.request(jsonObject, this.url);
                return this.filter(raw, hashIgnorer, jsonObject2);
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to validate message '{}'", (Object)raw, (Object)exception);
                return FilteredMessage.censored(raw);
            }
        }, executor);
    }

    protected abstract FilteredMessage filter(String var1, HashIgnorer var2, JsonObject var3);

    protected FilterMask createFilterMask(String raw, JsonArray redactedTextIndex, HashIgnorer hashIgnorer) {
        if (redactedTextIndex.isEmpty()) {
            return FilterMask.PASS_THROUGH;
        }
        if (hashIgnorer.shouldIgnore(raw, redactedTextIndex.size())) {
            return FilterMask.FULLY_FILTERED;
        }
        FilterMask filterMask = new FilterMask(raw.length());
        for (int i = 0; i < redactedTextIndex.size(); ++i) {
            filterMask.markFiltered(redactedTextIndex.get(i).getAsInt());
        }
        return filterMask;
    }

    @Override
    public void close() {
        this.threadPool.shutdownNow();
    }

    protected void discardRestOfInput(InputStream stream) throws IOException {
        byte[] bs = new byte[1024];
        while (stream.read(bs) != -1) {
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private JsonObject request(JsonObject request, URL url) throws IOException {
        HttpURLConnection httpURLConnection = this.openConnection(request, url);
        try (InputStream inputStream = httpURLConnection.getInputStream();){
            JsonObject jsonObject;
            if (httpURLConnection.getResponseCode() == 204) {
                JsonObject jsonObject2 = new JsonObject();
                return jsonObject2;
            }
            try {
                jsonObject = LenientJsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            }
            catch (Throwable throwable) {
                this.discardRestOfInput(inputStream);
                throw throwable;
            }
            this.discardRestOfInput(inputStream);
            return jsonObject;
        }
    }

    protected HttpURLConnection openConnection(JsonObject request, URL url) throws IOException {
        HttpURLConnection httpURLConnection = this.openConnection(url);
        this.addAuthentication(httpURLConnection);
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), StandardCharsets.UTF_8);
             JsonWriter jsonWriter = new JsonWriter((Writer)outputStreamWriter);){
            Streams.write((JsonElement)request, (JsonWriter)jsonWriter);
        }
        int i = httpURLConnection.getResponseCode();
        if (i < 200 || i >= 300) {
            throw new FailedHttpRequestException(i + " " + httpURLConnection.getResponseMessage());
        }
        return httpURLConnection;
    }

    protected abstract void addAuthentication(HttpURLConnection var1);

    protected int getReadTimeout() {
        return 2000;
    }

    protected HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setReadTimeout(this.getReadTimeout());
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getGameVersion().name());
        return httpURLConnection;
    }

    public TextStream createFilterer(GameProfile profile) {
        return new StreamImpl(profile);
    }

    @FunctionalInterface
    public static interface HashIgnorer {
        public static final HashIgnorer NEVER_IGNORE = (hashes, hashesSize) -> false;
        public static final HashIgnorer IGNORE_IF_MATCHES_ALL = (hashes, hashesSize) -> hashes.length() == hashesSize;

        public static HashIgnorer internalDropHashes(int hashesToDrop) {
            return (hashes, hashesSize) -> hashesSize >= hashesToDrop;
        }

        public static HashIgnorer dropHashes(int hashesToDrop) {
            return switch (hashesToDrop) {
                case -1 -> NEVER_IGNORE;
                case 0 -> IGNORE_IF_MATCHES_ALL;
                default -> HashIgnorer.internalDropHashes(hashesToDrop);
            };
        }

        public boolean shouldIgnore(String var1, int var2);
    }

    @FunctionalInterface
    protected static interface MessageEncoder {
        public JsonObject encode(GameProfile var1, String var2);
    }

    protected static class FailedHttpRequestException
    extends RuntimeException {
        protected FailedHttpRequestException(String message) {
            super(message);
        }
    }

    protected class StreamImpl
    implements TextStream {
        protected final GameProfile gameProfile;
        protected final Executor executor;

        protected StreamImpl(GameProfile gameProfile) {
            this.gameProfile = gameProfile;
            SimpleConsecutiveExecutor simpleConsecutiveExecutor = new SimpleConsecutiveExecutor(AbstractTextFilterer.this.threadPool, "chat stream for " + gameProfile.name());
            this.executor = simpleConsecutiveExecutor::send;
        }

        @Override
        public CompletableFuture<List<FilteredMessage>> filterTexts(List<String> texts) {
            List list = (List)texts.stream().map(text -> AbstractTextFilterer.this.filter(this.gameProfile, (String)text, AbstractTextFilterer.this.hashIgnorer, this.executor)).collect(ImmutableList.toImmutableList());
            return Util.combine(list).exceptionally(throwable -> ImmutableList.of());
        }

        @Override
        public CompletableFuture<FilteredMessage> filterText(String text) {
            return AbstractTextFilterer.this.filter(this.gameProfile, text, AbstractTextFilterer.this.hashIgnorer, this.executor);
        }
    }
}

