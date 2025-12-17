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

public class ShriekParticleEffect
implements ParticleEffect {
    public static final MapCodec<ShriekParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("delay").forGetter(particleEffect -> particleEffect.delay)).apply((Applicative)instance, ShriekParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, ShriekParticleEffect> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, effect -> effect.delay, ShriekParticleEffect::new);
    private final int delay;

    public ShriekParticleEffect(int delay) {
        this.delay = delay;
    }

    public ParticleType<ShriekParticleEffect> getType() {
        return ParticleTypes.SHRIEK;
    }

    public int getDelay() {
        return this.delay;
    }
}

