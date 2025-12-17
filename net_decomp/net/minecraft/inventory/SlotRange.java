/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.inventory;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.StringIdentifiable;

public interface SlotRange
extends StringIdentifiable {
    public IntList getSlotIds();

    default public int getSlotCount() {
        return this.getSlotIds().size();
    }

    public static SlotRange create(final String name, final IntList slotIds) {
        return new SlotRange(){

            @Override
            public IntList getSlotIds() {
                return slotIds;
            }

            @Override
            public String asString() {
                return name;
            }

            public String toString() {
                return name;
            }
        };
    }
}

