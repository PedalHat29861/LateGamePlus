/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonElement;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.dedicated.management.OutgoingRpcMethod;

public record PendingResponse<Result>(RegistryEntry.Reference<? extends OutgoingRpcMethod<?, ? extends Result>> method, CompletableFuture<Result> resultFuture, long timeoutTime) {
    public void handleResponse(JsonElement result) {
        try {
            Result object = this.method.value().decodeResult(result);
            this.resultFuture.complete(Objects.requireNonNull(object));
        }
        catch (Exception exception) {
            this.resultFuture.completeExceptionally(exception);
        }
    }

    public boolean shouldTimeout(long time) {
        return time > this.timeoutTime;
    }
}

