/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.poi;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;

public record PointOfInterestType(Set<BlockState> blockStates, int ticketCount, int searchDistance) {
    public static final Predicate<RegistryEntry<PointOfInterestType>> NONE = type -> false;

    public PointOfInterestType {
        blockStates = Set.copyOf(blockStates);
    }

    public boolean contains(BlockState state) {
        return this.blockStates.contains(state);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PointOfInterestType.class, "matchingStates;maxTickets;validRange", "blockStates", "ticketCount", "searchDistance"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PointOfInterestType.class, "matchingStates;maxTickets;validRange", "blockStates", "ticketCount", "searchDistance"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PointOfInterestType.class, "matchingStates;maxTickets;validRange", "blockStates", "ticketCount", "searchDistance"}, this, object);
    }
}

