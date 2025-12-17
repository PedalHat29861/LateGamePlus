/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Vector3f
 */
package net.minecraft.particle;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector3f;

public class DustParticleEffect
extends AbstractDustParticleEffect {
    public static final int RED = 0xFF0000;
    public static final DustParticleEffect DEFAULT = new DustParticleEffect(0xFF0000, 1.0f);
    public static final MapCodec<DustParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.RGB.fieldOf("color").forGetter(particle -> particle.color), (App)SCALE_CODEC.fieldOf("scale").forGetter(AbstractDustParticleEffect::getScale)).apply((Applicative)instance, DustParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, DustParticleEffect> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, particle -> particle.color, PacketCodecs.FLOAT, AbstractDustParticleEffect::getScale, DustParticleEffect::new);
    private final int color;

    public DustParticleEffect(int color, float scale) {
        super(scale);
        this.color = color;
    }

    public ParticleType<DustParticleEffect> getType() {
        return ParticleTypes.DUST;
    }

    public Vector3f getColor() {
        return ColorHelper.toRgbVector(this.color);
    }
}

