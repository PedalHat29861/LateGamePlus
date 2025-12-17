/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Interpolator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;

public interface ColorModifier<Argument>
extends EnvironmentAttributeModifier<Integer, Argument> {
    public static final ColorModifier<Integer> ALPHA_BLEND = new ColorModifier<Integer>(){

        @Override
        public Integer apply(Integer integer, Integer integer2) {
            return ColorHelper.alphaBlend(integer, integer2);
        }

        @Override
        public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> environmentAttribute) {
            return Codecs.HEX_ARGB;
        }

        @Override
        public Interpolator<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> environmentAttribute) {
            return Interpolator.ofColor();
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((Integer)object, (Integer)object2);
        }
    };
    public static final ColorModifier<Integer> ADD = ColorHelper::add;
    public static final ColorModifier<Integer> SUBTRACT = ColorHelper::subtract;
    public static final ColorModifier<Integer> MULTIPLY_RGB = ColorHelper::mix;
    public static final ColorModifier<Integer> MULTIPLY_ARGB = ColorHelper::mix;
    public static final ColorModifier<BlendToGrayArg> BLEND_TO_GRAY = new ColorModifier<BlendToGrayArg>(){

        @Override
        public Integer apply(Integer integer, BlendToGrayArg blendToGrayArg) {
            int i = ColorHelper.scaleRgb(ColorHelper.grayscale(integer), blendToGrayArg.brightness);
            return ColorHelper.lerp(blendToGrayArg.factor, integer, i);
        }

        @Override
        public Codec<BlendToGrayArg> argumentCodec(EnvironmentAttribute<Integer> environmentAttribute) {
            return BlendToGrayArg.CODEC;
        }

        @Override
        public Interpolator<BlendToGrayArg> argumentKeyframeLerp(EnvironmentAttribute<Integer> environmentAttribute) {
            return (t, a, b) -> new BlendToGrayArg(MathHelper.lerp(t, a.brightness, b.brightness), MathHelper.lerp(t, a.factor, b.factor));
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((Integer)object, (BlendToGrayArg)object2);
        }
    };

    @FunctionalInterface
    public static interface Rgb
    extends ColorModifier<Integer> {
        @Override
        default public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> environmentAttribute) {
            return Codecs.HEX_RGB;
        }

        @Override
        default public Interpolator<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> environmentAttribute) {
            return Interpolator.ofColor();
        }
    }

    @FunctionalInterface
    public static interface Argb
    extends ColorModifier<Integer> {
        @Override
        default public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> environmentAttribute) {
            return Codec.either(Codecs.HEX_ARGB, Codecs.RGB).xmap(Either::unwrap, argb -> ColorHelper.getAlpha(argb) == 255 ? Either.right((Object)argb) : Either.left((Object)argb));
        }

        @Override
        default public Interpolator<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> environmentAttribute) {
            return Interpolator.ofColor();
        }
    }

    public static final class BlendToGrayArg
    extends Record {
        final float brightness;
        final float factor;
        public static final Codec<BlendToGrayArg> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("brightness").forGetter(BlendToGrayArg::brightness), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("factor").forGetter(BlendToGrayArg::factor)).apply((Applicative)instance, BlendToGrayArg::new));

        public BlendToGrayArg(float brightness, float factor) {
            this.brightness = brightness;
            this.factor = factor;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BlendToGrayArg.class, "brightness;factor", "brightness", "factor"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BlendToGrayArg.class, "brightness;factor", "brightness", "factor"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BlendToGrayArg.class, "brightness;factor", "brightness", "factor"}, this, object);
        }

        public float brightness() {
            return this.brightness;
        }

        public float factor() {
            return this.factor;
        }
    }
}

