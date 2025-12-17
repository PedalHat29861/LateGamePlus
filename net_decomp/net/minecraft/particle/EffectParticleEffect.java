/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.particle;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;

public class EffectParticleEffect
implements ParticleEffect {
    private final ParticleType<EffectParticleEffect> type;
    private final int color;
    private final float power;

    public static MapCodec<EffectParticleEffect> createCodec(ParticleType<EffectParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.RGB.optionalFieldOf("color", (Object)-1).forGetter(effect -> effect.color), (App)Codec.FLOAT.optionalFieldOf("power", (Object)Float.valueOf(1.0f)).forGetter(effect -> Float.valueOf(effect.power))).apply((Applicative)instance, (color, power) -> new EffectParticleEffect(type, (int)color, power.floatValue())));
    }

    public static PacketCodec<? super ByteBuf, EffectParticleEffect> createPacketCodec(ParticleType<EffectParticleEffect> type) {
        return PacketCodec.tuple(PacketCodecs.INTEGER, effect -> effect.color, PacketCodecs.FLOAT, effect -> Float.valueOf(effect.power), (color, power) -> new EffectParticleEffect(type, (int)color, power.floatValue()));
    }

    private EffectParticleEffect(ParticleType<EffectParticleEffect> type, int color, float power) {
        this.type = type;
        this.color = color;
        this.power = power;
    }

    public ParticleType<EffectParticleEffect> getType() {
        return this.type;
    }

    public float getRed() {
        return (float)ColorHelper.getRed(this.color) / 255.0f;
    }

    public float getGreen() {
        return (float)ColorHelper.getGreen(this.color) / 255.0f;
    }

    public float getBlue() {
        return (float)ColorHelper.getBlue(this.color) / 255.0f;
    }

    public float getPower() {
        return this.power;
    }

    public static EffectParticleEffect of(ParticleType<EffectParticleEffect> type, int color, float power) {
        return new EffectParticleEffect(type, color, power);
    }

    public static EffectParticleEffect of(ParticleType<EffectParticleEffect> type, float r, float g, float b, float power) {
        return EffectParticleEffect.of(type, ColorHelper.fromFloats(1.0f, r, g, b), power);
    }
}

