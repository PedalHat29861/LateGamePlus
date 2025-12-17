/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.packrat.ParseError;
import net.minecraft.util.packrat.Suggestable;
import org.jspecify.annotations.Nullable;

public interface ParseErrorList<S> {
    public void add(int var1, Suggestable<S> var2, Object var3);

    default public void add(int cursor, Object reason) {
        this.add(cursor, Suggestable.empty(), reason);
    }

    public void setCursor(int var1);

    public static class Impl<S>
    implements ParseErrorList<S> {
        private @Nullable Entry<S>[] errors = new Entry[16];
        private int topIndex;
        private int cursor = -1;

        private void moveCursor(int cursor) {
            if (cursor > this.cursor) {
                this.cursor = cursor;
                this.topIndex = 0;
            }
        }

        @Override
        public void setCursor(int cursor) {
            this.moveCursor(cursor);
        }

        @Override
        public void add(int cursor, Suggestable<S> suggestions, Object reason) {
            this.moveCursor(cursor);
            if (cursor == this.cursor) {
                this.add(suggestions, reason);
            }
        }

        private void add(Suggestable<S> suggestions, Object reason) {
            Entry<S> entry;
            int j;
            int i = this.errors.length;
            if (this.topIndex >= i) {
                j = Util.nextCapacity(i, this.topIndex + 1);
                Entry[] entrys = new Entry[j];
                System.arraycopy(this.errors, 0, entrys, 0, i);
                this.errors = entrys;
            }
            if ((entry = this.errors[j = this.topIndex++]) == null) {
                this.errors[j] = entry = new Entry();
            }
            entry.suggestions = suggestions;
            entry.reason = reason;
        }

        public List<ParseError<S>> getErrors() {
            int i = this.topIndex;
            if (i == 0) {
                return List.of();
            }
            ArrayList<ParseError<S>> list = new ArrayList<ParseError<S>>(i);
            for (int j = 0; j < i; ++j) {
                Entry<S> entry = this.errors[j];
                list.add(new ParseError(this.cursor, entry.suggestions, entry.reason));
            }
            return list;
        }

        public int getCursor() {
            return this.cursor;
        }

        static class Entry<S> {
            Suggestable<S> suggestions = Suggestable.empty();
            Object reason = "empty";

            Entry() {
            }
        }
    }

    public static class Noop<S>
    implements ParseErrorList<S> {
        @Override
        public void add(int cursor, Suggestable<S> suggestions, Object reason) {
        }

        @Override
        public void setCursor(int cursor) {
        }
    }
}

