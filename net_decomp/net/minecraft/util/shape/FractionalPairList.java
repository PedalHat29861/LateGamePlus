/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.IntMath
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.util.shape;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.shape.FractionalDoubleList;
import net.minecraft.util.shape.PairList;
import net.minecraft.util.shape.VoxelShapes;

public final class FractionalPairList
implements PairList {
    private final FractionalDoubleList mergedList;
    private final int firstSectionCount;
    private final int gcd;

    FractionalPairList(int i, int j) {
        this.mergedList = new FractionalDoubleList((int)VoxelShapes.lcm(i, j));
        int k = IntMath.gcd((int)i, (int)j);
        this.firstSectionCount = i / k;
        this.gcd = j / k;
    }

    @Override
    public boolean forEachPair(PairList.Consumer predicate) {
        int i = this.mergedList.size() - 1;
        for (int j = 0; j < i; ++j) {
            if (predicate.merge(j / this.gcd, j / this.firstSectionCount, j)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return this.mergedList.size();
    }

    @Override
    public DoubleList getPairs() {
        return this.mergedList;
    }
}

