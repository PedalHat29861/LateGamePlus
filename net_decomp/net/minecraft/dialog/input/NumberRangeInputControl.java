/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.dialog.input;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;

public record NumberRangeInputControl(int width, Text label, String labelFormat, RangeInfo rangeInfo) implements InputControl
{
    public static final MapCodec<NumberRangeInputControl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Dialog.WIDTH_CODEC.optionalFieldOf("width", (Object)200).forGetter(NumberRangeInputControl::width), (App)TextCodecs.CODEC.fieldOf("label").forGetter(NumberRangeInputControl::label), (App)Codec.STRING.optionalFieldOf("label_format", (Object)"options.generic_value").forGetter(NumberRangeInputControl::labelFormat), (App)RangeInfo.CODEC.forGetter(NumberRangeInputControl::rangeInfo)).apply((Applicative)instance, NumberRangeInputControl::new));

    public MapCodec<NumberRangeInputControl> getCodec() {
        return CODEC;
    }

    public Text getFormattedLabel(String value) {
        return Text.translatable(this.labelFormat, this.label, value);
    }

    public record RangeInfo(float start, float end, Optional<Float> initial, Optional<Float> step) {
        public static final MapCodec<RangeInfo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("start").forGetter(RangeInfo::start), (App)Codec.FLOAT.fieldOf("end").forGetter(RangeInfo::end), (App)Codec.FLOAT.optionalFieldOf("initial").forGetter(RangeInfo::initial), (App)Codecs.POSITIVE_FLOAT.optionalFieldOf("step").forGetter(RangeInfo::step)).apply((Applicative)instance, RangeInfo::new)).validate(rangeInfo -> {
            if (rangeInfo.initial.isPresent()) {
                double d = rangeInfo.initial.get().floatValue();
                double e = Math.min(rangeInfo.start, rangeInfo.end);
                double f = Math.max(rangeInfo.start, rangeInfo.end);
                if (d < e || d > f) {
                    return DataResult.error(() -> "Initial value " + d + " is outside of range [" + e + ", " + f + "]");
                }
            }
            return DataResult.success((Object)rangeInfo);
        });

        public float sliderProgressToValue(float sliderProgress) {
            float i;
            int j;
            float f = MathHelper.lerp(sliderProgress, this.start, this.end);
            if (this.step.isEmpty()) {
                return f;
            }
            float g = this.step.get().floatValue();
            float h = this.getInitialValue();
            float k = h + (float)(j = Math.round((i = f - h) / g)) * g;
            if (!this.isValueOutOfRange(k)) {
                return k;
            }
            int l = j - MathHelper.sign(j);
            return h + (float)l * g;
        }

        private boolean isValueOutOfRange(float value) {
            float f = this.valueToSliderProgress(value);
            return (double)f < 0.0 || (double)f > 1.0;
        }

        private float getInitialValue() {
            if (this.initial.isPresent()) {
                return this.initial.get().floatValue();
            }
            return (this.start + this.end) / 2.0f;
        }

        public float getInitialSliderProgress() {
            float f = this.getInitialValue();
            return this.valueToSliderProgress(f);
        }

        private float valueToSliderProgress(float value) {
            if (this.start == this.end) {
                return 0.5f;
            }
            return MathHelper.getLerpProgress(value, this.start, this.end);
        }
    }
}

