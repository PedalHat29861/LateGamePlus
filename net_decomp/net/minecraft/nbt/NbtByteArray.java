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
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.nbt.visitor.StringNbtWriter;
import org.apache.commons.lang3.ArrayUtils;

public final class NbtByteArray
implements AbstractNbtList {
    private static final int SIZE = 24;
    public static final NbtType<NbtByteArray> TYPE = new NbtType.OfVariableSize<NbtByteArray>(){

        @Override
        public NbtByteArray read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
            return new NbtByteArray(1.readByteArray(dataInput, nbtSizeTracker));
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            return visitor.visitByteArray(1.readByteArray(input, tracker));
        }

        private static byte[] readByteArray(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(24L);
            int i = input.readInt();
            tracker.add(1L, i);
            byte[] bs = new byte[i];
            input.readFully(bs);
            return bs;
        }

        @Override
        public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
            input.skipBytes(input.readInt() * 1);
        }

        @Override
        public String getCrashReportName() {
            return "BYTE[]";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Byte_Array";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };
    private byte[] value;

    public NbtByteArray(byte[] value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(this.value.length);
        output.write(this.value);
    }

    @Override
    public int getSizeInBytes() {
        return 24 + 1 * this.value.length;
    }

    @Override
    public byte getType() {
        return 7;
    }

    public NbtType<NbtByteArray> getNbtType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringNbtWriter stringNbtWriter = new StringNbtWriter();
        stringNbtWriter.visitByteArray(this);
        return stringNbtWriter.getString();
    }

    @Override
    public NbtElement copy() {
        byte[] bs = new byte[this.value.length];
        System.arraycopy(this.value, 0, bs, 0, this.value.length);
        return new NbtByteArray(bs);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtByteArray && Arrays.equals(this.value, ((NbtByteArray)o).value);
    }

    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitByteArray(this);
    }

    public byte[] getByteArray() {
        return this.value;
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public NbtByte method_10534(int i) {
        return NbtByte.of(this.value[i]);
    }

    @Override
    public boolean setElement(int index, NbtElement element) {
        if (element instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)element;
            this.value[index] = abstractNbtNumber.byteValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addElement(int index, NbtElement element) {
        if (element instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)element;
            this.value = ArrayUtils.add((byte[])this.value, (int)index, (byte)abstractNbtNumber.byteValue());
            return true;
        }
        return false;
    }

    @Override
    public NbtByte method_10536(int i) {
        byte b = this.value[i];
        this.value = ArrayUtils.remove((byte[])this.value, (int)i);
        return NbtByte.of(b);
    }

    @Override
    public void clear() {
        this.value = new byte[0];
    }

    @Override
    public Optional<byte[]> asByteArray() {
        return Optional.of(this.value);
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        return visitor.visitByteArray(this.value);
    }

    @Override
    public /* synthetic */ NbtElement method_10534(int i) {
        return this.method_10534(i);
    }

    @Override
    public /* synthetic */ NbtElement method_10536(int i) {
        return this.method_10536(i);
    }
}

