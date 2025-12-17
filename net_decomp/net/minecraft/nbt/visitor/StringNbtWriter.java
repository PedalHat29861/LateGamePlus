/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.visitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class StringNbtWriter
implements NbtElementVisitor {
    private static final Pattern QUOTATION_UNNECESSARY_PATTERN = Pattern.compile("[A-Za-z._]+[A-Za-z0-9._+-]*");
    private final StringBuilder result = new StringBuilder();

    public String getString() {
        return this.result.toString();
    }

    @Override
    public void visitString(NbtString element) {
        this.result.append(NbtString.escape(element.value()));
    }

    @Override
    public void visitByte(NbtByte element) {
        this.result.append(element.value()).append('b');
    }

    @Override
    public void visitShort(NbtShort element) {
        this.result.append(element.value()).append('s');
    }

    @Override
    public void visitInt(NbtInt element) {
        this.result.append(element.value());
    }

    @Override
    public void visitLong(NbtLong element) {
        this.result.append(element.value()).append('L');
    }

    @Override
    public void visitFloat(NbtFloat element) {
        this.result.append(element.value()).append('f');
    }

    @Override
    public void visitDouble(NbtDouble element) {
        this.result.append(element.value()).append('d');
    }

    @Override
    public void visitByteArray(NbtByteArray element) {
        this.result.append("[B;");
        byte[] bs = element.getByteArray();
        for (int i = 0; i < bs.length; ++i) {
            if (i != 0) {
                this.result.append(',');
            }
            this.result.append(bs[i]).append('B');
        }
        this.result.append(']');
    }

    @Override
    public void visitIntArray(NbtIntArray element) {
        this.result.append("[I;");
        int[] is = element.getIntArray();
        for (int i = 0; i < is.length; ++i) {
            if (i != 0) {
                this.result.append(',');
            }
            this.result.append(is[i]);
        }
        this.result.append(']');
    }

    @Override
    public void visitLongArray(NbtLongArray element) {
        this.result.append("[L;");
        long[] ls = element.getLongArray();
        for (int i = 0; i < ls.length; ++i) {
            if (i != 0) {
                this.result.append(',');
            }
            this.result.append(ls[i]).append('L');
        }
        this.result.append(']');
    }

    @Override
    public void visitList(NbtList element) {
        this.result.append('[');
        for (int i = 0; i < element.size(); ++i) {
            if (i != 0) {
                this.result.append(',');
            }
            element.method_10534(i).accept(this);
        }
        this.result.append(']');
    }

    @Override
    public void visitCompound(NbtCompound compound) {
        this.result.append('{');
        ArrayList<Map.Entry<String, NbtElement>> list = new ArrayList<Map.Entry<String, NbtElement>>(compound.entrySet());
        list.sort(Map.Entry.comparingByKey());
        for (int i = 0; i < list.size(); ++i) {
            Map.Entry entry = (Map.Entry)list.get(i);
            if (i != 0) {
                this.result.append(',');
            }
            this.append((String)entry.getKey());
            this.result.append(':');
            ((NbtElement)entry.getValue()).accept(this);
        }
        this.result.append('}');
    }

    private void append(String string) {
        if (!string.equalsIgnoreCase("true") && !string.equalsIgnoreCase("false") && QUOTATION_UNNECESSARY_PATTERN.matcher(string).matches()) {
            this.result.append(string);
        } else {
            NbtString.appendEscaped(string, this.result);
        }
    }

    @Override
    public void visitEnd(NbtEnd element) {
        this.result.append("END");
    }
}

