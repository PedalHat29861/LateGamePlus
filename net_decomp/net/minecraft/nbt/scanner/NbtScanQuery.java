/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.scanner;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.nbt.NbtType;

public record NbtScanQuery(List<String> path, NbtType<?> type, String key) {
    public NbtScanQuery(NbtType<?> type, String key) {
        this(List.of(), type, key);
    }

    public NbtScanQuery(String path, NbtType<?> type, String key) {
        this(List.of(path), type, key);
    }

    public NbtScanQuery(String path1, String path2, NbtType<?> type, String key) {
        this(List.of(path1, path2), type, key);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{NbtScanQuery.class, "path;type;name", "path", "type", "key"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NbtScanQuery.class, "path;type;name", "path", "type", "key"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NbtScanQuery.class, "path;type;name", "path", "type", "key"}, this, object);
    }
}

