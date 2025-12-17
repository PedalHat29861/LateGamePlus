/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Arm;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class MannequinEntity
extends PlayerLikeEntity {
    protected static final TrackedData<ProfileComponent> PROFILE = DataTracker.registerData(MannequinEntity.class, TrackedDataHandlerRegistry.PROFILE);
    private static final TrackedData<Boolean> IMMOVABLE = DataTracker.registerData(MannequinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Optional<Text>> DESCRIPTION = DataTracker.registerData(MannequinEntity.class, TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT);
    private static final byte ALL_MODEL_PARTS = (byte)Arrays.stream(PlayerModelPart.values()).mapToInt(PlayerModelPart::getBitFlag).reduce(0, (flagL, flagR) -> flagL | flagR);
    private static final Set<EntityPose> POSES = Set.of(EntityPose.STANDING, EntityPose.CROUCHING, EntityPose.SWIMMING, EntityPose.GLIDING, EntityPose.SLEEPING);
    public static final Codec<EntityPose> POSE_CODEC = EntityPose.CODEC.validate(pose -> POSES.contains(pose) ? DataResult.success((Object)pose) : DataResult.error(() -> "Invalid pose: " + pose.asString()));
    private static final Codec<Byte> MODEL_PARTS_CODEC = PlayerModelPart.CODEC.listOf().xmap(parts -> (byte)parts.stream().mapToInt(PlayerModelPart::getBitFlag).reduce(ALL_MODEL_PARTS, (flagL, flagR) -> flagL & ~flagR), bitFlag -> Arrays.stream(PlayerModelPart.values()).filter(part -> (bitFlag & part.getBitFlag()) == 0).toList());
    public static final ProfileComponent DEFAULT_INFO = ProfileComponent.Static.EMPTY;
    private static final Text DEFAULT_DESCRIPTION = Text.translatable("entity.minecraft.mannequin.label");
    protected static EntityType.EntityFactory<MannequinEntity> factory = MannequinEntity::new;
    private static final String PROFILE_KEY = "profile";
    private static final String HIDDEN_LAYERS_KEY = "hidden_layers";
    private static final String MAIN_HAND_KEY = "main_hand";
    private static final String POSE_KEY = "pose";
    private static final String IMMOVABLE_KEY = "immovable";
    private static final String DESCRIPTION_KEY = "description";
    private static final String HIDE_DESCRIPTION_KEY = "hide_description";
    private Text description = DEFAULT_DESCRIPTION;
    private boolean hideDescription = false;

    public MannequinEntity(EntityType<MannequinEntity> entityType, World world) {
        super((EntityType<? extends LivingEntity>)entityType, world);
        this.dataTracker.set(PLAYER_MODE_CUSTOMIZATION_ID, ALL_MODEL_PARTS);
    }

    protected MannequinEntity(World world) {
        this(EntityType.MANNEQUIN, world);
    }

    public static @Nullable MannequinEntity create(EntityType<MannequinEntity> type, World world) {
        return factory.create(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(PROFILE, DEFAULT_INFO);
        builder.add(IMMOVABLE, false);
        builder.add(DESCRIPTION, Optional.of(DEFAULT_DESCRIPTION));
    }

    protected ProfileComponent getMannequinProfile() {
        return this.dataTracker.get(PROFILE);
    }

    private void setMannequinProfile(ProfileComponent profile) {
        this.dataTracker.set(PROFILE, profile);
    }

    private boolean isImmovable() {
        return this.dataTracker.get(IMMOVABLE);
    }

    private void setImmovable(boolean immovable) {
        this.dataTracker.set(IMMOVABLE, immovable);
    }

    protected @Nullable Text getDescription() {
        return this.dataTracker.get(DESCRIPTION).orElse(null);
    }

    private void setDescription(Text description) {
        this.description = description;
        this.updateTrackedDescription();
    }

    private void setHideDescription(boolean hideDescription) {
        this.hideDescription = hideDescription;
        this.updateTrackedDescription();
    }

    private void updateTrackedDescription() {
        this.dataTracker.set(DESCRIPTION, this.hideDescription ? Optional.empty() : Optional.of(this.description));
    }

    @Override
    protected boolean isImmobile() {
        return this.isImmovable() || super.isImmobile();
    }

    @Override
    public boolean canActVoluntarily() {
        return !this.isImmovable() && super.canActVoluntarily();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put(PROFILE_KEY, ProfileComponent.CODEC, this.getMannequinProfile());
        view.put(HIDDEN_LAYERS_KEY, MODEL_PARTS_CODEC, (Byte)this.dataTracker.get(PLAYER_MODE_CUSTOMIZATION_ID));
        view.put(MAIN_HAND_KEY, Arm.CODEC, this.getMainArm());
        view.put(POSE_KEY, POSE_CODEC, this.getPose());
        view.putBoolean(IMMOVABLE_KEY, this.isImmovable());
        Text text = this.getDescription();
        if (text != null) {
            if (!text.equals(DEFAULT_DESCRIPTION)) {
                view.put(DESCRIPTION_KEY, TextCodecs.CODEC, text);
            }
        } else {
            view.putBoolean(HIDE_DESCRIPTION_KEY, true);
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        view.read(PROFILE_KEY, ProfileComponent.CODEC).ifPresent(this::setMannequinProfile);
        this.dataTracker.set(PLAYER_MODE_CUSTOMIZATION_ID, view.read(HIDDEN_LAYERS_KEY, MODEL_PARTS_CODEC).orElse(ALL_MODEL_PARTS));
        this.setMainArm(view.read(MAIN_HAND_KEY, Arm.CODEC).orElse(MAIN_ARM));
        this.setPose(view.read(POSE_KEY, POSE_CODEC).orElse(EntityPose.STANDING));
        this.setImmovable(view.getBoolean(IMMOVABLE_KEY, false));
        this.setHideDescription(view.getBoolean(HIDE_DESCRIPTION_KEY, false));
        this.setDescription(view.read(DESCRIPTION_KEY, TextCodecs.CODEC).orElse(DEFAULT_DESCRIPTION));
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.PROFILE) {
            return MannequinEntity.castComponentValue(type, this.getMannequinProfile());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.PROFILE);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.PROFILE) {
            this.setMannequinProfile(MannequinEntity.castComponentValue(DataComponentTypes.PROFILE, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }
}

