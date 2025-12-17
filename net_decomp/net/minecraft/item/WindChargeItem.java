/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class WindChargeItem
extends Item
implements ProjectileItem {
    public static float POWER = 1.5f;

    public WindChargeItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            ProjectileEntity.spawnWithVelocity((world2, shooter, stack) -> new WindChargeEntity(user, world, user.getEntityPos().getX(), user.getEyePos().getY(), user.getEntityPos().getZ()), serverWorld, itemStack, user, 0.0f, POWER, 1.0f);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        Random random = world.getRandom();
        double d = random.nextTriangular((double)direction.getOffsetX(), 0.11485000000000001);
        double e = random.nextTriangular((double)direction.getOffsetY(), 0.11485000000000001);
        double f = random.nextTriangular((double)direction.getOffsetZ(), 0.11485000000000001);
        Vec3d vec3d = new Vec3d(d, e, f);
        WindChargeEntity windChargeEntity = new WindChargeEntity(world, pos.getX(), pos.getY(), pos.getZ(), vec3d);
        windChargeEntity.setVelocity(vec3d);
        return windChargeEntity;
    }

    @Override
    public void initializeProjectile(ProjectileEntity entity, double x, double y, double z, float power, float uncertainty) {
    }

    @Override
    public ProjectileItem.Settings getProjectileSettings() {
        return ProjectileItem.Settings.builder().positionFunction((pointer, facing) -> DispenserBlock.getOutputLocation(pointer, 1.0, Vec3d.ZERO)).uncertainty(6.6666665f).power(1.0f).overrideDispenseEvent(1051).build();
    }
}

