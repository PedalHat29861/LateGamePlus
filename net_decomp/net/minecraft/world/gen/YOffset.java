/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.gen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.HeightContext;

public interface YOffset {
    public static final Codec<YOffset> OFFSET_CODEC = Codec.xor(Fixed.CODEC, (Codec)Codec.xor(AboveBottom.CODEC, BelowTop.CODEC)).xmap(YOffset::fromEither, YOffset::map);
    public static final YOffset BOTTOM = YOffset.aboveBottom(0);
    public static final YOffset TOP = YOffset.belowTop(0);

    public static YOffset fixed(int offset) {
        return new Fixed(offset);
    }

    public static YOffset aboveBottom(int offset) {
        return new AboveBottom(offset);
    }

    public static YOffset belowTop(int offset) {
        return new BelowTop(offset);
    }

    public static YOffset getBottom() {
        return BOTTOM;
    }

    public static YOffset getTop() {
        return TOP;
    }

    private static YOffset fromEither(Either<Fixed, Either<AboveBottom, BelowTop>> either) {
        return (YOffset)either.map(Function.identity(), Either::unwrap);
    }

    private static Either<Fixed, Either<AboveBottom, BelowTop>> map(YOffset yOffset) {
        if (yOffset instanceof Fixed) {
            return Either.left((Object)((Fixed)yOffset));
        }
        return Either.right((Object)(yOffset instanceof AboveBottom ? Either.left((Object)((AboveBottom)yOffset)) : Either.right((Object)((BelowTop)yOffset))));
    }

    public int getY(HeightContext var1);

    public record Fixed(int y) implements YOffset
    {
        public static final Codec<Fixed> CODEC = Codec.intRange((int)DimensionType.MIN_HEIGHT, (int)DimensionType.MAX_COLUMN_HEIGHT).fieldOf("absolute").xmap(Fixed::new, Fixed::y).codec();

        @Override
        public int getY(HeightContext context) {
            return this.y;
        }

        @Override
        public String toString() {
            return this.y + " absolute";
        }
    }

    public record AboveBottom(int offset) implements YOffset
    {
        public static final Codec<AboveBottom> CODEC = Codec.intRange((int)DimensionType.MIN_HEIGHT, (int)DimensionType.MAX_COLUMN_HEIGHT).fieldOf("above_bottom").xmap(AboveBottom::new, AboveBottom::offset).codec();

        @Override
        public int getY(HeightContext context) {
            return context.getMinY() + this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " above bottom";
        }
    }

    public record BelowTop(int offset) implements YOffset
    {
        public static final Codec<BelowTop> CODEC = Codec.intRange((int)DimensionType.MIN_HEIGHT, (int)DimensionType.MAX_COLUMN_HEIGHT).fieldOf("below_top").xmap(BelowTop::new, BelowTop::offset).codec();

        @Override
        public int getY(HeightContext context) {
            return context.getHeight() - 1 + context.getMinY() - this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " below top";
        }
    }
}

