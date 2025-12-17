/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.scoreboard.number;

import com.mojang.serialization.MapCodec;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record FixedNumberFormat(Text text) implements NumberFormat
{
    public static final NumberFormatType<FixedNumberFormat> TYPE = new NumberFormatType<FixedNumberFormat>(){
        private static final MapCodec<FixedNumberFormat> CODEC = TextCodecs.CODEC.fieldOf("value").xmap(FixedNumberFormat::new, FixedNumberFormat::text);
        private static final PacketCodec<RegistryByteBuf, FixedNumberFormat> PACKET_CODEC = PacketCodec.tuple(TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, FixedNumberFormat::text, FixedNumberFormat::new);

        @Override
        public MapCodec<FixedNumberFormat> getCodec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, FixedNumberFormat> getPacketCodec() {
            return PACKET_CODEC;
        }
    };

    @Override
    public MutableText format(int number) {
        return this.text.copy();
    }

    public NumberFormatType<FixedNumberFormat> getType() {
        return TYPE;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{FixedNumberFormat.class, "value", "text"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FixedNumberFormat.class, "value", "text"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FixedNumberFormat.class, "value", "text"}, this, object);
    }
}

