/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.world.attribute;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;
import net.minecraft.world.attribute.BlendArgument;
import net.minecraft.world.attribute.ColorModifier;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.FloatModifier;
import net.minecraft.world.attribute.WorldEnvironmentAttributeAccess;
import net.minecraft.world.attribute.timeline.Timelines;

public class WeatherAttributes {
    public static final EnvironmentAttributeMap RAIN_EFFECTS = EnvironmentAttributeMap.builder().with(EnvironmentAttributes.SKY_COLOR_VISUAL, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGrayArg(0.6f, 0.75f)).with(EnvironmentAttributes.FOG_COLOR_VISUAL, ColorModifier.MULTIPLY_RGB, ColorHelper.fromFloats(1.0f, 0.5f, 0.5f, 0.6f)).with(EnvironmentAttributes.CLOUD_COLOR_VISUAL, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGrayArg(0.24f, 0.5f)).with(EnvironmentAttributes.SKY_LIGHT_LEVEL_GAMEPLAY, FloatModifier.ALPHA_BLEND, new BlendArgument(4.0f, 0.3125f)).with(EnvironmentAttributes.SKY_LIGHT_COLOR_VISUAL, ColorModifier.ALPHA_BLEND, ColorHelper.withAlpha(0.3125f, Timelines.NIGHT_SKY_LIGHT_COLOR)).with(EnvironmentAttributes.SKY_LIGHT_FACTOR_VISUAL, FloatModifier.ALPHA_BLEND, new BlendArgument(0.24f, 0.3125f)).with(EnvironmentAttributes.STAR_BRIGHTNESS_VISUAL, Float.valueOf(0.0f)).with(EnvironmentAttributes.SUNRISE_SUNSET_COLOR_VISUAL, ColorModifier.MULTIPLY_ARGB, ColorHelper.fromFloats(1.0f, 0.5f, 0.5f, 0.6f)).with(EnvironmentAttributes.BEES_STAY_IN_HIVE_GAMEPLAY, true).build();
    public static final EnvironmentAttributeMap THUNDER_EFFECTS = EnvironmentAttributeMap.builder().with(EnvironmentAttributes.SKY_COLOR_VISUAL, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGrayArg(0.24f, 0.94f)).with(EnvironmentAttributes.FOG_COLOR_VISUAL, ColorModifier.MULTIPLY_RGB, ColorHelper.fromFloats(1.0f, 0.25f, 0.25f, 0.3f)).with(EnvironmentAttributes.CLOUD_COLOR_VISUAL, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGrayArg(0.095f, 0.94f)).with(EnvironmentAttributes.SKY_LIGHT_LEVEL_GAMEPLAY, FloatModifier.ALPHA_BLEND, new BlendArgument(4.0f, 0.52734375f)).with(EnvironmentAttributes.SKY_LIGHT_COLOR_VISUAL, ColorModifier.ALPHA_BLEND, ColorHelper.withAlpha(0.52734375f, Timelines.NIGHT_SKY_LIGHT_COLOR)).with(EnvironmentAttributes.SKY_LIGHT_FACTOR_VISUAL, FloatModifier.ALPHA_BLEND, new BlendArgument(0.24f, 0.52734375f)).with(EnvironmentAttributes.STAR_BRIGHTNESS_VISUAL, Float.valueOf(0.0f)).with(EnvironmentAttributes.SUNRISE_SUNSET_COLOR_VISUAL, ColorModifier.MULTIPLY_ARGB, ColorHelper.fromFloats(1.0f, 0.25f, 0.25f, 0.3f)).with(EnvironmentAttributes.BEES_STAY_IN_HIVE_GAMEPLAY, true).build();
    private static final Set<EnvironmentAttribute<?>> ATTRIBUTES = Sets.union(RAIN_EFFECTS.keySet(), THUNDER_EFFECTS.keySet());

    public static void addWeatherAttributes(WorldEnvironmentAttributeAccess.Builder builder, WeatherAccess weather) {
        for (EnvironmentAttribute<?> environmentAttribute : ATTRIBUTES) {
            WeatherAttributes.addWeatherAttribute(builder, weather, environmentAttribute);
        }
    }

    private static <Value> void addWeatherAttribute(WorldEnvironmentAttributeAccess.Builder builder, WeatherAccess weather, EnvironmentAttribute<Value> attribute) {
        EnvironmentAttributeMap.Entry entry = RAIN_EFFECTS.getEntry(attribute);
        EnvironmentAttributeMap.Entry entry2 = THUNDER_EFFECTS.getEntry(attribute);
        builder.timeBased(attribute, (value, time) -> {
            Object object;
            float f = weather.getThunderGradient();
            float g = weather.getRainGradient() - f;
            if (entry != null && g > 0.0f) {
                object = entry.apply(value);
                value = attribute.getType().stateChangeLerp().apply(g, value, object);
            }
            if (entry2 != null && f > 0.0f) {
                object = entry2.apply(value);
                value = attribute.getType().stateChangeLerp().apply(f, value, object);
            }
            return value;
        });
    }

    public static interface WeatherAccess {
        public static WeatherAccess ofWorld(final World world) {
            return new WeatherAccess(){

                @Override
                public float getRainGradient() {
                    return world.getRainGradient(1.0f);
                }

                @Override
                public float getThunderGradient() {
                    return world.getThunderGradient(1.0f);
                }
            };
        }

        public float getRainGradient();

        public float getThunderGradient();
    }
}

