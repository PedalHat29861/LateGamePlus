/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.scoreboard.number;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record StyledNumberFormat(Style style) implements NumberFormat
{
    public static final NumberFormatType<StyledNumberFormat> TYPE = new NumberFormatType<StyledNumberFormat>(){
        private static final MapCodec<StyledNumberFormat> CODEC = Style.Codecs.MAP_CODEC.xmap(StyledNumberFormat::new, StyledNumberFormat::style);
        private static final PacketCodec<RegistryByteBuf, StyledNumberFormat> PACKET_CODEC = PacketCodec.tuple(Style.Codecs.PACKET_CODEC, StyledNumberFormat::style, StyledNumberFormat::new);

        @Override
        public MapCodec<StyledNumberFormat> getCodec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, StyledNumberFormat> getPacketCodec() {
            return PACKET_CODEC;
        }
    };
    public static final StyledNumberFormat EMPTY = new StyledNumberFormat(Style.EMPTY);
    public static final StyledNumberFormat RED = new StyledNumberFormat(Style.EMPTY.withColor(Formatting.RED));
    public static final StyledNumberFormat YELLOW = new StyledNumberFormat(Style.EMPTY.withColor(Formatting.YELLOW));

    @Override
    public MutableText format(int number) {
        return Text.literal(Integer.toString(number)).fillStyle(this.style);
    }

    public NumberFormatType<StyledNumberFormat> getType() {
        return TYPE;
    }
}

