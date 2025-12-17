/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.packrat;

public record Symbol<T>(String name) {
    @Override
    public String toString() {
        return "<" + this.name + ">";
    }

    public static <T> Symbol<T> of(String name) {
        return new Symbol<T>(name);
    }
}

