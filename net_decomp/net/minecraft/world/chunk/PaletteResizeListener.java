/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

public interface PaletteResizeListener<T> {
    public int onResize(int var1, T var2);

    public static <T> PaletteResizeListener<T> throwing() {
        return (newBits, object) -> {
            throw new IllegalArgumentException("Unexpected palette resize, bits = " + newBits + ", added value = " + String.valueOf(object));
        };
    }
}

