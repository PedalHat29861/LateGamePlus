/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.ListBuilder
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.RecordBuilder$AbstractUniversalBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.dynamic;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.Unit;
import net.minecraft.util.dynamic.AbstractListBuilder;
import org.jspecify.annotations.Nullable;

public class NullOps
implements DynamicOps<Unit> {
    public static final NullOps INSTANCE = new NullOps();
    private static final MapLike<Unit> field_61219 = new MapLike<Unit>(){

        public @Nullable Unit get(Unit unit) {
            return null;
        }

        public @Nullable Unit get(String string) {
            return null;
        }

        public Stream<Pair<Unit, Unit>> entries() {
            return Stream.empty();
        }

        public /* synthetic */ @Nullable Object get(String string) {
            return this.get(string);
        }

        public /* synthetic */ @Nullable Object get(Object object) {
            return this.get((Unit)((Object)object));
        }
    };

    private NullOps() {
    }

    public <U> U convertTo(DynamicOps<U> dynamicOps, Unit unit) {
        return (U)dynamicOps.empty();
    }

    public Unit empty() {
        return Unit.INSTANCE;
    }

    public Unit emptyMap() {
        return Unit.INSTANCE;
    }

    public Unit emptyList() {
        return Unit.INSTANCE;
    }

    public Unit createNumeric(Number number) {
        return Unit.INSTANCE;
    }

    public Unit createByte(byte b) {
        return Unit.INSTANCE;
    }

    public Unit createShort(short s) {
        return Unit.INSTANCE;
    }

    public Unit createInt(int i) {
        return Unit.INSTANCE;
    }

    public Unit createLong(long l) {
        return Unit.INSTANCE;
    }

    public Unit createFloat(float f) {
        return Unit.INSTANCE;
    }

    public Unit createDouble(double d) {
        return Unit.INSTANCE;
    }

    public Unit createBoolean(boolean bl) {
        return Unit.INSTANCE;
    }

    public Unit createString(String string) {
        return Unit.INSTANCE;
    }

    public DataResult<Number> getNumberValue(Unit unit) {
        return DataResult.success((Object)0);
    }

    public DataResult<Boolean> getBooleanValue(Unit unit) {
        return DataResult.success((Object)false);
    }

    public DataResult<String> getStringValue(Unit unit) {
        return DataResult.success((Object)"");
    }

    public DataResult<Unit> mergeToList(Unit unit, Unit unit2) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToList(Unit unit, List<Unit> list) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit unit, Unit unit2, Unit unit3) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit unit, Map<Unit, Unit> map) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit unit, MapLike<Unit> mapLike) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Stream<Pair<Unit, Unit>>> getMapValues(Unit unit) {
        return DataResult.success(Stream.empty());
    }

    public DataResult<Consumer<BiConsumer<Unit, Unit>>> getMapEntries(Unit unit) {
        return DataResult.success(biConsumer -> {});
    }

    public DataResult<MapLike<Unit>> getMap(Unit unit) {
        return DataResult.success(field_61219);
    }

    public DataResult<Stream<Unit>> getStream(Unit unit) {
        return DataResult.success(Stream.empty());
    }

    public DataResult<Consumer<Consumer<Unit>>> getList(Unit unit) {
        return DataResult.success(consumer -> {});
    }

    public DataResult<ByteBuffer> getByteBuffer(Unit unit) {
        return DataResult.success((Object)ByteBuffer.wrap(new byte[0]));
    }

    public DataResult<IntStream> getIntStream(Unit unit) {
        return DataResult.success((Object)IntStream.empty());
    }

    public DataResult<LongStream> getLongStream(Unit unit) {
        return DataResult.success((Object)LongStream.empty());
    }

    public Unit createMap(Stream<Pair<Unit, Unit>> stream) {
        return Unit.INSTANCE;
    }

    public Unit createMap(Map<Unit, Unit> map) {
        return Unit.INSTANCE;
    }

    public Unit createList(Stream<Unit> stream) {
        return Unit.INSTANCE;
    }

    public Unit createByteList(ByteBuffer byteBuffer) {
        return Unit.INSTANCE;
    }

    public Unit createIntList(IntStream intStream) {
        return Unit.INSTANCE;
    }

    public Unit createLongList(LongStream longStream) {
        return Unit.INSTANCE;
    }

    public Unit remove(Unit unit, String string) {
        return unit;
    }

    public RecordBuilder<Unit> mapBuilder() {
        return new NullMapBuilder(this);
    }

    public ListBuilder<Unit> listBuilder() {
        return new NullListBuilder(this);
    }

    public String toString() {
        return "Null";
    }

    public /* synthetic */ Object remove(Object input, String key) {
        return this.remove((Unit)((Object)input), key);
    }

    public /* synthetic */ Object createLongList(LongStream stream) {
        return this.createLongList(stream);
    }

    public /* synthetic */ DataResult getLongStream(Object input) {
        return this.getLongStream((Unit)((Object)input));
    }

    public /* synthetic */ Object createIntList(IntStream stream) {
        return this.createIntList(stream);
    }

    public /* synthetic */ DataResult getIntStream(Object input) {
        return this.getIntStream((Unit)((Object)input));
    }

    public /* synthetic */ Object createByteList(ByteBuffer buf) {
        return this.createByteList(buf);
    }

    public /* synthetic */ DataResult getByteBuffer(Object input) {
        return this.getByteBuffer((Unit)((Object)input));
    }

    public /* synthetic */ Object createList(Stream list) {
        return this.createList((Stream<Unit>)list);
    }

    public /* synthetic */ DataResult getList(Object input) {
        return this.getList((Unit)((Object)input));
    }

    public /* synthetic */ DataResult getStream(Object input) {
        return this.getStream((Unit)((Object)input));
    }

    public /* synthetic */ Object createMap(Map map) {
        return this.createMap((Map<Unit, Unit>)map);
    }

    public /* synthetic */ DataResult getMap(Object input) {
        return this.getMap((Unit)((Object)input));
    }

    public /* synthetic */ Object createMap(Stream map) {
        return this.createMap((Stream<Pair<Unit, Unit>>)map);
    }

    public /* synthetic */ DataResult getMapEntries(Object input) {
        return this.getMapEntries((Unit)((Object)input));
    }

    public /* synthetic */ DataResult getMapValues(Object input) {
        return this.getMapValues((Unit)((Object)input));
    }

    public /* synthetic */ DataResult mergeToMap(Object map, MapLike values) {
        return this.mergeToMap((Unit)((Object)map), (MapLike<Unit>)values);
    }

    public /* synthetic */ DataResult mergeToMap(Object map, Map values) {
        return this.mergeToMap((Unit)((Object)map), (Map<Unit, Unit>)values);
    }

    public /* synthetic */ DataResult mergeToMap(Object map, Object key, Object value) {
        return this.mergeToMap((Unit)((Object)map), (Unit)((Object)key), (Unit)((Object)value));
    }

    public /* synthetic */ DataResult mergeToList(Object list, List values) {
        return this.mergeToList((Unit)((Object)list), (List<Unit>)values);
    }

    public /* synthetic */ DataResult mergeToList(Object list, Object value) {
        return this.mergeToList((Unit)((Object)list), (Unit)((Object)value));
    }

    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    public /* synthetic */ DataResult getStringValue(Object input) {
        return this.getStringValue((Unit)((Object)input));
    }

    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
    }

    public /* synthetic */ DataResult getBooleanValue(Object input) {
        return this.getBooleanValue((Unit)((Object)input));
    }

    public /* synthetic */ Object createDouble(double d) {
        return this.createDouble(d);
    }

    public /* synthetic */ Object createFloat(float f) {
        return this.createFloat(f);
    }

    public /* synthetic */ Object createLong(long l) {
        return this.createLong(l);
    }

    public /* synthetic */ Object createInt(int i) {
        return this.createInt(i);
    }

    public /* synthetic */ Object createShort(short s) {
        return this.createShort(s);
    }

    public /* synthetic */ Object createByte(byte b) {
        return this.createByte(b);
    }

    public /* synthetic */ Object createNumeric(Number number) {
        return this.createNumeric(number);
    }

    public /* synthetic */ DataResult getNumberValue(Object input) {
        return this.getNumberValue((Unit)((Object)input));
    }

    public /* synthetic */ Object convertTo(DynamicOps ops, Object unit) {
        return this.convertTo(ops, (Unit)((Object)unit));
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

    static final class NullMapBuilder
    extends RecordBuilder.AbstractUniversalBuilder<Unit, Unit> {
        public NullMapBuilder(DynamicOps<Unit> ops) {
            super(ops);
        }

        protected Unit initBuilder() {
            return Unit.INSTANCE;
        }

        protected Unit append(Unit unit, Unit unit2, Unit unit3) {
            return unit3;
        }

        protected DataResult<Unit> build(Unit unit, Unit unit2) {
            return DataResult.success((Object)((Object)unit2));
        }

        protected /* synthetic */ Object append(Object key, Object value, Object builder) {
            return this.append((Unit)((Object)key), (Unit)((Object)value), (Unit)((Object)builder));
        }

        protected /* synthetic */ DataResult build(Object builder, Object prefix) {
            return this.build((Unit)((Object)builder), (Unit)((Object)prefix));
        }

        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    static final class NullListBuilder
    extends AbstractListBuilder<Unit, Unit> {
        public NullListBuilder(DynamicOps<Unit> dynamicOps) {
            super(dynamicOps);
        }

        @Override
        protected Unit initBuilder() {
            return Unit.INSTANCE;
        }

        @Override
        protected Unit add(Unit unit, Unit unit2) {
            return unit;
        }

        @Override
        protected DataResult<Unit> build(Unit unit, Unit unit2) {
            return DataResult.success((Object)((Object)unit));
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }
}

