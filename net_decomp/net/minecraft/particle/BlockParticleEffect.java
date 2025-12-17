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
package net.minecraft.particle;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public record BlockParticleEffect(ParticleEffect particle, float scaling, float speed) {
    public static final MapCodec<BlockParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ParticleTypes.TYPE_CODEC.fieldOf("particle").forGetter(BlockParticleEffect::particle), (App)Codec.FLOAT.optionalFieldOf("scaling", (Object)Float.valueOf(1.0f)).forGetter(BlockParticleEffect::scaling), (App)Codec.FLOAT.optionalFieldOf("speed", (Object)Float.valueOf(1.0f)).forGetter(BlockParticleEffect::speed)).apply((Applicative)instance, BlockParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, BlockParticleEffect> PACKET_CODEC = PacketCodec.tuple(ParticleTypes.PACKET_CODEC, BlockParticleEffect::particle, PacketCodecs.FLOAT, BlockParticleEffect::scaling, PacketCodecs.FLOAT, BlockParticleEffect::speed, BlockParticleEffect::new);
}

