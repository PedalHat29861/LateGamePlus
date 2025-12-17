/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jspecify.annotations.Nullable;

public class JsonRpc {
    public static final String JSON_RPC_VERSION = "2.0";
    public static final String field_62309 = "1.3.2";

    public static JsonObject encodeResult(JsonElement id, JsonElement result) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jsonrpc", JSON_RPC_VERSION);
        jsonObject.add("id", id);
        jsonObject.add("result", result);
        return jsonObject;
    }

    public static JsonObject encodeRequest(@Nullable Integer id, Identifier method, List<JsonElement> parameters) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jsonrpc", JSON_RPC_VERSION);
        if (id != null) {
            jsonObject.addProperty("id", (Number)id);
        }
        jsonObject.addProperty("method", method.toString());
        if (!parameters.isEmpty()) {
            JsonArray jsonArray = new JsonArray(parameters.size());
            for (JsonElement jsonElement : parameters) {
                jsonArray.add(jsonElement);
            }
            jsonObject.add("params", (JsonElement)jsonArray);
        }
        return jsonObject;
    }

    public static JsonObject encodeError(JsonElement id, String message, int code, @Nullable String data) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jsonrpc", JSON_RPC_VERSION);
        jsonObject.add("id", id);
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("code", (Number)code);
        jsonObject2.addProperty("message", message);
        if (data != null && !data.isBlank()) {
            jsonObject2.addProperty("data", data);
        }
        jsonObject.add("error", (JsonElement)jsonObject2);
        return jsonObject;
    }

    public static @Nullable JsonElement getId(JsonObject request) {
        return request.get("id");
    }

    public static @Nullable String getMethod(JsonObject request) {
        return JsonHelper.getString(request, "method", null);
    }

    public static @Nullable JsonElement getParameters(JsonObject request) {
        return request.get("params");
    }

    public static @Nullable JsonElement getResult(JsonObject response) {
        return response.get("result");
    }

    public static @Nullable JsonObject getError(JsonObject response) {
        return JsonHelper.getObject(response, "error", null);
    }
}

