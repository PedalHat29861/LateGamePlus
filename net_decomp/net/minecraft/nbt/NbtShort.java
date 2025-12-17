/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.nbt.visitor.StringNbtWriter;

public record NbtShort(short value) implements AbstractNbtNumber
{
    private static final int SIZE = 10;
    public static final NbtType<NbtShort> TYPE = new NbtType.OfFixedSize<NbtShort>(){

        @Override
        public NbtShort read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
            return NbtShort.of(1.readShort(dataInput, nbtSizeTracker));
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            return visitor.visitShort(1.readShort(input, tracker));
        }

        private static short readShort(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(10L);
            return input.readShort();
        }

        @Override
        public int getSizeInBytes() {
            return 2;
        }

        @Override
        public String getCrashReportName() {
            return "SHORT";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Short";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };

    public static NbtShort of(short value) {
        if (value >= -128 && value <= 1024) {
            return Cache.VALUES[value - -128];
        }
        return new NbtShort(value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeShort(this.value);
    }

    @Override
    public int getSizeInBytes() {
        return 10;
    }

    @Override
    public byte getType() {
        return 2;
    }

    public NbtType<NbtShort> getNbtType() {
        return TYPE;
    }

    @Override
    public NbtShort copy() {
        return this;
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitShort(this);
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public short shortValue() {
        return this.value;
    }

    @Override
    public byte byteValue() {
        return (byte)(this.value & 0xFF);
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public Number numberValue() {
        return this.value;
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        return visitor.visitShort(this.value);
    }

    @Override
    public String toString() {
        StringNbtWriter stringNbtWriter = new StringNbtWriter();
        stringNbtWriter.visitShort(this);
        return stringNbtWriter.getString();
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }

    static class Cache {
        private static final int MAX = 1024;
        private static final int MIN = -128;
        static final NbtShort[] VALUES = new NbtShort[1153];

        private Cache() {
        }

        static {
            for (int i = 0; i < VALUES.length; ++i) {
                Cache.VALUES[i] = new NbtShort((short)(-128 + i));
            }
        }
    }
}

