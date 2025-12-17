/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.piston;

public final class PistonBehavior
extends Enum<PistonBehavior> {
    public static final /* enum */ PistonBehavior NORMAL = new PistonBehavior();
    public static final /* enum */ PistonBehavior DESTROY = new PistonBehavior();
    public static final /* enum */ PistonBehavior BLOCK = new PistonBehavior();
    public static final /* enum */ PistonBehavior IGNORE = new PistonBehavior();
    public static final /* enum */ PistonBehavior PUSH_ONLY = new PistonBehavior();
    private static final /* synthetic */ PistonBehavior[] field_15973;

    public static PistonBehavior[] values() {
        return (PistonBehavior[])field_15973.clone();
    }

    public static PistonBehavior valueOf(String string) {
        return Enum.valueOf(PistonBehavior.class, string);
    }

    private static /* synthetic */ PistonBehavior[] method_36765() {
        return new PistonBehavior[]{NORMAL, DESTROY, BLOCK, IGNORE, PUSH_ONLY};
    }

    static {
        field_15973 = PistonBehavior.method_36765();
    }
}

