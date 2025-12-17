/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Interpolator;
import net.minecraft.world.MoonPhase;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;
import net.minecraft.world.attribute.EnvironmentAttributeType;

public interface EnvironmentAttributeTypes {
    public static final EnvironmentAttributeType<Boolean> BOOLEAN = EnvironmentAttributeTypes.register("boolean", EnvironmentAttributeType.discrete(Codec.BOOL, EnvironmentAttributeModifier.BOOLEAN_MODIFIERS));
    public static final EnvironmentAttributeType<TriState> TRI_STATE = EnvironmentAttributeTypes.register("tri_state", EnvironmentAttributeType.discrete(TriState.CODEC));
    public static final EnvironmentAttributeType<Float> FLOAT = EnvironmentAttributeTypes.register("float", EnvironmentAttributeType.interpolated(Codec.FLOAT, EnvironmentAttributeModifier.FLOAT_MODIFIERS, Interpolator.ofFloat()));
    public static final EnvironmentAttributeType<Float> ANGLE_DEGREES = EnvironmentAttributeTypes.register("angle_degrees", EnvironmentAttributeType.interpolated(Codec.FLOAT, EnvironmentAttributeModifier.FLOAT_MODIFIERS, Interpolator.ofFloat(), Interpolator.angle(90.0f)));
    public static final EnvironmentAttributeType<Integer> RGB_COLOR = EnvironmentAttributeTypes.register("rgb_color", EnvironmentAttributeType.interpolated(Codecs.HEX_RGB, EnvironmentAttributeModifier.RGB, Interpolator.ofColor()));
    public static final EnvironmentAttributeType<Integer> ARGB_COLOR = EnvironmentAttributeTypes.register("argb_color", EnvironmentAttributeType.interpolated(Codecs.HEX_ARGB, EnvironmentAttributeModifier.ARGB, Interpolator.ofColor()));
    public static final EnvironmentAttributeType<MoonPhase> MOON_PHASE = EnvironmentAttributeTypes.register("moon_phase", EnvironmentAttributeType.discrete(MoonPhase.CODEC));
    public static final EnvironmentAttributeType<Activity> ACTIVITY = EnvironmentAttributeTypes.register("activity", EnvironmentAttributeType.discrete(Registries.ACTIVITY.getCodec()));
    public static final EnvironmentAttributeType<BedRule> BED_RULE = EnvironmentAttributeTypes.register("bed_rule", EnvironmentAttributeType.discrete(BedRule.CODEC));
    public static final EnvironmentAttributeType<ParticleEffect> PARTICLE = EnvironmentAttributeTypes.register("particle", EnvironmentAttributeType.discrete(ParticleTypes.TYPE_CODEC));
    public static final EnvironmentAttributeType<List<AmbientParticle>> AMBIENT_PARTICLES = EnvironmentAttributeTypes.register("ambient_particles", EnvironmentAttributeType.discrete(AmbientParticle.CODEC.listOf()));
    public static final EnvironmentAttributeType<BackgroundMusic> BACKGROUND_MUSIC = EnvironmentAttributeTypes.register("background_music", EnvironmentAttributeType.discrete(BackgroundMusic.CODEC));
    public static final EnvironmentAttributeType<AmbientSounds> AMBIENT_SOUNDS = EnvironmentAttributeTypes.register("ambient_sounds", EnvironmentAttributeType.discrete(AmbientSounds.CODEC));
    public static final Codec<EnvironmentAttributeType<?>> CODEC = Registries.ATTRIBUTE_TYPE.getCodec();

    public static EnvironmentAttributeType<?> registerAndGetDefault(Registry<EnvironmentAttributeType<?>> registry) {
        return BOOLEAN;
    }

    public static <Value> EnvironmentAttributeType<Value> register(String path, EnvironmentAttributeType<Value> type) {
        Registry.register(Registries.ATTRIBUTE_TYPE, Identifier.ofVanilla(path), type);
        return type;
    }
}

