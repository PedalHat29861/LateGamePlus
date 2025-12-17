/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.screen;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.sync.TrackedSlot;

public interface ScreenHandlerSyncHandler {
    public void updateState(ScreenHandler var1, List<ItemStack> var2, ItemStack var3, int[] var4);

    public void updateSlot(ScreenHandler var1, int var2, ItemStack var3);

    public void updateCursorStack(ScreenHandler var1, ItemStack var2);

    public void updateProperty(ScreenHandler var1, int var2, int var3);

    public TrackedSlot createTrackedSlot();
}

