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

public interface NumberFormatType<T extends NumberFormat> {
    public MapCodec<T> getCodec();

    public PacketCodec<RegistryByteBuf, T> getPacketCodec();
}

