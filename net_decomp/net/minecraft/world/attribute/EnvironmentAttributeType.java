/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableBiMap
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Interpolator;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;

public record EnvironmentAttributeType<Value>(Codec<Value> valueCodec, Map<EnvironmentAttributeModifier.Type, EnvironmentAttributeModifier<Value, ?>> modifierLibrary, Codec<EnvironmentAttributeModifier<Value, ?>> modifierCodec, Interpolator<Value> keyframeLerp, Interpolator<Value> stateChangeLerp, Interpolator<Value> spatialLerp, Interpolator<Value> partialTickLerp) {
    public static <Value> EnvironmentAttributeType<Value> interpolated(Codec<Value> valueCodec, Map<EnvironmentAttributeModifier.Type, EnvironmentAttributeModifier<Value, ?>> modifierLibrary, Interpolator<Value> lerp) {
        return EnvironmentAttributeType.interpolated(valueCodec, modifierLibrary, lerp, lerp);
    }

    public static <Value> EnvironmentAttributeType<Value> interpolated(Codec<Value> valueCodec, Map<EnvironmentAttributeModifier.Type, EnvironmentAttributeModifier<Value, ?>> modifierLibrary, Interpolator<Value> spatialLerp, Interpolator<Value> partialTickLerp) {
        return new EnvironmentAttributeType<Value>(valueCodec, modifierLibrary, EnvironmentAttributeType.createModifierCodec(modifierLibrary), spatialLerp, spatialLerp, spatialLerp, partialTickLerp);
    }

    public static <Value> EnvironmentAttributeType<Value> discrete(Codec<Value> valueCodec, Map<EnvironmentAttributeModifier.Type, EnvironmentAttributeModifier<Value, ?>> modifierLibrary) {
        return new EnvironmentAttributeType<Value>(valueCodec, modifierLibrary, EnvironmentAttributeType.createModifierCodec(modifierLibrary), Interpolator.threshold(1.0f), Interpolator.threshold(0.0f), Interpolator.threshold(0.5f), Interpolator.threshold(0.0f));
    }

    public static <Value> EnvironmentAttributeType<Value> discrete(Codec<Value> valueCodec) {
        return EnvironmentAttributeType.discrete(valueCodec, Map.of());
    }

    private static <Value> Codec<EnvironmentAttributeModifier<Value, ?>> createModifierCodec(Map<EnvironmentAttributeModifier.Type, EnvironmentAttributeModifier<Value, ?>> modifierLibrary) {
        ImmutableBiMap immutableBiMap = ImmutableBiMap.builder().put((Object)EnvironmentAttributeModifier.Type.OVERRIDE, EnvironmentAttributeModifier.override()).putAll(modifierLibrary).buildOrThrow();
        return Codecs.idChecked(EnvironmentAttributeModifier.Type.CODEC, arg_0 -> ((ImmutableBiMap)immutableBiMap).get(arg_0), arg_0 -> ((ImmutableBiMap)immutableBiMap.inverse()).get(arg_0));
    }

    public void validate(EnvironmentAttributeModifier<Value, ?> modifier) {
        if (modifier != EnvironmentAttributeModifier.override() && !this.modifierLibrary.containsValue(modifier)) {
            throw new IllegalArgumentException("Modifier " + String.valueOf(modifier) + " is not valid for " + String.valueOf(this));
        }
    }

    @Override
    public String toString() {
        return Util.registryValueToString(Registries.ATTRIBUTE_TYPE, this);
    }
}

