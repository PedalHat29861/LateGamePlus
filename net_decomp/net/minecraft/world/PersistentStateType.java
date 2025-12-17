/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.world.PersistentState;

public record PersistentStateType<T extends PersistentState>(String id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType) {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersistentStateType)) return false;
        PersistentStateType persistentStateType = (PersistentStateType)o;
        if (!this.id.equals(persistentStateType.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "SavedDataType[" + this.id + "]";
    }
}

