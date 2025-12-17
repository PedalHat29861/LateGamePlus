/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler;

public final class ProfilerTiming
implements Comparable<ProfilerTiming> {
    public final double parentSectionUsagePercentage;
    public final double totalUsagePercentage;
    public final long visitCount;
    public final String name;

    public ProfilerTiming(String name, double parentUsagePercentage, double totalUsagePercentage, long visitCount) {
        this.name = name;
        this.parentSectionUsagePercentage = parentUsagePercentage;
        this.totalUsagePercentage = totalUsagePercentage;
        this.visitCount = visitCount;
    }

    @Override
    public int compareTo(ProfilerTiming profilerTiming) {
        if (profilerTiming.parentSectionUsagePercentage < this.parentSectionUsagePercentage) {
            return -1;
        }
        if (profilerTiming.parentSectionUsagePercentage > this.parentSectionUsagePercentage) {
            return 1;
        }
        return profilerTiming.name.compareTo(this.name);
    }

    public int getColor() {
        return (this.name.hashCode() & 0xAAAAAA) + -12303292;
    }

    @Override
    public /* synthetic */ int compareTo(Object other) {
        return this.compareTo((ProfilerTiming)other);
    }
}

