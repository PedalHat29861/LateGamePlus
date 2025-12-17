/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.tick;

import com.mojang.serialization.Codec;

public final class TickPriority
extends Enum<TickPriority> {
    public static final /* enum */ TickPriority EXTREMELY_HIGH = new TickPriority(-3);
    public static final /* enum */ TickPriority VERY_HIGH = new TickPriority(-2);
    public static final /* enum */ TickPriority HIGH = new TickPriority(-1);
    public static final /* enum */ TickPriority NORMAL = new TickPriority(0);
    public static final /* enum */ TickPriority LOW = new TickPriority(1);
    public static final /* enum */ TickPriority VERY_LOW = new TickPriority(2);
    public static final /* enum */ TickPriority EXTREMELY_LOW = new TickPriority(3);
    public static final Codec<TickPriority> CODEC;
    private final int index;
    private static final /* synthetic */ TickPriority[] field_9312;

    public static TickPriority[] values() {
        return (TickPriority[])field_9312.clone();
    }

    public static TickPriority valueOf(String string) {
        return Enum.valueOf(TickPriority.class, string);
    }

    private TickPriority(int index) {
        this.index = index;
    }

    public static TickPriority byIndex(int index) {
        for (TickPriority tickPriority : TickPriority.values()) {
            if (tickPriority.index != index) continue;
            return tickPriority;
        }
        if (index < TickPriority.EXTREMELY_HIGH.index) {
            return EXTREMELY_HIGH;
        }
        return EXTREMELY_LOW;
    }

    public int getIndex() {
        return this.index;
    }

    private static /* synthetic */ TickPriority[] method_36697() {
        return new TickPriority[]{EXTREMELY_HIGH, VERY_HIGH, HIGH, NORMAL, LOW, VERY_LOW, EXTREMELY_LOW};
    }

    static {
        field_9312 = TickPriority.method_36697();
        CODEC = Codec.INT.xmap(TickPriority::byIndex, TickPriority::getIndex);
    }
}

