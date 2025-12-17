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
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;

public record RpcKickReason(Optional<String> literal, Optional<String> translatable, Optional<List<String>> translatableParams) {
    public static final Codec<RpcKickReason> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.optionalFieldOf("literal").forGetter(RpcKickReason::literal), (App)Codec.STRING.optionalFieldOf("translatable").forGetter(RpcKickReason::translatable), (App)Codec.STRING.listOf().lenientOptionalFieldOf("translatableParams").forGetter(RpcKickReason::translatableParams)).apply((Applicative)instance, RpcKickReason::new));

    public Optional<Text> toText() {
        if (this.translatable.isPresent()) {
            String string = this.translatable.get();
            if (this.translatableParams.isPresent()) {
                List<String> list = this.translatableParams.get();
                return Optional.of(Text.translatable(string, list.toArray()));
            }
            return Optional.of(Text.translatable(string));
        }
        return this.literal.map(Text::literal);
    }
}

