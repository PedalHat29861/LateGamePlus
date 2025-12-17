/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneController;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import org.jspecify.annotations.Nullable;

public class ExperimentalRedstoneController
extends RedstoneController {
    private final Deque<BlockPos> powerIncreaseQueue = new ArrayDeque<BlockPos>();
    private final Deque<BlockPos> powerDecreaseQueue = new ArrayDeque<BlockPos>();
    private final Object2IntMap<BlockPos> wireOrientationsAndPowers = new Object2IntLinkedOpenHashMap();

    public ExperimentalRedstoneController(RedstoneWireBlock redstoneWireBlock) {
        super(redstoneWireBlock);
    }

    @Override
    public void update(World world, BlockPos pos, BlockState state, @Nullable WireOrientation orientation, boolean blockAdded) {
        WireOrientation wireOrientation = ExperimentalRedstoneController.tweakOrientation(world, orientation);
        this.propagatePowerUpdates(world, pos, wireOrientation);
        ObjectIterator objectIterator = this.wireOrientationsAndPowers.object2IntEntrySet().iterator();
        boolean bl = true;
        while (objectIterator.hasNext()) {
            Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
            BlockPos blockPos = (BlockPos)entry.getKey();
            int i = entry.getIntValue();
            int j = ExperimentalRedstoneController.unpackPower(i);
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOf(this.wire) && !blockState.get(RedstoneWireBlock.POWER).equals(j)) {
                int k = 2;
                if (!blockAdded || !bl) {
                    k |= 0x80;
                }
                world.setBlockState(blockPos, (BlockState)blockState.with(RedstoneWireBlock.POWER, j), k);
            } else {
                objectIterator.remove();
            }
            bl = false;
        }
        this.update(world);
    }

    private void update(World world) {
        ServerWorld serverWorld;
        this.wireOrientationsAndPowers.forEach((pos, orientationAndPower) -> {
            WireOrientation wireOrientation = ExperimentalRedstoneController.unpackOrientation(orientationAndPower);
            BlockState blockState = world.getBlockState((BlockPos)pos);
            for (Direction direction : wireOrientation.getDirectionsByPriority()) {
                if (!ExperimentalRedstoneController.canProvidePowerTo(blockState, direction)) continue;
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState2 = world.getBlockState(blockPos);
                WireOrientation wireOrientation2 = wireOrientation.withFrontIfNotUp(direction);
                world.updateNeighbor(blockState2, blockPos, this.wire, wireOrientation2, false);
                if (!blockState2.isSolidBlock(world, blockPos)) continue;
                for (Direction direction2 : wireOrientation2.getDirectionsByPriority()) {
                    if (direction2 == direction.getOpposite()) continue;
                    world.updateNeighbor(blockPos.offset(direction2), this.wire, wireOrientation2.withFrontIfNotUp(direction2));
                }
            }
        });
        if (world instanceof ServerWorld && (serverWorld = (ServerWorld)world).getSubscriptionTracker().isSubscribed(DebugSubscriptionTypes.REDSTONE_WIRE_ORIENTATIONS)) {
            this.wireOrientationsAndPowers.forEach((pos, power) -> serverWorld.getSubscriptionTracker().sendBlockDebugData((BlockPos)pos, DebugSubscriptionTypes.REDSTONE_WIRE_ORIENTATIONS, ExperimentalRedstoneController.unpackOrientation(power)));
        }
    }

    private static boolean canProvidePowerTo(BlockState wireState, Direction direction) {
        EnumProperty<WireConnection> enumProperty = RedstoneWireBlock.DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction);
        if (enumProperty == null) {
            return direction == Direction.DOWN;
        }
        return wireState.get(enumProperty).isConnected();
    }

    private static WireOrientation tweakOrientation(World world, @Nullable WireOrientation orientation) {
        WireOrientation wireOrientation = orientation != null ? orientation : WireOrientation.random(world.random);
        return wireOrientation.withUp(Direction.UP).withSideBias(WireOrientation.SideBias.LEFT);
    }

    private void propagatePowerUpdates(World world, BlockPos pos, WireOrientation orientation) {
        int l;
        int k;
        int j;
        int i;
        BlockPos blockPos;
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOf(this.wire)) {
            this.updatePowerAt(pos, blockState.get(RedstoneWireBlock.POWER), orientation);
            this.powerIncreaseQueue.add(pos);
        } else {
            this.spreadPowerUpdateToNeighbors(world, pos, 0, orientation, true);
        }
        while (!this.powerIncreaseQueue.isEmpty()) {
            int n;
            blockPos = this.powerIncreaseQueue.removeFirst();
            i = this.wireOrientationsAndPowers.getInt((Object)blockPos);
            WireOrientation wireOrientation = ExperimentalRedstoneController.unpackOrientation(i);
            j = ExperimentalRedstoneController.unpackPower(i);
            k = this.getStrongPowerAt(world, blockPos);
            int m = Math.max(k, l = this.calculateWirePowerAt(world, blockPos));
            if (m < j) {
                if (k > 0 && !this.powerDecreaseQueue.contains(blockPos)) {
                    this.powerDecreaseQueue.add(blockPos);
                }
                n = 0;
            } else {
                n = m;
            }
            if (n != j) {
                this.updatePowerAt(blockPos, n, wireOrientation);
            }
            this.spreadPowerUpdateToNeighbors(world, blockPos, n, wireOrientation, j > m);
        }
        while (!this.powerDecreaseQueue.isEmpty()) {
            blockPos = this.powerDecreaseQueue.removeFirst();
            i = this.wireOrientationsAndPowers.getInt((Object)blockPos);
            int o = ExperimentalRedstoneController.unpackPower(i);
            j = this.getStrongPowerAt(world, blockPos);
            k = this.calculateWirePowerAt(world, blockPos);
            l = Math.max(j, k);
            WireOrientation wireOrientation2 = ExperimentalRedstoneController.unpackOrientation(i);
            if (l > o) {
                this.updatePowerAt(blockPos, l, wireOrientation2);
            } else if (l < o) {
                throw new IllegalStateException("Turning off wire while trying to turn it on. Should not happen.");
            }
            this.spreadPowerUpdateToNeighbors(world, blockPos, l, wireOrientation2, false);
        }
    }

    private static int packOrientationAndPower(WireOrientation orientation, int power) {
        return orientation.ordinal() << 4 | power;
    }

    private static WireOrientation unpackOrientation(int packed) {
        return WireOrientation.fromOrdinal(packed >> 4);
    }

    private static int unpackPower(int packed) {
        return packed & 0xF;
    }

    private void updatePowerAt(BlockPos pos, int power, WireOrientation defaultOrientation) {
        this.wireOrientationsAndPowers.compute((Object)pos, (pos2, orientationAndPower) -> {
            if (orientationAndPower == null) {
                return ExperimentalRedstoneController.packOrientationAndPower(defaultOrientation, power);
            }
            return ExperimentalRedstoneController.packOrientationAndPower(ExperimentalRedstoneController.unpackOrientation(orientationAndPower), power);
        });
    }

    private void spreadPowerUpdateToNeighbors(World world, BlockPos pos, int power, WireOrientation orientation, boolean canIncreasePower) {
        BlockPos blockPos;
        for (Direction direction : orientation.getHorizontalDirections()) {
            blockPos = pos.offset(direction);
            this.spreadPowerUpdateTo(world, blockPos, power, orientation.withFront(direction), canIncreasePower);
        }
        for (Direction direction : orientation.getVerticalDirections()) {
            blockPos = pos.offset(direction);
            boolean bl = world.getBlockState(blockPos).isSolidBlock(world, blockPos);
            for (Direction direction2 : orientation.getHorizontalDirections()) {
                BlockPos blockPos3;
                BlockPos blockPos2 = pos.offset(direction2);
                if (direction == Direction.UP && !bl) {
                    blockPos3 = blockPos.offset(direction2);
                    this.spreadPowerUpdateTo(world, blockPos3, power, orientation.withFront(direction2), canIncreasePower);
                    continue;
                }
                if (direction != Direction.DOWN || world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) continue;
                blockPos3 = blockPos.offset(direction2);
                this.spreadPowerUpdateTo(world, blockPos3, power, orientation.withFront(direction2), canIncreasePower);
            }
        }
    }

    private void spreadPowerUpdateTo(World world, BlockPos neighborPos, int power, WireOrientation orientation, boolean canIncreasePower) {
        BlockState blockState = world.getBlockState(neighborPos);
        if (blockState.isOf(this.wire)) {
            int i = this.getWirePowerAt(neighborPos, blockState);
            if (i < power - 1 && !this.powerDecreaseQueue.contains(neighborPos)) {
                this.powerDecreaseQueue.add(neighborPos);
                this.updatePowerAt(neighborPos, i, orientation);
            }
            if (canIncreasePower && i > power && !this.powerIncreaseQueue.contains(neighborPos)) {
                this.powerIncreaseQueue.add(neighborPos);
                this.updatePowerAt(neighborPos, i, orientation);
            }
        }
    }

    @Override
    protected int getWirePowerAt(BlockPos world, BlockState pos) {
        int i = this.wireOrientationsAndPowers.getOrDefault((Object)world, -1);
        if (i != -1) {
            return ExperimentalRedstoneController.unpackPower(i);
        }
        return super.getWirePowerAt(world, pos);
    }
}

