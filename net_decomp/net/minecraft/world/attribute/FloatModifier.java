/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.Interpolator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.attribute.BlendArgument;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;

public interface FloatModifier<Argument>
extends EnvironmentAttributeModifier<Float, Argument> {
    public static final FloatModifier<BlendArgument> ALPHA_BLEND = new FloatModifier<BlendArgument>(){

        @Override
        public Float apply(Float float_, BlendArgument blendArgument) {
            return Float.valueOf(MathHelper.lerp(blendArgument.alpha(), float_.floatValue(), blendArgument.value()));
        }

        @Override
        public Codec<BlendArgument> argumentCodec(EnvironmentAttribute<Float> environmentAttribute) {
            return BlendArgument.CODEC;
        }

        @Override
        public Interpolator<BlendArgument> argumentKeyframeLerp(EnvironmentAttribute<Float> environmentAttribute) {
            return (t, a, b) -> new BlendArgument(MathHelper.lerp(t, a.value(), b.value()), MathHelper.lerp(t, a.alpha(), b.alpha()));
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((Float)object, (BlendArgument)object2);
        }
    };
    public static final FloatModifier<Float> ADD = Float::sum;
    public static final FloatModifier<Float> SUBTRACT = (a, b) -> Float.valueOf(a.floatValue() - b.floatValue());
    public static final FloatModifier<Float> MULTIPLY = (a, b) -> Float.valueOf(a.floatValue() * b.floatValue());
    public static final FloatModifier<Float> MINIMUM = Math::min;
    public static final FloatModifier<Float> MAXIMUM = Math::max;

    @FunctionalInterface
    public static interface Binary
    extends FloatModifier<Float> {
        @Override
        default public Codec<Float> argumentCodec(EnvironmentAttribute<Float> environmentAttribute) {
            return Codec.FLOAT;
        }

        @Override
        default public Interpolator<Float> argumentKeyframeLerp(EnvironmentAttribute<Float> environmentAttribute) {
            return Interpolator.ofFloat();
        }
    }
}

