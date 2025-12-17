/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.pathing;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class MobNavigation
extends EntityNavigation {
    private boolean avoidSunlight;
    private boolean skipRetarget;

    public MobNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new LandPathNodeMaker();
        return new PathNodeNavigator(this.nodeMaker, range);
    }

    @Override
    protected boolean isAtValidPosition() {
        return this.entity.isOnGround() || this.entity.isInFluid() || this.entity.hasVehicle();
    }

    @Override
    protected Vec3d getPos() {
        return new Vec3d(this.entity.getX(), this.getPathfindingY(), this.entity.getZ());
    }

    @Override
    public Path findPathTo(BlockPos target, int distance) {
        WorldChunk worldChunk = this.world.getChunkManager().getWorldChunk(ChunkSectionPos.getSectionCoord(target.getX()), ChunkSectionPos.getSectionCoord(target.getZ()));
        if (worldChunk == null) {
            return null;
        }
        if (!this.skipRetarget) {
            target = this.retargetToSolidBlock(worldChunk, target, distance);
        }
        return super.findPathTo(target, distance);
    }

    final BlockPos retargetToSolidBlock(WorldChunk chunk, BlockPos pos, int distance) {
        BlockPos.Mutable mutable;
        if (chunk.getBlockState(pos).isAir()) {
            mutable = pos.mutableCopy().move(Direction.DOWN);
            while (mutable.getY() >= this.world.getBottomY() && chunk.getBlockState(mutable).isAir()) {
                mutable.move(Direction.DOWN);
            }
            if (mutable.getY() >= this.world.getBottomY()) {
                return mutable.up();
            }
            mutable.setY(pos.getY() + 1);
            while (mutable.getY() <= this.world.getTopYInclusive() && chunk.getBlockState(mutable).isAir()) {
                mutable.move(Direction.UP);
            }
            pos = mutable;
        }
        if (chunk.getBlockState(pos).isSolid()) {
            mutable = pos.mutableCopy().move(Direction.UP);
            while (mutable.getY() <= this.world.getTopYInclusive() && chunk.getBlockState(mutable).isSolid()) {
                mutable.move(Direction.UP);
            }
            return mutable.toImmutable();
        }
        return pos;
    }

    @Override
    public Path findPathTo(Entity entity, int distance) {
        return this.findPathTo(entity.getBlockPos(), distance);
    }

    private int getPathfindingY() {
        if (!this.entity.isTouchingWater() || !this.canSwim()) {
            return MathHelper.floor(this.entity.getY() + 0.5);
        }
        int i = this.entity.getBlockY();
        BlockState blockState = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), i, this.entity.getZ()));
        int j = 0;
        while (blockState.isOf(Blocks.WATER)) {
            blockState = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), ++i, this.entity.getZ()));
            if (++j <= 16) continue;
            return this.entity.getBlockY();
        }
        return i;
    }

    @Override
    protected void adjustPath() {
        super.adjustPath();
        if (this.avoidSunlight) {
            if (this.world.isSkyVisible(BlockPos.ofFloored(this.entity.getX(), this.entity.getY() + 0.5, this.entity.getZ()))) {
                return;
            }
            for (int i = 0; i < this.currentPath.getLength(); ++i) {
                PathNode pathNode = this.currentPath.getNode(i);
                if (!this.world.isSkyVisible(new BlockPos(pathNode.x, pathNode.y, pathNode.z))) continue;
                this.currentPath.setLength(i);
                return;
            }
        }
    }

    @Override
    public boolean canControlOpeningDoors() {
        return true;
    }

    protected boolean canWalkOnPath(PathNodeType pathType) {
        if (pathType == PathNodeType.WATER) {
            return false;
        }
        if (pathType == PathNodeType.LAVA) {
            return false;
        }
        return pathType != PathNodeType.OPEN;
    }

    public void setAvoidSunlight(boolean avoidSunlight) {
        this.avoidSunlight = avoidSunlight;
    }

    public void setCanWalkOverFences(boolean canWalkOverFences) {
        this.nodeMaker.setCanWalkOverFences(canWalkOverFences);
    }

    public void setSkipRetarget(boolean skipRetarget) {
        this.skipRetarget = skipRetarget;
    }
}

