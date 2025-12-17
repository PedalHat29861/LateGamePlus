/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.Quantiles
 *  com.google.common.math.Quantiles$ScaleAndIndexes
 *  it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap
 *  it.unimi.dsi.fastutil.ints.Int2DoubleSortedMap
 *  it.unimi.dsi.fastutil.ints.Int2DoubleSortedMaps
 */
package net.minecraft.util.math;

import com.google.common.math.Quantiles;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMaps;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.util.Util;

public class Quantiles {
    public static final Quantiles.ScaleAndIndexes QUANTILE_POINTS = com.google.common.math.Quantiles.scale((int)100).indexes(new int[]{50, 75, 90, 99});

    private Quantiles() {
    }

    public static Map<Integer, Double> create(long[] values) {
        return values.length == 0 ? Map.of() : Quantiles.reverseMap(QUANTILE_POINTS.compute(values));
    }

    public static Map<Integer, Double> create(int[] values) {
        return values.length == 0 ? Map.of() : Quantiles.reverseMap(QUANTILE_POINTS.compute(values));
    }

    public static Map<Integer, Double> create(double[] values) {
        return values.length == 0 ? Map.of() : Quantiles.reverseMap(QUANTILE_POINTS.compute(values));
    }

    private static Map<Integer, Double> reverseMap(Map<Integer, Double> map) {
        Int2DoubleSortedMap int2DoubleSortedMap = (Int2DoubleSortedMap)Util.make(new Int2DoubleRBTreeMap(Comparator.reverseOrder()), reversedMap -> reversedMap.putAll(map));
        return Int2DoubleSortedMaps.unmodifiable((Int2DoubleSortedMap)int2DoubleSortedMap);
    }
}

