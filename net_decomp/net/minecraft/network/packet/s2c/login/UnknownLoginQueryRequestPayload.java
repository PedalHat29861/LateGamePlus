/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.login;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestPayload;
import net.minecraft.util.Identifier;

public record UnknownLoginQueryRequestPayload(Identifier id) implements LoginQueryRequestPayload
{
    @Override
    public void write(PacketByteBuf buf) {
    }
}

