/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.nbt.visitor.StringNbtWriter;
import org.apache.commons.lang3.ArrayUtils;

public final class NbtIntArray
implements AbstractNbtList {
    private static final int SIZE = 24;
    public static final NbtType<NbtIntArray> TYPE = new NbtType.OfVariableSize<NbtIntArray>(){

        @Override
        public NbtIntArray read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
            return new NbtIntArray(1.readIntArray(dataInput, nbtSizeTracker));
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            return visitor.visitIntArray(1.readIntArray(input, tracker));
        }

        private static int[] readIntArray(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(24L);
            int i = input.readInt();
            tracker.add(4L, i);
            int[] is = new int[i];
            for (int j = 0; j < i; ++j) {
                is[j] = input.readInt();
            }
            return is;
        }

        @Override
        public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
            input.skipBytes(input.readInt() * 4);
        }

        @Override
        public String getCrashReportName() {
            return "INT[]";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Int_Array";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };
    private int[] value;

    public NbtIntArray(int[] value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(this.value.length);
        for (int i : this.value) {
            output.writeInt(i);
        }
    }

    @Override
    public int getSizeInBytes() {
        return 24 + 4 * this.value.length;
    }

    @Override
    public byte getType() {
        return 11;
    }

    public NbtType<NbtIntArray> getNbtType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringNbtWriter stringNbtWriter = new StringNbtWriter();
        stringNbtWriter.visitIntArray(this);
        return stringNbtWriter.getString();
    }

    @Override
    public NbtIntArray copy() {
        int[] is = new int[this.value.length];
        System.arraycopy(this.value, 0, is, 0, this.value.length);
        return new NbtIntArray(is);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtIntArray && Arrays.equals(this.value, ((NbtIntArray)o).value);
    }

    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    public int[] getIntArray() {
        return this.value;
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitIntArray(this);
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public NbtInt method_10534(int i) {
        return NbtInt.of(this.value[i]);
    }

    @Override
    public boolean setElement(int index, NbtElement element) {
        if (element instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)element;
            this.value[index] = abstractNbtNumber.intValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addElement(int index, NbtElement element) {
        if (element instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)element;
            this.value = ArrayUtils.add((int[])this.value, (int)index, (int)abstractNbtNumber.intValue());
            return true;
        }
        return false;
    }

    @Override
    public NbtInt method_10536(int i) {
        int j = this.value[i];
        this.value = ArrayUtils.remove((int[])this.value, (int)i);
        return NbtInt.of(j);
    }

    @Override
    public void clear() {
        this.value = new int[0];
    }

    @Override
    public Optional<int[]> asIntArray() {
        return Optional.of(this.value);
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        return visitor.visitIntArray(this.value);
    }

    @Override
    public /* synthetic */ NbtElement method_10534(int i) {
        return this.method_10534(i);
    }

    @Override
    public /* synthetic */ NbtElement method_10536(int i) {
        return this.method_10536(i);
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }
}

