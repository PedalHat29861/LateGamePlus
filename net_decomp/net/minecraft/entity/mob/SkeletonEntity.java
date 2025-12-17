/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;

public class SkeletonEntity
extends AbstractSkeletonEntity {
    private static final int TOTAL_CONVERSION_TIME = 300;
    private static final TrackedData<Boolean> CONVERTING = DataTracker.registerData(SkeletonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final String STRAY_CONVERSION_TIME_KEY = "StrayConversionTime";
    private static final int DEFAULT_STRAY_CONVERSION_TIME = -1;
    private int inPowderSnowTime;
    private int conversionTime;

    public SkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
        super((EntityType<? extends AbstractSkeletonEntity>)entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CONVERTING, false);
    }

    public boolean isConverting() {
        return this.getDataTracker().get(CONVERTING);
    }

    public void setConverting(boolean converting) {
        this.dataTracker.set(CONVERTING, converting);
    }

    @Override
    public boolean isShaking() {
        return this.isConverting();
    }

    @Override
    public void tick() {
        if (!this.getEntityWorld().isClient() && this.isAlive() && !this.isAiDisabled()) {
            if (this.inPowderSnow) {
                if (this.isConverting()) {
                    --this.conversionTime;
                    if (this.conversionTime < 0) {
                        this.convertToStray();
                    }
                } else {
                    ++this.inPowderSnowTime;
                    if (this.inPowderSnowTime >= 140) {
                        this.setConversionTime(300);
                    }
                }
            } else {
                this.inPowderSnowTime = -1;
                this.setConverting(false);
            }
        }
        super.tick();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt(STRAY_CONVERSION_TIME_KEY, this.isConverting() ? this.conversionTime : -1);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        int i = view.getInt(STRAY_CONVERSION_TIME_KEY, -1);
        if (i != -1) {
            this.setConversionTime(i);
        } else {
            this.setConverting(false);
        }
    }

    @VisibleForTesting
    public void setConversionTime(int time) {
        this.conversionTime = time;
        this.setConverting(true);
    }

    protected void convertToStray() {
        this.convertTo(EntityType.STRAY, EntityConversionContext.create(this, true, true), stray -> {
            if (!this.isSilent()) {
                this.getEntityWorld().syncWorldEvent(null, 1048, this.getBlockPos(), 0);
            }
        });
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SKELETON_DEATH;
    }

    @Override
    SoundEvent getStepSound() {
        return SoundEvents.ENTITY_SKELETON_STEP;
    }
}

