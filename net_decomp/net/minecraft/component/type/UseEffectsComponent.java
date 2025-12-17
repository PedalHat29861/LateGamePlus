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

public record UseEffectsComponent(boolean canSprint, boolean interactVibrations, float speedMultiplier) {
    public static final UseEffectsComponent DEFAULT = new UseEffectsComponent(false, true, 0.2f);
    public static final Codec<UseEffectsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("can_sprint", (Object)UseEffectsComponent.DEFAULT.canSprint).forGetter(UseEffectsComponent::canSprint), (App)Codec.BOOL.optionalFieldOf("interact_vibrations", (Object)UseEffectsComponent.DEFAULT.interactVibrations).forGetter(UseEffectsComponent::interactVibrations), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("speed_multiplier", (Object)Float.valueOf(UseEffectsComponent.DEFAULT.speedMultiplier)).forGetter(UseEffectsComponent::speedMultiplier)).apply((Applicative)instance, UseEffectsComponent::new));
    public static final PacketCodec<ByteBuf, UseEffectsComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, UseEffectsComponent::canSprint, PacketCodecs.BOOLEAN, UseEffectsComponent::interactVibrations, PacketCodecs.FLOAT, UseEffectsComponent::speedMultiplier, UseEffectsComponent::new);
}

