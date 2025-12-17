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
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

public record SculkChargeParticleEffect(float roll) implements ParticleEffect
{
    public static final MapCodec<SculkChargeParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("roll").forGetter(particleEffect -> Float.valueOf(particleEffect.roll))).apply((Applicative)instance, SculkChargeParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, SculkChargeParticleEffect> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, effect -> Float.valueOf(effect.roll), SculkChargeParticleEffect::new);

    public ParticleType<SculkChargeParticleEffect> getType() {
        return ParticleTypes.SCULK_CHARGE;
    }
}

