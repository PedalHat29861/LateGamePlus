package com.pedalhat.lategameplus.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilMenu.class)
public interface AnvilScreenHandlerAccessor {
    @Accessor("cost")
    DataSlot getLevelCost();

    @Accessor("onlyRenaming")
    boolean getKeepSecondSlot();
}