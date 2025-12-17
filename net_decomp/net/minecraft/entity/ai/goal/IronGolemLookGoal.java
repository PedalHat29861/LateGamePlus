/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.Box;
import org.jspecify.annotations.Nullable;

public class IronGolemLookGoal
extends Goal {
    private static final TargetPredicate CLOSE_VILLAGER_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(6.0);
    private static final Item GIFT = Items.POPPY;
    public static final int MAX_LOOK_COOLDOWN = 400;
    private final IronGolemEntity golem;
    private @Nullable LivingEntity recipient;
    private int lookCountdown;

    public IronGolemLookGoal(IronGolemEntity golem) {
        this.golem = golem;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!this.golem.getEntityWorld().isDay()) {
            return false;
        }
        if (this.golem.getRandom().nextInt(8000) != 0) {
            return false;
        }
        this.recipient = IronGolemLookGoal.getServerWorld(this.golem).getClosestEntity(EntityTypeTags.CANDIDATE_FOR_IRON_GOLEM_GIFT, CLOSE_VILLAGER_PREDICATE, (LivingEntity)this.golem, this.golem.getX(), this.golem.getY(), this.golem.getZ(), this.getRange());
        return this.recipient != null;
    }

    @Override
    public boolean shouldContinue() {
        return this.lookCountdown > 0;
    }

    @Override
    public void start() {
        this.lookCountdown = this.getTickCount(400);
        this.golem.setLookingAtVillager(true);
    }

    @Override
    public void stop() {
        MobEntity mobEntity;
        LivingEntity livingEntity;
        this.golem.setLookingAtVillager(false);
        if (this.lookCountdown == 0 && (livingEntity = this.recipient) instanceof MobEntity && (mobEntity = (MobEntity)livingEntity).getType().isIn(EntityTypeTags.ACCEPTS_IRON_GOLEM_GIFT) && mobEntity.getEquippedStack(CopperGolemEntity.POPPY_SLOT).isEmpty() && this.getRange().intersects(mobEntity.getBoundingBox())) {
            mobEntity.equipStack(CopperGolemEntity.POPPY_SLOT, GIFT.getDefaultStack());
            mobEntity.setDropGuaranteed(CopperGolemEntity.POPPY_SLOT);
        }
        this.recipient = null;
    }

    @Override
    public void tick() {
        if (this.recipient != null) {
            this.golem.getLookControl().lookAt(this.recipient, 30.0f, 30.0f);
        }
        --this.lookCountdown;
    }

    private Box getRange() {
        return this.golem.getBoundingBox().expand(6.0, 2.0, 6.0);
    }
}

