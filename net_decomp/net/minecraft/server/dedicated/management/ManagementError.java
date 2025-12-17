/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.server.dedicated.management.JsonRpc;
import org.jspecify.annotations.Nullable;

public final class ManagementError
extends Enum<ManagementError> {
    public static final /* enum */ ManagementError PARSE_ERROR = new ManagementError(-32700, "Parse error");
    public static final /* enum */ ManagementError INVALID_REQUEST = new ManagementError(-32600, "Invalid Request");
    public static final /* enum */ ManagementError METHOD_NOT_FOUND = new ManagementError(-32601, "Method not found");
    public static final /* enum */ ManagementError INVALID_PARAMS = new ManagementError(-32602, "Invalid params");
    public static final /* enum */ ManagementError INTERNAL_ERROR = new ManagementError(-32603, "Internal error");
    private final int code;
    private final String message;
    private static final /* synthetic */ ManagementError[] field_62307;

    public static ManagementError[] values() {
        return (ManagementError[])field_62307.clone();
    }

    public static ManagementError valueOf(String string) {
        return Enum.valueOf(ManagementError.class, string);
    }

    private ManagementError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JsonObject encode(@Nullable String data) {
        return JsonRpc.encodeError((JsonElement)JsonNull.INSTANCE, this.message, this.code, data);
    }

    public JsonObject encode(JsonElement json) {
        return JsonRpc.encodeError(json, this.message, this.code, null);
    }

    public JsonObject encode(JsonElement json, String data) {
        return JsonRpc.encodeError(json, this.message, this.code, data);
    }

    private static /* synthetic */ ManagementError[] method_73644() {
        return new ManagementError[]{PARSE_ERROR, INVALID_REQUEST, METHOD_NOT_FOUND, INVALID_PARAMS, INTERNAL_ERROR};
    }

    static {
        field_62307 = ManagementError.method_73644();
    }
}

