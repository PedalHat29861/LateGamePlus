/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler.log;

import net.minecraft.world.debug.DebugSubscriptionType;
import net.minecraft.world.debug.DebugSubscriptionTypes;

public final class DebugSampleType
extends Enum<DebugSampleType> {
    public static final /* enum */ DebugSampleType TICK_TIME = new DebugSampleType(DebugSubscriptionTypes.DEDICATED_SERVER_TICK_TIME);
    private final DebugSubscriptionType<?> subscriptionType;
    private static final /* synthetic */ DebugSampleType[] field_48818;

    public static DebugSampleType[] values() {
        return (DebugSampleType[])field_48818.clone();
    }

    public static DebugSampleType valueOf(String string) {
        return Enum.valueOf(DebugSampleType.class, string);
    }

    private DebugSampleType(DebugSubscriptionType<?> subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public DebugSubscriptionType<?> getSubscriptionType() {
        return this.subscriptionType;
    }

    private static /* synthetic */ DebugSampleType[] method_56665() {
        return new DebugSampleType[]{TICK_TIME};
    }

    static {
        field_48818 = DebugSampleType.method_56665();
    }
}

