/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class FilterMask {
    public static final Codec<FilterMask> CODEC = StringIdentifiable.createCodec(FilterStatus::values).dispatch(FilterMask::getStatus, FilterStatus::getCodec);
    public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), FilterStatus.FULLY_FILTERED);
    public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), FilterStatus.PASS_THROUGH);
    public static final Style FILTERED_STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY).withHoverEvent(new HoverEvent.ShowText(Text.translatable("chat.filtered")));
    static final MapCodec<FilterMask> PASS_THROUGH_CODEC = MapCodec.unit((Object)PASS_THROUGH);
    static final MapCodec<FilterMask> FULLY_FILTERED_CODEC = MapCodec.unit((Object)FULLY_FILTERED);
    static final MapCodec<FilterMask> PARTIALLY_FILTERED_CODEC = Codecs.BIT_SET.xmap(FilterMask::new, FilterMask::getMask).fieldOf("value");
    private static final char FILTERED = '#';
    private final BitSet mask;
    private final FilterStatus status;

    private FilterMask(BitSet mask, FilterStatus status) {
        this.mask = mask;
        this.status = status;
    }

    private FilterMask(BitSet mask) {
        this.mask = mask;
        this.status = FilterStatus.PARTIALLY_FILTERED;
    }

    public FilterMask(int length) {
        this(new BitSet(length), FilterStatus.PARTIALLY_FILTERED);
    }

    private FilterStatus getStatus() {
        return this.status;
    }

    private BitSet getMask() {
        return this.mask;
    }

    public static FilterMask readMask(PacketByteBuf buf) {
        FilterStatus filterStatus = buf.readEnumConstant(FilterStatus.class);
        return switch (filterStatus.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PASS_THROUGH;
            case 1 -> FULLY_FILTERED;
            case 2 -> new FilterMask(buf.readBitSet(), FilterStatus.PARTIALLY_FILTERED);
        };
    }

    public static void writeMask(PacketByteBuf buf, FilterMask mask) {
        buf.writeEnumConstant(mask.status);
        if (mask.status == FilterStatus.PARTIALLY_FILTERED) {
            buf.writeBitSet(mask.mask);
        }
    }

    public void markFiltered(int index) {
        this.mask.set(index);
    }

    public @Nullable String filter(String raw) {
        return switch (this.status.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> raw;
            case 2 -> {
                char[] cs = raw.toCharArray();
                for (int i = 0; i < cs.length && i < this.mask.length(); ++i) {
                    if (!this.mask.get(i)) continue;
                    cs[i] = 35;
                }
                yield new String(cs);
            }
        };
    }

    public @Nullable Text getFilteredText(String message) {
        return switch (this.status.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> Text.literal(message);
            case 2 -> {
                MutableText mutableText = Text.empty();
                int i = 0;
                boolean bl = this.mask.get(0);
                while (true) {
                    int j = bl ? this.mask.nextClearBit(i) : this.mask.nextSetBit(i);
                    int v1 = j = j < 0 ? message.length() : j;
                    if (j == i) break;
                    if (bl) {
                        mutableText.append(Text.literal(StringUtils.repeat((char)'#', (int)(j - i))).fillStyle(FILTERED_STYLE));
                    } else {
                        mutableText.append(message.substring(i, j));
                    }
                    bl = !bl;
                    i = j;
                }
                yield mutableText;
            }
        };
    }

    public boolean isPassThrough() {
        return this.status == FilterStatus.PASS_THROUGH;
    }

    public boolean isFullyFiltered() {
        return this.status == FilterStatus.FULLY_FILTERED;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FilterMask filterMask = (FilterMask)o;
        return this.mask.equals(filterMask.mask) && this.status == filterMask.status;
    }

    public int hashCode() {
        int i = this.mask.hashCode();
        i = 31 * i + this.status.hashCode();
        return i;
    }

    static final class FilterStatus
    extends Enum<FilterStatus>
    implements StringIdentifiable {
        public static final /* enum */ FilterStatus PASS_THROUGH = new FilterStatus("pass_through", () -> PASS_THROUGH_CODEC);
        public static final /* enum */ FilterStatus FULLY_FILTERED = new FilterStatus("fully_filtered", () -> FULLY_FILTERED_CODEC);
        public static final /* enum */ FilterStatus PARTIALLY_FILTERED = new FilterStatus("partially_filtered", () -> PARTIALLY_FILTERED_CODEC);
        private final String id;
        private final Supplier<MapCodec<FilterMask>> codecSupplier;
        private static final /* synthetic */ FilterStatus[] field_39950;

        public static FilterStatus[] values() {
            return (FilterStatus[])field_39950.clone();
        }

        public static FilterStatus valueOf(String string) {
            return Enum.valueOf(FilterStatus.class, string);
        }

        private FilterStatus(String id, Supplier<MapCodec<FilterMask>> codecSupplier) {
            this.id = id;
            this.codecSupplier = codecSupplier;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private MapCodec<FilterMask> getCodec() {
            return this.codecSupplier.get();
        }

        private static /* synthetic */ FilterStatus[] method_45094() {
            return new FilterStatus[]{PASS_THROUGH, FULLY_FILTERED, PARTIALLY_FILTERED};
        }

        static {
            field_39950 = FilterStatus.method_45094();
        }
    }
}

