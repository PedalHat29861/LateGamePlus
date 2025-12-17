/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.crash;

import org.jspecify.annotations.Nullable;

public class CrashMemoryReserve {
    private static byte @Nullable [] reservedMemory;

    public static void reserveMemory() {
        reservedMemory = new byte[0xA00000];
    }

    public static void releaseMemory() {
        if (reservedMemory != null) {
            reservedMemory = null;
            try {
                System.gc();
                System.gc();
                System.gc();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }
}

