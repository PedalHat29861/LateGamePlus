/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.particle;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;

public record TrailParticleEffect(Vec3d target, int color, int duration) implements ParticleEffect
{
    public static final MapCodec<TrailParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Vec3d.CODEC.fieldOf("target").forGetter(TrailParticleEffect::target), (App)Codecs.RGB.fieldOf("color").forGetter(TrailParticleEffect::color), (App)Codecs.POSITIVE_INT.fieldOf("duration").forGetter(TrailParticleEffect::duration)).apply((Applicative)instance, TrailParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, TrailParticleEffect> PACKET_CODEC = PacketCodec.tuple(Vec3d.PACKET_CODEC, TrailParticleEffect::target, PacketCodecs.INTEGER, TrailParticleEffect::color, PacketCodecs.VAR_INT, TrailParticleEffect::duration, TrailParticleEffect::new);

    public ParticleType<TrailParticleEffect> getType() {
        return ParticleTypes.TRAIL;
    }
}

