/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.codec;

@FunctionalInterface
public interface PacketEncoder<O, T> {
    public void encode(O var1, T var2);
}

