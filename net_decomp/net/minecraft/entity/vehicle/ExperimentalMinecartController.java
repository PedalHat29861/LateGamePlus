/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartController;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class ExperimentalMinecartController
extends MinecartController {
    public static final int REFRESH_FREQUENCY = 3;
    public static final double field_52528 = 0.1;
    public static final double field_53756 = 0.005;
    private @Nullable InterpolatedStep lastReturnedInterpolatedStep;
    private int lastQueriedTicksToNextRefresh;
    private float lastQueriedTickProgress;
    private int ticksToNextRefresh = 0;
    public final List<Step> stagingLerpSteps = new LinkedList<Step>();
    public final List<Step> currentLerpSteps = new LinkedList<Step>();
    public double totalWeight = 0.0;
    public Step initialStep = Step.ZERO;

    public ExperimentalMinecartController(AbstractMinecartEntity abstractMinecartEntity) {
        super(abstractMinecartEntity);
    }

    @Override
    public void tick() {
        World world = this.getWorld();
        if (!(world instanceof ServerWorld)) {
            this.tickClient();
            boolean bl = AbstractRailBlock.isRail(this.getWorld().getBlockState(this.minecart.getRailOrMinecartPos()));
            this.minecart.setOnRail(bl);
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        BlockPos blockPos = this.minecart.getRailOrMinecartPos();
        BlockState blockState = this.getWorld().getBlockState(blockPos);
        if (this.minecart.isFirstUpdate()) {
            this.minecart.setOnRail(AbstractRailBlock.isRail(blockState));
            this.adjustToRail(blockPos, blockState, true);
        }
        this.minecart.applyGravity();
        this.minecart.moveOnRail(serverWorld);
    }

    private void tickClient() {
        if (--this.ticksToNextRefresh <= 0) {
            this.setInitialStep();
            this.currentLerpSteps.clear();
            if (!this.stagingLerpSteps.isEmpty()) {
                this.currentLerpSteps.addAll(this.stagingLerpSteps);
                this.stagingLerpSteps.clear();
                this.totalWeight = 0.0;
                for (Step step : this.currentLerpSteps) {
                    this.totalWeight += (double)step.weight;
                }
                int n = this.ticksToNextRefresh = this.totalWeight == 0.0 ? 0 : 3;
            }
        }
        if (this.hasCurrentLerpSteps()) {
            this.setPos(this.getLerpedPosition(1.0f));
            this.setVelocity(this.getLerpedVelocity(1.0f));
            this.setPitch(this.getLerpedPitch(1.0f));
            this.setYaw(this.getLerpedYaw(1.0f));
        }
    }

    public void setInitialStep() {
        this.initialStep = new Step(this.getPos(), this.getVelocity(), this.getYaw(), this.getPitch(), 0.0f);
    }

    public boolean hasCurrentLerpSteps() {
        return !this.currentLerpSteps.isEmpty();
    }

    public float getLerpedPitch(float tickProgress) {
        InterpolatedStep interpolatedStep = this.getLerpedStep(tickProgress);
        return MathHelper.lerpAngleDegrees(interpolatedStep.partialTicksInStep, interpolatedStep.previousStep.xRot, interpolatedStep.currentStep.xRot);
    }

    public float getLerpedYaw(float tickProgress) {
        InterpolatedStep interpolatedStep = this.getLerpedStep(tickProgress);
        return MathHelper.lerpAngleDegrees(interpolatedStep.partialTicksInStep, interpolatedStep.previousStep.yRot, interpolatedStep.currentStep.yRot);
    }

    public Vec3d getLerpedPosition(float tickProgress) {
        InterpolatedStep interpolatedStep = this.getLerpedStep(tickProgress);
        return MathHelper.lerp((double)interpolatedStep.partialTicksInStep, interpolatedStep.previousStep.position, interpolatedStep.currentStep.position);
    }

    public Vec3d getLerpedVelocity(float tickProgress) {
        InterpolatedStep interpolatedStep = this.getLerpedStep(tickProgress);
        return MathHelper.lerp((double)interpolatedStep.partialTicksInStep, interpolatedStep.previousStep.movement, interpolatedStep.currentStep.movement);
    }

    private InterpolatedStep getLerpedStep(float tickProgress) {
        int i;
        if (tickProgress == this.lastQueriedTickProgress && this.ticksToNextRefresh == this.lastQueriedTicksToNextRefresh && this.lastReturnedInterpolatedStep != null) {
            return this.lastReturnedInterpolatedStep;
        }
        float f = ((float)(3 - this.ticksToNextRefresh) + tickProgress) / 3.0f;
        float g = 0.0f;
        float h = 1.0f;
        boolean bl = false;
        for (i = 0; i < this.currentLerpSteps.size(); ++i) {
            float j = this.currentLerpSteps.get((int)i).weight;
            if (j <= 0.0f || !((double)(g += j) >= this.totalWeight * (double)f)) continue;
            float k = g - j;
            h = (float)(((double)f * this.totalWeight - (double)k) / (double)j);
            bl = true;
            break;
        }
        if (!bl) {
            i = this.currentLerpSteps.size() - 1;
        }
        Step step = this.currentLerpSteps.get(i);
        Step step2 = i > 0 ? this.currentLerpSteps.get(i - 1) : this.initialStep;
        this.lastReturnedInterpolatedStep = new InterpolatedStep(h, step, step2);
        this.lastQueriedTicksToNextRefresh = this.ticksToNextRefresh;
        this.lastQueriedTickProgress = tickProgress;
        return this.lastReturnedInterpolatedStep;
    }

    public void adjustToRail(BlockPos pos, BlockState blockState, boolean ignoreWeight) {
        boolean bl4;
        Vec3d vec3d10;
        Vec3d vec3d7;
        boolean bl;
        if (!AbstractRailBlock.isRail(blockState)) {
            return;
        }
        RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
        Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(railShape);
        Vec3d vec3d = new Vec3d((Vec3i)pair.getFirst()).multiply(0.5);
        Vec3d vec3d2 = new Vec3d((Vec3i)pair.getSecond()).multiply(0.5);
        Vec3d vec3d3 = vec3d.getHorizontal();
        Vec3d vec3d4 = vec3d2.getHorizontal();
        if (this.getVelocity().length() > (double)1.0E-5f && this.getVelocity().dotProduct(vec3d3) < this.getVelocity().dotProduct(vec3d4) || this.ascends(vec3d4, railShape)) {
            Vec3d vec3d5 = vec3d3;
            vec3d3 = vec3d4;
            vec3d4 = vec3d5;
        }
        float f = 180.0f - (float)(Math.atan2(vec3d3.z, vec3d3.x) * 180.0 / Math.PI);
        f += this.minecart.isYawFlipped() ? 180.0f : 0.0f;
        Vec3d vec3d6 = this.getPos();
        boolean bl2 = bl = vec3d.getX() != vec3d2.getX() && vec3d.getZ() != vec3d2.getZ();
        if (bl) {
            vec3d7 = vec3d2.subtract(vec3d);
            Vec3d vec3d8 = vec3d6.subtract(pos.toBottomCenterPos()).subtract(vec3d);
            Vec3d vec3d9 = vec3d7.multiply(vec3d7.dotProduct(vec3d8) / vec3d7.dotProduct(vec3d7));
            vec3d10 = pos.toBottomCenterPos().add(vec3d).add(vec3d9);
            f = 180.0f - (float)(Math.atan2(vec3d9.z, vec3d9.x) * 180.0 / Math.PI);
            f += this.minecart.isYawFlipped() ? 180.0f : 0.0f;
        } else {
            boolean bl22 = vec3d.subtract((Vec3d)vec3d2).x != 0.0;
            boolean bl3 = vec3d.subtract((Vec3d)vec3d2).z != 0.0;
            vec3d10 = new Vec3d(bl3 ? pos.toCenterPos().x : vec3d6.x, pos.getY(), bl22 ? pos.toCenterPos().z : vec3d6.z);
        }
        vec3d7 = vec3d10.subtract(vec3d6);
        this.setPos(vec3d6.add(vec3d7));
        float g = 0.0f;
        boolean bl3 = bl4 = vec3d.getY() != vec3d2.getY();
        if (bl4) {
            Vec3d vec3d11 = pos.toBottomCenterPos().add(vec3d4);
            double d = vec3d11.distanceTo(this.getPos());
            this.setPos(this.getPos().add(0.0, d + 0.1, 0.0));
            g = this.minecart.isYawFlipped() ? 45.0f : -45.0f;
        } else {
            this.setPos(this.getPos().add(0.0, 0.1, 0.0));
        }
        this.setAngles(f, g);
        double e = vec3d6.distanceTo(this.getPos());
        if (e > 0.0) {
            this.stagingLerpSteps.add(new Step(this.getPos(), this.getVelocity(), this.getYaw(), this.getPitch(), ignoreWeight ? 0.0f : (float)e));
        }
    }

    private void setAngles(float yaw, float pitch) {
        double d = Math.abs(yaw - this.getYaw());
        if (d >= 175.0 && d <= 185.0) {
            this.minecart.setYawFlipped(!this.minecart.isYawFlipped());
            yaw -= 180.0f;
            pitch *= -1.0f;
        }
        pitch = Math.clamp(pitch, -45.0f, 45.0f);
        this.setPitch(pitch % 360.0f);
        this.setYaw(yaw % 360.0f);
    }

    @Override
    public void moveOnRail(ServerWorld world) {
        MoveIteration moveIteration = new MoveIteration();
        while (moveIteration.shouldContinue() && this.minecart.isAlive()) {
            Vec3d vec3d2;
            Vec3d vec3d = this.getVelocity();
            BlockPos blockPos = this.minecart.getRailOrMinecartPos();
            BlockState blockState = this.getWorld().getBlockState(blockPos);
            boolean bl = AbstractRailBlock.isRail(blockState);
            if (this.minecart.isOnRail() != bl) {
                this.minecart.setOnRail(bl);
                this.adjustToRail(blockPos, blockState, false);
            }
            if (bl) {
                this.minecart.onLanding();
                this.minecart.resetPosition();
                if (blockState.isOf(Blocks.ACTIVATOR_RAIL)) {
                    this.minecart.onActivatorRail(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockState.get(PoweredRailBlock.POWERED));
                }
                RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
                vec3d2 = this.calcNewHorizontalVelocity(world, vec3d.getHorizontal(), moveIteration, blockPos, blockState, railShape);
                moveIteration.remainingMovement = moveIteration.initial ? vec3d2.horizontalLength() : (moveIteration.remainingMovement += vec3d2.horizontalLength() - vec3d.horizontalLength());
                this.setVelocity(vec3d2);
                moveIteration.remainingMovement = this.minecart.moveAlongTrack(blockPos, railShape, moveIteration.remainingMovement);
            } else {
                this.minecart.moveOffRail(world);
                moveIteration.remainingMovement = 0.0;
            }
            Vec3d vec3d3 = this.getPos();
            vec3d2 = vec3d3.subtract(this.minecart.getLastRenderPos());
            double d = vec3d2.length();
            if (d > (double)1.0E-5f) {
                if (vec3d2.horizontalLengthSquared() > (double)1.0E-5f) {
                    float f = 180.0f - (float)(Math.atan2(vec3d2.z, vec3d2.x) * 180.0 / Math.PI);
                    float g = this.minecart.isOnGround() && !this.minecart.isOnRail() ? 0.0f : 90.0f - (float)(Math.atan2(vec3d2.horizontalLength(), vec3d2.y) * 180.0 / Math.PI);
                    this.setAngles(f += this.minecart.isYawFlipped() ? 180.0f : 0.0f, g *= this.minecart.isYawFlipped() ? -1.0f : 1.0f);
                } else if (!this.minecart.isOnRail()) {
                    this.setPitch(this.minecart.isOnGround() ? 0.0f : MathHelper.lerpAngleDegrees(0.2f, this.getPitch(), 0.0f));
                }
                this.stagingLerpSteps.add(new Step(vec3d3, this.getVelocity(), this.getYaw(), this.getPitch(), (float)Math.min(d, this.getMaxSpeed(world))));
            } else if (vec3d.horizontalLengthSquared() > 0.0) {
                this.stagingLerpSteps.add(new Step(vec3d3, this.getVelocity(), this.getYaw(), this.getPitch(), 1.0f));
            }
            if (d > (double)1.0E-5f || moveIteration.initial) {
                this.minecart.tickBlockCollision();
                this.minecart.tickBlockCollision();
            }
            moveIteration.initial = false;
        }
    }

    private Vec3d calcNewHorizontalVelocity(ServerWorld world, Vec3d horizontalVelocity, MoveIteration iteration, BlockPos pos, BlockState railState, RailShape railShape) {
        Vec3d vec3d2;
        Vec3d vec3d22;
        Vec3d vec3d = horizontalVelocity;
        if (!iteration.slopeVelocityApplied && (vec3d22 = this.applySlopeVelocity(vec3d, railShape)).horizontalLengthSquared() != vec3d.horizontalLengthSquared()) {
            iteration.slopeVelocityApplied = true;
            vec3d = vec3d22;
        }
        if (iteration.initial && (vec3d22 = this.applyInitialVelocity(vec3d)).horizontalLengthSquared() != vec3d.horizontalLengthSquared()) {
            iteration.decelerated = true;
            vec3d = vec3d22;
        }
        if (!iteration.decelerated && (vec3d22 = this.decelerateFromPoweredRail(vec3d, railState)).horizontalLengthSquared() != vec3d.horizontalLengthSquared()) {
            iteration.decelerated = true;
            vec3d = vec3d22;
        }
        if (iteration.initial && (vec3d = this.minecart.applySlowdown(vec3d)).lengthSquared() > 0.0) {
            double d = Math.min(vec3d.length(), this.minecart.getMaxSpeed(world));
            vec3d = vec3d.normalize().multiply(d);
        }
        if (!iteration.accelerated && (vec3d2 = this.accelerateFromPoweredRail(vec3d, pos, railState)).horizontalLengthSquared() != vec3d.horizontalLengthSquared()) {
            iteration.accelerated = true;
            vec3d = vec3d2;
        }
        return vec3d;
    }

    private Vec3d applySlopeVelocity(Vec3d horizontalVelocity, RailShape railShape) {
        double d = Math.max(0.0078125, horizontalVelocity.horizontalLength() * 0.02);
        if (this.minecart.isTouchingWater()) {
            d *= 0.2;
        }
        return switch (railShape) {
            case RailShape.ASCENDING_EAST -> horizontalVelocity.add(-d, 0.0, 0.0);
            case RailShape.ASCENDING_WEST -> horizontalVelocity.add(d, 0.0, 0.0);
            case RailShape.ASCENDING_NORTH -> horizontalVelocity.add(0.0, 0.0, d);
            case RailShape.ASCENDING_SOUTH -> horizontalVelocity.add(0.0, 0.0, -d);
            default -> horizontalVelocity;
        };
    }

    private Vec3d applyInitialVelocity(Vec3d horizontalVelocity) {
        Entity entity = this.minecart.getFirstPassenger();
        if (!(entity instanceof ServerPlayerEntity)) {
            return horizontalVelocity;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
        Vec3d vec3d = serverPlayerEntity.getInputVelocityForMinecart();
        if (vec3d.lengthSquared() > 0.0) {
            Vec3d vec3d2 = vec3d.normalize();
            double d = horizontalVelocity.horizontalLengthSquared();
            if (vec3d2.lengthSquared() > 0.0 && d < 0.01) {
                return horizontalVelocity.add(new Vec3d(vec3d2.x, 0.0, vec3d2.z).normalize().multiply(0.001));
            }
        }
        return horizontalVelocity;
    }

    private Vec3d decelerateFromPoweredRail(Vec3d velocity, BlockState railState) {
        if (!railState.isOf(Blocks.POWERED_RAIL) || railState.get(PoweredRailBlock.POWERED).booleanValue()) {
            return velocity;
        }
        if (velocity.length() < 0.03) {
            return Vec3d.ZERO;
        }
        return velocity.multiply(0.5);
    }

    private Vec3d accelerateFromPoweredRail(Vec3d velocity, BlockPos railPos, BlockState railState) {
        if (!railState.isOf(Blocks.POWERED_RAIL) || !railState.get(PoweredRailBlock.POWERED).booleanValue()) {
            return velocity;
        }
        if (velocity.length() > 0.01) {
            return velocity.normalize().multiply(velocity.length() + 0.06);
        }
        Vec3d vec3d = this.minecart.getLaunchDirection(railPos);
        if (vec3d.lengthSquared() <= 0.0) {
            return velocity;
        }
        return vec3d.multiply(velocity.length() + 0.2);
    }

    @Override
    public double moveAlongTrack(BlockPos blockPos, RailShape railShape, double remainingMovement) {
        if (remainingMovement < (double)1.0E-5f) {
            return 0.0;
        }
        Vec3d vec3d = this.getPos();
        Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(railShape);
        Vec3i vec3i = (Vec3i)pair.getFirst();
        Vec3i vec3i2 = (Vec3i)pair.getSecond();
        Vec3d vec3d2 = this.getVelocity().getHorizontal();
        if (vec3d2.length() < (double)1.0E-5f) {
            this.setVelocity(Vec3d.ZERO);
            return 0.0;
        }
        boolean bl = vec3i.getY() != vec3i2.getY();
        Vec3d vec3d3 = new Vec3d(vec3i2).multiply(0.5).getHorizontal();
        Vec3d vec3d4 = new Vec3d(vec3i).multiply(0.5).getHorizontal();
        if (vec3d2.dotProduct(vec3d4) < vec3d2.dotProduct(vec3d3)) {
            vec3d4 = vec3d3;
        }
        Vec3d vec3d5 = blockPos.toBottomCenterPos().add(vec3d4).add(0.0, 0.1, 0.0).add(vec3d4.normalize().multiply(1.0E-5f));
        if (bl && !this.ascends(vec3d2, railShape)) {
            vec3d5 = vec3d5.add(0.0, 1.0, 0.0);
        }
        Vec3d vec3d6 = vec3d5.subtract(this.getPos()).normalize();
        vec3d2 = vec3d6.multiply(vec3d2.length() / vec3d6.horizontalLength());
        Vec3d vec3d7 = vec3d.add(vec3d2.normalize().multiply(remainingMovement * (double)(bl ? MathHelper.SQUARE_ROOT_OF_TWO : 1.0f)));
        if (vec3d.squaredDistanceTo(vec3d5) <= vec3d.squaredDistanceTo(vec3d7)) {
            remainingMovement = vec3d5.subtract(vec3d7).horizontalLength();
            vec3d7 = vec3d5;
        } else {
            remainingMovement = 0.0;
        }
        this.minecart.move(MovementType.SELF, vec3d7.subtract(vec3d));
        BlockState blockState = this.getWorld().getBlockState(BlockPos.ofFloored(vec3d7));
        if (bl) {
            RailShape railShape2;
            if (AbstractRailBlock.isRail(blockState) && this.restOnVShapedTrack(railShape, railShape2 = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty()))) {
                return 0.0;
            }
            double d = vec3d5.getHorizontal().distanceTo(this.getPos().getHorizontal());
            double e = vec3d5.y + (this.ascends(vec3d2, railShape) ? d : -d);
            if (this.getPos().y < e) {
                this.setPos(this.getPos().x, e, this.getPos().z);
            }
        }
        if (this.getPos().distanceTo(vec3d) < (double)1.0E-5f && vec3d7.distanceTo(vec3d) > (double)1.0E-5f) {
            this.setVelocity(Vec3d.ZERO);
            return 0.0;
        }
        this.setVelocity(vec3d2);
        return remainingMovement;
    }

    private boolean restOnVShapedTrack(RailShape currentRailShape, RailShape newRailShape) {
        if (this.getVelocity().lengthSquared() < 0.005 && newRailShape.isAscending() && this.ascends(this.getVelocity(), currentRailShape) && !this.ascends(this.getVelocity(), newRailShape)) {
            this.setVelocity(Vec3d.ZERO);
            return true;
        }
        return false;
    }

    @Override
    public double getMaxSpeed(ServerWorld world) {
        return (double)world.getGameRules().getValue(GameRules.MAX_MINECART_SPEED).intValue() * (this.minecart.isTouchingWater() ? 0.5 : 1.0) / 20.0;
    }

    private boolean ascends(Vec3d velocity, RailShape railShape) {
        return switch (railShape) {
            case RailShape.ASCENDING_EAST -> {
                if (velocity.x < 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_WEST -> {
                if (velocity.x > 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_NORTH -> {
                if (velocity.z > 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_SOUTH -> {
                if (velocity.z < 0.0) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public double getSpeedRetention() {
        return this.minecart.hasPassengers() ? 0.997 : 0.975;
    }

    @Override
    public boolean handleCollision() {
        boolean bl = this.pickUpEntities(this.minecart.getBoundingBox().expand(0.2, 0.0, 0.2));
        if (this.minecart.horizontalCollision || this.minecart.verticalCollision) {
            boolean bl2 = this.pushAwayFromEntities(this.minecart.getBoundingBox().expand(1.0E-7));
            return bl && !bl2;
        }
        return false;
    }

    public boolean pickUpEntities(Box box) {
        List<Entity> list;
        if (this.minecart.isRideable() && !this.minecart.hasPassengers() && !(list = this.getWorld().getOtherEntities(this.minecart, box, EntityPredicates.canBePushedBy(this.minecart))).isEmpty()) {
            for (Entity entity : list) {
                boolean bl;
                if (entity instanceof PlayerEntity || entity instanceof IronGolemEntity || entity instanceof AbstractMinecartEntity || this.minecart.hasPassengers() || entity.hasVehicle() || !(bl = entity.startRiding(this.minecart))) continue;
                return true;
            }
        }
        return false;
    }

    public boolean pushAwayFromEntities(Box box) {
        boolean bl;
        block3: {
            block2: {
                bl = false;
                if (!this.minecart.isRideable()) break block2;
                List<Entity> list = this.getWorld().getOtherEntities(this.minecart, box, EntityPredicates.canBePushedBy(this.minecart));
                if (list.isEmpty()) break block3;
                for (Entity entity : list) {
                    if (!(entity instanceof PlayerEntity) && !(entity instanceof IronGolemEntity) && !(entity instanceof AbstractMinecartEntity) && !this.minecart.hasPassengers() && !entity.hasVehicle()) continue;
                    entity.pushAwayFrom(this.minecart);
                    bl = true;
                }
                break block3;
            }
            for (Entity entity2 : this.getWorld().getOtherEntities(this.minecart, box)) {
                if (this.minecart.hasPassenger(entity2) || !entity2.isPushable() || !(entity2 instanceof AbstractMinecartEntity)) continue;
                entity2.pushAwayFrom(this.minecart);
                bl = true;
            }
        }
        return bl;
    }

    public static final class Step
    extends Record {
        final Vec3d position;
        final Vec3d movement;
        final float yRot;
        final float xRot;
        final float weight;
        public static final PacketCodec<ByteBuf, Step> PACKET_CODEC = PacketCodec.tuple(Vec3d.PACKET_CODEC, Step::position, Vec3d.PACKET_CODEC, Step::movement, PacketCodecs.DEGREES, Step::yRot, PacketCodecs.DEGREES, Step::xRot, PacketCodecs.FLOAT, Step::weight, Step::new);
        public static Step ZERO = new Step(Vec3d.ZERO, Vec3d.ZERO, 0.0f, 0.0f, 0.0f);

        public Step(Vec3d position, Vec3d movement, float yRot, float xRot, float weight) {
            this.position = position;
            this.movement = movement;
            this.yRot = yRot;
            this.xRot = xRot;
            this.weight = weight;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Step.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Step.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Step.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this, object);
        }

        public Vec3d position() {
            return this.position;
        }

        public Vec3d movement() {
            return this.movement;
        }

        public float yRot() {
            return this.yRot;
        }

        public float xRot() {
            return this.xRot;
        }

        public float weight() {
            return this.weight;
        }
    }

    static final class InterpolatedStep
    extends Record {
        final float partialTicksInStep;
        final Step currentStep;
        final Step previousStep;

        InterpolatedStep(float partialTicksInStep, Step currentStep, Step previousStep) {
            this.partialTicksInStep = partialTicksInStep;
            this.currentStep = currentStep;
            this.previousStep = previousStep;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InterpolatedStep.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InterpolatedStep.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InterpolatedStep.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this, object);
        }

        public float partialTicksInStep() {
            return this.partialTicksInStep;
        }

        public Step currentStep() {
            return this.currentStep;
        }

        public Step previousStep() {
            return this.previousStep;
        }
    }

    static class MoveIteration {
        double remainingMovement = 0.0;
        boolean initial = true;
        boolean slopeVelocityApplied = false;
        boolean decelerated = false;
        boolean accelerated = false;

        MoveIteration() {
        }

        public boolean shouldContinue() {
            return this.initial || this.remainingMovement > (double)1.0E-5f;
        }
    }
}

