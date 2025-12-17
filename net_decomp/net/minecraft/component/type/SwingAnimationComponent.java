/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.SwingAnimationType;
import net.minecraft.util.dynamic.Codecs;

public record SwingAnimationComponent(SwingAnimationType type, int duration) {
    public static final SwingAnimationComponent DEFAULT = new SwingAnimationComponent(SwingAnimationType.WHACK, 6);
    public static final Codec<SwingAnimationComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SwingAnimationType.CODEC.optionalFieldOf("type", (Object)SwingAnimationComponent.DEFAULT.type).forGetter(SwingAnimationComponent::type), (App)Codecs.POSITIVE_INT.optionalFieldOf("duration", (Object)SwingAnimationComponent.DEFAULT.duration).forGetter(SwingAnimationComponent::duration)).apply((Applicative)instance, SwingAnimationComponent::new));
    public static final PacketCodec<ByteBuf, SwingAnimationComponent> PACKET_CODEC = PacketCodec.tuple(SwingAnimationType.PACKET_CODEC, SwingAnimationComponent::type, PacketCodecs.VAR_INT, SwingAnimationComponent::duration, SwingAnimationComponent::new);
}

