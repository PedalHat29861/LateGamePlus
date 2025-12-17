/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.packrat;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Util;
import net.minecraft.util.packrat.Symbol;
import org.jspecify.annotations.Nullable;

public final class ParseResults {
    private static final int MISSING = -1;
    private static final Object FRAME = new Object(){

        public String toString() {
            return "frame";
        }
    };
    private static final int ENTRY_SIZE = 2;
    private @Nullable Object[] stack = new Object[128];
    private int stackTop = 0;
    private int stackBottom = 0;

    public ParseResults() {
        this.stack[0] = FRAME;
        this.stack[1] = null;
    }

    private int indexOf(Symbol<?> symbol) {
        for (int i = this.stackTop; i > this.stackBottom; i -= 2) {
            Object object = this.stack[i];
            assert (object instanceof Symbol);
            if (object != symbol) continue;
            return i + 1;
        }
        return -1;
    }

    public int indexOf(Symbol<?> ... symbols) {
        for (int i = this.stackTop; i > this.stackBottom; i -= 2) {
            Object object = this.stack[i];
            assert (object instanceof Symbol);
            for (Symbol<?> symbol : symbols) {
                if (symbol != object) continue;
                return i + 1;
            }
        }
        return -1;
    }

    private void expandIfNeeded(int amount) {
        int j = this.stackTop + 1;
        int k = j + amount * 2;
        int i = this.stack.length;
        if (k >= i) {
            int l = Util.nextCapacity(i, k + 1);
            Object[] objects = new Object[l];
            System.arraycopy(this.stack, 0, objects, 0, i);
            this.stack = objects;
        }
        assert (this.isValid());
    }

    private void addFrame() {
        this.stackTop += 2;
        this.stack[this.stackTop] = FRAME;
        this.stack[this.stackTop + 1] = this.stackBottom;
        this.stackBottom = this.stackTop;
    }

    public void pushFrame() {
        this.expandIfNeeded(1);
        this.addFrame();
        assert (this.isValid());
    }

    private int getPreviousStackBottom(int current) {
        return (Integer)this.stack[current + 1];
    }

    public void popFrame() {
        assert (this.stackBottom != 0);
        this.stackTop = this.stackBottom - 2;
        this.stackBottom = this.getPreviousStackBottom(this.stackBottom);
        assert (this.isValid());
    }

    public void duplicateFrames() {
        int i = this.stackBottom;
        int j = (this.stackTop - this.stackBottom) / 2;
        this.expandIfNeeded(j + 1);
        this.addFrame();
        int k = i + 2;
        int l = this.stackTop;
        for (int m = 0; m < j; ++m) {
            l += 2;
            Object object = this.stack[k];
            assert (object != null);
            this.stack[l] = object;
            this.stack[l + 1] = null;
            k += 2;
        }
        this.stackTop = l;
        assert (this.isValid());
    }

    public void clearFrameValues() {
        for (int i = this.stackTop; i > this.stackBottom; i -= 2) {
            assert (this.stack[i] instanceof Symbol);
            this.stack[i + 1] = null;
        }
        assert (this.isValid());
    }

    public void chooseCurrentFrame() {
        int i;
        int j = i = this.getPreviousStackBottom(this.stackBottom);
        int k = this.stackBottom;
        while (k < this.stackTop) {
            j += 2;
            Object object = this.stack[k += 2];
            assert (object instanceof Symbol);
            Object object2 = this.stack[k + 1];
            Object object3 = this.stack[j];
            if (object3 != object) {
                this.stack[j] = object;
                this.stack[j + 1] = object2;
                continue;
            }
            if (object2 == null) continue;
            this.stack[j + 1] = object2;
        }
        this.stackTop = j;
        this.stackBottom = i;
        assert (this.isValid());
    }

    public <T> void put(Symbol<T> symbol, @Nullable T value) {
        int i = this.indexOf((Symbol<?>)symbol);
        if (i != -1) {
            this.stack[i] = value;
        } else {
            this.expandIfNeeded(1);
            this.stackTop += 2;
            this.stack[this.stackTop] = symbol;
            this.stack[this.stackTop + 1] = value;
        }
        assert (this.isValid());
    }

    public <T> @Nullable T get(Symbol<T> symbol) {
        int i = this.indexOf((Symbol<?>)symbol);
        return (T)(i != -1 ? this.stack[i] : null);
    }

    public <T> T getOrThrow(Symbol<T> symbol) {
        int i = this.indexOf((Symbol<?>)symbol);
        if (i == -1) {
            throw new IllegalArgumentException("No value for atom " + String.valueOf(symbol));
        }
        return (T)this.stack[i];
    }

    public <T> T getOrDefault(Symbol<T> symbol, T fallback) {
        int i = this.indexOf((Symbol<?>)symbol);
        return (T)(i != -1 ? this.stack[i] : fallback);
    }

    @SafeVarargs
    public final <T> @Nullable T getAny(Symbol<? extends T> ... symbols) {
        int i = this.indexOf(symbols);
        return (T)(i != -1 ? this.stack[i] : null);
    }

    @SafeVarargs
    public final <T> T getAnyOrThrow(Symbol<? extends T> ... symbols) {
        int i = this.indexOf(symbols);
        if (i == -1) {
            throw new IllegalArgumentException("No value for atoms " + Arrays.toString(symbols));
        }
        return (T)this.stack[i];
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl = true;
        for (int i = 0; i <= this.stackTop; i += 2) {
            Object object = this.stack[i];
            Object object2 = this.stack[i + 1];
            if (object == FRAME) {
                stringBuilder.append('|');
                bl = true;
                continue;
            }
            if (!bl) {
                stringBuilder.append(',');
            }
            bl = false;
            stringBuilder.append(object).append(':').append(object2);
        }
        return stringBuilder.toString();
    }

    @VisibleForTesting
    public Map<Symbol<?>, ?> toSymbolKeyedMap() {
        HashMap<Symbol, Object> hashMap = new HashMap<Symbol, Object>();
        for (int i = this.stackTop; i > this.stackBottom; i -= 2) {
            Object object = this.stack[i];
            Object object2 = this.stack[i + 1];
            hashMap.put((Symbol)object, object2);
        }
        return hashMap;
    }

    public boolean areFramesPlacedCorrectly() {
        for (int i = this.stackTop; i > 0; --i) {
            if (this.stack[i] != FRAME) continue;
            return false;
        }
        if (this.stack[0] != FRAME) {
            throw new IllegalStateException("Corrupted stack");
        }
        return true;
    }

    private boolean isValid() {
        Object object;
        int i;
        assert (this.stackBottom >= 0);
        assert (this.stackTop >= this.stackBottom);
        for (i = 0; i <= this.stackTop; i += 2) {
            object = this.stack[i];
            if (object == FRAME || object instanceof Symbol) continue;
            return false;
        }
        i = this.stackBottom;
        while (i != 0) {
            object = this.stack[i];
            if (object != FRAME) {
                return false;
            }
            i = this.getPreviousStackBottom(i);
        }
        return true;
    }
}

