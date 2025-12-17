/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RpcRemoteErrorException
extends RuntimeException {
    private final JsonElement id;
    private final JsonObject error;

    public RpcRemoteErrorException(JsonElement id, JsonObject error) {
        this.id = id;
        this.error = error;
    }

    private JsonObject getError() {
        return this.error;
    }

    private JsonElement getId() {
        return this.id;
    }
}

