/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.registry.Registry;
import net.minecraft.server.dedicated.management.RpcEncodingException;
import net.minecraft.server.dedicated.management.RpcException;
import net.minecraft.server.dedicated.management.RpcMethodInfo;
import net.minecraft.server.dedicated.management.RpcRequestParameter;
import net.minecraft.server.dedicated.management.RpcResponseResult;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.dedicated.management.schema.RpcSchema;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public interface IncomingRpcMethod<Params, Result> {
    public RpcMethodInfo<Params, Result> info();

    public Attributes attributes();

    public JsonElement handle(ManagementHandlerDispatcher var1, @Nullable JsonElement var2, ManagementConnectionId var3);

    public static <Result> Builder<Void, Result> createParameterlessBuilder(ParameterlessHandler<Result> handler) {
        return new Builder(handler);
    }

    public static <Params, Result> Builder<Params, Result> createParameterizedBuilder(ParameterizedHandler<Params, Result> handler) {
        return new Builder<Params, Result>(handler);
    }

    public static <Result> Builder<Void, Result> createParameterlessBuilder(Function<ManagementHandlerDispatcher, Result> handler) {
        return new Builder(handler);
    }

    public static class Builder<Params, Result> {
        private String description = "";
        private @Nullable RpcRequestParameter<Params> params;
        private @Nullable RpcResponseResult<Result> result;
        private boolean runOnMainThread = true;
        private boolean discoverable = true;
        private @Nullable ParameterlessHandler<Result> parameterlessHandler;
        private @Nullable ParameterizedHandler<Params, Result> parameterizedHandler;

        public Builder(ParameterlessHandler<Result> parameterlessHandler) {
            this.parameterlessHandler = parameterlessHandler;
        }

        public Builder(ParameterizedHandler<Params, Result> parameterizedHandler) {
            this.parameterizedHandler = parameterizedHandler;
        }

        public Builder(Function<ManagementHandlerDispatcher, Result> parameterlessHandler) {
            this.parameterlessHandler = (dispatcher, remote) -> parameterlessHandler.apply(dispatcher);
        }

        public Builder<Params, Result> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<Params, Result> result(String name, RpcSchema<Result> schema) {
            this.result = new RpcResponseResult<Result>(name, schema.copy());
            return this;
        }

        public Builder<Params, Result> parameter(String name, RpcSchema<Params> schema) {
            this.params = new RpcRequestParameter<Params>(name, schema.copy());
            return this;
        }

        public Builder<Params, Result> noRequireMainThread() {
            this.runOnMainThread = false;
            return this;
        }

        public Builder<Params, Result> notDiscoverable() {
            this.discoverable = false;
            return this;
        }

        public IncomingRpcMethod<Params, Result> build() {
            if (this.result == null) {
                throw new IllegalStateException("No response defined");
            }
            Attributes attributes = new Attributes(this.discoverable, this.runOnMainThread);
            RpcMethodInfo<Params, Result> rpcMethodInfo = new RpcMethodInfo<Params, Result>(this.description, this.params, this.result);
            if (this.parameterlessHandler != null) {
                return new Parameterless<Params, Result>(rpcMethodInfo, attributes, this.parameterlessHandler);
            }
            if (this.parameterizedHandler != null) {
                if (this.params == null) {
                    throw new IllegalStateException("No param schema defined");
                }
                return new Parameterized<Params, Result>(rpcMethodInfo, attributes, this.parameterizedHandler);
            }
            throw new IllegalStateException("No method defined");
        }

        public IncomingRpcMethod<?, ?> buildAndRegisterVanilla(Registry<IncomingRpcMethod<?, ?>> registry, String path) {
            return this.buildAndRegister(registry, Identifier.ofVanilla(path));
        }

        private IncomingRpcMethod<?, ?> buildAndRegister(Registry<IncomingRpcMethod<?, ?>> registry, Identifier id) {
            return Registry.register(registry, id, this.build());
        }
    }

    @FunctionalInterface
    public static interface ParameterlessHandler<Result> {
        public Result apply(ManagementHandlerDispatcher var1, ManagementConnectionId var2);
    }

    @FunctionalInterface
    public static interface ParameterizedHandler<Params, Result> {
        public Result apply(ManagementHandlerDispatcher var1, Params var2, ManagementConnectionId var3);
    }

    public record Parameterized<Params, Result>(RpcMethodInfo<Params, Result> info, Attributes attributes, ParameterizedHandler<Params, Result> handler) implements IncomingRpcMethod<Params, Result>
    {
        @Override
        public JsonElement handle(ManagementHandlerDispatcher dispatcher, @Nullable JsonElement parameters, ManagementConnectionId remote) {
            JsonElement jsonElement2;
            if (parameters == null || !parameters.isJsonArray() && !parameters.isJsonObject()) {
                throw new RpcException("Expected params as array or named");
            }
            if (this.info.params().isEmpty()) {
                throw new IllegalArgumentException("Method defined as having parameters without describing them");
            }
            if (parameters.isJsonObject()) {
                String string = this.info.params().get().name();
                JsonElement jsonElement = parameters.getAsJsonObject().get(string);
                if (jsonElement == null) {
                    throw new RpcException(String.format(Locale.ROOT, "Params passed by-name, but expected param [%s] does not exist", string));
                }
                jsonElement2 = jsonElement;
            } else {
                JsonArray jsonArray = parameters.getAsJsonArray();
                if (jsonArray.isEmpty() || jsonArray.size() > 1) {
                    throw new RpcException("Expected exactly one element in the params array");
                }
                jsonElement2 = jsonArray.get(0);
            }
            Object object = this.info.params().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement2).getOrThrow(RpcException::new);
            Result object2 = this.handler.apply(dispatcher, object, remote);
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("No result codec defined");
            }
            return (JsonElement)this.info.result().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, object2).getOrThrow(RpcEncodingException::new);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Parameterized.class, "info;attributes;function", "info", "attributes", "handler"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Parameterized.class, "info;attributes;function", "info", "attributes", "handler"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Parameterized.class, "info;attributes;function", "info", "attributes", "handler"}, this, object);
        }
    }

    public record Parameterless<Params, Result>(RpcMethodInfo<Params, Result> info, Attributes attributes, ParameterlessHandler<Result> handler) implements IncomingRpcMethod<Params, Result>
    {
        @Override
        public JsonElement handle(ManagementHandlerDispatcher dispatcher, @Nullable JsonElement parameters, ManagementConnectionId remote) {
            if (!(parameters == null || parameters.isJsonArray() && parameters.getAsJsonArray().isEmpty())) {
                throw new RpcException("Expected no params, or an empty array");
            }
            if (this.info.params().isPresent()) {
                throw new IllegalArgumentException("Parameterless method unexpectedly has parameter description");
            }
            Result object = this.handler.apply(dispatcher, remote);
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("No result codec defined");
            }
            return (JsonElement)this.info.result().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, object).getOrThrow(RpcException::new);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Parameterless.class, "info;attributes;supplier", "info", "attributes", "handler"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Parameterless.class, "info;attributes;supplier", "info", "attributes", "handler"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Parameterless.class, "info;attributes;supplier", "info", "attributes", "handler"}, this, object);
        }
    }

    public record Attributes(boolean runOnMainThread, boolean discoverable) {
    }
}

