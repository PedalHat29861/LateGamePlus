/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class DragonBreathParticleEffect
implements ParticleEffect {
    private final ParticleType<DragonBreathParticleEffect> type;
    private final float power;

    public static MapCodec<DragonBreathParticleEffect> createCodec(ParticleType<DragonBreathParticleEffect> type) {
        return Codec.FLOAT.xmap(power -> new DragonBreathParticleEffect(type, power.floatValue()), effect -> Float.valueOf(effect.power)).optionalFieldOf("power", (Object)DragonBreathParticleEffect.of(type, 1.0f));
    }

    public static PacketCodec<? super ByteBuf, DragonBreathParticleEffect> createPacketCodec(ParticleType<DragonBreathParticleEffect> type) {
        return PacketCodecs.FLOAT.xmap(power -> new DragonBreathParticleEffect(type, power.floatValue()), effect -> Float.valueOf(effect.power));
    }

    private DragonBreathParticleEffect(ParticleType<DragonBreathParticleEffect> type, float power) {
        this.type = type;
        this.power = power;
    }

    public ParticleType<DragonBreathParticleEffect> getType() {
        return this.type;
    }

    public float getPower() {
        return this.power;
    }

    public static DragonBreathParticleEffect of(ParticleType<DragonBreathParticleEffect> type, float power) {
        return new DragonBreathParticleEffect(type, power);
    }
}

