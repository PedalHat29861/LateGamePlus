/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler;

public final class SampleType
extends Enum<SampleType> {
    public static final /* enum */ SampleType PATH_FINDING = new SampleType("pathfinding");
    public static final /* enum */ SampleType EVENT_LOOPS = new SampleType("event-loops");
    public static final /* enum */ SampleType CONSECUTIVE_EXECUTORS = new SampleType("consecutive-executors");
    public static final /* enum */ SampleType TICK_LOOP = new SampleType("ticking");
    public static final /* enum */ SampleType JVM = new SampleType("jvm");
    public static final /* enum */ SampleType CHUNK_RENDERING = new SampleType("chunk rendering");
    public static final /* enum */ SampleType CHUNK_RENDERING_DISPATCHING = new SampleType("chunk rendering dispatching");
    public static final /* enum */ SampleType CPU = new SampleType("cpu");
    public static final /* enum */ SampleType GPU = new SampleType("gpu");
    private final String name;
    private static final /* synthetic */ SampleType[] field_29554;

    public static SampleType[] values() {
        return (SampleType[])field_29554.clone();
    }

    public static SampleType valueOf(String string) {
        return Enum.valueOf(SampleType.class, string);
    }

    private SampleType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    private static /* synthetic */ SampleType[] method_36594() {
        return new SampleType[]{PATH_FINDING, EVENT_LOOPS, CONSECUTIVE_EXECUTORS, TICK_LOOP, JVM, CHUNK_RENDERING, CHUNK_RENDERING_DISPATCHING, CPU, GPU};
    }

    static {
        field_29554 = SampleType.method_36594();
    }
}

