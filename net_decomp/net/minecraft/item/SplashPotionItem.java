/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SplashPotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class SplashPotionItem
extends ThrowablePotionItem {
    public SplashPotionItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.PLAYERS, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        return super.use(world, user, hand);
    }

    @Override
    protected PotionEntity createEntity(ServerWorld world, LivingEntity user, ItemStack stack) {
        return new SplashPotionEntity(world, user, stack);
    }

    @Override
    protected PotionEntity createEntity(World world, Position pos, ItemStack stack) {
        return new SplashPotionEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
    }
}

