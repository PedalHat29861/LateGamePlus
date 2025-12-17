/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.arguments.ArgumentType
 */
package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.PacketByteBuf;

public interface ArgumentSerializer<A extends ArgumentType<?>, T extends ArgumentTypeProperties<A>> {
    public void writePacket(T var1, PacketByteBuf var2);

    public T fromPacket(PacketByteBuf var1);

    public void writeJson(T var1, JsonObject var2);

    public T getArgumentTypeProperties(A var1);

    public static interface ArgumentTypeProperties<A extends ArgumentType<?>> {
        public A createType(CommandRegistryAccess var1);

        public ArgumentSerializer<A, ?> getSerializer();
    }
}

