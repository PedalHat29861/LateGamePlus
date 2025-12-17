/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;

public record AmbientParticle(ParticleEffect particle, float probability) {
    public static final Codec<AmbientParticle> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ParticleTypes.TYPE_CODEC.fieldOf("particle").forGetter(config -> config.particle), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").forGetter(config -> Float.valueOf(config.probability))).apply((Applicative)instance, AmbientParticle::new));

    public boolean shouldAddParticle(Random random) {
        return random.nextFloat() <= this.probability;
    }

    public static List<AmbientParticle> of(ParticleEffect particle, float probability) {
        return List.of(new AmbientParticle(particle, probability));
    }
}

