/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.Streams
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.DataResult$Success
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.storage;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadContext;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import org.jspecify.annotations.Nullable;

public class NbtReadView
implements ReadView {
    private final ErrorReporter reporter;
    private final ReadContext context;
    private final NbtCompound nbt;

    private NbtReadView(ErrorReporter reporter, ReadContext context, NbtCompound nbt) {
        this.reporter = reporter;
        this.context = context;
        this.nbt = nbt;
    }

    public static ReadView create(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, NbtCompound nbt) {
        return new NbtReadView(reporter, new ReadContext(registries, NbtOps.INSTANCE), nbt);
    }

    public static ReadView.ListReadView createList(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, List<NbtCompound> elements) {
        return new NbtListReadView(reporter, new ReadContext(registries, NbtOps.INSTANCE), elements);
    }

    @Override
    public <T> Optional<T> read(String key, Codec<T> codec) {
        NbtElement nbtElement = this.nbt.get(key);
        if (nbtElement == null) {
            return Optional.empty();
        }
        DataResult dataResult = codec.parse(this.context.getOps(), (Object)nbtElement);
        Objects.requireNonNull(dataResult);
        DataResult dataResult2 = dataResult;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                DataResult.Success success = (DataResult.Success)dataResult2;
                yield Optional.of(success.value());
            }
            case 1 -> {
                DataResult.Error error = (DataResult.Error)dataResult2;
                this.reporter.report(new DecodeError(key, nbtElement, error));
                yield error.partialValue();
            }
        };
    }

    @Override
    public <T> Optional<T> read(MapCodec<T> mapCodec) {
        DynamicOps<NbtElement> dynamicOps = this.context.getOps();
        DataResult dataResult = dynamicOps.getMap((Object)this.nbt).flatMap(map -> mapCodec.decode(dynamicOps, map));
        Objects.requireNonNull(dataResult);
        DataResult dataResult2 = dataResult;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                DataResult.Success success = (DataResult.Success)dataResult2;
                yield Optional.of(success.value());
            }
            case 1 -> {
                DataResult.Error error = (DataResult.Error)dataResult2;
                this.reporter.report(new DecodeMapError(error));
                yield error.partialValue();
            }
        };
    }

    private <T extends NbtElement> @Nullable T get(String key, NbtType<T> type) {
        NbtElement nbtElement = this.nbt.get(key);
        if (nbtElement == null) {
            return null;
        }
        NbtType<?> nbtType = nbtElement.getNbtType();
        if (nbtType != type) {
            this.reporter.report(new ExpectedTypeError(key, type, nbtType));
            return null;
        }
        return (T)nbtElement;
    }

    private @Nullable AbstractNbtNumber get(String key) {
        NbtElement nbtElement = this.nbt.get(key);
        if (nbtElement == null) {
            return null;
        }
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)nbtElement;
            return abstractNbtNumber;
        }
        this.reporter.report(new ExpectedNumberError(key, nbtElement.getNbtType()));
        return null;
    }

    @Override
    public Optional<ReadView> getOptionalReadView(String key) {
        NbtCompound nbtCompound = this.get(key, NbtCompound.TYPE);
        return nbtCompound != null ? Optional.of(this.createChildReadView(key, nbtCompound)) : Optional.empty();
    }

    @Override
    public ReadView getReadView(String key) {
        NbtCompound nbtCompound = this.get(key, NbtCompound.TYPE);
        return nbtCompound != null ? this.createChildReadView(key, nbtCompound) : this.context.getEmptyReadView();
    }

    @Override
    public Optional<ReadView.ListReadView> getOptionalListReadView(String key) {
        NbtList nbtList = this.get(key, NbtList.TYPE);
        return nbtList != null ? Optional.of(this.createChildListReadView(key, this.context, nbtList)) : Optional.empty();
    }

    @Override
    public ReadView.ListReadView getListReadView(String key) {
        NbtList nbtList = this.get(key, NbtList.TYPE);
        return nbtList != null ? this.createChildListReadView(key, this.context, nbtList) : this.context.getEmptyListReadView();
    }

    @Override
    public <T> Optional<ReadView.TypedListReadView<T>> getOptionalTypedListView(String key, Codec<T> typeCodec) {
        NbtList nbtList = this.get(key, NbtList.TYPE);
        return nbtList != null ? Optional.of(this.createTypedListReadView(key, nbtList, typeCodec)) : Optional.empty();
    }

    @Override
    public <T> ReadView.TypedListReadView<T> getTypedListView(String key, Codec<T> typeCodec) {
        NbtList nbtList = this.get(key, NbtList.TYPE);
        return nbtList != null ? this.createTypedListReadView(key, nbtList, typeCodec) : this.context.getEmptyTypedListReadView();
    }

    @Override
    public boolean getBoolean(String key, boolean fallback) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? abstractNbtNumber.byteValue() != 0 : fallback;
    }

    @Override
    public byte getByte(String key, byte fallback) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? abstractNbtNumber.byteValue() : fallback;
    }

    @Override
    public int getShort(String key, short fallback) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? abstractNbtNumber.shortValue() : fallback;
    }

    @Override
    public Optional<Integer> getOptionalInt(String key) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? Optional.of(abstractNbtNumber.intValue()) : Optional.empty();
    }

    @Override
    public int getInt(String key, int fallback) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? abstractNbtNumber.intValue() : fallback;
    }

    @Override
    public long getLong(String key, long fallback) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? abstractNbtNumber.longValue() : fallback;
    }

    @Override
    public Optional<Long> getOptionalLong(String key) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? Optional.of(abstractNbtNumber.longValue()) : Optional.empty();
    }

    @Override
    public float getFloat(String key, float fallback) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? abstractNbtNumber.floatValue() : fallback;
    }

    @Override
    public double getDouble(String key, double fallback) {
        AbstractNbtNumber abstractNbtNumber = this.get(key);
        return abstractNbtNumber != null ? abstractNbtNumber.doubleValue() : fallback;
    }

    @Override
    public Optional<String> getOptionalString(String key) {
        NbtString nbtString = this.get(key, NbtString.TYPE);
        return nbtString != null ? Optional.of(nbtString.value()) : Optional.empty();
    }

    @Override
    public String getString(String key, String fallback) {
        NbtString nbtString = this.get(key, NbtString.TYPE);
        return nbtString != null ? nbtString.value() : fallback;
    }

    @Override
    public Optional<int[]> getOptionalIntArray(String key) {
        NbtIntArray nbtIntArray = this.get(key, NbtIntArray.TYPE);
        return nbtIntArray != null ? Optional.of(nbtIntArray.getIntArray()) : Optional.empty();
    }

    @Override
    public RegistryWrapper.WrapperLookup getRegistries() {
        return this.context.getRegistries();
    }

    private ReadView createChildReadView(String key, NbtCompound nbt) {
        return nbt.isEmpty() ? this.context.getEmptyReadView() : new NbtReadView(this.reporter.makeChild(new ErrorReporter.MapElementContext(key)), this.context, nbt);
    }

    static ReadView createReadView(ErrorReporter reporter, ReadContext context, NbtCompound nbt) {
        return nbt.isEmpty() ? context.getEmptyReadView() : new NbtReadView(reporter, context, nbt);
    }

    private ReadView.ListReadView createChildListReadView(String key, ReadContext context, NbtList list) {
        return list.isEmpty() ? context.getEmptyListReadView() : new ChildListReadView(this.reporter, key, context, list);
    }

    private <T> ReadView.TypedListReadView<T> createTypedListReadView(String key, NbtList list, Codec<T> typeCodec) {
        return list.isEmpty() ? this.context.getEmptyTypedListReadView() : new NbtTypedListReadView<T>(this.reporter, key, this.context, typeCodec, list);
    }

    static class NbtListReadView
    implements ReadView.ListReadView {
        private final ErrorReporter reporter;
        private final ReadContext context;
        private final List<NbtCompound> nbts;

        public NbtListReadView(ErrorReporter reporter, ReadContext context, List<NbtCompound> nbts) {
            this.reporter = reporter;
            this.context = context;
            this.nbts = nbts;
        }

        ReadView createReadView(int index, NbtCompound nbt) {
            return NbtReadView.createReadView(this.reporter.makeChild(new ErrorReporter.ListElementContext(index)), this.context, nbt);
        }

        @Override
        public boolean isEmpty() {
            return this.nbts.isEmpty();
        }

        @Override
        public Stream<ReadView> stream() {
            return Streams.mapWithIndex(this.nbts.stream(), (nbt, index) -> this.createReadView((int)index, (NbtCompound)nbt));
        }

        @Override
        public Iterator<ReadView> iterator() {
            final ListIterator<NbtCompound> listIterator = this.nbts.listIterator();
            return new AbstractIterator<ReadView>(){

                protected @Nullable ReadView computeNext() {
                    if (listIterator.hasNext()) {
                        int i = listIterator.nextIndex();
                        NbtCompound nbtCompound = (NbtCompound)listIterator.next();
                        return this.createReadView(i, nbtCompound);
                    }
                    return (ReadView)this.endOfData();
                }

                protected /* synthetic */ @Nullable Object computeNext() {
                    return this.computeNext();
                }
            };
        }
    }

    public record DecodeError(String name, NbtElement element, DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to decode value '" + String.valueOf(this.element) + "' from field '" + this.name + "': " + this.error.message();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DecodeError.class, "name;tag;error", "name", "element", "error"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DecodeError.class, "name;tag;error", "name", "element", "error"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DecodeError.class, "name;tag;error", "name", "element", "error"}, this, object);
        }
    }

    public record DecodeMapError(DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to decode from map: " + this.error.message();
        }
    }

    public record ExpectedTypeError(String name, NbtType<?> expected, NbtType<?> actual) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Expected field '" + this.name + "' to contain value of type " + this.expected.getCrashReportName() + ", but got " + this.actual.getCrashReportName();
        }
    }

    public record ExpectedNumberError(String name, NbtType<?> actual) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Expected field '" + this.name + "' to contain number, but got " + this.actual.getCrashReportName();
        }
    }

    static class ChildListReadView
    implements ReadView.ListReadView {
        private final ErrorReporter reporter;
        private final String name;
        final ReadContext context;
        private final NbtList list;

        ChildListReadView(ErrorReporter reporter, String name, ReadContext context, NbtList list) {
            this.reporter = reporter;
            this.name = name;
            this.context = context;
            this.list = list;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        ErrorReporter createErrorReporter(int index) {
            return this.reporter.makeChild(new ErrorReporter.NamedListElementContext(this.name, index));
        }

        void reportExpectedTypeAtIndexError(int index, NbtElement element) {
            this.reporter.report(new ExpectedTypeAtIndexError(this.name, index, NbtCompound.TYPE, element.getNbtType()));
        }

        @Override
        public Stream<ReadView> stream() {
            return Streams.mapWithIndex(this.list.stream(), (element, index) -> {
                if (element instanceof NbtCompound) {
                    NbtCompound nbtCompound = (NbtCompound)element;
                    return NbtReadView.createReadView(this.createErrorReporter((int)index), this.context, nbtCompound);
                }
                this.reportExpectedTypeAtIndexError((int)index, (NbtElement)element);
                return null;
            }).filter(Objects::nonNull);
        }

        @Override
        public Iterator<ReadView> iterator() {
            final Iterator iterator = this.list.iterator();
            return new AbstractIterator<ReadView>(){
                private int index;

                protected @Nullable ReadView computeNext() {
                    while (iterator.hasNext()) {
                        int i;
                        NbtElement nbtElement = (NbtElement)iterator.next();
                        ++this.index;
                        if (nbtElement instanceof NbtCompound) {
                            NbtCompound nbtCompound = (NbtCompound)nbtElement;
                            return NbtReadView.createReadView(this.createErrorReporter(i), context, nbtCompound);
                        }
                        this.reportExpectedTypeAtIndexError(i, nbtElement);
                    }
                    return (ReadView)this.endOfData();
                }

                protected /* synthetic */ @Nullable Object computeNext() {
                    return this.computeNext();
                }
            };
        }
    }

    static class NbtTypedListReadView<T>
    implements ReadView.TypedListReadView<T> {
        private final ErrorReporter reporter;
        private final String name;
        final ReadContext context;
        final Codec<T> typeCodec;
        private final NbtList list;

        NbtTypedListReadView(ErrorReporter reporter, String name, ReadContext context, Codec<T> typeCodec, NbtList list) {
            this.reporter = reporter;
            this.name = name;
            this.context = context;
            this.typeCodec = typeCodec;
            this.list = list;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        void reportDecodeAtIndexError(int index, NbtElement element, DataResult.Error<?> error) {
            this.reporter.report(new DecodeAtIndexError(this.name, index, element, error));
        }

        @Override
        public Stream<T> stream() {
            return Streams.mapWithIndex(this.list.stream(), (element, index) -> {
                DataResult dataResult = this.typeCodec.parse(this.context.getOps(), element);
                Objects.requireNonNull(dataResult);
                DataResult dataResult2 = dataResult;
                int i = 0;
                return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, i)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        DataResult.Success success = (DataResult.Success)dataResult2;
                        yield success.value();
                    }
                    case 1 -> {
                        DataResult.Error error = (DataResult.Error)dataResult2;
                        this.reportDecodeAtIndexError((int)index, (NbtElement)element, (DataResult.Error<?>)error);
                        yield error.partialValue().orElse(null);
                    }
                };
            }).filter(Objects::nonNull);
        }

        @Override
        public Iterator<T> iterator() {
            final ListIterator listIterator = this.list.listIterator();
            return new AbstractIterator<T>(){

                protected @Nullable T computeNext() {
                    while (listIterator.hasNext()) {
                        DataResult dataResult;
                        int i = listIterator.nextIndex();
                        NbtElement nbtElement = (NbtElement)listIterator.next();
                        Objects.requireNonNull(typeCodec.parse(context.getOps(), (Object)nbtElement));
                        int n = 0;
                        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult, n)) {
                            default: {
                                throw new MatchException(null, null);
                            }
                            case 0: {
                                DataResult.Success success = (DataResult.Success)dataResult;
                                return success.value();
                            }
                            case 1: 
                        }
                        DataResult.Error error = (DataResult.Error)dataResult;
                        this.reportDecodeAtIndexError(i, nbtElement, error);
                        if (!error.partialValue().isPresent()) continue;
                        return error.partialValue().get();
                    }
                    return this.endOfData();
                }
            };
        }
    }

    public record ExpectedTypeAtIndexError(String name, int index, NbtType<?> expected, NbtType<?> actual) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Expected list '" + this.name + "' to contain at index " + this.index + " value of type " + this.expected.getCrashReportName() + ", but got " + this.actual.getCrashReportName();
        }
    }

    public record DecodeAtIndexError(String name, int index, NbtElement element, DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to decode value '" + String.valueOf(this.element) + "' from field '" + this.name + "' at index " + this.index + "': " + this.error.message();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DecodeAtIndexError.class, "name;index;tag;error", "name", "index", "element", "error"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DecodeAtIndexError.class, "name;index;tag;error", "name", "index", "element", "error"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DecodeAtIndexError.class, "name;index;tag;error", "name", "index", "element", "error"}, this, object);
        }
    }
}

