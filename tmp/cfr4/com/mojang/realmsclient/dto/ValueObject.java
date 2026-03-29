/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class ValueObject {
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (Field f : this.getClass().getFields()) {
            if (ValueObject.isStatic(f)) continue;
            try {
                sb.append(ValueObject.getName(f)).append("=").append(f.get(this)).append(" ");
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append('}');
        return sb.toString();
    }

    private static String getName(Field f) {
        SerializedName override = f.getAnnotation(SerializedName.class);
        return override != null ? override.value() : f.getName();
    }

    private static boolean isStatic(Field f) {
        return Modifier.isStatic(f.getModifiers());
    }
}

