/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dedicated.management;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public record RpcRequestParameter<Param>(String name, RpcSchema<Param> schema, boolean required) {
    public RpcRequestParameter(String name, RpcSchema<Param> schema) {
        this(name, schema, true);
    }

    public static <Param> MapCodec<RpcRequestParameter<Param>> createCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(RpcRequestParameter::name), (App)RpcSchema.getCodec().fieldOf("schema").forGetter(RpcRequestParameter::schema), (App)Codec.BOOL.fieldOf("required").forGetter(RpcRequestParameter::required)).apply((Applicative)instance, RpcRequestParameter::new));
    }
}

