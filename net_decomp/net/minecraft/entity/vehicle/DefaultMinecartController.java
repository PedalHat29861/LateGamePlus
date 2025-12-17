/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartController;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class DefaultMinecartController
extends MinecartController {
    private static final double field_52547 = 0.01;
    private static final double field_54466 = 0.2;
    private static final double field_54467 = 0.4;
    private static final double field_54468 = 0.4;
    private final PositionInterpolator interpolator;
    private Vec3d velocity = Vec3d.ZERO;

    public DefaultMinecartController(AbstractMinecartEntity abstractMinecartEntity) {
        super(abstractMinecartEntity);
        this.interpolator = new PositionInterpolator((Entity)abstractMinecartEntity, this::onLerp);
    }

    @Override
    public PositionInterpolator getInterpolator() {
        return this.interpolator;
    }

    public void onLerp(PositionInterpolator interpolator) {
        this.setVelocity(this.velocity);
    }

    @Override
    public void setLerpTargetVelocity(Vec3d vec3d) {
        this.velocity = vec3d;
        this.setVelocity(this.velocity);
    }

    @Override
    public void tick() {
        double f;
        World world = this.getWorld();
        if (!(world instanceof ServerWorld)) {
            if (this.interpolator.isInterpolating()) {
                this.interpolator.tick();
            } else {
                this.minecart.refreshPosition();
                this.setPitch(this.getPitch() % 360.0f);
                this.setYaw(this.getYaw() % 360.0f);
            }
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        this.minecart.applyGravity();
        BlockPos blockPos = this.minecart.getRailOrMinecartPos();
        BlockState blockState = this.getWorld().getBlockState(blockPos);
        boolean bl = AbstractRailBlock.isRail(blockState);
        this.minecart.setOnRail(bl);
        if (bl) {
            this.moveOnRail(serverWorld);
            if (blockState.isOf(Blocks.ACTIVATOR_RAIL)) {
                this.minecart.onActivatorRail(serverWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockState.get(PoweredRailBlock.POWERED));
            }
        } else {
            this.minecart.moveOffRail(serverWorld);
        }
        this.minecart.tickBlockCollision();
        this.setPitch(0.0f);
        double d = this.minecart.lastX - this.getX();
        double e = this.minecart.lastZ - this.getZ();
        if (d * d + e * e > 0.001) {
            this.setYaw((float)(MathHelper.atan2(e, d) * 180.0 / Math.PI));
            if (this.minecart.isYawFlipped()) {
                this.setYaw(this.getYaw() + 180.0f);
            }
        }
        if ((f = (double)MathHelper.wrapDegrees(this.getYaw() - this.minecart.lastYaw)) < -170.0 || f >= 170.0) {
            this.setYaw(this.getYaw() + 180.0f);
            this.minecart.setYawFlipped(!this.minecart.isYawFlipped());
        }
        this.setPitch(this.getPitch() % 360.0f);
        this.setYaw(this.getYaw() % 360.0f);
        this.handleCollision();
    }

    @Override
    public void moveOnRail(ServerWorld world) {
        double v;
        Vec3d vec3d6;
        double t;
        double s;
        double r;
        Vec3d vec3d3;
        BlockPos blockPos = this.minecart.getRailOrMinecartPos();
        BlockState blockState = this.getWorld().getBlockState(blockPos);
        this.minecart.onLanding();
        double d = this.minecart.getX();
        double e = this.minecart.getY();
        double f = this.minecart.getZ();
        Vec3d vec3d = this.snapPositionToRail(d, e, f);
        e = blockPos.getY();
        boolean bl = false;
        boolean bl2 = false;
        if (blockState.isOf(Blocks.POWERED_RAIL)) {
            bl = blockState.get(PoweredRailBlock.POWERED);
            bl2 = !bl;
        }
        double g = 0.0078125;
        if (this.minecart.isTouchingWater()) {
            g *= 0.2;
        }
        Vec3d vec3d2 = this.getVelocity();
        RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
        switch (railShape) {
            case ASCENDING_EAST: {
                this.setVelocity(vec3d2.add(-g, 0.0, 0.0));
                e += 1.0;
                break;
            }
            case ASCENDING_WEST: {
                this.setVelocity(vec3d2.add(g, 0.0, 0.0));
                e += 1.0;
                break;
            }
            case ASCENDING_NORTH: {
                this.setVelocity(vec3d2.add(0.0, 0.0, g));
                e += 1.0;
                break;
            }
            case ASCENDING_SOUTH: {
                this.setVelocity(vec3d2.add(0.0, 0.0, -g));
                e += 1.0;
            }
        }
        vec3d2 = this.getVelocity();
        Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(railShape);
        Vec3i vec3i = (Vec3i)pair.getFirst();
        Vec3i vec3i2 = (Vec3i)pair.getSecond();
        double h = vec3i2.getX() - vec3i.getX();
        double i = vec3i2.getZ() - vec3i.getZ();
        double j = Math.sqrt(h * h + i * i);
        double k = vec3d2.x * h + vec3d2.z * i;
        if (k < 0.0) {
            h = -h;
            i = -i;
        }
        double l = Math.min(2.0, vec3d2.horizontalLength());
        vec3d2 = new Vec3d(l * h / j, vec3d2.y, l * i / j);
        this.setVelocity(vec3d2);
        Entity entity = this.minecart.getFirstPassenger();
        Entity entity2 = this.minecart.getFirstPassenger();
        if (entity2 instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity2;
            vec3d3 = serverPlayerEntity.getInputVelocityForMinecart();
        } else {
            vec3d3 = Vec3d.ZERO;
        }
        if (entity instanceof PlayerEntity && vec3d3.lengthSquared() > 0.0) {
            Vec3d vec3d4 = vec3d3.normalize();
            double m = this.getVelocity().horizontalLengthSquared();
            if (vec3d4.lengthSquared() > 0.0 && m < 0.01) {
                this.setVelocity(this.getVelocity().add(vec3d3.x * 0.001, 0.0, vec3d3.z * 0.001));
                bl2 = false;
            }
        }
        if (bl2) {
            double n = this.getVelocity().horizontalLength();
            if (n < 0.03) {
                this.setVelocity(Vec3d.ZERO);
            } else {
                this.setVelocity(this.getVelocity().multiply(0.5, 0.0, 0.5));
            }
        }
        double n = (double)blockPos.getX() + 0.5 + (double)vec3i.getX() * 0.5;
        double o = (double)blockPos.getZ() + 0.5 + (double)vec3i.getZ() * 0.5;
        double p = (double)blockPos.getX() + 0.5 + (double)vec3i2.getX() * 0.5;
        double q = (double)blockPos.getZ() + 0.5 + (double)vec3i2.getZ() * 0.5;
        h = p - n;
        i = q - o;
        if (h == 0.0) {
            r = f - (double)blockPos.getZ();
        } else if (i == 0.0) {
            r = d - (double)blockPos.getX();
        } else {
            s = d - n;
            t = f - o;
            r = (s * h + t * i) * 2.0;
        }
        d = n + h * r;
        f = o + i * r;
        this.setPos(d, e, f);
        s = this.minecart.hasPassengers() ? 0.75 : 1.0;
        t = this.minecart.getMaxSpeed(world);
        vec3d2 = this.getVelocity();
        this.minecart.move(MovementType.SELF, new Vec3d(MathHelper.clamp(s * vec3d2.x, -t, t), 0.0, MathHelper.clamp(s * vec3d2.z, -t, t)));
        if (vec3i.getY() != 0 && MathHelper.floor(this.minecart.getX()) - blockPos.getX() == vec3i.getX() && MathHelper.floor(this.minecart.getZ()) - blockPos.getZ() == vec3i.getZ()) {
            this.setPos(this.minecart.getX(), this.minecart.getY() + (double)vec3i.getY(), this.minecart.getZ());
        } else if (vec3i2.getY() != 0 && MathHelper.floor(this.minecart.getX()) - blockPos.getX() == vec3i2.getX() && MathHelper.floor(this.minecart.getZ()) - blockPos.getZ() == vec3i2.getZ()) {
            this.setPos(this.minecart.getX(), this.minecart.getY() + (double)vec3i2.getY(), this.minecart.getZ());
        }
        this.setVelocity(this.minecart.applySlowdown(this.getVelocity()));
        Vec3d vec3d5 = this.snapPositionToRail(this.minecart.getX(), this.minecart.getY(), this.minecart.getZ());
        if (vec3d5 != null && vec3d != null) {
            double u = (vec3d.y - vec3d5.y) * 0.05;
            vec3d6 = this.getVelocity();
            v = vec3d6.horizontalLength();
            if (v > 0.0) {
                this.setVelocity(vec3d6.multiply((v + u) / v, 1.0, (v + u) / v));
            }
            this.setPos(this.minecart.getX(), vec3d5.y, this.minecart.getZ());
        }
        int w = MathHelper.floor(this.minecart.getX());
        int x = MathHelper.floor(this.minecart.getZ());
        if (w != blockPos.getX() || x != blockPos.getZ()) {
            vec3d6 = this.getVelocity();
            v = vec3d6.horizontalLength();
            this.setVelocity(v * (double)(w - blockPos.getX()), vec3d6.y, v * (double)(x - blockPos.getZ()));
        }
        if (bl) {
            vec3d6 = this.getVelocity();
            v = vec3d6.horizontalLength();
            if (v > 0.01) {
                double y = 0.06;
                this.setVelocity(vec3d6.add(vec3d6.x / v * 0.06, 0.0, vec3d6.z / v * 0.06));
            } else {
                Vec3d vec3d7 = this.getVelocity();
                double z = vec3d7.x;
                double aa = vec3d7.z;
                if (railShape == RailShape.EAST_WEST) {
                    if (this.minecart.willHitBlockAt(blockPos.west())) {
                        z = 0.02;
                    } else if (this.minecart.willHitBlockAt(blockPos.east())) {
                        z = -0.02;
                    }
                } else if (railShape == RailShape.NORTH_SOUTH) {
                    if (this.minecart.willHitBlockAt(blockPos.north())) {
                        aa = 0.02;
                    } else if (this.minecart.willHitBlockAt(blockPos.south())) {
                        aa = -0.02;
                    }
                } else {
                    return;
                }
                this.setVelocity(z, vec3d7.y, aa);
            }
        }
    }

    public @Nullable Vec3d simulateMovement(double x, double y, double z, double movement) {
        BlockState blockState;
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);
        if (this.getWorld().getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }
        if (AbstractRailBlock.isRail(blockState = this.getWorld().getBlockState(new BlockPos(i, j, k)))) {
            RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
            y = j;
            if (railShape.isAscending()) {
                y = j + 1;
            }
            Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(railShape);
            Vec3i vec3i = (Vec3i)pair.getFirst();
            Vec3i vec3i2 = (Vec3i)pair.getSecond();
            double d = vec3i2.getX() - vec3i.getX();
            double e = vec3i2.getZ() - vec3i.getZ();
            double f = Math.sqrt(d * d + e * e);
            if (vec3i.getY() != 0 && MathHelper.floor(x += (d /= f) * movement) - i == vec3i.getX() && MathHelper.floor(z += (e /= f) * movement) - k == vec3i.getZ()) {
                y += (double)vec3i.getY();
            } else if (vec3i2.getY() != 0 && MathHelper.floor(x) - i == vec3i2.getX() && MathHelper.floor(z) - k == vec3i2.getZ()) {
                y += (double)vec3i2.getY();
            }
            return this.snapPositionToRail(x, y, z);
        }
        return null;
    }

    public @Nullable Vec3d snapPositionToRail(double x, double y, double z) {
        BlockState blockState;
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);
        if (this.getWorld().getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }
        if (AbstractRailBlock.isRail(blockState = this.getWorld().getBlockState(new BlockPos(i, j, k)))) {
            double p;
            RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(railShape);
            Vec3i vec3i = (Vec3i)pair.getFirst();
            Vec3i vec3i2 = (Vec3i)pair.getSecond();
            double d = (double)i + 0.5 + (double)vec3i.getX() * 0.5;
            double e = (double)j + 0.0625 + (double)vec3i.getY() * 0.5;
            double f = (double)k + 0.5 + (double)vec3i.getZ() * 0.5;
            double g = (double)i + 0.5 + (double)vec3i2.getX() * 0.5;
            double h = (double)j + 0.0625 + (double)vec3i2.getY() * 0.5;
            double l = (double)k + 0.5 + (double)vec3i2.getZ() * 0.5;
            double m = g - d;
            double n = (h - e) * 2.0;
            double o = l - f;
            if (m == 0.0) {
                p = z - (double)k;
            } else if (o == 0.0) {
                p = x - (double)i;
            } else {
                double q = x - d;
                double r = z - f;
                p = (q * m + r * o) * 2.0;
            }
            x = d + m * p;
            y = e + n * p;
            z = f + o * p;
            if (n < 0.0) {
                y += 1.0;
            } else if (n > 0.0) {
                y += 0.5;
            }
            return new Vec3d(x, y, z);
        }
        return null;
    }

    @Override
    public double moveAlongTrack(BlockPos blockPos, RailShape railShape, double remainingMovement) {
        return 0.0;
    }

    @Override
    public boolean handleCollision() {
        block4: {
            Box box;
            block3: {
                box = this.minecart.getBoundingBox().expand(0.2f, 0.0, 0.2f);
                if (!this.minecart.isRideable() || !(this.getVelocity().horizontalLengthSquared() >= 0.01)) break block3;
                List<Entity> list = this.getWorld().getOtherEntities(this.minecart, box, EntityPredicates.canBePushedBy(this.minecart));
                if (list.isEmpty()) break block4;
                for (Entity entity : list) {
                    if (entity instanceof PlayerEntity || entity instanceof IronGolemEntity || entity instanceof AbstractMinecartEntity || this.minecart.hasPassengers() || entity.hasVehicle()) {
                        entity.pushAwayFrom(this.minecart);
                        continue;
                    }
                    entity.startRiding(this.minecart);
                }
                break block4;
            }
            for (Entity entity2 : this.getWorld().getOtherEntities(this.minecart, box)) {
                if (this.minecart.hasPassenger(entity2) || !entity2.isPushable() || !(entity2 instanceof AbstractMinecartEntity)) continue;
                entity2.pushAwayFrom(this.minecart);
            }
        }
        return false;
    }

    @Override
    public Direction getHorizontalFacing() {
        return this.minecart.isYawFlipped() ? this.minecart.getHorizontalFacing().getOpposite().rotateYClockwise() : this.minecart.getHorizontalFacing().rotateYClockwise();
    }

    @Override
    public Vec3d limitSpeed(Vec3d velocity) {
        if (Double.isNaN(velocity.x) || Double.isNaN(velocity.y) || Double.isNaN(velocity.z)) {
            return Vec3d.ZERO;
        }
        return new Vec3d(MathHelper.clamp(velocity.x, -0.4, 0.4), velocity.y, MathHelper.clamp(velocity.z, -0.4, 0.4));
    }

    @Override
    public double getMaxSpeed(ServerWorld world) {
        return this.minecart.isTouchingWater() ? 0.2 : 0.4;
    }

    @Override
    public double getSpeedRetention() {
        return this.minecart.hasPassengers() ? 0.997 : 0.96;
    }
}

