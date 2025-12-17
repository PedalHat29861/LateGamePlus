/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.HashCommon
 *  it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
 */
package net.minecraft.util.collection;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.NoSuchElementException;
import net.minecraft.util.math.MathHelper;

public class LinkedBlockPosHashSet
extends LongLinkedOpenHashSet {
    private final Storage buffer;

    public LinkedBlockPosHashSet(int expectedSize, float loadFactor) {
        super(expectedSize, loadFactor);
        this.buffer = new Storage(expectedSize / 64, loadFactor);
    }

    public boolean add(long posLong) {
        return this.buffer.add(posLong);
    }

    public boolean rem(long posLong) {
        return this.buffer.rem(posLong);
    }

    public long removeFirstLong() {
        return this.buffer.removeFirstLong();
    }

    public int size() {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return this.buffer.isEmpty();
    }

    protected static class Storage
    extends Long2LongLinkedOpenHashMap {
        private static final int STARTING_OFFSET = MathHelper.floorLog2(60000000);
        private static final int HORIZONTAL_COLUMN_BIT_SEPARATION = MathHelper.floorLog2(60000000);
        private static final int FIELD_SPACING;
        private static final int Y_BIT_OFFSET = 0;
        private static final int X_BIT_OFFSET;
        private static final int Z_BIT_OFFSET;
        private static final long MAX_POSITION;
        private int lastWrittenIndex = -1;
        private long lastWrittenKey;
        private final int expectedSize;

        public Storage(int expectedSize, float loadFactor) {
            super(expectedSize, loadFactor);
            this.expectedSize = expectedSize;
        }

        static long getKey(long posLong) {
            return posLong & (MAX_POSITION ^ 0xFFFFFFFFFFFFFFFFL);
        }

        static int getBlockOffset(long posLong) {
            int i = (int)(posLong >>> Z_BIT_OFFSET & 3L);
            int j = (int)(posLong >>> 0 & 3L);
            int k = (int)(posLong >>> X_BIT_OFFSET & 3L);
            return i << 4 | k << 2 | j;
        }

        static long getBlockPosLong(long key, int valueLength) {
            key |= (long)(valueLength >>> 4 & 3) << Z_BIT_OFFSET;
            key |= (long)(valueLength >>> 2 & 3) << X_BIT_OFFSET;
            return key |= (long)(valueLength >>> 0 & 3) << 0;
        }

        public boolean add(long posLong) {
            int j;
            long l = Storage.getKey(posLong);
            int i = Storage.getBlockOffset(posLong);
            long m = 1L << i;
            if (l == 0L) {
                if (this.containsNullKey) {
                    return this.setBits(this.n, m);
                }
                this.containsNullKey = true;
                j = this.n;
            } else {
                if (this.lastWrittenIndex != -1 && l == this.lastWrittenKey) {
                    return this.setBits(this.lastWrittenIndex, m);
                }
                long[] ls = this.key;
                j = (int)HashCommon.mix((long)l) & this.mask;
                long n = ls[j];
                while (n != 0L) {
                    if (n == l) {
                        this.lastWrittenIndex = j;
                        this.lastWrittenKey = l;
                        return this.setBits(j, m);
                    }
                    j = j + 1 & this.mask;
                    n = ls[j];
                }
            }
            this.key[j] = l;
            this.value[j] = m;
            if (this.size == 0) {
                this.first = this.last = j;
                this.link[j] = -1L;
            } else {
                int n = this.last;
                this.link[n] = this.link[n] ^ (this.link[this.last] ^ (long)j & 0xFFFFFFFFL) & 0xFFFFFFFFL;
                this.link[j] = ((long)this.last & 0xFFFFFFFFL) << 32 | 0xFFFFFFFFL;
                this.last = j;
            }
            if (this.size++ >= this.maxFill) {
                this.rehash(HashCommon.arraySize((int)(this.size + 1), (float)this.f));
            }
            return false;
        }

        private boolean setBits(int index, long mask) {
            boolean bl = (this.value[index] & mask) != 0L;
            int n = index;
            this.value[n] = this.value[n] | mask;
            return bl;
        }

        public boolean rem(long posLong) {
            long l = Storage.getKey(posLong);
            int i = Storage.getBlockOffset(posLong);
            long m = 1L << i;
            if (l == 0L) {
                if (this.containsNullKey) {
                    return this.unsetBits(m);
                }
                return false;
            }
            if (this.lastWrittenIndex != -1 && l == this.lastWrittenKey) {
                return this.unsetBitsAt(this.lastWrittenIndex, m);
            }
            long[] ls = this.key;
            int j = (int)HashCommon.mix((long)l) & this.mask;
            long n = ls[j];
            while (n != 0L) {
                if (l == n) {
                    this.lastWrittenIndex = j;
                    this.lastWrittenKey = l;
                    return this.unsetBitsAt(j, m);
                }
                j = j + 1 & this.mask;
                n = ls[j];
            }
            return false;
        }

        private boolean unsetBits(long mask) {
            if ((this.value[this.n] & mask) == 0L) {
                return false;
            }
            int n = this.n;
            this.value[n] = this.value[n] & (mask ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[this.n] != 0L) {
                return true;
            }
            this.containsNullKey = false;
            --this.size;
            this.fixPointers(this.n);
            if (this.size < this.maxFill / 4 && this.n > 16) {
                this.rehash(this.n / 2);
            }
            return true;
        }

        private boolean unsetBitsAt(int index, long mask) {
            if ((this.value[index] & mask) == 0L) {
                return false;
            }
            int n = index;
            this.value[n] = this.value[n] & (mask ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[index] != 0L) {
                return true;
            }
            this.lastWrittenIndex = -1;
            --this.size;
            this.fixPointers(index);
            this.shiftKeys(index);
            if (this.size < this.maxFill / 4 && this.n > 16) {
                this.rehash(this.n / 2);
            }
            return true;
        }

        public long removeFirstLong() {
            if (this.size == 0) {
                throw new NoSuchElementException();
            }
            int i = this.first;
            long l = this.key[i];
            int j = Long.numberOfTrailingZeros(this.value[i]);
            int n = i;
            this.value[n] = this.value[n] & (1L << j ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[i] == 0L) {
                this.removeFirstLong();
                this.lastWrittenIndex = -1;
            }
            return Storage.getBlockPosLong(l, j);
        }

        protected void rehash(int newN) {
            if (newN > this.expectedSize) {
                super.rehash(newN);
            }
        }

        static {
            X_BIT_OFFSET = FIELD_SPACING = 64 - STARTING_OFFSET - HORIZONTAL_COLUMN_BIT_SEPARATION;
            Z_BIT_OFFSET = FIELD_SPACING + HORIZONTAL_COLUMN_BIT_SEPARATION;
            MAX_POSITION = 3L << Z_BIT_OFFSET | 3L | 3L << X_BIT_OFFSET;
        }
    }
}

