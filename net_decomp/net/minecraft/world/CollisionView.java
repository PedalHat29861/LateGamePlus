/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.border.WorldBorder;
import org.jspecify.annotations.Nullable;

public interface CollisionView
extends BlockView {
    public WorldBorder getWorldBorder();

    public @Nullable BlockView getChunkAsView(int var1, int var2);

    default public boolean doesNotIntersectEntities(@Nullable Entity except, VoxelShape shape) {
        return true;
    }

    default public boolean canPlace(BlockState state, BlockPos pos, ShapeContext context) {
        VoxelShape voxelShape = state.getCollisionShape(this, pos, context);
        return voxelShape.isEmpty() || this.doesNotIntersectEntities(null, voxelShape.offset(pos));
    }

    default public boolean doesNotIntersectEntities(Entity entity) {
        return this.doesNotIntersectEntities(entity, VoxelShapes.cuboid(entity.getBoundingBox()));
    }

    default public boolean isSpaceEmpty(Box box) {
        return this.isSpaceEmpty(null, box);
    }

    default public boolean isSpaceEmpty(Entity entity) {
        return this.isSpaceEmpty(entity, entity.getBoundingBox());
    }

    default public boolean isSpaceEmpty(@Nullable Entity entity, Box box) {
        return this.isSpaceEmpty(entity, box, false);
    }

    default public boolean isSpaceEmpty(@Nullable Entity entity, Box box, boolean checkFluid) {
        return this.isBlockSpaceEmpty(entity, box, checkFluid) && this.doesNotCollideWithEntities(entity, box) && this.doesNotCollideWithWorldBorder(entity, box);
    }

    default public boolean isBlockSpaceEmpty(@Nullable Entity entity, Box box) {
        return this.isBlockSpaceEmpty(entity, box, false);
    }

    default public boolean isBlockSpaceEmpty(@Nullable Entity entity, Box box, boolean blockOrFluid) {
        Iterable<VoxelShape> iterable = blockOrFluid ? this.getBlockOrFluidCollisions(entity, box) : this.getBlockCollisions(entity, box);
        for (VoxelShape voxelShape : iterable) {
            if (voxelShape.isEmpty()) continue;
            return false;
        }
        return true;
    }

    default public boolean doesNotCollideWithEntities(@Nullable Entity entity, Box box) {
        return this.getEntityCollisions(entity, box).isEmpty();
    }

    default public boolean doesNotCollideWithWorldBorder(@Nullable Entity entity, Box box) {
        if (entity != null) {
            VoxelShape voxelShape = this.getWorldBorderCollisions(entity, box);
            return voxelShape == null || !VoxelShapes.matchesAnywhere(voxelShape, VoxelShapes.cuboid(box), BooleanBiFunction.AND);
        }
        return true;
    }

    public List<VoxelShape> getEntityCollisions(@Nullable Entity var1, Box var2);

    default public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, Box box) {
        List<VoxelShape> list = this.getEntityCollisions(entity, box);
        Iterable iterable = this.getBlockCollisions(entity, box);
        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, Box box, Vec3d pos) {
        List<VoxelShape> list = this.getEntityCollisions(entity, box);
        Iterable iterable = this.getBlockOrFluidCollisions(ShapeContext.ofCollision(entity, pos.y), box);
        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default public Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, Box box) {
        return this.getBlockOrFluidCollisions(entity == null ? ShapeContext.absent() : ShapeContext.of(entity), box);
    }

    default public Iterable<VoxelShape> getBlockOrFluidCollisions(@Nullable Entity entity, Box box) {
        return this.getBlockOrFluidCollisions(entity == null ? ShapeContext.absentTreatingFluidAsCube() : ShapeContext.of(entity, true), box);
    }

    private Iterable<VoxelShape> getBlockOrFluidCollisions(ShapeContext shapeContext, Box box) {
        return () -> new BlockCollisionSpliterator<VoxelShape>(this, shapeContext, box, false, (pos, shape) -> shape);
    }

    private @Nullable VoxelShape getWorldBorderCollisions(Entity entity, Box box) {
        WorldBorder worldBorder = this.getWorldBorder();
        return worldBorder.canCollide(entity, box) ? worldBorder.asVoxelShape() : null;
    }

    default public BlockHitResult getCollisionsIncludingWorldBorder(RaycastContext context) {
        BlockHitResult blockHitResult = this.raycast(context);
        WorldBorder worldBorder = this.getWorldBorder();
        if (worldBorder.contains(context.getStart()) && !worldBorder.contains(blockHitResult.getPos())) {
            Vec3d vec3d = blockHitResult.getPos().subtract(context.getStart());
            Direction direction = Direction.getFacing(vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = worldBorder.clamp(blockHitResult.getPos());
            return new BlockHitResult(vec3d2, direction, BlockPos.ofFloored(vec3d2), false, true);
        }
        return blockHitResult;
    }

    default public boolean canCollide(@Nullable Entity entity, Box box) {
        BlockCollisionSpliterator<VoxelShape> blockCollisionSpliterator = new BlockCollisionSpliterator<VoxelShape>(this, entity, box, true, (pos, voxelShape) -> voxelShape);
        while (blockCollisionSpliterator.hasNext()) {
            if (((VoxelShape)blockCollisionSpliterator.next()).isEmpty()) continue;
            return true;
        }
        return false;
    }

    default public Optional<BlockPos> findSupportingBlockPos(Entity entity, Box box) {
        BlockPos blockPos = null;
        double d = Double.MAX_VALUE;
        BlockCollisionSpliterator<BlockPos> blockCollisionSpliterator = new BlockCollisionSpliterator<BlockPos>(this, entity, box, false, (pos, voxelShape) -> pos);
        while (blockCollisionSpliterator.hasNext()) {
            BlockPos blockPos2 = (BlockPos)blockCollisionSpliterator.next();
            double e = blockPos2.getSquaredDistance(entity.getEntityPos());
            if (!(e < d) && (e != d || blockPos != null && blockPos.compareTo(blockPos2) >= 0)) continue;
            blockPos = blockPos2.toImmutable();
            d = e;
        }
        return Optional.ofNullable(blockPos);
    }

    default public Optional<Vec3d> findClosestCollision(@Nullable Entity entity, VoxelShape shape, Vec3d target, double x, double y, double z) {
        if (shape.isEmpty()) {
            return Optional.empty();
        }
        Box box2 = shape.getBoundingBox().expand(x, y, z);
        VoxelShape voxelShape = StreamSupport.stream(this.getBlockCollisions(entity, box2).spliterator(), false).filter(collision -> this.getWorldBorder() == null || this.getWorldBorder().contains(collision.getBoundingBox())).flatMap(collision -> collision.getBoundingBoxes().stream()).map(box -> box.expand(x / 2.0, y / 2.0, z / 2.0)).map(VoxelShapes::cuboid).reduce(VoxelShapes.empty(), VoxelShapes::union);
        VoxelShape voxelShape2 = VoxelShapes.combineAndSimplify(shape, voxelShape, BooleanBiFunction.ONLY_FIRST);
        return voxelShape2.getClosestPointTo(target);
    }
}

