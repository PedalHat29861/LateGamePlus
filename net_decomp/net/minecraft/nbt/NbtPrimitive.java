/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public sealed interface NbtPrimitive
extends NbtElement
permits AbstractNbtNumber, NbtString {
    @Override
    default public NbtElement copy() {
        return this;
    }
}

