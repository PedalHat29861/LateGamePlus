/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.border;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.border.WorldBorderStage;

public class WorldBorder
extends PersistentState {
    public static final double STATIC_AREA_SIZE = 5.9999968E7;
    public static final double MAX_CENTER_COORDINATES = 2.9999984E7;
    public static final Codec<WorldBorder> CODEC = Properties.CODEC.xmap(WorldBorder::new, Properties::new);
    public static final PersistentStateType<WorldBorder> TYPE = new PersistentStateType<WorldBorder>("world_border", WorldBorder::new, CODEC, DataFixTypes.SAVED_DATA_WORLD_BORDER);
    private final Properties properties;
    private boolean initialized;
    private final List<WorldBorderListener> listeners = Lists.newArrayList();
    double damagePerBlock = 0.2;
    double safeZone = 5.0;
    int warningTime = 15;
    int warningBlocks = 5;
    double centerX;
    double centerZ;
    int maxRadius = 29999984;
    Area area = new StaticArea(5.9999968E7);

    public WorldBorder() {
        this(Properties.DEFAULT);
    }

    public WorldBorder(Properties properties) {
        this.properties = properties;
    }

    public boolean contains(BlockPos pos) {
        return this.contains(pos.getX(), pos.getZ());
    }

    public boolean contains(Vec3d pos) {
        return this.contains(pos.x, pos.z);
    }

    public boolean contains(ChunkPos chunkPos) {
        return this.contains(chunkPos.getStartX(), chunkPos.getStartZ()) && this.contains(chunkPos.getEndX(), chunkPos.getEndZ());
    }

    public boolean contains(Box box) {
        return this.contains(box.minX, box.minZ, box.maxX - (double)1.0E-5f, box.maxZ - (double)1.0E-5f);
    }

    private boolean contains(double minX, double minZ, double maxX, double maxZ) {
        return this.contains(minX, minZ) && this.contains(maxX, maxZ);
    }

    public boolean contains(double x, double z) {
        return this.contains(x, z, 0.0);
    }

    public boolean contains(double x, double z, double margin) {
        return x >= this.getBoundWest() - margin && x < this.getBoundEast() + margin && z >= this.getBoundNorth() - margin && z < this.getBoundSouth() + margin;
    }

    public BlockPos clampFloored(BlockPos pos) {
        return this.clampFloored(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos clampFloored(Vec3d pos) {
        return this.clampFloored(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos clampFloored(double x, double y, double z) {
        return BlockPos.ofFloored(this.clamp(x, y, z));
    }

    public Vec3d clamp(Vec3d pos) {
        return this.clamp(pos.x, pos.y, pos.z);
    }

    public Vec3d clamp(double x, double y, double z) {
        return new Vec3d(MathHelper.clamp(x, this.getBoundWest(), this.getBoundEast() - (double)1.0E-5f), y, MathHelper.clamp(z, this.getBoundNorth(), this.getBoundSouth() - (double)1.0E-5f));
    }

    public double getDistanceInsideBorder(Entity entity) {
        return this.getDistanceInsideBorder(entity.getX(), entity.getZ());
    }

    public VoxelShape asVoxelShape() {
        return this.area.asVoxelShape();
    }

    public double getDistanceInsideBorder(double x, double z) {
        double d = z - this.getBoundNorth();
        double e = this.getBoundSouth() - z;
        double f = x - this.getBoundWest();
        double g = this.getBoundEast() - x;
        double h = Math.min(f, g);
        h = Math.min(h, d);
        return Math.min(h, e);
    }

    public boolean canCollide(Entity entity, Box box) {
        double d = Math.max(MathHelper.absMax(box.getLengthX(), box.getLengthZ()), 1.0);
        return this.getDistanceInsideBorder(entity) < d * 2.0 && this.contains(entity.getX(), entity.getZ(), d);
    }

    public WorldBorderStage getStage() {
        return this.area.getStage();
    }

    public double getBoundWest() {
        return this.getBoundWest(0.0f);
    }

    public double getBoundWest(float tickProgress) {
        return this.area.getBoundWest(tickProgress);
    }

    public double getBoundNorth() {
        return this.getBoundNorth(0.0f);
    }

    public double getBoundNorth(float tickProgress) {
        return this.area.getBoundNorth(tickProgress);
    }

    public double getBoundEast() {
        return this.getBoundEast(0.0f);
    }

    public double getBoundEast(float tickProgress) {
        return this.area.getBoundEast(tickProgress);
    }

    public double getBoundSouth() {
        return this.getBoundSouth(0.0f);
    }

    public double getBoundSouth(float tickProgress) {
        return this.area.getBoundSouth(tickProgress);
    }

    public double getCenterX() {
        return this.centerX;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public void setCenter(double x, double z) {
        this.centerX = x;
        this.centerZ = z;
        this.area.onCenterChanged();
        this.markDirty();
        for (WorldBorderListener worldBorderListener : this.getListeners()) {
            worldBorderListener.onCenterChanged(this, x, z);
        }
    }

    public double getSize() {
        return this.area.getSize();
    }

    public long getSizeLerpTime() {
        return this.area.getSizeLerpTime();
    }

    public double getSizeLerpTarget() {
        return this.area.getSizeLerpTarget();
    }

    public void setSize(double size) {
        this.area = new StaticArea(size);
        this.markDirty();
        for (WorldBorderListener worldBorderListener : this.getListeners()) {
            worldBorderListener.onSizeChange(this, size);
        }
    }

    public void interpolateSize(double fromSize, double toSize, long timeDuration, long timeStart) {
        this.area = fromSize == toSize ? new StaticArea(toSize) : new MovingArea(fromSize, toSize, timeDuration, timeStart);
        this.markDirty();
        for (WorldBorderListener worldBorderListener : this.getListeners()) {
            worldBorderListener.onInterpolateSize(this, fromSize, toSize, timeDuration, timeStart);
        }
    }

    protected List<WorldBorderListener> getListeners() {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(WorldBorderListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(WorldBorderListener listener) {
        this.listeners.remove(listener);
    }

    public void setMaxRadius(int maxRadius) {
        this.maxRadius = maxRadius;
        this.area.onMaxRadiusChanged();
    }

    public int getMaxRadius() {
        return this.maxRadius;
    }

    public double getSafeZone() {
        return this.safeZone;
    }

    public void setSafeZone(double safeZone) {
        this.safeZone = safeZone;
        this.markDirty();
        for (WorldBorderListener worldBorderListener : this.getListeners()) {
            worldBorderListener.onSafeZoneChanged(this, safeZone);
        }
    }

    public double getDamagePerBlock() {
        return this.damagePerBlock;
    }

    public void setDamagePerBlock(double damagePerBlock) {
        this.damagePerBlock = damagePerBlock;
        this.markDirty();
        for (WorldBorderListener worldBorderListener : this.getListeners()) {
            worldBorderListener.onDamagePerBlockChanged(this, damagePerBlock);
        }
    }

    public double getShrinkingSpeed() {
        return this.area.getShrinkingSpeed();
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public void setWarningTime(int warningTime) {
        this.warningTime = warningTime;
        this.markDirty();
        for (WorldBorderListener worldBorderListener : this.getListeners()) {
            worldBorderListener.onWarningTimeChanged(this, warningTime);
        }
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int warningBlocks) {
        this.warningBlocks = warningBlocks;
        this.markDirty();
        for (WorldBorderListener worldBorderListener : this.getListeners()) {
            worldBorderListener.onWarningBlocksChanged(this, warningBlocks);
        }
    }

    public void tick() {
        this.area = this.area.getAreaInstance();
    }

    public void ensureInitialized(long time) {
        if (!this.initialized) {
            this.setCenter(this.properties.centerX(), this.properties.centerZ());
            this.setDamagePerBlock(this.properties.damagePerBlock());
            this.setSafeZone(this.properties.safeZone());
            this.setWarningBlocks(this.properties.warningBlocks());
            this.setWarningTime(this.properties.warningTime());
            if (this.properties.lerpTime() > 0L) {
                this.interpolateSize(this.properties.size(), this.properties.lerpTarget(), this.properties.lerpTime(), time);
            } else {
                this.setSize(this.properties.size());
            }
            this.initialized = true;
        }
    }

    public record Properties(double centerX, double centerZ, double damagePerBlock, double safeZone, int warningBlocks, int warningTime, double size, long lerpTime, double lerpTarget) {
        public static final Properties DEFAULT = new Properties(0.0, 0.0, 0.2, 5.0, 5, 300, 5.9999968E7, 0L, 0.0);
        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.doubleRange((double)-2.9999984E7, (double)2.9999984E7).fieldOf("center_x").forGetter(Properties::centerX), (App)Codec.doubleRange((double)-2.9999984E7, (double)2.9999984E7).fieldOf("center_z").forGetter(Properties::centerZ), (App)Codec.DOUBLE.fieldOf("damage_per_block").forGetter(Properties::damagePerBlock), (App)Codec.DOUBLE.fieldOf("safe_zone").forGetter(Properties::safeZone), (App)Codec.INT.fieldOf("warning_blocks").forGetter(Properties::warningBlocks), (App)Codec.INT.fieldOf("warning_time").forGetter(Properties::warningTime), (App)Codec.DOUBLE.fieldOf("size").forGetter(Properties::size), (App)Codec.LONG.fieldOf("lerp_time").forGetter(Properties::lerpTime), (App)Codec.DOUBLE.fieldOf("lerp_target").forGetter(Properties::lerpTarget)).apply((Applicative)instance, Properties::new));

        public Properties(WorldBorder worldBorder) {
            this(worldBorder.centerX, worldBorder.centerZ, worldBorder.damagePerBlock, worldBorder.safeZone, worldBorder.warningBlocks, worldBorder.warningTime, worldBorder.area.getSize(), worldBorder.area.getSizeLerpTime(), worldBorder.area.getSizeLerpTarget());
        }
    }

    class StaticArea
    implements Area {
        private final double size;
        private double boundWest;
        private double boundNorth;
        private double boundEast;
        private double boundSouth;
        private VoxelShape shape;

        public StaticArea(double size) {
            this.size = size;
            this.recalculateBounds();
        }

        @Override
        public double getBoundWest(float tickProgress) {
            return this.boundWest;
        }

        @Override
        public double getBoundEast(float tickProgress) {
            return this.boundEast;
        }

        @Override
        public double getBoundNorth(float tickProgress) {
            return this.boundNorth;
        }

        @Override
        public double getBoundSouth(float tickProgress) {
            return this.boundSouth;
        }

        @Override
        public double getSize() {
            return this.size;
        }

        @Override
        public WorldBorderStage getStage() {
            return WorldBorderStage.STATIONARY;
        }

        @Override
        public double getShrinkingSpeed() {
            return 0.0;
        }

        @Override
        public long getSizeLerpTime() {
            return 0L;
        }

        @Override
        public double getSizeLerpTarget() {
            return this.size;
        }

        private void recalculateBounds() {
            this.boundWest = MathHelper.clamp(WorldBorder.this.getCenterX() - this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
            this.boundNorth = MathHelper.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
            this.boundEast = MathHelper.clamp(WorldBorder.this.getCenterX() + this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
            this.boundSouth = MathHelper.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
            this.shape = VoxelShapes.combineAndSimplify(VoxelShapes.UNBOUNDED, VoxelShapes.cuboid(Math.floor(this.getBoundWest(0.0f)), Double.NEGATIVE_INFINITY, Math.floor(this.getBoundNorth(0.0f)), Math.ceil(this.getBoundEast(0.0f)), Double.POSITIVE_INFINITY, Math.ceil(this.getBoundSouth(0.0f))), BooleanBiFunction.ONLY_FIRST);
        }

        @Override
        public void onMaxRadiusChanged() {
            this.recalculateBounds();
        }

        @Override
        public void onCenterChanged() {
            this.recalculateBounds();
        }

        @Override
        public Area getAreaInstance() {
            return this;
        }

        @Override
        public VoxelShape asVoxelShape() {
            return this.shape;
        }
    }

    static interface Area {
        public double getBoundWest(float var1);

        public double getBoundEast(float var1);

        public double getBoundNorth(float var1);

        public double getBoundSouth(float var1);

        public double getSize();

        public double getShrinkingSpeed();

        public long getSizeLerpTime();

        public double getSizeLerpTarget();

        public WorldBorderStage getStage();

        public void onMaxRadiusChanged();

        public void onCenterChanged();

        public Area getAreaInstance();

        public VoxelShape asVoxelShape();
    }

    class MovingArea
    implements Area {
        private final double oldSize;
        private final double newSize;
        private final long timeEnd;
        private final long timeStart;
        private final double timeDuration;
        private long remainingTimeDuration;
        private double currentSize;
        private double lastSize;

        MovingArea(double oldSize, double newSize, long timeDuration, long timeStart) {
            double d;
            this.oldSize = oldSize;
            this.newSize = newSize;
            this.timeDuration = timeDuration;
            this.remainingTimeDuration = timeDuration;
            this.timeStart = timeStart;
            this.timeEnd = this.timeStart + timeDuration;
            this.currentSize = d = this.currentSize();
            this.lastSize = d;
        }

        @Override
        public double getBoundWest(float tickProgress) {
            return MathHelper.clamp(WorldBorder.this.getCenterX() - MathHelper.lerp((double)tickProgress, this.getLastSize(), this.getSize()) / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
        }

        @Override
        public double getBoundNorth(float tickProgress) {
            return MathHelper.clamp(WorldBorder.this.getCenterZ() - MathHelper.lerp((double)tickProgress, this.getLastSize(), this.getSize()) / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
        }

        @Override
        public double getBoundEast(float tickProgress) {
            return MathHelper.clamp(WorldBorder.this.getCenterX() + MathHelper.lerp((double)tickProgress, this.getLastSize(), this.getSize()) / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
        }

        @Override
        public double getBoundSouth(float tickProgress) {
            return MathHelper.clamp(WorldBorder.this.getCenterZ() + MathHelper.lerp((double)tickProgress, this.getLastSize(), this.getSize()) / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
        }

        @Override
        public double getSize() {
            return this.currentSize;
        }

        public double getLastSize() {
            return this.lastSize;
        }

        private double currentSize() {
            double d = (this.timeDuration - (double)this.remainingTimeDuration) / this.timeDuration;
            return d < 1.0 ? MathHelper.lerp(d, this.oldSize, this.newSize) : this.newSize;
        }

        @Override
        public double getShrinkingSpeed() {
            return Math.abs(this.oldSize - this.newSize) / (double)(this.timeEnd - this.timeStart);
        }

        @Override
        public long getSizeLerpTime() {
            return this.remainingTimeDuration;
        }

        @Override
        public double getSizeLerpTarget() {
            return this.newSize;
        }

        @Override
        public WorldBorderStage getStage() {
            return this.newSize < this.oldSize ? WorldBorderStage.SHRINKING : WorldBorderStage.GROWING;
        }

        @Override
        public void onCenterChanged() {
        }

        @Override
        public void onMaxRadiusChanged() {
        }

        @Override
        public Area getAreaInstance() {
            --this.remainingTimeDuration;
            this.lastSize = this.currentSize;
            this.currentSize = this.currentSize();
            if (this.remainingTimeDuration <= 0L) {
                WorldBorder.this.markDirty();
                return new StaticArea(this.newSize);
            }
            return this;
        }

        @Override
        public VoxelShape asVoxelShape() {
            return VoxelShapes.combineAndSimplify(VoxelShapes.UNBOUNDED, VoxelShapes.cuboid(Math.floor(this.getBoundWest(0.0f)), Double.NEGATIVE_INFINITY, Math.floor(this.getBoundNorth(0.0f)), Math.ceil(this.getBoundEast(0.0f)), Double.POSITIVE_INFINITY, Math.ceil(this.getBoundSouth(0.0f))), BooleanBiFunction.ONLY_FIRST);
        }
    }
}

