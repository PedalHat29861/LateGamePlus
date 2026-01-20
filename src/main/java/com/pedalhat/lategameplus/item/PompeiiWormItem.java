package com.pedalhat.lategameplus.item;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PompeiiWormItem extends Item {
    public PompeiiWormItem(Settings settings) {
        super(settings);
    }
    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 16;
    }
    
}
