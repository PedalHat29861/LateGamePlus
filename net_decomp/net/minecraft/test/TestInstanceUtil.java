/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.TestInstance;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.tick.WorldTickScheduler;

public class TestInstanceUtil {
    public static final int field_51468 = 10;
    public static final String TEST_STRUCTURES_DIRECTORY_NAME = "Minecraft.Server/src/test/convertables/data";
    public static Path testStructuresDirectoryName = Paths.get("Minecraft.Server/src/test/convertables/data", new String[0]);

    public static BlockRotation getRotation(int steps) {
        switch (steps) {
            case 0: {
                return BlockRotation.NONE;
            }
            case 1: {
                return BlockRotation.CLOCKWISE_90;
            }
            case 2: {
                return BlockRotation.CLOCKWISE_180;
            }
            case 3: {
                return BlockRotation.COUNTERCLOCKWISE_90;
            }
        }
        throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + steps);
    }

    public static int getRotationSteps(BlockRotation rotation) {
        switch (rotation) {
            case NONE: {
                return 0;
            }
            case CLOCKWISE_90: {
                return 1;
            }
            case CLOCKWISE_180: {
                return 2;
            }
            case COUNTERCLOCKWISE_90: {
                return 3;
            }
        }
        throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + String.valueOf(rotation));
    }

    public static TestInstanceBlockEntity createTestInstanceBlockEntity(Identifier testInstanceId, BlockPos pos, Vec3i size, BlockRotation rotation, ServerWorld world) {
        BlockBox blockBox = TestInstanceUtil.getTestInstanceBlockBox(TestInstanceBlockEntity.getStructurePos(pos), size, rotation);
        TestInstanceUtil.clearArea(blockBox, world);
        world.setBlockState(pos, Blocks.TEST_INSTANCE_BLOCK.getDefaultState());
        TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)world.getBlockEntity(pos);
        RegistryKey<TestInstance> registryKey = RegistryKey.of(RegistryKeys.TEST_INSTANCE, testInstanceId);
        testInstanceBlockEntity.setData(new TestInstanceBlockEntity.Data(Optional.of(registryKey), size, rotation, false, TestInstanceBlockEntity.Status.CLEARED, Optional.empty()));
        return testInstanceBlockEntity;
    }

    public static void clearArea(BlockBox area, ServerWorld world) {
        int i = area.getMinY() - 1;
        BlockPos.stream(area).forEach(pos -> TestInstanceUtil.resetBlock(i, pos, world));
        ((WorldTickScheduler)world.getBlockTickScheduler()).clearNextTicks(area);
        world.clearUpdatesInArea(area);
        Box box = Box.from(area);
        List<Entity> list = world.getEntitiesByClass(Entity.class, box, entity -> !(entity instanceof PlayerEntity));
        list.forEach(Entity::discard);
    }

    public static BlockPos getTestInstanceBlockBoxCornerPos(BlockPos pos, Vec3i size, BlockRotation rotation) {
        BlockPos blockPos = pos.add(size).add(-1, -1, -1);
        return StructureTemplate.transformAround(blockPos, BlockMirror.NONE, rotation, pos);
    }

    public static BlockBox getTestInstanceBlockBox(BlockPos pos, Vec3i relativePos, BlockRotation rotation) {
        BlockPos blockPos = TestInstanceUtil.getTestInstanceBlockBoxCornerPos(pos, relativePos, rotation);
        BlockBox blockBox = BlockBox.create(pos, blockPos);
        int i = Math.min(blockBox.getMinX(), blockBox.getMaxX());
        int j = Math.min(blockBox.getMinZ(), blockBox.getMaxZ());
        return blockBox.move(pos.getX() - i, 0, pos.getZ() - j);
    }

    public static Optional<BlockPos> findContainingTestInstanceBlock(BlockPos pos, int radius, ServerWorld world) {
        return TestInstanceUtil.findTestInstanceBlocks(pos, radius, world).filter(blockPos -> TestInstanceUtil.isInBounds(blockPos, pos, world)).findFirst();
    }

    public static Optional<BlockPos> findNearestTestInstanceBlock(BlockPos pos, int radius, ServerWorld world) {
        Comparator<BlockPos> comparator = Comparator.comparingInt(blockPos -> blockPos.getManhattanDistance(pos));
        return TestInstanceUtil.findTestInstanceBlocks(pos, radius, world).min(comparator);
    }

    public static Stream<BlockPos> findTestInstanceBlocks(BlockPos pos, int radius, ServerWorld world) {
        return world.getPointOfInterestStorage().getPositions(poiType -> poiType.matchesKey(PointOfInterestTypes.TEST_INSTANCE), poiPos -> true, pos, radius, PointOfInterestStorage.OccupationStatus.ANY).map(BlockPos::toImmutable);
    }

    public static Stream<BlockPos> findTargetedTestInstanceBlock(BlockPos pos, Entity entity, ServerWorld world) {
        int i = 250;
        Vec3d vec3d = entity.getEyePos();
        Vec3d vec3d2 = vec3d.add(entity.getRotationVector().multiply(250.0));
        return TestInstanceUtil.findTestInstanceBlocks(pos, 250, world).map(blockPos -> world.getBlockEntity((BlockPos)blockPos, BlockEntityType.TEST_INSTANCE_BLOCK)).flatMap(Optional::stream).filter(testInstanceBlockEntity -> testInstanceBlockEntity.getBox().raycast(vec3d, vec3d2).isPresent()).map(BlockEntity::getPos).sorted(Comparator.comparing(pos::getSquaredDistance)).limit(1L);
    }

    private static void resetBlock(int altitude, BlockPos pos, ServerWorld world) {
        BlockState blockState = pos.getY() < altitude ? Blocks.STONE.getDefaultState() : Blocks.AIR.getDefaultState();
        BlockStateArgument blockStateArgument = new BlockStateArgument(blockState, Collections.emptySet(), null);
        blockStateArgument.setBlockState(world, pos, 818);
        world.updateNeighbors(pos, blockState.getBlock());
    }

    private static boolean isInBounds(BlockPos testInstanceBlockPos, BlockPos pos, ServerWorld world) {
        BlockEntity blockEntity = world.getBlockEntity(testInstanceBlockPos);
        if (blockEntity instanceof TestInstanceBlockEntity) {
            TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
            return testInstanceBlockEntity.getBlockBox().contains(pos);
        }
        return false;
    }
}

