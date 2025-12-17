/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.InvalidNbtException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.nbt.visitor.StringNbtWriter;
import org.jspecify.annotations.Nullable;

public final class NbtList
extends AbstractList<NbtElement>
implements AbstractNbtList {
    private static final String HOMOGENIZED_ENTRY_KEY = "";
    private static final int SIZE = 36;
    public static final NbtType<NbtList> TYPE = new NbtType.OfVariableSize<NbtList>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public NbtList read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
            nbtSizeTracker.pushStack();
            try {
                NbtList nbtList = 1.readList(dataInput, nbtSizeTracker);
                return nbtList;
            }
            finally {
                nbtSizeTracker.popStack();
            }
        }

        private static NbtList readList(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(36L);
            byte b = input.readByte();
            int i = 1.readListLength(input);
            if (b == 0 && i > 0) {
                throw new InvalidNbtException("Missing type on ListTag");
            }
            tracker.add(4L, i);
            NbtType<?> nbtType = NbtTypes.byId(b);
            NbtList nbtList = new NbtList(new ArrayList<NbtElement>(i));
            for (int j = 0; j < i; ++j) {
                nbtList.unwrapAndAdd((NbtElement)nbtType.read(input, tracker));
            }
            return nbtList;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            tracker.pushStack();
            try {
                NbtScanner.Result result = 1.scanList(input, visitor, tracker);
                return result;
            }
            finally {
                tracker.popStack();
            }
        }

        /*
         * Exception decompiling
         */
        private static NbtScanner.Result scanList(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [4[SWITCH], 8[CASE]], but top level block is 9[SWITCH]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        private static int readListLength(DataInput input) throws IOException {
            int i = input.readInt();
            if (i < 0) {
                throw new InvalidNbtException("ListTag length cannot be negative: " + i);
            }
            return i;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.pushStack();
            try {
                NbtType<?> nbtType = NbtTypes.byId(input.readByte());
                int i = input.readInt();
                nbtType.skip(input, i, tracker);
            }
            finally {
                tracker.popStack();
            }
        }

        @Override
        public String getCrashReportName() {
            return "LIST";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_List";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };
    private final List<NbtElement> value;

    public NbtList() {
        this(new ArrayList<NbtElement>());
    }

    NbtList(List<NbtElement> value) {
        this.value = value;
    }

    private static NbtElement unwrap(NbtCompound nbt) {
        NbtElement nbtElement;
        if (nbt.getSize() == 1 && (nbtElement = nbt.get(HOMOGENIZED_ENTRY_KEY)) != null) {
            return nbtElement;
        }
        return nbt;
    }

    private static boolean isConvertedEntry(NbtCompound nbt) {
        return nbt.getSize() == 1 && nbt.contains(HOMOGENIZED_ENTRY_KEY);
    }

    private static NbtElement wrapIfNeeded(byte type, NbtElement value) {
        NbtCompound nbtCompound;
        if (type != 10) {
            return value;
        }
        if (value instanceof NbtCompound && !NbtList.isConvertedEntry(nbtCompound = (NbtCompound)value)) {
            return nbtCompound;
        }
        return NbtList.convertToCompound(value);
    }

    private static NbtCompound convertToCompound(NbtElement nbt) {
        return new NbtCompound(Map.of(HOMOGENIZED_ENTRY_KEY, nbt));
    }

    @Override
    public void write(DataOutput output) throws IOException {
        byte b = this.getValueType();
        output.writeByte(b);
        output.writeInt(this.value.size());
        for (NbtElement nbtElement : this.value) {
            NbtList.wrapIfNeeded(b, nbtElement).write(output);
        }
    }

    @VisibleForTesting
    byte getValueType() {
        byte b = 0;
        for (NbtElement nbtElement : this.value) {
            byte c = nbtElement.getType();
            if (b == 0) {
                b = c;
                continue;
            }
            if (b == c) continue;
            return 10;
        }
        return b;
    }

    public void unwrapAndAdd(NbtElement nbt) {
        if (nbt instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)nbt;
            this.add(NbtList.unwrap(nbtCompound));
        } else {
            this.add(nbt);
        }
    }

    @Override
    public int getSizeInBytes() {
        int i = 36;
        i += 4 * this.value.size();
        for (NbtElement nbtElement : this.value) {
            i += nbtElement.getSizeInBytes();
        }
        return i;
    }

    @Override
    public byte getType() {
        return 9;
    }

    public NbtType<NbtList> getNbtType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringNbtWriter stringNbtWriter = new StringNbtWriter();
        stringNbtWriter.visitList(this);
        return stringNbtWriter.getString();
    }

    @Override
    public NbtElement method_10536(int i) {
        return this.value.remove(i);
    }

    @Override
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    public Optional<NbtCompound> getCompound(int index) {
        NbtElement nbtElement = this.getNullable(index);
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)nbtElement;
            return Optional.of(nbtCompound);
        }
        return Optional.empty();
    }

    public NbtCompound getCompoundOrEmpty(int index) {
        return this.getCompound(index).orElseGet(NbtCompound::new);
    }

    public Optional<NbtList> getList(int index) {
        NbtElement nbtElement = this.getNullable(index);
        if (nbtElement instanceof NbtList) {
            NbtList nbtList = (NbtList)nbtElement;
            return Optional.of(nbtList);
        }
        return Optional.empty();
    }

    public NbtList getListOrEmpty(int index) {
        return this.getList(index).orElseGet(NbtList::new);
    }

    public Optional<Short> getShort(int index) {
        return this.getOptional(index).flatMap(NbtElement::asShort);
    }

    public short getShort(int index, short fallback) {
        NbtElement nbtElement = this.getNullable(index);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)nbtElement;
            return abstractNbtNumber.shortValue();
        }
        return fallback;
    }

    public Optional<Integer> getInt(int index) {
        return this.getOptional(index).flatMap(NbtElement::asInt);
    }

    public int getInt(int index, int fallback) {
        NbtElement nbtElement = this.getNullable(index);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)nbtElement;
            return abstractNbtNumber.intValue();
        }
        return fallback;
    }

    public Optional<int[]> getIntArray(int index) {
        NbtElement nbtElement = this.getNullable(index);
        if (nbtElement instanceof NbtIntArray) {
            NbtIntArray nbtIntArray = (NbtIntArray)nbtElement;
            return Optional.of(nbtIntArray.getIntArray());
        }
        return Optional.empty();
    }

    public Optional<long[]> getLongArray(int index) {
        NbtElement nbtElement = this.getNullable(index);
        if (nbtElement instanceof NbtLongArray) {
            NbtLongArray nbtLongArray = (NbtLongArray)nbtElement;
            return Optional.of(nbtLongArray.getLongArray());
        }
        return Optional.empty();
    }

    public Optional<Double> getDouble(int index) {
        return this.getOptional(index).flatMap(NbtElement::asDouble);
    }

    public double getDouble(int index, double fallback) {
        NbtElement nbtElement = this.getNullable(index);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)nbtElement;
            return abstractNbtNumber.doubleValue();
        }
        return fallback;
    }

    public Optional<Float> getFloat(int index) {
        return this.getOptional(index).flatMap(NbtElement::asFloat);
    }

    public float getFloat(int index, float fallback) {
        NbtElement nbtElement = this.getNullable(index);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)nbtElement;
            return abstractNbtNumber.floatValue();
        }
        return fallback;
    }

    public Optional<String> getString(int index) {
        return this.getOptional(index).flatMap(NbtElement::asString);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getString(int index, String fallback) {
        NbtElement nbtElement = this.getNullable(index);
        if (!(nbtElement instanceof NbtString)) return fallback;
        NbtString nbtString = (NbtString)nbtElement;
        try {
            String string = nbtString.value();
            return string;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    private @Nullable NbtElement getNullable(int index) {
        return index >= 0 && index < this.value.size() ? this.value.get(index) : null;
    }

    private Optional<NbtElement> getOptional(int index) {
        return Optional.ofNullable(this.getNullable(index));
    }

    @Override
    public int size() {
        return this.value.size();
    }

    @Override
    public NbtElement method_10534(int i) {
        return this.value.get(i);
    }

    @Override
    public NbtElement set(int i, NbtElement nbtElement) {
        return this.value.set(i, nbtElement);
    }

    @Override
    public void add(int i, NbtElement nbtElement) {
        this.value.add(i, nbtElement);
    }

    @Override
    public boolean setElement(int index, NbtElement element) {
        this.value.set(index, element);
        return true;
    }

    @Override
    public boolean addElement(int index, NbtElement element) {
        this.value.add(index, element);
        return true;
    }

    @Override
    public NbtList copy() {
        ArrayList<NbtElement> list = new ArrayList<NbtElement>(this.value.size());
        for (NbtElement nbtElement : this.value) {
            list.add(nbtElement.copy());
        }
        return new NbtList(list);
    }

    @Override
    public Optional<NbtList> asNbtList() {
        return Optional.of(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtList && Objects.equals(this.value, ((NbtList)o).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public Stream<NbtElement> stream() {
        return super.stream();
    }

    public Stream<NbtCompound> streamCompounds() {
        return this.stream().mapMulti((nbt, callback) -> {
            if (nbt instanceof NbtCompound) {
                NbtCompound nbtCompound = (NbtCompound)nbt;
                callback.accept(nbtCompound);
            }
        });
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitList(this);
    }

    @Override
    public void clear() {
        this.value.clear();
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        byte b = this.getValueType();
        switch (visitor.visitListMeta(NbtTypes.byId(b), this.value.size())) {
            case HALT: {
                return NbtScanner.Result.HALT;
            }
            case BREAK: {
                return visitor.endNested();
            }
        }
        block13: for (int i = 0; i < this.value.size(); ++i) {
            NbtElement nbtElement = NbtList.wrapIfNeeded(b, this.value.get(i));
            switch (visitor.startListItem(nbtElement.getNbtType(), i)) {
                case HALT: {
                    return NbtScanner.Result.HALT;
                }
                case SKIP: {
                    continue block13;
                }
                case BREAK: {
                    return visitor.endNested();
                }
                default: {
                    switch (nbtElement.doAccept(visitor)) {
                        case HALT: {
                            return NbtScanner.Result.HALT;
                        }
                        case BREAK: {
                            return visitor.endNested();
                        }
                    }
                }
            }
        }
        return visitor.endNested();
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.method_10536(i);
    }

    @Override
    public /* synthetic */ void add(int index, Object element) {
        this.add(index, (NbtElement)element);
    }

    @Override
    public /* synthetic */ Object set(int index, Object element) {
        return this.set(index, (NbtElement)element);
    }

    @Override
    public /* synthetic */ Object get(int index) {
        return this.method_10534(index);
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }
}

