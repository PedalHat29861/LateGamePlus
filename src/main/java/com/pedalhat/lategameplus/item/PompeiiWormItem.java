package com.pedalhat.lategameplus.item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PompeiiWormItem extends Item {
    public PompeiiWormItem(Properties settings) {
        super(settings);
    }
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 16;
    }
    
}
