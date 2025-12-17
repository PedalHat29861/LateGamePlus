/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.microsoft.aad.msal4j.ClientCredentialFactory
 *  com.microsoft.aad.msal4j.ClientCredentialParameters
 *  com.microsoft.aad.msal4j.ConfidentialClientApplication
 *  com.microsoft.aad.msal4j.ConfidentialClientApplication$Builder
 *  com.microsoft.aad.msal4j.IAuthenticationResult
 *  com.microsoft.aad.msal4j.IClientCertificate
 *  com.microsoft.aad.msal4j.IClientCredential
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCertificate;
import com.microsoft.aad.msal4j.IClientCredential;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import net.minecraft.server.filter.AbstractTextFilterer;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.util.JsonHelper;
import org.jspecify.annotations.Nullable;

public class V1TextFilterer
extends AbstractTextFilterer {
    private final ConfidentialClientApplication application;
    private final ClientCredentialParameters credentialParameters;
    private final Set<String> fullyFilteredEvents;
    private final int readTimeout;

    private V1TextFilterer(URL url, AbstractTextFilterer.MessageEncoder messageEncoder, AbstractTextFilterer.HashIgnorer hashIgnorer, ExecutorService threadPool, ConfidentialClientApplication application, ClientCredentialParameters credentialParameters, Set<String> fullyFilteredEvents, int readTimeout) {
        super(url, messageEncoder, hashIgnorer, threadPool);
        this.application = application;
        this.credentialParameters = credentialParameters;
        this.fullyFilteredEvents = fullyFilteredEvents;
        this.readTimeout = readTimeout;
    }

    public static @Nullable AbstractTextFilterer load(String response) {
        ConfidentialClientApplication confidentialClientApplication;
        IClientCertificate iClientCertificate;
        URL uRL;
        JsonObject jsonObject = JsonHelper.deserialize(response);
        URI uRI = URI.create(JsonHelper.getString(jsonObject, "apiServer"));
        String string = JsonHelper.getString(jsonObject, "apiPath");
        String string2 = JsonHelper.getString(jsonObject, "scope");
        String string3 = JsonHelper.getString(jsonObject, "serverId", "");
        String string4 = JsonHelper.getString(jsonObject, "applicationId");
        String string5 = JsonHelper.getString(jsonObject, "tenantId");
        String string6 = JsonHelper.getString(jsonObject, "roomId", "Java:Chat");
        String string7 = JsonHelper.getString(jsonObject, "certificatePath");
        String string8 = JsonHelper.getString(jsonObject, "certificatePassword", "");
        int i = JsonHelper.getInt(jsonObject, "hashesToDrop", -1);
        int j = JsonHelper.getInt(jsonObject, "maxConcurrentRequests", 7);
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "fullyFilteredEvents");
        HashSet<String> set = new HashSet<String>();
        jsonArray.forEach(json -> set.add(JsonHelper.asString(json, "filteredEvent")));
        int k = JsonHelper.getInt(jsonObject, "connectionReadTimeoutMs", 2000);
        try {
            uRL = uRI.resolve(string).toURL();
        }
        catch (MalformedURLException malformedURLException) {
            throw new RuntimeException(malformedURLException);
        }
        AbstractTextFilterer.MessageEncoder messageEncoder = (profile, message) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId", profile.id().toString());
            jsonObject.addProperty("userDisplayName", profile.name());
            jsonObject.addProperty("server", string3);
            jsonObject.addProperty("room", string6);
            jsonObject.addProperty("area", "JavaChatRealms");
            jsonObject.addProperty("data", message);
            jsonObject.addProperty("language", "*");
            return jsonObject;
        };
        AbstractTextFilterer.HashIgnorer hashIgnorer = AbstractTextFilterer.HashIgnorer.dropHashes(i);
        ExecutorService executorService = V1TextFilterer.newThreadPool(j);
        try (InputStream inputStream = Files.newInputStream(Path.of(string7, new String[0]), new OpenOption[0]);){
            iClientCertificate = ClientCredentialFactory.createFromCertificate((InputStream)inputStream, (String)string8);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to open certificate file");
            return null;
        }
        try {
            confidentialClientApplication = ((ConfidentialClientApplication.Builder)((ConfidentialClientApplication.Builder)ConfidentialClientApplication.builder((String)string4, (IClientCredential)iClientCertificate).sendX5c(true).executorService(executorService)).authority(String.format(Locale.ROOT, "https://login.microsoftonline.com/%s/", string5))).build();
        }
        catch (Exception exception2) {
            LOGGER.warn("Failed to create confidential client application");
            return null;
        }
        ClientCredentialParameters clientCredentialParameters = ClientCredentialParameters.builder(Set.of(string2)).build();
        return new V1TextFilterer(uRL, messageEncoder, hashIgnorer, executorService, confidentialClientApplication, clientCredentialParameters, set, k);
    }

    private IAuthenticationResult getAuthToken() {
        return (IAuthenticationResult)this.application.acquireToken(this.credentialParameters).join();
    }

    @Override
    protected void addAuthentication(HttpURLConnection connection) {
        IAuthenticationResult iAuthenticationResult = this.getAuthToken();
        connection.setRequestProperty("Authorization", "Bearer " + iAuthenticationResult.accessToken());
    }

    @Override
    protected FilteredMessage filter(String raw, AbstractTextFilterer.HashIgnorer hashIgnorer, JsonObject response) {
        JsonObject jsonObject = JsonHelper.getObject(response, "result", null);
        if (jsonObject == null) {
            return FilteredMessage.censored(raw);
        }
        boolean bl = JsonHelper.getBoolean(jsonObject, "filtered", true);
        if (!bl) {
            return FilteredMessage.permitted(raw);
        }
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "events", new JsonArray());
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject2 = jsonElement.getAsJsonObject();
            String string = JsonHelper.getString(jsonObject2, "id", "");
            if (!this.fullyFilteredEvents.contains(string)) continue;
            return FilteredMessage.censored(raw);
        }
        JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "redactedTextIndex", new JsonArray());
        return new FilteredMessage(raw, this.createFilterMask(raw, jsonArray2, hashIgnorer));
    }

    @Override
    protected int getReadTimeout() {
        return this.readTimeout;
    }
}

