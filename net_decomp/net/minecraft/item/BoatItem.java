/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class BoatItem
extends Item {
    private final EntityType<? extends AbstractBoatEntity> boatEntityType;

    public BoatItem(EntityType<? extends AbstractBoatEntity> boatEntityType, Item.Settings settings) {
        super(settings);
        this.boatEntityType = boatEntityType;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult hitResult = BoatItem.raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (((HitResult)hitResult).getType() == HitResult.Type.MISS) {
            return ActionResult.PASS;
        }
        Vec3d vec3d = user.getRotationVec(1.0f);
        double d = 5.0;
        List<Entity> list = world.getOtherEntities(user, user.getBoundingBox().stretch(vec3d.multiply(5.0)).expand(1.0), EntityPredicates.CAN_HIT);
        if (!list.isEmpty()) {
            Vec3d vec3d2 = user.getEyePos();
            for (Entity entity : list) {
                Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
                if (!box.contains(vec3d2)) continue;
                return ActionResult.PASS;
            }
        }
        if (((HitResult)hitResult).getType() == HitResult.Type.BLOCK) {
            AbstractBoatEntity abstractBoatEntity = this.createEntity(world, hitResult, itemStack, user);
            if (abstractBoatEntity == null) {
                return ActionResult.FAIL;
            }
            abstractBoatEntity.setYaw(user.getYaw());
            if (!world.isSpaceEmpty(abstractBoatEntity, abstractBoatEntity.getBoundingBox())) {
                return ActionResult.FAIL;
            }
            if (!world.isClient()) {
                world.spawnEntity(abstractBoatEntity);
                world.emitGameEvent((Entity)user, GameEvent.ENTITY_PLACE, hitResult.getPos());
                itemStack.decrementUnlessCreative(1, user);
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private @Nullable AbstractBoatEntity createEntity(World world, HitResult hitResult, ItemStack stack, PlayerEntity player) {
        AbstractBoatEntity abstractBoatEntity = this.boatEntityType.create(world, SpawnReason.SPAWN_ITEM_USE);
        if (abstractBoatEntity != null) {
            Vec3d vec3d = hitResult.getPos();
            abstractBoatEntity.initPosition(vec3d.x, vec3d.y, vec3d.z);
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                EntityType.copier(serverWorld, stack, player).accept(abstractBoatEntity);
            }
        }
        return abstractBoatEntity;
    }
}

