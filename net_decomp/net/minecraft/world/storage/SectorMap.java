/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntSet
 */
package net.minecraft.world.storage;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.BitSet;

public class SectorMap {
    private final BitSet bitSet = new BitSet();

    public void allocate(int start, int size) {
        this.bitSet.set(start, start + size);
    }

    public void free(int start, int size) {
        this.bitSet.clear(start, start + size);
    }

    public int allocate(int size) {
        int i = 0;
        while (true) {
            int j;
            int k;
            if ((k = this.bitSet.nextSetBit(j = this.bitSet.nextClearBit(i))) == -1 || k - j >= size) {
                this.allocate(j, size);
                return j;
            }
            i = k;
        }
    }

    @VisibleForTesting
    public IntSet getAllocatedBits() {
        return (IntSet)this.bitSet.stream().collect(IntArraySet::new, IntCollection::add, IntCollection::addAll);
    }
}

