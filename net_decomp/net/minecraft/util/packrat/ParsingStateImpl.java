/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.packrat;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.util.Util;
import net.minecraft.util.packrat.Cut;
import net.minecraft.util.packrat.ParseErrorList;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Symbol;
import org.jspecify.annotations.Nullable;

public abstract class ParsingStateImpl<S>
implements ParsingState<S> {
    private @Nullable MemoizedData[] memoStack = new MemoizedData[256];
    private final ParseErrorList<S> errors;
    private final ParseResults results = new ParseResults();
    private @Nullable Cutter[] cutters = new Cutter[16];
    private int topCutterIndex;
    private final ErrorSuppressing errorSuppressingState = new ErrorSuppressing();

    protected ParsingStateImpl(ParseErrorList<S> errors) {
        this.errors = errors;
    }

    @Override
    public ParseResults getResults() {
        return this.results;
    }

    @Override
    public ParseErrorList<S> getErrors() {
        return this.errors;
    }

    @Override
    public <T> @Nullable T parse(ParsingRuleEntry<S, T> rule) {
        MemoizedValue memoizedValue2;
        T object;
        int i = this.getCursor();
        MemoizedData memoizedData = this.pushMemoizedData(i);
        int j = memoizedData.get(rule.getSymbol());
        if (j != -1) {
            MemoizedValue memoizedValue = memoizedData.get(j);
            if (memoizedValue != null) {
                if (memoizedValue == MemoizedValue.EMPTY) {
                    return null;
                }
                this.setCursor(memoizedValue.markAfterParse);
                return memoizedValue.value;
            }
        } else {
            j = memoizedData.push(rule.getSymbol());
        }
        if ((object = rule.getRule().parse(this)) == null) {
            memoizedValue2 = MemoizedValue.empty();
        } else {
            int k = this.getCursor();
            memoizedValue2 = new MemoizedValue<T>(object, k);
        }
        memoizedData.put(j, memoizedValue2);
        return object;
    }

    private MemoizedData pushMemoizedData(int cursor) {
        MemoizedData memoizedData;
        int i = this.memoStack.length;
        if (cursor >= i) {
            int j = Util.nextCapacity(i, cursor + 1);
            MemoizedData[] memoizedDatas = new MemoizedData[j];
            System.arraycopy(this.memoStack, 0, memoizedDatas, 0, i);
            this.memoStack = memoizedDatas;
        }
        if ((memoizedData = this.memoStack[cursor]) == null) {
            this.memoStack[cursor] = memoizedData = new MemoizedData();
        }
        return memoizedData;
    }

    @Override
    public Cut pushCutter() {
        Cutter cutter;
        int j;
        int i = this.cutters.length;
        if (this.topCutterIndex >= i) {
            j = Util.nextCapacity(i, this.topCutterIndex + 1);
            Cutter[] cutters = new Cutter[j];
            System.arraycopy(this.cutters, 0, cutters, 0, i);
            this.cutters = cutters;
        }
        if ((cutter = this.cutters[j = this.topCutterIndex++]) == null) {
            this.cutters[j] = cutter = new Cutter();
        } else {
            cutter.reset();
        }
        return cutter;
    }

    @Override
    public void popCutter() {
        --this.topCutterIndex;
    }

    @Override
    public ParsingState<S> getErrorSuppressingState() {
        return this.errorSuppressingState;
    }

    static class MemoizedData {
        public static final int SIZE_PER_SYMBOL = 2;
        private static final int MISSING = -1;
        private Object[] values = new Object[16];
        private int top;

        MemoizedData() {
        }

        public int get(Symbol<?> symbol) {
            for (int i = 0; i < this.top; i += 2) {
                if (this.values[i] != symbol) continue;
                return i;
            }
            return -1;
        }

        public int push(Symbol<?> symbol) {
            int i = this.top;
            this.top += 2;
            int j = i + 1;
            int k = this.values.length;
            if (j >= k) {
                int l = Util.nextCapacity(k, j + 1);
                Object[] objects = new Object[l];
                System.arraycopy(this.values, 0, objects, 0, k);
                this.values = objects;
            }
            this.values[i] = symbol;
            return i;
        }

        public <T> @Nullable MemoizedValue<T> get(int index) {
            return (MemoizedValue)this.values[index + 1];
        }

        public void put(int index, MemoizedValue<?> value) {
            this.values[index + 1] = value;
        }
    }

    static class Cutter
    implements Cut {
        private boolean cut;

        Cutter() {
        }

        @Override
        public void cut() {
            this.cut = true;
        }

        @Override
        public boolean isCut() {
            return this.cut;
        }

        public void reset() {
            this.cut = false;
        }
    }

    class ErrorSuppressing
    implements ParsingState<S> {
        private final ParseErrorList<S> errors = new ParseErrorList.Noop();

        ErrorSuppressing() {
        }

        @Override
        public ParseErrorList<S> getErrors() {
            return this.errors;
        }

        @Override
        public ParseResults getResults() {
            return ParsingStateImpl.this.getResults();
        }

        @Override
        public <T> @Nullable T parse(ParsingRuleEntry<S, T> rule) {
            return ParsingStateImpl.this.parse(rule);
        }

        @Override
        public S getReader() {
            return ParsingStateImpl.this.getReader();
        }

        @Override
        public int getCursor() {
            return ParsingStateImpl.this.getCursor();
        }

        @Override
        public void setCursor(int cursor) {
            ParsingStateImpl.this.setCursor(cursor);
        }

        @Override
        public Cut pushCutter() {
            return ParsingStateImpl.this.pushCutter();
        }

        @Override
        public void popCutter() {
            ParsingStateImpl.this.popCutter();
        }

        @Override
        public ParsingState<S> getErrorSuppressingState() {
            return this;
        }
    }

    static final class MemoizedValue<T>
    extends Record {
        final @Nullable T value;
        final int markAfterParse;
        public static final MemoizedValue<?> EMPTY = new MemoizedValue<Object>(null, -1);

        MemoizedValue(@Nullable T value, int markAfterParse) {
            this.value = value;
            this.markAfterParse = markAfterParse;
        }

        public static <T> MemoizedValue<T> empty() {
            return EMPTY;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{MemoizedValue.class, "value;markAfterParse", "value", "markAfterParse"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MemoizedValue.class, "value;markAfterParse", "value", "markAfterParse"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MemoizedValue.class, "value;markAfterParse", "value", "markAfterParse"}, this, object);
        }

        public @Nullable T value() {
            return this.value;
        }

        public int markAfterParse() {
            return this.markAfterParse;
        }
    }
}

