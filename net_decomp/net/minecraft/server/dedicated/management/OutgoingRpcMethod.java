/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.dedicated.management.RpcMethodInfo;
import net.minecraft.server.dedicated.management.RpcRequestParameter;
import net.minecraft.server.dedicated.management.RpcResponseResult;
import net.minecraft.server.dedicated.management.schema.RpcSchema;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public interface OutgoingRpcMethod<Params, Result> {
    public static final String NOTIFICATION_PREFIX = "notification/";

    public RpcMethodInfo<Params, Result> info();

    public Attributes attributes();

    default public @Nullable JsonElement encodeParams(Params params) {
        return null;
    }

    default public @Nullable Result decodeResult(JsonElement result) {
        return null;
    }

    public static Builder<Void, Void> createSimpleBuilder() {
        return new Builder<Void, Void>(Simple::new);
    }

    public static <Params> Builder<Params, Void> createNotificationBuilder() {
        return new Builder(Notification::new);
    }

    public static <Result> Builder<Void, Result> createParameterlessBuilder() {
        return new Builder(Parameterless::new);
    }

    public static <Params, Result> Builder<Params, Result> createParameterizedBuilder() {
        return new Builder(Parameterized::new);
    }

    public static class Builder<Params, Result> {
        public static final Attributes DEFAULT_ATTRIBUTES = new Attributes(true);
        private final Factory<Params, Result> factory;
        private String description = "";
        private @Nullable RpcRequestParameter<Params> requestParameter;
        private @Nullable RpcResponseResult<Result> responseResult;

        public Builder(Factory<Params, Result> factory) {
            this.factory = factory;
        }

        public Builder<Params, Result> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<Params, Result> responseResult(String name, RpcSchema<Result> schema) {
            this.responseResult = new RpcResponseResult<Result>(name, schema);
            return this;
        }

        public Builder<Params, Result> requestParameter(String name, RpcSchema<Params> schema) {
            this.requestParameter = new RpcRequestParameter<Params>(name, schema);
            return this;
        }

        private OutgoingRpcMethod<Params, Result> build() {
            RpcMethodInfo<Params, Result> rpcMethodInfo = new RpcMethodInfo<Params, Result>(this.description, this.requestParameter, this.responseResult);
            return this.factory.create(rpcMethodInfo, DEFAULT_ATTRIBUTES);
        }

        public RegistryEntry.Reference<OutgoingRpcMethod<Params, Result>> buildAndRegisterVanilla(String path) {
            return this.buildAndRegister(Identifier.ofVanilla(OutgoingRpcMethod.NOTIFICATION_PREFIX + path));
        }

        private RegistryEntry.Reference<OutgoingRpcMethod<Params, Result>> buildAndRegister(Identifier id) {
            return Registry.registerReference(Registries.OUTGOING_RPC_METHOD, id, this.build());
        }
    }

    @FunctionalInterface
    public static interface Factory<Params, Result> {
        public OutgoingRpcMethod<Params, Result> create(RpcMethodInfo<Params, Result> var1, Attributes var2);
    }

    public record Parameterized<Params, Result>(RpcMethodInfo<Params, Result> info, Attributes attributes) implements OutgoingRpcMethod<Params, Result>
    {
        @Override
        public @Nullable JsonElement encodeParams(Params params) {
            if (this.info.params().isEmpty()) {
                throw new IllegalStateException("Method defined as having no parameters");
            }
            return (JsonElement)this.info.params().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, params).getOrThrow();
        }

        @Override
        public Result decodeResult(JsonElement result) {
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("Method defined as having no result");
            }
            return (Result)this.info.result().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)result).getOrThrow();
        }
    }

    public record Parameterless<Result>(RpcMethodInfo<Void, Result> info, Attributes attributes) implements OutgoingRpcMethod<Void, Result>
    {
        @Override
        public Result decodeResult(JsonElement result) {
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("Method defined as having no result");
            }
            return (Result)this.info.result().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)result).getOrThrow();
        }
    }

    public record Notification<Params>(RpcMethodInfo<Params, Void> info, Attributes attributes) implements OutgoingRpcMethod<Params, Void>
    {
        @Override
        public @Nullable JsonElement encodeParams(Params params) {
            if (this.info.params().isEmpty()) {
                throw new IllegalStateException("Method defined as having no parameters");
            }
            return (JsonElement)this.info.params().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, params).getOrThrow();
        }
    }

    public record Simple(RpcMethodInfo<Void, Void> info, Attributes attributes) implements OutgoingRpcMethod<Void, Void>
    {
    }

    public record Attributes(boolean discoverable) {
    }
}

