/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.screen.slot;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;

public class ForgingSlotsManager {
    private final List<ForgingSlot> inputSlots;
    private final ForgingSlot resultSlot;

    ForgingSlotsManager(List<ForgingSlot> inputSlots, ForgingSlot resultSlot) {
        if (inputSlots.isEmpty() || resultSlot.equals(ForgingSlot.DEFAULT)) {
            throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
        }
        this.inputSlots = inputSlots;
        this.resultSlot = resultSlot;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ForgingSlot getInputSlot(int index) {
        return this.inputSlots.get(index);
    }

    public ForgingSlot getResultSlot() {
        return this.resultSlot;
    }

    public List<ForgingSlot> getInputSlots() {
        return this.inputSlots;
    }

    public int getInputSlotCount() {
        return this.inputSlots.size();
    }

    public int getResultSlotIndex() {
        return this.getInputSlotCount();
    }

    public static final class ForgingSlot
    extends Record {
        final int slotId;
        private final int x;
        private final int y;
        private final Predicate<ItemStack> mayPlace;
        static final ForgingSlot DEFAULT = new ForgingSlot(0, 0, 0, stack -> true);

        public ForgingSlot(int slotId, int x, int y, Predicate<ItemStack> mayPlace) {
            this.slotId = slotId;
            this.x = x;
            this.y = y;
            this.mayPlace = mayPlace;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ForgingSlot.class, "slotIndex;x;y;mayPlace", "slotId", "x", "y", "mayPlace"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ForgingSlot.class, "slotIndex;x;y;mayPlace", "slotId", "x", "y", "mayPlace"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ForgingSlot.class, "slotIndex;x;y;mayPlace", "slotId", "x", "y", "mayPlace"}, this, object);
        }

        public int slotId() {
            return this.slotId;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public Predicate<ItemStack> mayPlace() {
            return this.mayPlace;
        }
    }

    public static class Builder {
        private final List<ForgingSlot> inputs = new ArrayList<ForgingSlot>();
        private ForgingSlot resultSlot = ForgingSlot.DEFAULT;

        public Builder input(int slotId, int x, int y, Predicate<ItemStack> mayPlace) {
            this.inputs.add(new ForgingSlot(slotId, x, y, mayPlace));
            return this;
        }

        public Builder output(int slotId, int x, int y) {
            this.resultSlot = new ForgingSlot(slotId, x, y, stack -> false);
            return this;
        }

        public ForgingSlotsManager build() {
            int i = this.inputs.size();
            for (int j = 0; j < i; ++j) {
                ForgingSlot forgingSlot = this.inputs.get(j);
                if (forgingSlot.slotId == j) continue;
                throw new IllegalArgumentException("Expected input slots to have continous indexes");
            }
            if (this.resultSlot.slotId != i) {
                throw new IllegalArgumentException("Expected result slot index to follow last input slot");
            }
            return new ForgingSlotsManager(this.inputs, this.resultSlot);
        }
    }
}

