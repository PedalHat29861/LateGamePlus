/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.DataResult$Success
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import org.jspecify.annotations.Nullable;

public class NbtWriteView
implements WriteView {
    private final ErrorReporter reporter;
    private final DynamicOps<NbtElement> ops;
    private final NbtCompound nbt;

    NbtWriteView(ErrorReporter reporter, DynamicOps<NbtElement> ops, NbtCompound nbt) {
        this.reporter = reporter;
        this.ops = ops;
        this.nbt = nbt;
    }

    public static NbtWriteView create(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries) {
        return new NbtWriteView(reporter, registries.getOps(NbtOps.INSTANCE), new NbtCompound());
    }

    public static NbtWriteView create(ErrorReporter reporter) {
        return new NbtWriteView(reporter, NbtOps.INSTANCE, new NbtCompound());
    }

    @Override
    public <T> void put(String key, Codec<T> codec, T value) {
        DataResult dataResult = codec.encodeStart(this.ops, value);
        Objects.requireNonNull(dataResult);
        DataResult dataResult2 = dataResult;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                DataResult.Success success = (DataResult.Success)dataResult2;
                this.nbt.put(key, (NbtElement)success.value());
                break;
            }
            case 1: {
                DataResult.Error error = (DataResult.Error)dataResult2;
                this.reporter.report(new EncodeFieldError(key, value, error));
                error.partialValue().ifPresent(partialValue -> this.nbt.put(key, (NbtElement)partialValue));
            }
        }
    }

    @Override
    public <T> void putNullable(String key, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            this.put(key, codec, value);
        }
    }

    @Override
    public <T> void put(MapCodec<T> codec, T value) {
        DataResult dataResult = codec.encoder().encodeStart(this.ops, value);
        Objects.requireNonNull(dataResult);
        DataResult dataResult2 = dataResult;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                DataResult.Success success = (DataResult.Success)dataResult2;
                this.nbt.copyFrom((NbtCompound)success.value());
                break;
            }
            case 1: {
                DataResult.Error error = (DataResult.Error)dataResult2;
                this.reporter.report(new MergeError(value, error));
                error.partialValue().ifPresent(partialValue -> this.nbt.copyFrom((NbtCompound)partialValue));
            }
        }
    }

    @Override
    public void putBoolean(String key, boolean value) {
        this.nbt.putBoolean(key, value);
    }

    @Override
    public void putByte(String key, byte value) {
        this.nbt.putByte(key, value);
    }

    @Override
    public void putShort(String key, short value) {
        this.nbt.putShort(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        this.nbt.putInt(key, value);
    }

    @Override
    public void putLong(String key, long value) {
        this.nbt.putLong(key, value);
    }

    @Override
    public void putFloat(String key, float value) {
        this.nbt.putFloat(key, value);
    }

    @Override
    public void putDouble(String key, double value) {
        this.nbt.putDouble(key, value);
    }

    @Override
    public void putString(String key, String value) {
        this.nbt.putString(key, value);
    }

    @Override
    public void putIntArray(String key, int[] value) {
        this.nbt.putIntArray(key, value);
    }

    private ErrorReporter makeChildReporter(String key) {
        return this.reporter.makeChild(new ErrorReporter.MapElementContext(key));
    }

    @Override
    public WriteView get(String key) {
        NbtCompound nbtCompound = new NbtCompound();
        this.nbt.put(key, nbtCompound);
        return new NbtWriteView(this.makeChildReporter(key), this.ops, nbtCompound);
    }

    @Override
    public WriteView.ListView getList(String key) {
        NbtList nbtList = new NbtList();
        this.nbt.put(key, nbtList);
        return new NbtListView(key, this.reporter, this.ops, nbtList);
    }

    @Override
    public <T> WriteView.ListAppender<T> getListAppender(String key, Codec<T> codec) {
        NbtList nbtList = new NbtList();
        this.nbt.put(key, nbtList);
        return new NbtListAppender<T>(this.reporter, key, this.ops, codec, nbtList);
    }

    @Override
    public void remove(String key) {
        this.nbt.remove(key);
    }

    @Override
    public boolean isEmpty() {
        return this.nbt.isEmpty();
    }

    public NbtCompound getNbt() {
        return this.nbt;
    }

    public record EncodeFieldError(String name, Object value, DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to encode value '" + String.valueOf(this.value) + "' to field '" + this.name + "': " + this.error.message();
        }
    }

    public record MergeError(Object value, DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to merge value '" + String.valueOf(this.value) + "' to an object: " + this.error.message();
        }
    }

    static class NbtListView
    implements WriteView.ListView {
        private final String key;
        private final ErrorReporter reporter;
        private final DynamicOps<NbtElement> ops;
        private final NbtList list;

        NbtListView(String key, ErrorReporter reporter, DynamicOps<NbtElement> ops, NbtList list) {
            this.key = key;
            this.reporter = reporter;
            this.ops = ops;
            this.list = list;
        }

        @Override
        public WriteView add() {
            int i = this.list.size();
            NbtCompound nbtCompound = new NbtCompound();
            this.list.add(nbtCompound);
            return new NbtWriteView(this.reporter.makeChild(new ErrorReporter.NamedListElementContext(this.key, i)), this.ops, nbtCompound);
        }

        @Override
        public void removeLast() {
            this.list.removeLast();
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }
    }

    static class NbtListAppender<T>
    implements WriteView.ListAppender<T> {
        private final ErrorReporter reporter;
        private final String key;
        private final DynamicOps<NbtElement> ops;
        private final Codec<T> codec;
        private final NbtList list;

        NbtListAppender(ErrorReporter reporter, String key, DynamicOps<NbtElement> ops, Codec<T> codec, NbtList list) {
            this.reporter = reporter;
            this.key = key;
            this.ops = ops;
            this.codec = codec;
            this.list = list;
        }

        @Override
        public void add(T value) {
            DataResult dataResult = this.codec.encodeStart(this.ops, value);
            Objects.requireNonNull(dataResult);
            DataResult dataResult2 = dataResult;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, n)) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    DataResult.Success success = (DataResult.Success)dataResult2;
                    this.list.add((NbtElement)success.value());
                    break;
                }
                case 1: {
                    DataResult.Error error = (DataResult.Error)dataResult2;
                    this.reporter.report(new AppendToListError(this.key, value, error));
                    error.partialValue().ifPresent(this.list::add);
                }
            }
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }
    }

    public record AppendToListError(String name, Object value, DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to append value '" + String.valueOf(this.value) + "' to list '" + this.name + "': " + this.error.message();
        }
    }
}

