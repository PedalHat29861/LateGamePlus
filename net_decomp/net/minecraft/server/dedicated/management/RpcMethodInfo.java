/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.dedicated.management.RpcRequestParameter;
import net.minecraft.server.dedicated.management.RpcResponseResult;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public record RpcMethodInfo<Params, Result>(String description, Optional<RpcRequestParameter<Params>> params, Optional<RpcResponseResult<Result>> result) {
    public RpcMethodInfo(String description, @Nullable RpcRequestParameter<Params> param, @Nullable RpcResponseResult<Result> result) {
        this(description, Optional.ofNullable(param), Optional.ofNullable(result));
    }

    private static <Params> Optional<RpcRequestParameter<Params>> getParameter(List<RpcRequestParameter<Params>> params) {
        return params.isEmpty() ? Optional.empty() : Optional.of(params.getFirst());
    }

    private static <Params> List<RpcRequestParameter<Params>> toParameterList(Optional<RpcRequestParameter<Params>> param) {
        if (param.isPresent()) {
            return List.of(param.get());
        }
        return List.of();
    }

    private static <Params> Codec<Optional<RpcRequestParameter<Params>>> createParamsCodec() {
        return RpcRequestParameter.createCodec().codec().listOf().xmap(RpcMethodInfo::getParameter, RpcMethodInfo::toParameterList);
    }

    static <Params, Result> MapCodec<RpcMethodInfo<Params, Result>> createCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("description").forGetter(RpcMethodInfo::description), (App)RpcMethodInfo.createParamsCodec().fieldOf("params").forGetter(RpcMethodInfo::params), (App)RpcResponseResult.getCodec().optionalFieldOf("result").forGetter(RpcMethodInfo::result)).apply((Applicative)instance, RpcMethodInfo::new));
    }

    public Entry<Params, Result> toEntry(Identifier name) {
        return new Entry(name, this);
    }

    public record Entry<Params, Result>(Identifier name, RpcMethodInfo<Params, Result> contents) {
        public static final Codec<Entry<?, ?>> CODEC = Entry.createCodec();

        public static <Params, Result> Codec<Entry<Params, Result>> createCodec() {
            return RecordCodecBuilder.create(instance -> instance.group((App)Identifier.CODEC.fieldOf("name").forGetter(Entry::name), (App)RpcMethodInfo.createCodec().forGetter(Entry::contents)).apply((Applicative)instance, Entry::new));
        }
    }
}

