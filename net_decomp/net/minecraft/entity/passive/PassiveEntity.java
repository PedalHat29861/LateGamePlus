/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public abstract class PassiveEntity
extends PathAwareEntity {
    private static final TrackedData<Boolean> CHILD = DataTracker.registerData(PassiveEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final int BABY_AGE = -24000;
    private static final int HAPPY_TICKS = 40;
    protected static final int DEFAULT_AGE = 0;
    protected static final int DEFAULT_FORCED_AGE = 0;
    protected int breedingAge = 0;
    protected int forcedAge = 0;
    protected int happyTicksRemaining;

    protected PassiveEntity(EntityType<? extends PassiveEntity> entityType, World world) {
        super((EntityType<? extends PathAwareEntity>)entityType, world);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        PassiveData passiveData;
        if (entityData == null) {
            entityData = new PassiveData(true);
        }
        if ((passiveData = (PassiveData)entityData).canSpawnBaby() && passiveData.getSpawnedCount() > 0 && world.getRandom().nextFloat() <= passiveData.getBabyChance()) {
            this.setBreedingAge(-24000);
        }
        passiveData.countSpawned();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public abstract @Nullable PassiveEntity createChild(ServerWorld var1, PassiveEntity var2);

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CHILD, false);
    }

    public boolean isReadyToBreed() {
        return false;
    }

    public int getBreedingAge() {
        if (this.getEntityWorld().isClient()) {
            return this.dataTracker.get(CHILD) != false ? -1 : 1;
        }
        return this.breedingAge;
    }

    public void growUp(int age, boolean overGrow) {
        int i;
        int j = i = this.getBreedingAge();
        if ((i += age * 20) > 0) {
            i = 0;
        }
        int k = i - j;
        this.setBreedingAge(i);
        if (overGrow) {
            this.forcedAge += k;
            if (this.happyTicksRemaining == 0) {
                this.happyTicksRemaining = 40;
            }
        }
        if (this.getBreedingAge() == 0) {
            this.setBreedingAge(this.forcedAge);
        }
    }

    public void growUp(int age) {
        this.growUp(age, false);
    }

    public void setBreedingAge(int age) {
        int i = this.getBreedingAge();
        this.breedingAge = age;
        if (i < 0 && age >= 0 || i >= 0 && age < 0) {
            this.dataTracker.set(CHILD, age < 0);
            this.onGrowUp();
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("Age", this.getBreedingAge());
        view.putInt("ForcedAge", this.forcedAge);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setBreedingAge(view.getInt("Age", 0));
        this.forcedAge = view.getInt("ForcedAge", 0);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (CHILD.equals(data)) {
            this.calculateDimensions();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getEntityWorld().isClient()) {
            if (this.happyTicksRemaining > 0) {
                if (this.happyTicksRemaining % 4 == 0) {
                    this.getEntityWorld().addParticleClient(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
                }
                --this.happyTicksRemaining;
            }
        } else if (this.isAlive()) {
            int i = this.getBreedingAge();
            if (i < 0) {
                this.setBreedingAge(++i);
            } else if (i > 0) {
                this.setBreedingAge(--i);
            }
        }
    }

    protected void onGrowUp() {
        AbstractBoatEntity abstractBoatEntity;
        Entity entity;
        if (!this.isBaby() && this.hasVehicle() && (entity = this.getVehicle()) instanceof AbstractBoatEntity && !(abstractBoatEntity = (AbstractBoatEntity)entity).isSmallerThanBoat(this)) {
            this.stopRiding();
        }
    }

    @Override
    public boolean isBaby() {
        return this.getBreedingAge() < 0;
    }

    @Override
    public void setBaby(boolean baby) {
        this.setBreedingAge(baby ? -24000 : 0);
    }

    public static int toGrowUpAge(int breedingAge) {
        return (int)((float)(breedingAge / 20) * 0.1f);
    }

    @VisibleForTesting
    public int getForcedAge() {
        return this.forcedAge;
    }

    @VisibleForTesting
    public int getHappyTicksRemaining() {
        return this.happyTicksRemaining;
    }

    public static class PassiveData
    implements EntityData {
        private int spawnCount;
        private final boolean babyAllowed;
        private final float babyChance;

        public PassiveData(boolean babyAllowed, float babyChance) {
            this.babyAllowed = babyAllowed;
            this.babyChance = babyChance;
        }

        public PassiveData(boolean babyAllowed) {
            this(babyAllowed, 0.05f);
        }

        public PassiveData(float babyChance) {
            this(true, babyChance);
        }

        public int getSpawnedCount() {
            return this.spawnCount;
        }

        public void countSpawned() {
            ++this.spawnCount;
        }

        public boolean canSpawnBaby() {
            return this.babyAllowed;
        }

        public float getBabyChance() {
            return this.babyChance;
        }
    }
}

