/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.entity.decoration;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

public abstract class AbstractDecorationEntity
extends BlockAttachedEntity {
    private static final TrackedData<Direction> FACING = DataTracker.registerData(AbstractDecorationEntity.class, TrackedDataHandlerRegistry.FACING);
    private static final Direction DEFAULT_FACING = Direction.SOUTH;

    protected AbstractDecorationEntity(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super((EntityType<? extends BlockAttachedEntity>)entityType, world);
    }

    protected AbstractDecorationEntity(EntityType<? extends AbstractDecorationEntity> type, World world, BlockPos pos) {
        this(type, world);
        this.attachedBlockPos = pos;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(FACING, DEFAULT_FACING);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (data.equals(FACING)) {
            this.setFacing(this.getHorizontalFacing());
        }
    }

    @Override
    public Direction getHorizontalFacing() {
        return this.dataTracker.get(FACING);
    }

    protected void setFacingInternal(Direction facing) {
        this.dataTracker.set(FACING, facing);
    }

    protected void setFacing(Direction facing) {
        Objects.requireNonNull(facing);
        Validate.isTrue((boolean)facing.getAxis().isHorizontal());
        this.setFacingInternal(facing);
        this.setYaw(facing.getHorizontalQuarterTurns() * 90);
        this.lastYaw = this.getYaw();
        this.updateAttachmentPosition();
    }

    @Override
    protected void updateAttachmentPosition() {
        if (this.getHorizontalFacing() == null) {
            return;
        }
        Box box = this.calculateBoundingBox(this.attachedBlockPos, this.getHorizontalFacing());
        Vec3d vec3d = box.getCenter();
        this.setPos(vec3d.x, vec3d.y, vec3d.z);
        this.setBoundingBox(box);
    }

    protected abstract Box calculateBoundingBox(BlockPos var1, Direction var2);

    @Override
    public boolean canStayAttached() {
        if (this.shouldPopOff(this.calculatePopOffCheckBoundingBox())) {
            return false;
        }
        boolean bl = BlockPos.stream(this.getAttachmentBox()).allMatch(pos -> {
            BlockState blockState = this.getEntityWorld().getBlockState((BlockPos)pos);
            return blockState.isSolid() || AbstractRedstoneGateBlock.isRedstoneGate(blockState);
        });
        return bl && this.hasNoIntersectingDecoration(false);
    }

    protected Box getAttachmentBox() {
        return this.getBoundingBox().offset(this.getHorizontalFacing().getUnitVector().mul(-0.5f)).contract(1.0E-7);
    }

    protected boolean hasNoIntersectingDecoration(boolean skipTypeCheck) {
        Predicate<AbstractDecorationEntity> predicate = entity -> {
            boolean bl2 = !skipTypeCheck && entity.getType() == this.getType();
            boolean bl3 = entity.getHorizontalFacing() == this.getHorizontalFacing();
            return entity != this && (bl2 || bl3);
        };
        return !this.getEntityWorld().hasEntities(TypeFilter.instanceOf(AbstractDecorationEntity.class), this.calculatePopOffCheckBoundingBox(), predicate);
    }

    protected boolean shouldPopOff(Box checkBoundingBox) {
        World world = this.getEntityWorld();
        return !world.isBlockSpaceEmpty(this, checkBoundingBox) || !world.doesNotCollideWithWorldBorder(this, checkBoundingBox);
    }

    protected Box calculatePopOffCheckBoundingBox() {
        return this.getBoundingBox();
    }

    public abstract void onPlace();

    @Override
    public ItemEntity dropStack(ServerWorld world, ItemStack stack, float yOffset) {
        ItemEntity itemEntity = new ItemEntity(this.getEntityWorld(), this.getX() + (double)((float)this.getHorizontalFacing().getOffsetX() * 0.15f), this.getY() + (double)yOffset, this.getZ() + (double)((float)this.getHorizontalFacing().getOffsetZ() * 0.15f), stack);
        itemEntity.setToDefaultPickupDelay();
        this.getEntityWorld().spawnEntity(itemEntity);
        return itemEntity;
    }

    @Override
    public float applyRotation(BlockRotation rotation) {
        Direction direction = this.getHorizontalFacing();
        if (direction.getAxis() != Direction.Axis.Y) {
            switch (rotation) {
                case CLOCKWISE_180: {
                    direction = direction.getOpposite();
                    break;
                }
                case COUNTERCLOCKWISE_90: {
                    direction = direction.rotateYCounterclockwise();
                    break;
                }
                case CLOCKWISE_90: {
                    direction = direction.rotateYClockwise();
                    break;
                }
            }
            this.setFacing(direction);
        }
        float f = MathHelper.wrapDegrees(this.getYaw());
        return switch (rotation) {
            case BlockRotation.CLOCKWISE_180 -> f + 180.0f;
            case BlockRotation.COUNTERCLOCKWISE_90 -> f + 90.0f;
            case BlockRotation.CLOCKWISE_90 -> f + 270.0f;
            default -> f;
        };
    }

    @Override
    public float applyMirror(BlockMirror mirror) {
        return this.applyRotation(mirror.getRotation(this.getHorizontalFacing()));
    }
}

