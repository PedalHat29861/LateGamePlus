/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.util.collection;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import org.slf4j.Logger;

public interface ListOperation {
    public static final MapCodec<ListOperation> UNLIMITED_SIZE_CODEC = ListOperation.createCodec(Integer.MAX_VALUE);

    public static MapCodec<ListOperation> createCodec(int maxSize) {
        return Mode.CODEC.dispatchMap("mode", ListOperation::getMode, mode -> mode.codec).validate(operation -> {
            int j;
            ReplaceSection replaceSection;
            if (operation instanceof ReplaceSection && (replaceSection = (ReplaceSection)operation).size().isPresent() && (j = replaceSection.size().get().intValue()) > maxSize) {
                return DataResult.error(() -> "Size value too large: " + j + ", max size is " + maxSize);
            }
            return DataResult.success((Object)operation);
        });
    }

    public Mode getMode();

    default public <T> List<T> apply(List<T> current, List<T> values) {
        return this.apply(current, values, Integer.MAX_VALUE);
    }

    public <T> List<T> apply(List<T> var1, List<T> var2, int var3);

    public static final class Mode
    extends Enum<Mode>
    implements StringIdentifiable {
        public static final /* enum */ Mode REPLACE_ALL = new Mode("replace_all", ReplaceAll.CODEC);
        public static final /* enum */ Mode REPLACE_SECTION = new Mode("replace_section", ReplaceSection.CODEC);
        public static final /* enum */ Mode INSERT = new Mode("insert", Insert.CODEC);
        public static final /* enum */ Mode APPEND = new Mode("append", Append.CODEC);
        public static final Codec<Mode> CODEC;
        private final String id;
        final MapCodec<? extends ListOperation> codec;
        private static final /* synthetic */ Mode[] field_49864;

        public static Mode[] values() {
            return (Mode[])field_49864.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private Mode(String id, MapCodec<? extends ListOperation> codec) {
            this.id = id;
            this.codec = codec;
        }

        public MapCodec<? extends ListOperation> getCodec() {
            return this.codec;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ Mode[] method_58199() {
            return new Mode[]{REPLACE_ALL, REPLACE_SECTION, INSERT, APPEND};
        }

        static {
            field_49864 = Mode.method_58199();
            CODEC = StringIdentifiable.createCodec(Mode::values);
        }
    }

    public record ReplaceSection(int offset, Optional<Integer> size) implements ListOperation
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ReplaceSection> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.NON_NEGATIVE_INT.optionalFieldOf("offset", (Object)0).forGetter(ReplaceSection::offset), (App)Codecs.NON_NEGATIVE_INT.optionalFieldOf("size").forGetter(ReplaceSection::size)).apply((Applicative)instance, ReplaceSection::new));

        public ReplaceSection(int offset) {
            this(offset, Optional.empty());
        }

        @Override
        public Mode getMode() {
            return Mode.REPLACE_SECTION;
        }

        @Override
        public <T> List<T> apply(List<T> current, List<T> values, int maxSize) {
            ImmutableList list;
            int i = current.size();
            if (this.offset > i) {
                LOGGER.error("Cannot replace when offset is out of bounds");
                return current;
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            builder.addAll(current.subList(0, this.offset));
            builder.addAll(values);
            int j = this.offset + this.size.orElse(values.size());
            if (j < i) {
                builder.addAll(current.subList(j, i));
            }
            if ((list = builder.build()).size() > maxSize) {
                LOGGER.error("Contents overflow in section replacement");
                return current;
            }
            return list;
        }
    }

    public record Values<T>(List<T> value, ListOperation operation) {
        public static <T> Codec<Values<T>> createCodec(Codec<T> codec, int maxSize) {
            return RecordCodecBuilder.create(instance -> instance.group((App)codec.sizeLimitedListOf(maxSize).fieldOf("values").forGetter(values -> values.value), (App)ListOperation.createCodec(maxSize).forGetter(values -> values.operation)).apply((Applicative)instance, Values::new));
        }

        public List<T> apply(List<T> current) {
            return this.operation.apply(current, this.value);
        }
    }

    public static class Append
    implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final Append INSTANCE = new Append();
        public static final MapCodec<Append> CODEC = MapCodec.unit(() -> INSTANCE);

        private Append() {
        }

        @Override
        public Mode getMode() {
            return Mode.APPEND;
        }

        @Override
        public <T> List<T> apply(List<T> current, List<T> values, int maxSize) {
            if (current.size() + values.size() > maxSize) {
                LOGGER.error("Contents overflow in section append");
                return current;
            }
            return Stream.concat(current.stream(), values.stream()).toList();
        }
    }

    public record Insert(int offset) implements ListOperation
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<Insert> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.NON_NEGATIVE_INT.optionalFieldOf("offset", (Object)0).forGetter(Insert::offset)).apply((Applicative)instance, Insert::new));

        @Override
        public Mode getMode() {
            return Mode.INSERT;
        }

        @Override
        public <T> List<T> apply(List<T> current, List<T> values, int maxSize) {
            int i = current.size();
            if (this.offset > i) {
                LOGGER.error("Cannot insert when offset is out of bounds");
                return current;
            }
            if (i + values.size() > maxSize) {
                LOGGER.error("Contents overflow in section insertion");
                return current;
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            builder.addAll(current.subList(0, this.offset));
            builder.addAll(values);
            builder.addAll(current.subList(this.offset, i));
            return builder.build();
        }
    }

    public static class ReplaceAll
    implements ListOperation {
        public static final ReplaceAll INSTANCE = new ReplaceAll();
        public static final MapCodec<ReplaceAll> CODEC = MapCodec.unit(() -> INSTANCE);

        private ReplaceAll() {
        }

        @Override
        public Mode getMode() {
            return Mode.REPLACE_ALL;
        }

        @Override
        public <T> List<T> apply(List<T> current, List<T> values, int maxSize) {
            return values;
        }
    }
}

