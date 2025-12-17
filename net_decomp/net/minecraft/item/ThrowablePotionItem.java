/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ProjectileItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public abstract class ThrowablePotionItem
extends PotionItem
implements ProjectileItem {
    public static float POWER = 0.5f;

    public ThrowablePotionItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            ProjectileEntity.spawnWithVelocity(this::createEntity, serverWorld, itemStack, user, -20.0f, POWER, 1.0f);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }

    protected abstract PotionEntity createEntity(ServerWorld var1, LivingEntity var2, ItemStack var3);

    protected abstract PotionEntity createEntity(World var1, Position var2, ItemStack var3);

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        return this.createEntity(world, pos, stack);
    }

    @Override
    public ProjectileItem.Settings getProjectileSettings() {
        return ProjectileItem.Settings.builder().uncertainty(ProjectileItem.Settings.DEFAULT.uncertainty() * 0.5f).power(ProjectileItem.Settings.DEFAULT.power() * 1.25f).build();
    }
}

