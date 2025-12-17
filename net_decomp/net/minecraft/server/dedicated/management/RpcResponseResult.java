/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dedicated.management;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public record RpcResponseResult<Result>(String name, RpcSchema<Result> schema) {
    public static <Result> Codec<RpcResponseResult<Result>> getCodec() {
        return RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(RpcResponseResult::name), (App)RpcSchema.getCodec().fieldOf("schema").forGetter(RpcResponseResult::schema)).apply((Applicative)instance, RpcResponseResult::new));
    }
}

