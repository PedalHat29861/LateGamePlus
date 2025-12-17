/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleMaps
 */
package net.minecraft.world.attribute;

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMaps;
import java.util.Objects;
import net.minecraft.util.math.Interpolator;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;

public class WeightedAttributeList {
    private final Reference2DoubleArrayMap<EnvironmentAttributeMap> entries = new Reference2DoubleArrayMap();

    public void clear() {
        this.entries.clear();
    }

    public WeightedAttributeList add(double weight, EnvironmentAttributeMap attributes) {
        this.entries.mergeDouble((Object)attributes, weight, Double::sum);
        return this;
    }

    public <Value> Value interpolate(EnvironmentAttribute<Value> attribute, Value defaultValue) {
        if (this.entries.isEmpty()) {
            return defaultValue;
        }
        if (this.entries.size() == 1) {
            EnvironmentAttributeMap environmentAttributeMap = (EnvironmentAttributeMap)this.entries.keySet().iterator().next();
            return environmentAttributeMap.apply(attribute, defaultValue);
        }
        Interpolator<Value> interpolator = attribute.getType().spatialLerp();
        Object object = null;
        double d = 0.0;
        for (Reference2DoubleMap.Entry entry : Reference2DoubleMaps.fastIterable(this.entries)) {
            EnvironmentAttributeMap environmentAttributeMap2 = (EnvironmentAttributeMap)entry.getKey();
            double e = entry.getDoubleValue();
            Value object2 = environmentAttributeMap2.apply(attribute, defaultValue);
            d += e;
            if (object == null) {
                object = object2;
                continue;
            }
            float f = (float)(e / d);
            object = interpolator.apply(f, object, object2);
        }
        return Objects.requireNonNull(object);
    }
}

