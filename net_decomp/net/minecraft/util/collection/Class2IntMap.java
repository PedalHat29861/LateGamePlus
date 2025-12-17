/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.util.collection;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.Util;

public class Class2IntMap {
    public static final int MISSING = -1;
    private final Object2IntMap<Class<?>> backingMap = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> object2IntOpenHashMap.defaultReturnValue(-1));

    public int get(Class<?> clazz) {
        int i = this.backingMap.getInt(clazz);
        if (i != -1) {
            return i;
        }
        Class<?> class_ = clazz;
        while ((class_ = class_.getSuperclass()) != Object.class) {
            int j = this.backingMap.getInt(class_);
            if (j == -1) continue;
            return j;
        }
        return -1;
    }

    public int getNext(Class<?> clazz) {
        return this.get(clazz) + 1;
    }

    public int put(Class<?> clazz) {
        int i = this.get(clazz);
        int j = i == -1 ? 0 : i + 1;
        this.backingMap.put(clazz, j);
        return j;
    }
}

