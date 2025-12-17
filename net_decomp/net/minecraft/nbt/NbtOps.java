/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.RecordBuilder$AbstractStringBuilder
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtList;
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
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class NbtOps
implements DynamicOps<NbtElement> {
    public static final NbtOps INSTANCE = new NbtOps();

    private NbtOps() {
    }

    public NbtElement empty() {
        return NbtEnd.INSTANCE;
    }

    public NbtElement emptyList() {
        return new NbtList();
    }

    public NbtElement emptyMap() {
        return new NbtCompound();
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public <U> U convertTo(DynamicOps<U> dynamicOps, NbtElement nbtElement) {
        Object object;
        NbtElement nbtElement2 = nbtElement;
        Objects.requireNonNull(nbtElement2);
        NbtElement nbtElement3 = nbtElement2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{NbtEnd.class, NbtByte.class, NbtShort.class, NbtInt.class, NbtLong.class, NbtFloat.class, NbtDouble.class, NbtByteArray.class, NbtString.class, NbtList.class, NbtCompound.class, NbtIntArray.class, NbtLongArray.class}, (Object)nbtElement3, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                NbtEnd nbtEnd = (NbtEnd)nbtElement3;
                object = dynamicOps.empty();
                return (U)object;
            }
            case 1: {
                byte b;
                NbtByte nbtByte = (NbtByte)nbtElement3;
                try {
                    byte by;
                    b = by = nbtByte.value();
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
                object = dynamicOps.createByte(b);
                return (U)object;
            }
            case 2: {
                short s2;
                NbtShort nbtShort = (NbtShort)nbtElement3;
                {
                    short s;
                    s2 = s = nbtShort.value();
                }
                object = dynamicOps.createShort(s2);
                return (U)object;
            }
            case 3: {
                int i;
                NbtInt nbtInt = (NbtInt)nbtElement3;
                {
                    int n2;
                    i = n2 = nbtInt.value();
                }
                object = dynamicOps.createInt(i);
                return (U)object;
            }
            case 4: {
                long l2;
                NbtLong nbtLong = (NbtLong)nbtElement3;
                {
                    long l;
                    l2 = l = nbtLong.value();
                }
                object = dynamicOps.createLong(l2);
                return (U)object;
            }
            case 5: {
                float f2;
                NbtFloat nbtFloat = (NbtFloat)nbtElement3;
                {
                    float f;
                    f2 = f = nbtFloat.value();
                }
                object = dynamicOps.createFloat(f2);
                return (U)object;
            }
            case 6: {
                double d2;
                NbtDouble nbtDouble = (NbtDouble)nbtElement3;
                {
                    double d;
                    d2 = d = nbtDouble.value();
                }
                object = dynamicOps.createDouble(d2);
                return (U)object;
            }
            case 7: {
                NbtByteArray nbtByteArray = (NbtByteArray)nbtElement3;
                object = dynamicOps.createByteList(ByteBuffer.wrap(nbtByteArray.getByteArray()));
                return (U)object;
            }
            case 8: {
                String string2;
                NbtString nbtString = (NbtString)nbtElement3;
                {
                    String string;
                    string2 = string = nbtString.value();
                }
                object = dynamicOps.createString(string2);
                return (U)object;
            }
            case 9: {
                NbtList nbtList = (NbtList)nbtElement3;
                object = this.convertList(dynamicOps, nbtList);
                return (U)object;
            }
            case 10: {
                NbtCompound nbtCompound = (NbtCompound)nbtElement3;
                object = this.convertMap(dynamicOps, nbtCompound);
                return (U)object;
            }
            case 11: {
                NbtIntArray nbtIntArray = (NbtIntArray)nbtElement3;
                object = dynamicOps.createIntList(Arrays.stream(nbtIntArray.getIntArray()));
                return (U)object;
            }
            case 12: 
        }
        NbtLongArray nbtLongArray = (NbtLongArray)nbtElement3;
        object = dynamicOps.createLongList(Arrays.stream(nbtLongArray.getLongArray()));
        return (U)object;
    }

    public DataResult<Number> getNumberValue(NbtElement nbtElement) {
        return nbtElement.asNumber().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Not a number"));
    }

    public NbtElement createNumeric(Number number) {
        return NbtDouble.of(number.doubleValue());
    }

    public NbtElement createByte(byte b) {
        return NbtByte.of(b);
    }

    public NbtElement createShort(short s) {
        return NbtShort.of(s);
    }

    public NbtElement createInt(int i) {
        return NbtInt.of(i);
    }

    public NbtElement createLong(long l) {
        return NbtLong.of(l);
    }

    public NbtElement createFloat(float f) {
        return NbtFloat.of(f);
    }

    public NbtElement createDouble(double d) {
        return NbtDouble.of(d);
    }

    public NbtElement createBoolean(boolean bl) {
        return NbtByte.of(bl);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public DataResult<String> getStringValue(NbtElement nbtElement) {
        String string2;
        if (!(nbtElement instanceof NbtString)) return DataResult.error(() -> "Not a string");
        NbtString nbtString = (NbtString)nbtElement;
        try {
            String string;
            string2 = string = nbtString.value();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        return DataResult.success((Object)string2);
    }

    public NbtElement createString(String string) {
        return NbtString.of(string);
    }

    public DataResult<NbtElement> mergeToList(NbtElement nbtElement, NbtElement nbtElement2) {
        return NbtOps.createMerger(nbtElement).map(merger -> DataResult.success((Object)merger.merge(nbtElement2).getResult())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(nbtElement), (Object)nbtElement));
    }

    public DataResult<NbtElement> mergeToList(NbtElement nbtElement, List<NbtElement> list) {
        return NbtOps.createMerger(nbtElement).map(merger -> DataResult.success((Object)merger.merge(list).getResult())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(nbtElement), (Object)nbtElement));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, NbtElement nbtElement2, NbtElement nbtElement3) {
        NbtCompound nbtCompound;
        String string;
        if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(nbtElement), (Object)nbtElement);
        }
        if (!(nbtElement2 instanceof NbtString)) return DataResult.error(() -> "key is not a string: " + String.valueOf(nbtElement2), (Object)nbtElement);
        NbtString nbtString = (NbtString)nbtElement2;
        try {
            String string2;
            string = string2 = nbtString.value();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
            nbtCompound = nbtCompound2.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound nbtCompound2 = nbtCompound;
        nbtCompound2.put(string, nbtElement3);
        return DataResult.success((Object)nbtCompound2);
    }

    public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, MapLike<NbtElement> mapLike) {
        NbtCompound nbtCompound;
        if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(nbtElement), (Object)nbtElement);
        }
        Iterator iterator = mapLike.entries().iterator();
        if (!iterator.hasNext()) {
            if (nbtElement == this.empty()) {
                return DataResult.success((Object)this.emptyMap());
            }
            return DataResult.success((Object)nbtElement);
        }
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
            nbtCompound = nbtCompound2.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound nbtCompound2 = nbtCompound;
        ArrayList list = new ArrayList();
        iterator.forEachRemaining(pair -> {
            String string2;
            NbtElement nbtElement = (NbtElement)pair.getFirst();
            if (!(nbtElement instanceof NbtString)) {
                list.add(nbtElement);
                return;
            }
            NbtString nbtString = (NbtString)nbtElement;
            try {
                String string;
                string2 = string = nbtString.value();
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
            nbtCompound2.put(string2, (NbtElement)pair.getSecond());
        });
        if (!list.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + String.valueOf(list), (Object)nbtCompound2);
        }
        return DataResult.success((Object)nbtCompound2);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, Map<NbtElement, NbtElement> map) {
        NbtCompound nbtCompound;
        if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(nbtElement), (Object)nbtElement);
        }
        if (map.isEmpty()) {
            if (nbtElement == this.empty()) {
                return DataResult.success((Object)this.emptyMap());
            }
            return DataResult.success((Object)nbtElement);
        }
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
            nbtCompound = nbtCompound2.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound nbtCompound2 = nbtCompound;
        ArrayList<NbtElement> list = new ArrayList<NbtElement>();
        for (Map.Entry<NbtElement, NbtElement> entry : map.entrySet()) {
            NbtElement nbtElement2 = entry.getKey();
            if (nbtElement2 instanceof NbtString) {
                NbtString nbtString = (NbtString)nbtElement2;
                try {
                    String string;
                    String string2 = string = nbtString.value();
                    nbtCompound2.put(string2, entry.getValue());
                    continue;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            list.add(nbtElement2);
        }
        if (!list.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + String.valueOf(list), (Object)nbtCompound2);
        }
        return DataResult.success((Object)nbtCompound2);
    }

    public DataResult<Stream<Pair<NbtElement, NbtElement>>> getMapValues(NbtElement nbtElement) {
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)nbtElement;
            return DataResult.success(nbtCompound.entrySet().stream().map(entry -> Pair.of((Object)this.createString((String)entry.getKey()), (Object)((NbtElement)entry.getValue()))));
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(nbtElement));
    }

    public DataResult<Consumer<BiConsumer<NbtElement, NbtElement>>> getMapEntries(NbtElement nbtElement) {
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)nbtElement;
            return DataResult.success(biConsumer -> {
                for (Map.Entry<String, NbtElement> entry : nbtCompound.entrySet()) {
                    biConsumer.accept(this.createString(entry.getKey()), entry.getValue());
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(nbtElement));
    }

    public DataResult<MapLike<NbtElement>> getMap(NbtElement nbtElement) {
        if (nbtElement instanceof NbtCompound) {
            final NbtCompound nbtCompound = (NbtCompound)nbtElement;
            return DataResult.success((Object)new MapLike<NbtElement>(){

                /*
                 * Enabled force condition propagation
                 * Lifted jumps to return sites
                 */
                public @Nullable NbtElement get(NbtElement nbtElement) {
                    if (!(nbtElement instanceof NbtString)) throw new UnsupportedOperationException("Cannot get map entry with non-string key: " + String.valueOf(nbtElement));
                    NbtString nbtString = (NbtString)nbtElement;
                    try {
                        String string;
                        String string2 = string = nbtString.value();
                        return nbtCompound.get(string2);
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                }

                public @Nullable NbtElement get(String string) {
                    return nbtCompound.get(string);
                }

                public Stream<Pair<NbtElement, NbtElement>> entries() {
                    return nbtCompound.entrySet().stream().map(entry -> Pair.of((Object)NbtOps.this.createString((String)entry.getKey()), (Object)((NbtElement)entry.getValue())));
                }

                public String toString() {
                    return "MapLike[" + String.valueOf(nbtCompound) + "]";
                }

                public /* synthetic */ @Nullable Object get(String key) {
                    return this.get(key);
                }

                public /* synthetic */ @Nullable Object get(Object nbt) {
                    return this.get((NbtElement)nbt);
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(nbtElement));
    }

    public NbtElement createMap(Stream<Pair<NbtElement, NbtElement>> stream) {
        NbtCompound nbtCompound = new NbtCompound();
        stream.forEach(entry -> {
            NbtElement nbtElement = (NbtElement)entry.getFirst();
            NbtElement nbtElement2 = (NbtElement)entry.getSecond();
            if (!(nbtElement instanceof NbtString)) throw new UnsupportedOperationException("Cannot create map with non-string key: " + String.valueOf(nbtElement));
            NbtString nbtString = (NbtString)nbtElement;
            try {
                String string;
                String string2 = string = nbtString.value();
                nbtCompound.put(string2, nbtElement2);
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
        });
        return nbtCompound;
    }

    public DataResult<Stream<NbtElement>> getStream(NbtElement nbtElement) {
        if (nbtElement instanceof AbstractNbtList) {
            AbstractNbtList abstractNbtList = (AbstractNbtList)nbtElement;
            return DataResult.success(abstractNbtList.stream());
        }
        return DataResult.error(() -> "Not a list");
    }

    public DataResult<Consumer<Consumer<NbtElement>>> getList(NbtElement nbtElement) {
        if (nbtElement instanceof AbstractNbtList) {
            AbstractNbtList abstractNbtList = (AbstractNbtList)nbtElement;
            return DataResult.success(abstractNbtList::forEach);
        }
        return DataResult.error(() -> "Not a list: " + String.valueOf(nbtElement));
    }

    public DataResult<ByteBuffer> getByteBuffer(NbtElement nbtElement) {
        if (nbtElement instanceof NbtByteArray) {
            NbtByteArray nbtByteArray = (NbtByteArray)nbtElement;
            return DataResult.success((Object)ByteBuffer.wrap(nbtByteArray.getByteArray()));
        }
        return super.getByteBuffer((Object)nbtElement);
    }

    public NbtElement createByteList(ByteBuffer byteBuffer) {
        ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
        byte[] bs = new byte[byteBuffer.capacity()];
        byteBuffer2.get(0, bs, 0, bs.length);
        return new NbtByteArray(bs);
    }

    public DataResult<IntStream> getIntStream(NbtElement nbtElement) {
        if (nbtElement instanceof NbtIntArray) {
            NbtIntArray nbtIntArray = (NbtIntArray)nbtElement;
            return DataResult.success((Object)Arrays.stream(nbtIntArray.getIntArray()));
        }
        return super.getIntStream((Object)nbtElement);
    }

    public NbtElement createIntList(IntStream intStream) {
        return new NbtIntArray(intStream.toArray());
    }

    public DataResult<LongStream> getLongStream(NbtElement nbtElement) {
        if (nbtElement instanceof NbtLongArray) {
            NbtLongArray nbtLongArray = (NbtLongArray)nbtElement;
            return DataResult.success((Object)Arrays.stream(nbtLongArray.getLongArray()));
        }
        return super.getLongStream((Object)nbtElement);
    }

    public NbtElement createLongList(LongStream longStream) {
        return new NbtLongArray(longStream.toArray());
    }

    public NbtElement createList(Stream<NbtElement> stream) {
        return new NbtList(stream.collect(Util.toArrayList()));
    }

    public NbtElement remove(NbtElement nbtElement, String string) {
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)nbtElement;
            NbtCompound nbtCompound2 = nbtCompound.shallowCopy();
            nbtCompound2.remove(string);
            return nbtCompound2;
        }
        return nbtElement;
    }

    public String toString() {
        return "NBT";
    }

    public RecordBuilder<NbtElement> mapBuilder() {
        return new MapBuilder(this);
    }

    private static Optional<Merger> createMerger(NbtElement nbt) {
        if (nbt instanceof NbtEnd) {
            return Optional.of(new CompoundListMerger());
        }
        if (nbt instanceof AbstractNbtList) {
            AbstractNbtList abstractNbtList = (AbstractNbtList)nbt;
            if (abstractNbtList.isEmpty()) {
                return Optional.of(new CompoundListMerger());
            }
            AbstractNbtList abstractNbtList2 = abstractNbtList;
            Objects.requireNonNull(abstractNbtList2);
            AbstractNbtList abstractNbtList3 = abstractNbtList2;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{NbtList.class, NbtByteArray.class, NbtIntArray.class, NbtLongArray.class}, (Object)abstractNbtList3, n)) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    NbtList nbtList = (NbtList)abstractNbtList3;
                    yield Optional.of(new CompoundListMerger(nbtList));
                }
                case 1 -> {
                    NbtByteArray nbtByteArray = (NbtByteArray)abstractNbtList3;
                    yield Optional.of(new ByteArrayMerger(nbtByteArray.getByteArray()));
                }
                case 2 -> {
                    NbtIntArray nbtIntArray = (NbtIntArray)abstractNbtList3;
                    yield Optional.of(new IntArrayMerger(nbtIntArray.getIntArray()));
                }
                case 3 -> {
                    NbtLongArray nbtLongArray = (NbtLongArray)abstractNbtList3;
                    yield Optional.of(new LongArrayMerger(nbtLongArray.getLongArray()));
                }
            };
        }
        return Optional.empty();
    }

    public /* synthetic */ Object remove(Object element, String key) {
        return this.remove((NbtElement)element, key);
    }

    public /* synthetic */ Object createLongList(LongStream stream) {
        return this.createLongList(stream);
    }

    public /* synthetic */ DataResult getLongStream(Object element) {
        return this.getLongStream((NbtElement)element);
    }

    public /* synthetic */ Object createIntList(IntStream stream) {
        return this.createIntList(stream);
    }

    public /* synthetic */ DataResult getIntStream(Object element) {
        return this.getIntStream((NbtElement)element);
    }

    public /* synthetic */ Object createByteList(ByteBuffer buf) {
        return this.createByteList(buf);
    }

    public /* synthetic */ DataResult getByteBuffer(Object element) {
        return this.getByteBuffer((NbtElement)element);
    }

    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    public /* synthetic */ DataResult getList(Object element) {
        return this.getList((NbtElement)element);
    }

    public /* synthetic */ DataResult getStream(Object element) {
        return this.getStream((NbtElement)element);
    }

    public /* synthetic */ DataResult getMap(Object element) {
        return this.getMap((NbtElement)element);
    }

    public /* synthetic */ Object createMap(Stream entries) {
        return this.createMap(entries);
    }

    public /* synthetic */ DataResult getMapEntries(Object element) {
        return this.getMapEntries((NbtElement)element);
    }

    public /* synthetic */ DataResult getMapValues(Object element) {
        return this.getMapValues((NbtElement)element);
    }

    public /* synthetic */ DataResult mergeToMap(Object element, MapLike map) {
        return this.mergeToMap((NbtElement)element, (MapLike<NbtElement>)map);
    }

    public /* synthetic */ DataResult mergeToMap(Object nbt, Map map) {
        return this.mergeToMap((NbtElement)nbt, (Map<NbtElement, NbtElement>)map);
    }

    public /* synthetic */ DataResult mergeToMap(Object map, Object key, Object value) {
        return this.mergeToMap((NbtElement)map, (NbtElement)key, (NbtElement)value);
    }

    public /* synthetic */ DataResult mergeToList(Object list, List values) {
        return this.mergeToList((NbtElement)list, (List<NbtElement>)values);
    }

    public /* synthetic */ DataResult mergeToList(Object list, Object value) {
        return this.mergeToList((NbtElement)list, (NbtElement)value);
    }

    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    public /* synthetic */ DataResult getStringValue(Object element) {
        return this.getStringValue((NbtElement)element);
    }

    public /* synthetic */ Object createBoolean(boolean value) {
        return this.createBoolean(value);
    }

    public /* synthetic */ Object createDouble(double value) {
        return this.createDouble(value);
    }

    public /* synthetic */ Object createFloat(float value) {
        return this.createFloat(value);
    }

    public /* synthetic */ Object createLong(long value) {
        return this.createLong(value);
    }

    public /* synthetic */ Object createInt(int value) {
        return this.createInt(value);
    }

    public /* synthetic */ Object createShort(short value) {
        return this.createShort(value);
    }

    public /* synthetic */ Object createByte(byte value) {
        return this.createByte(value);
    }

    public /* synthetic */ Object createNumeric(Number value) {
        return this.createNumeric(value);
    }

    public /* synthetic */ DataResult getNumberValue(Object element) {
        return this.getNumberValue((NbtElement)element);
    }

    public /* synthetic */ Object convertTo(DynamicOps ops, Object element) {
        return this.convertTo(ops, (NbtElement)element);
    }

    public /* synthetic */ Object emptyList() {
        return this.emptyList();
    }

    public /* synthetic */ Object emptyMap() {
        return this.emptyMap();
    }

    public /* synthetic */ Object empty() {
        return this.empty();
    }

    class MapBuilder
    extends RecordBuilder.AbstractStringBuilder<NbtElement, NbtCompound> {
        protected MapBuilder(NbtOps ops) {
            super((DynamicOps)ops);
        }

        protected NbtCompound initBuilder() {
            return new NbtCompound();
        }

        protected NbtCompound append(String string, NbtElement nbtElement, NbtCompound nbtCompound) {
            nbtCompound.put(string, nbtElement);
            return nbtCompound;
        }

        protected DataResult<NbtElement> build(NbtCompound nbtCompound, NbtElement nbtElement) {
            if (nbtElement == null || nbtElement == NbtEnd.INSTANCE) {
                return DataResult.success((Object)nbtCompound);
            }
            if (nbtElement instanceof NbtCompound) {
                NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
                NbtCompound nbtCompound3 = nbtCompound2.shallowCopy();
                for (Map.Entry<String, NbtElement> entry : nbtCompound.entrySet()) {
                    nbtCompound3.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success((Object)nbtCompound3);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(nbtElement), (Object)nbtElement);
        }

        protected /* synthetic */ Object append(String key, Object value, Object nbt) {
            return this.append(key, (NbtElement)value, (NbtCompound)nbt);
        }

        protected /* synthetic */ DataResult build(Object nbt, Object mergedValue) {
            return this.build((NbtCompound)nbt, (NbtElement)mergedValue);
        }

        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    static class CompoundListMerger
    implements Merger {
        private final NbtList list = new NbtList();

        CompoundListMerger() {
        }

        CompoundListMerger(NbtList nbtList) {
            this.list.addAll(nbtList);
        }

        public CompoundListMerger(IntArrayList list) {
            list.forEach(value -> this.list.add(NbtInt.of(value)));
        }

        public CompoundListMerger(ByteArrayList list) {
            list.forEach(value -> this.list.add(NbtByte.of(value)));
        }

        public CompoundListMerger(LongArrayList list) {
            list.forEach(value -> this.list.add(NbtLong.of(value)));
        }

        @Override
        public Merger merge(NbtElement nbt) {
            this.list.add(nbt);
            return this;
        }

        @Override
        public NbtElement getResult() {
            return this.list;
        }
    }

    static class ByteArrayMerger
    implements Merger {
        private final ByteArrayList list = new ByteArrayList();

        public ByteArrayMerger(byte[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtByte) {
                NbtByte nbtByte = (NbtByte)nbt;
                this.list.add(nbtByte.byteValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtByteArray(this.list.toByteArray());
        }
    }

    static class IntArrayMerger
    implements Merger {
        private final IntArrayList list = new IntArrayList();

        public IntArrayMerger(int[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtInt) {
                NbtInt nbtInt = (NbtInt)nbt;
                this.list.add(nbtInt.intValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtIntArray(this.list.toIntArray());
        }
    }

    static class LongArrayMerger
    implements Merger {
        private final LongArrayList list = new LongArrayList();

        public LongArrayMerger(long[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtLong) {
                NbtLong nbtLong = (NbtLong)nbt;
                this.list.add(nbtLong.longValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtLongArray(this.list.toLongArray());
        }
    }

    static interface Merger {
        public Merger merge(NbtElement var1);

        default public Merger merge(Iterable<NbtElement> nbts) {
            Merger merger = this;
            for (NbtElement nbtElement : nbts) {
                merger = merger.merge(nbtElement);
            }
            return merger;
        }

        default public Merger merge(Stream<NbtElement> nbts) {
            return this.merge(nbts::iterator);
        }

        public NbtElement getResult();
    }
}

