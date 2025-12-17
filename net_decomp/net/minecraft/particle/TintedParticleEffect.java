/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.particle;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;

public class TintedParticleEffect
implements ParticleEffect {
    private final ParticleType<TintedParticleEffect> type;
    private final int color;

    public static MapCodec<TintedParticleEffect> createCodec(ParticleType<TintedParticleEffect> type) {
        return Codecs.ARGB.xmap(color -> new TintedParticleEffect(type, (int)color), effect -> effect.color).fieldOf("color");
    }

    public static PacketCodec<? super ByteBuf, TintedParticleEffect> createPacketCodec(ParticleType<TintedParticleEffect> type) {
        return PacketCodecs.INTEGER.xmap(color -> new TintedParticleEffect(type, (int)color), particleEffect -> particleEffect.color);
    }

    private TintedParticleEffect(ParticleType<TintedParticleEffect> type, int color) {
        this.type = type;
        this.color = color;
    }

    public ParticleType<TintedParticleEffect> getType() {
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

    public float getAlpha() {
        return (float)ColorHelper.getAlpha(this.color) / 255.0f;
    }

    public static TintedParticleEffect create(ParticleType<TintedParticleEffect> type, int color) {
        return new TintedParticleEffect(type, color);
    }

    public static TintedParticleEffect create(ParticleType<TintedParticleEffect> type, float r, float g, float b) {
        return TintedParticleEffect.create(type, ColorHelper.fromFloats(1.0f, r, g, b));
    }
}

