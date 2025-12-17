/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.login;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface LoginQueryRequestPayload {
    public Identifier id();

    public void write(PacketByteBuf var1);
}

