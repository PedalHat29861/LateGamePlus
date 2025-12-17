/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PlayerLikeEntity
extends LivingEntity {
    public static final Arm MAIN_ARM = Arm.RIGHT;
    public static final int MODEL_CUSTOMIZATION = 0;
    public static final float EYE_HEIGHT = 1.62f;
    public static final Vec3d VEHICLE_ATTACHMENT = new Vec3d(0.0, 0.6, 0.0);
    private static final float CROUCH_BOX_HEIGHT = 1.5f;
    private static final float SWIMMING_BOX_WIDTH = 0.6f;
    public static final float SWIMMING_BOX_HEIGHT = 0.6f;
    protected static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.changing(0.6f, 1.8f).withEyeHeight(1.62f).withAttachments(EntityAttachments.builder().add(EntityAttachmentType.VEHICLE, VEHICLE_ATTACHMENT));
    protected static final Map<EntityPose, EntityDimensions> POSE_DIMENSIONS = ImmutableMap.builder().put((Object)EntityPose.STANDING, (Object)STANDING_DIMENSIONS).put((Object)EntityPose.SLEEPING, (Object)SLEEPING_DIMENSIONS).put((Object)EntityPose.GLIDING, (Object)EntityDimensions.changing(0.6f, 0.6f).withEyeHeight(0.4f)).put((Object)EntityPose.SWIMMING, (Object)EntityDimensions.changing(0.6f, 0.6f).withEyeHeight(0.4f)).put((Object)EntityPose.SPIN_ATTACK, (Object)EntityDimensions.changing(0.6f, 0.6f).withEyeHeight(0.4f)).put((Object)EntityPose.CROUCHING, (Object)EntityDimensions.changing(0.6f, 1.5f).withEyeHeight(1.27f).withAttachments(EntityAttachments.builder().add(EntityAttachmentType.VEHICLE, VEHICLE_ATTACHMENT))).put((Object)EntityPose.DYING, (Object)EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(1.62f)).build();
    protected static final TrackedData<Arm> MAIN_ARM_ID = DataTracker.registerData(PlayerLikeEntity.class, TrackedDataHandlerRegistry.ARM);
    protected static final TrackedData<Byte> PLAYER_MODE_CUSTOMIZATION_ID = DataTracker.registerData(PlayerLikeEntity.class, TrackedDataHandlerRegistry.BYTE);

    protected PlayerLikeEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MAIN_ARM_ID, MAIN_ARM);
        builder.add(PLAYER_MODE_CUSTOMIZATION_ID, (byte)0);
    }

    @Override
    public Arm getMainArm() {
        return this.dataTracker.get(MAIN_ARM_ID);
    }

    public void setMainArm(Arm arm) {
        this.dataTracker.set(MAIN_ARM_ID, arm);
    }

    public boolean isModelPartVisible(PlayerModelPart part) {
        return (this.getDataTracker().get(PLAYER_MODE_CUSTOMIZATION_ID) & part.getBitFlag()) == part.getBitFlag();
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return POSE_DIMENSIONS.getOrDefault(pose, STANDING_DIMENSIONS);
    }
}

