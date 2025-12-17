/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class NameTagItem
extends Item {
    public NameTagItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        Text text = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (text != null && entity.getType().isSaveable()) {
            if (!user.getEntityWorld().isClient() && entity.isAlive()) {
                entity.setCustomName(text);
                if (entity instanceof MobEntity) {
                    MobEntity mobEntity = (MobEntity)entity;
                    mobEntity.setPersistent();
                }
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}

