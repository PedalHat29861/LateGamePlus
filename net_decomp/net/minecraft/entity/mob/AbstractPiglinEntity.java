/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PiglinActivity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.jspecify.annotations.Nullable;

public abstract class AbstractPiglinEntity
extends HostileEntity {
    protected static final TrackedData<Boolean> IMMUNE_TO_ZOMBIFICATION = DataTracker.registerData(AbstractPiglinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final int TIME_TO_ZOMBIFY = 300;
    private static final boolean DEFAULT_IS_IMMUNE_TO_ZOMBIFICATION = false;
    private static final boolean field_60362 = true;
    private static final int DEFAULT_TIME_IN_OVERWORLD = 0;
    protected int timeInOverworld = 0;

    public AbstractPiglinEntity(EntityType<? extends AbstractPiglinEntity> entityType, World world) {
        super((EntityType<? extends HostileEntity>)entityType, world);
        this.setCanPickUpLoot(true);
        this.setCanPathThroughDoors();
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 16.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0f);
    }

    private void setCanPathThroughDoors() {
        if (NavigationConditions.hasMobNavigation(this)) {
            this.getNavigation().setCanOpenDoors(true);
        }
    }

    protected abstract boolean canHunt();

    public void setImmuneToZombification(boolean immuneToZombification) {
        this.getDataTracker().set(IMMUNE_TO_ZOMBIFICATION, immuneToZombification);
    }

    protected boolean isImmuneToZombification() {
        return this.getDataTracker().get(IMMUNE_TO_ZOMBIFICATION);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("IsImmuneToZombification", this.isImmuneToZombification());
        view.putInt("TimeInOverworld", this.timeInOverworld);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setCanPickUpLoot(view.getBoolean("CanPickUpLoot", true));
        this.setImmuneToZombification(view.getBoolean("IsImmuneToZombification", false));
        this.timeInOverworld = view.getInt("TimeInOverworld", 0);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        this.timeInOverworld = this.shouldZombify() ? ++this.timeInOverworld : 0;
        if (this.timeInOverworld > 300) {
            this.playZombificationSound();
            this.zombify(world);
        }
    }

    @VisibleForTesting
    public void setTimeInOverworld(int timeInOverworld) {
        this.timeInOverworld = timeInOverworld;
    }

    public boolean shouldZombify() {
        return !this.isImmuneToZombification() && !this.isAiDisabled() && this.getEntityWorld().getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.PIGLINS_ZOMBIFY_GAMEPLAY, this.getEntityPos()) != false;
    }

    protected void zombify(ServerWorld world) {
        this.convertTo(EntityType.ZOMBIFIED_PIGLIN, EntityConversionContext.create(this, true, true), zombifiedPiglin -> zombifiedPiglin.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0)));
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    public abstract PiglinActivity getActivity();

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetInBrain();
    }

    protected boolean isHoldingTool() {
        return this.getMainHandStack().contains(DataComponentTypes.TOOL);
    }

    @Override
    public void playAmbientSound() {
        if (PiglinBrain.hasIdleActivity(this)) {
            super.playAmbientSound();
        }
    }

    protected abstract void playZombificationSound();
}

