/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.structure;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class StructureTemplate {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String PALETTE_KEY = "palette";
    public static final String PALETTES_KEY = "palettes";
    public static final String ENTITIES_KEY = "entities";
    public static final String BLOCKS_KEY = "blocks";
    public static final String BLOCKS_POS_KEY = "pos";
    public static final String BLOCKS_STATE_KEY = "state";
    public static final String BLOCKS_NBT_KEY = "nbt";
    public static final String ENTITIES_POS_KEY = "pos";
    public static final String ENTITIES_BLOCK_POS_KEY = "blockPos";
    public static final String ENTITIES_NBT_KEY = "nbt";
    public static final String SIZE_KEY = "size";
    private final List<PalettedBlockInfoList> blockInfoLists = Lists.newArrayList();
    private final List<StructureEntityInfo> entities = Lists.newArrayList();
    private Vec3i size = Vec3i.ZERO;
    private String author = "?";

    public Vec3i getSize() {
        return this.size;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return this.author;
    }

    public void saveFromWorld(World world, BlockPos start, Vec3i dimensions, boolean includeEntities, List<Block> ignoredBlocks) {
        if (dimensions.getX() < 1 || dimensions.getY() < 1 || dimensions.getZ() < 1) {
            return;
        }
        BlockPos blockPos = start.add(dimensions).add(-1, -1, -1);
        ArrayList list = Lists.newArrayList();
        ArrayList list2 = Lists.newArrayList();
        ArrayList list3 = Lists.newArrayList();
        BlockPos blockPos2 = new BlockPos(Math.min(start.getX(), blockPos.getX()), Math.min(start.getY(), blockPos.getY()), Math.min(start.getZ(), blockPos.getZ()));
        BlockPos blockPos3 = new BlockPos(Math.max(start.getX(), blockPos.getX()), Math.max(start.getY(), blockPos.getY()), Math.max(start.getZ(), blockPos.getZ()));
        this.size = dimensions;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER);){
            for (BlockPos blockPos4 : BlockPos.iterate(blockPos2, blockPos3)) {
                StructureBlockInfo structureBlockInfo;
                BlockPos blockPos5 = blockPos4.subtract(blockPos2);
                BlockState blockState = world.getBlockState(blockPos4);
                if (ignoredBlocks.stream().anyMatch(blockState::isOf)) continue;
                BlockEntity blockEntity = world.getBlockEntity(blockPos4);
                if (blockEntity != null) {
                    NbtWriteView nbtWriteView = NbtWriteView.create(logging, world.getRegistryManager());
                    blockEntity.writeDataWithId(nbtWriteView);
                    structureBlockInfo = new StructureBlockInfo(blockPos5, blockState, nbtWriteView.getNbt());
                } else {
                    structureBlockInfo = new StructureBlockInfo(blockPos5, blockState, null);
                }
                StructureTemplate.categorize(structureBlockInfo, list, list2, list3);
            }
            List<StructureBlockInfo> list4 = StructureTemplate.combineSorted(list, list2, list3);
            this.blockInfoLists.clear();
            this.blockInfoLists.add(new PalettedBlockInfoList(list4));
            if (includeEntities) {
                this.addEntitiesFromWorld(world, blockPos2, blockPos3, logging);
            } else {
                this.entities.clear();
            }
        }
    }

    private static void categorize(StructureBlockInfo blockInfo, List<StructureBlockInfo> fullBlocks, List<StructureBlockInfo> blocksWithNbt, List<StructureBlockInfo> otherBlocks) {
        if (blockInfo.nbt != null) {
            blocksWithNbt.add(blockInfo);
        } else if (!blockInfo.state.getBlock().hasDynamicBounds() && blockInfo.state.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) {
            fullBlocks.add(blockInfo);
        } else {
            otherBlocks.add(blockInfo);
        }
    }

    private static List<StructureBlockInfo> combineSorted(List<StructureBlockInfo> fullBlocks, List<StructureBlockInfo> blocksWithNbt, List<StructureBlockInfo> otherBlocks) {
        Comparator<StructureBlockInfo> comparator = Comparator.comparingInt(blockInfo -> blockInfo.pos.getY()).thenComparingInt(blockInfo -> blockInfo.pos.getX()).thenComparingInt(blockInfo -> blockInfo.pos.getZ());
        fullBlocks.sort(comparator);
        otherBlocks.sort(comparator);
        blocksWithNbt.sort(comparator);
        ArrayList list = Lists.newArrayList();
        list.addAll(fullBlocks);
        list.addAll(otherBlocks);
        list.addAll(blocksWithNbt);
        return list;
    }

    private void addEntitiesFromWorld(World world, BlockPos firstCorner, BlockPos secondCorner, ErrorReporter errorReporter) {
        List<Entity> list = world.getEntitiesByClass(Entity.class, Box.enclosing(firstCorner, secondCorner), entity -> !(entity instanceof PlayerEntity));
        this.entities.clear();
        for (Entity entity2 : list) {
            BlockPos blockPos;
            Vec3d vec3d = new Vec3d(entity2.getX() - (double)firstCorner.getX(), entity2.getY() - (double)firstCorner.getY(), entity2.getZ() - (double)firstCorner.getZ());
            NbtWriteView nbtWriteView = NbtWriteView.create(errorReporter.makeChild(entity2.getErrorReporterContext()), entity2.getRegistryManager());
            entity2.saveData(nbtWriteView);
            if (entity2 instanceof PaintingEntity) {
                PaintingEntity paintingEntity = (PaintingEntity)entity2;
                blockPos = paintingEntity.getAttachedBlockPos().subtract(firstCorner);
            } else {
                blockPos = BlockPos.ofFloored(vec3d);
            }
            this.entities.add(new StructureEntityInfo(vec3d, blockPos, nbtWriteView.getNbt().copy()));
        }
    }

    public List<StructureBlockInfo> getInfosForBlock(BlockPos pos, StructurePlacementData placementData, Block block) {
        return this.getInfosForBlock(pos, placementData, block, true);
    }

    public List<JigsawBlockInfo> getJigsawInfos(BlockPos pos, BlockRotation rotation) {
        if (this.blockInfoLists.isEmpty()) {
            return new ArrayList<JigsawBlockInfo>();
        }
        StructurePlacementData structurePlacementData = new StructurePlacementData().setRotation(rotation);
        List<JigsawBlockInfo> list = structurePlacementData.getRandomBlockInfos(this.blockInfoLists, pos).getOrCreateJigsawBlockInfos();
        ArrayList<JigsawBlockInfo> list2 = new ArrayList<JigsawBlockInfo>(list.size());
        for (JigsawBlockInfo jigsawBlockInfo : list) {
            StructureBlockInfo structureBlockInfo = jigsawBlockInfo.info;
            list2.add(jigsawBlockInfo.withInfo(new StructureBlockInfo(StructureTemplate.transform(structurePlacementData, structureBlockInfo.pos()).add(pos), structureBlockInfo.state.rotate(structurePlacementData.getRotation()), structureBlockInfo.nbt)));
        }
        return list2;
    }

    public ObjectArrayList<StructureBlockInfo> getInfosForBlock(BlockPos pos, StructurePlacementData placementData, Block block, boolean transformed) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        BlockBox blockBox = placementData.getBoundingBox();
        if (this.blockInfoLists.isEmpty()) {
            return objectArrayList;
        }
        for (StructureBlockInfo structureBlockInfo : placementData.getRandomBlockInfos(this.blockInfoLists, pos).getAllOf(block)) {
            BlockPos blockPos;
            BlockPos blockPos2 = blockPos = transformed ? StructureTemplate.transform(placementData, structureBlockInfo.pos).add(pos) : structureBlockInfo.pos;
            if (blockBox != null && !blockBox.contains(blockPos)) continue;
            objectArrayList.add((Object)new StructureBlockInfo(blockPos, structureBlockInfo.state.rotate(placementData.getRotation()), structureBlockInfo.nbt));
        }
        return objectArrayList;
    }

    public BlockPos transformBox(StructurePlacementData placementData1, BlockPos pos1, StructurePlacementData placementData2, BlockPos pos2) {
        BlockPos blockPos = StructureTemplate.transform(placementData1, pos1);
        BlockPos blockPos2 = StructureTemplate.transform(placementData2, pos2);
        return blockPos.subtract(blockPos2);
    }

    public static BlockPos transform(StructurePlacementData placementData, BlockPos pos) {
        return StructureTemplate.transformAround(pos, placementData.getMirror(), placementData.getRotation(), placementData.getPosition());
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public boolean place(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, Random random, @Block.SetBlockStateFlag int flags) {
        if (this.blockInfoLists.isEmpty()) {
            return false;
        }
        List<StructureBlockInfo> list = placementData.getRandomBlockInfos(this.blockInfoLists, pos).getAll();
        if (list.isEmpty() && (placementData.shouldIgnoreEntities() || this.entities.isEmpty()) || this.size.getX() < 1 || this.size.getY() < 1 || this.size.getZ() < 1) {
            return false;
        }
        BlockBox blockBox = placementData.getBoundingBox();
        ArrayList list2 = Lists.newArrayListWithCapacity((int)(placementData.shouldApplyWaterlogging() ? list.size() : 0));
        ArrayList list3 = Lists.newArrayListWithCapacity((int)(placementData.shouldApplyWaterlogging() ? list.size() : 0));
        @Nullable ArrayList list4 = Lists.newArrayListWithCapacity((int)list.size());
        int i = Integer.MAX_VALUE;
        int j = Integer.MAX_VALUE;
        int k = Integer.MAX_VALUE;
        int l = Integer.MIN_VALUE;
        int m = Integer.MIN_VALUE;
        int n = Integer.MIN_VALUE;
        List<StructureBlockInfo> list5 = StructureTemplate.process(world, pos, pivot, placementData, list);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER);){
            for (StructureBlockInfo structureBlockInfo : list5) {
                BlockEntity blockEntity;
                BlockPos blockPos = structureBlockInfo.pos;
                if (blockBox != null && !blockBox.contains(blockPos)) continue;
                FluidState fluidState = placementData.shouldApplyWaterlogging() ? world.getFluidState(blockPos) : null;
                BlockState blockState = structureBlockInfo.state.mirror(placementData.getMirror()).rotate(placementData.getRotation());
                if (structureBlockInfo.nbt != null) {
                    world.setBlockState(blockPos, Blocks.BARRIER.getDefaultState(), 820);
                }
                if (!world.setBlockState(blockPos, blockState, flags)) continue;
                i = Math.min(i, blockPos.getX());
                j = Math.min(j, blockPos.getY());
                k = Math.min(k, blockPos.getZ());
                l = Math.max(l, blockPos.getX());
                m = Math.max(m, blockPos.getY());
                n = Math.max(n, blockPos.getZ());
                list4.add(Pair.of((Object)blockPos, (Object)structureBlockInfo.nbt));
                if (structureBlockInfo.nbt != null && (blockEntity = world.getBlockEntity(blockPos)) != null) {
                    if (!SharedConstants.STRUCTURE_EDIT_MODE && blockEntity instanceof LootableInventory) {
                        structureBlockInfo.nbt.putLong("LootTableSeed", random.nextLong());
                    }
                    blockEntity.read(NbtReadView.create(logging.makeChild(blockEntity.getReporterContext()), world.getRegistryManager(), structureBlockInfo.nbt));
                }
                if (fluidState == null) continue;
                if (blockState.getFluidState().isStill()) {
                    list3.add(blockPos);
                    continue;
                }
                if (!(blockState.getBlock() instanceof FluidFillable)) continue;
                ((FluidFillable)((Object)blockState.getBlock())).tryFillWithFluid(world, blockPos, blockState, fluidState);
                if (fluidState.isStill()) continue;
                list2.add(blockPos);
            }
            boolean bl = true;
            Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            while (bl && !list2.isEmpty()) {
                bl = false;
                Iterator iterator = list2.iterator();
                while (iterator.hasNext()) {
                    BlockState blockState2;
                    Object block;
                    BlockPos blockPos2 = (BlockPos)iterator.next();
                    FluidState fluidState2 = world.getFluidState(blockPos2);
                    for (int o = 0; o < directions.length && !fluidState2.isStill(); ++o) {
                        BlockPos blockPos3 = blockPos2.offset(directions[o]);
                        FluidState fluidState3 = world.getFluidState(blockPos3);
                        if (!fluidState3.isStill() || list3.contains(blockPos3)) continue;
                        fluidState2 = fluidState3;
                    }
                    if (!fluidState2.isStill() || !((block = (blockState2 = world.getBlockState(blockPos2)).getBlock()) instanceof FluidFillable)) continue;
                    ((FluidFillable)block).tryFillWithFluid(world, blockPos2, blockState2, fluidState2);
                    bl = true;
                    iterator.remove();
                }
            }
            if (i <= l) {
                if (!placementData.shouldUpdateNeighbors()) {
                    BitSetVoxelSet voxelSet = new BitSetVoxelSet(l - i + 1, m - j + 1, n - k + 1);
                    int p = i;
                    int q = j;
                    int o = k;
                    for (Pair pair : list4) {
                        BlockPos blockPos4 = (BlockPos)pair.getFirst();
                        ((VoxelSet)voxelSet).set(blockPos4.getX() - p, blockPos4.getY() - q, blockPos4.getZ() - o);
                    }
                    StructureTemplate.updateCorner(world, flags, voxelSet, p, q, o);
                }
                for (Pair pair2 : list4) {
                    BlockEntity blockEntity;
                    BlockPos blockPos5 = (BlockPos)pair2.getFirst();
                    if (!placementData.shouldUpdateNeighbors()) {
                        BlockState blockState3;
                        BlockState blockState2 = world.getBlockState(blockPos5);
                        if (blockState2 != (blockState3 = Block.postProcessState(blockState2, world, blockPos5))) {
                            world.setBlockState(blockPos5, blockState3, flags & 0xFFFFFFFE | 0x10);
                        }
                        world.updateNeighbors(blockPos5, blockState3.getBlock());
                    }
                    if (pair2.getSecond() == null || (blockEntity = world.getBlockEntity(blockPos5)) == null) continue;
                    blockEntity.markDirty();
                }
            }
            if (!placementData.shouldIgnoreEntities()) {
                this.spawnEntities(world, pos, placementData.getMirror(), placementData.getRotation(), placementData.getPosition(), blockBox, placementData.shouldInitializeMobs(), logging);
            }
        }
        return true;
    }

    public static void updateCorner(WorldAccess world, @Block.SetBlockStateFlag int flags, VoxelSet set, BlockPos startPos) {
        StructureTemplate.updateCorner(world, flags, set, startPos.getX(), startPos.getY(), startPos.getZ());
    }

    public static void updateCorner(WorldAccess world, @Block.SetBlockStateFlag int flags, VoxelSet set, int startX, int startY, int startZ) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockPos.Mutable mutable2 = new BlockPos.Mutable();
        set.forEachDirection((direction, x, y, z) -> {
            BlockState blockState4;
            mutable.set(startX + x, startY + y, startZ + z);
            mutable2.set((Vec3i)mutable, direction);
            BlockState blockState = world.getBlockState(mutable);
            BlockState blockState2 = world.getBlockState(mutable2);
            BlockState blockState3 = blockState.getStateForNeighborUpdate(world, world, mutable, direction, mutable2, blockState2, world.getRandom());
            if (blockState != blockState3) {
                world.setBlockState(mutable, blockState3, flags & 0xFFFFFFFE);
            }
            if (blockState2 != (blockState4 = blockState2.getStateForNeighborUpdate(world, world, mutable2, direction.getOpposite(), mutable, blockState3, world.getRandom()))) {
                world.setBlockState(mutable2, blockState4, flags & 0xFFFFFFFE);
            }
        });
    }

    public static List<StructureBlockInfo> process(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, List<StructureBlockInfo> infos) {
        ArrayList<StructureBlockInfo> list = new ArrayList<StructureBlockInfo>();
        List<StructureBlockInfo> list2 = new ArrayList<StructureBlockInfo>();
        for (StructureBlockInfo structureBlockInfo : infos) {
            BlockPos blockPos = StructureTemplate.transform(placementData, structureBlockInfo.pos).add(pos);
            StructureBlockInfo structureBlockInfo2 = new StructureBlockInfo(blockPos, structureBlockInfo.state, structureBlockInfo.nbt != null ? structureBlockInfo.nbt.copy() : null);
            Iterator<StructureProcessor> iterator = placementData.getProcessors().iterator();
            while (structureBlockInfo2 != null && iterator.hasNext()) {
                structureBlockInfo2 = iterator.next().process(world, pos, pivot, structureBlockInfo, structureBlockInfo2, placementData);
            }
            if (structureBlockInfo2 == null) continue;
            list2.add(structureBlockInfo2);
            list.add(structureBlockInfo);
        }
        for (StructureProcessor structureProcessor : placementData.getProcessors()) {
            list2 = structureProcessor.reprocess(world, pos, pivot, list, list2, placementData);
        }
        return list2;
    }

    private void spawnEntities(ServerWorldAccess world, BlockPos pos, BlockMirror mirror, BlockRotation rotation, BlockPos pivot, @Nullable BlockBox area, boolean initializeMobs, ErrorReporter errorReporter) {
        for (StructureEntityInfo structureEntityInfo : this.entities) {
            BlockPos blockPos = StructureTemplate.transformAround(structureEntityInfo.blockPos, mirror, rotation, pivot).add(pos);
            if (area != null && !area.contains(blockPos)) continue;
            NbtCompound nbtCompound = structureEntityInfo.nbt.copy();
            Vec3d vec3d = StructureTemplate.transformAround(structureEntityInfo.pos, mirror, rotation, pivot);
            Vec3d vec3d2 = vec3d.add(pos.getX(), pos.getY(), pos.getZ());
            NbtList nbtList = new NbtList();
            nbtList.add(NbtDouble.of(vec3d2.x));
            nbtList.add(NbtDouble.of(vec3d2.y));
            nbtList.add(NbtDouble.of(vec3d2.z));
            nbtCompound.put("Pos", nbtList);
            nbtCompound.remove("UUID");
            StructureTemplate.getEntity(errorReporter, world, nbtCompound).ifPresent(entity -> {
                float f = entity.applyRotation(rotation);
                entity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, f += entity.applyMirror(mirror) - entity.getYaw(), entity.getPitch());
                entity.setBodyYaw(f);
                entity.setHeadYaw(f);
                if (initializeMobs && entity instanceof MobEntity) {
                    MobEntity mobEntity = (MobEntity)entity;
                    mobEntity.initialize(world, world.getLocalDifficulty(BlockPos.ofFloored(vec3d2)), SpawnReason.STRUCTURE, null);
                }
                world.spawnEntityAndPassengers((Entity)entity);
            });
        }
    }

    private static Optional<Entity> getEntity(ErrorReporter errorReporter, ServerWorldAccess world, NbtCompound nbt) {
        try {
            return EntityType.getEntityFromData(NbtReadView.create(errorReporter, world.getRegistryManager(), nbt), world.toServerWorld(), SpawnReason.STRUCTURE);
        }
        catch (Exception exception) {
            return Optional.empty();
        }
    }

    public Vec3i getRotatedSize(BlockRotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
            }
        }
        return this.size;
    }

    public static BlockPos transformAround(BlockPos pos, BlockMirror mirror, BlockRotation rotation, BlockPos pivot) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT: {
                k = -k;
                break;
            }
            case FRONT_BACK: {
                i = -i;
                break;
            }
            default: {
                bl = false;
            }
        }
        int l = pivot.getX();
        int m = pivot.getZ();
        switch (rotation) {
            case CLOCKWISE_180: {
                return new BlockPos(l + l - i, j, m + m - k);
            }
            case COUNTERCLOCKWISE_90: {
                return new BlockPos(l - m + k, j, l + m - i);
            }
            case CLOCKWISE_90: {
                return new BlockPos(l + m - k, j, m - l + i);
            }
        }
        return bl ? new BlockPos(i, j, k) : pos;
    }

    public static Vec3d transformAround(Vec3d point, BlockMirror mirror, BlockRotation rotation, BlockPos pivot) {
        double d = point.x;
        double e = point.y;
        double f = point.z;
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT: {
                f = 1.0 - f;
                break;
            }
            case FRONT_BACK: {
                d = 1.0 - d;
                break;
            }
            default: {
                bl = false;
            }
        }
        int i = pivot.getX();
        int j = pivot.getZ();
        switch (rotation) {
            case CLOCKWISE_180: {
                return new Vec3d((double)(i + i + 1) - d, e, (double)(j + j + 1) - f);
            }
            case COUNTERCLOCKWISE_90: {
                return new Vec3d((double)(i - j) + f, e, (double)(i + j + 1) - d);
            }
            case CLOCKWISE_90: {
                return new Vec3d((double)(i + j + 1) - f, e, (double)(j - i) + d);
            }
        }
        return bl ? new Vec3d(d, e, f) : point;
    }

    public BlockPos offsetByTransformedSize(BlockPos pos, BlockMirror mirror, BlockRotation rotation) {
        return StructureTemplate.applyTransformedOffset(pos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
    }

    public static BlockPos applyTransformedOffset(BlockPos pos, BlockMirror mirror, BlockRotation rotation, int offsetX, int offsetZ) {
        int i = mirror == BlockMirror.FRONT_BACK ? --offsetX : 0;
        int j = mirror == BlockMirror.LEFT_RIGHT ? --offsetZ : 0;
        BlockPos blockPos = pos;
        switch (rotation) {
            case NONE: {
                blockPos = pos.add(i, 0, j);
                break;
            }
            case CLOCKWISE_90: {
                blockPos = pos.add(offsetZ - j, 0, i);
                break;
            }
            case CLOCKWISE_180: {
                blockPos = pos.add(offsetX - i, 0, offsetZ - j);
                break;
            }
            case COUNTERCLOCKWISE_90: {
                blockPos = pos.add(j, 0, offsetX - i);
            }
        }
        return blockPos;
    }

    public BlockBox calculateBoundingBox(StructurePlacementData placementData, BlockPos pos) {
        return this.calculateBoundingBox(pos, placementData.getRotation(), placementData.getPosition(), placementData.getMirror());
    }

    public BlockBox calculateBoundingBox(BlockPos pos, BlockRotation rotation, BlockPos pivot, BlockMirror mirror) {
        return StructureTemplate.createBox(pos, rotation, pivot, mirror, this.size);
    }

    @VisibleForTesting
    protected static BlockBox createBox(BlockPos pos, BlockRotation rotation, BlockPos pivot, BlockMirror mirror, Vec3i dimensions) {
        Vec3i vec3i = dimensions.add(-1, -1, -1);
        BlockPos blockPos = StructureTemplate.transformAround(BlockPos.ORIGIN, mirror, rotation, pivot);
        BlockPos blockPos2 = StructureTemplate.transformAround(BlockPos.ORIGIN.add(vec3i), mirror, rotation, pivot);
        return BlockBox.create(blockPos, blockPos2).move(pos);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        if (this.blockInfoLists.isEmpty()) {
            nbt.put(BLOCKS_KEY, new NbtList());
            nbt.put(PALETTE_KEY, new NbtList());
        } else {
            ArrayList list = Lists.newArrayList();
            Palette palette = new Palette();
            list.add(palette);
            for (int i = 1; i < this.blockInfoLists.size(); ++i) {
                list.add(new Palette());
            }
            NbtList nbtList = new NbtList();
            List<StructureBlockInfo> list2 = this.blockInfoLists.get(0).getAll();
            for (int j = 0; j < list2.size(); ++j) {
                StructureBlockInfo structureBlockInfo = list2.get(j);
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.put("pos", this.createNbtIntList(structureBlockInfo.pos.getX(), structureBlockInfo.pos.getY(), structureBlockInfo.pos.getZ()));
                int k = palette.getId(structureBlockInfo.state);
                nbtCompound.putInt(BLOCKS_STATE_KEY, k);
                if (structureBlockInfo.nbt != null) {
                    nbtCompound.put("nbt", structureBlockInfo.nbt);
                }
                nbtList.add(nbtCompound);
                for (int l = 1; l < this.blockInfoLists.size(); ++l) {
                    Palette palette2 = (Palette)list.get(l);
                    palette2.set(this.blockInfoLists.get((int)l).getAll().get((int)j).state, k);
                }
            }
            nbt.put(BLOCKS_KEY, nbtList);
            if (list.size() == 1) {
                nbtList2 = new NbtList();
                for (BlockState blockState : palette) {
                    nbtList2.add(NbtHelper.fromBlockState(blockState));
                }
                nbt.put(PALETTE_KEY, nbtList2);
            } else {
                nbtList2 = new NbtList();
                for (Palette palette3 : list) {
                    NbtList nbtList3 = new NbtList();
                    for (BlockState blockState2 : palette3) {
                        nbtList3.add(NbtHelper.fromBlockState(blockState2));
                    }
                    nbtList2.add(nbtList3);
                }
                nbt.put(PALETTES_KEY, nbtList2);
            }
        }
        NbtList nbtList4 = new NbtList();
        for (StructureEntityInfo structureEntityInfo : this.entities) {
            NbtCompound nbtCompound2 = new NbtCompound();
            nbtCompound2.put("pos", this.createNbtDoubleList(structureEntityInfo.pos.x, structureEntityInfo.pos.y, structureEntityInfo.pos.z));
            nbtCompound2.put(ENTITIES_BLOCK_POS_KEY, this.createNbtIntList(structureEntityInfo.blockPos.getX(), structureEntityInfo.blockPos.getY(), structureEntityInfo.blockPos.getZ()));
            if (structureEntityInfo.nbt != null) {
                nbtCompound2.put("nbt", structureEntityInfo.nbt);
            }
            nbtList4.add(nbtCompound2);
        }
        nbt.put(ENTITIES_KEY, nbtList4);
        nbt.put(SIZE_KEY, this.createNbtIntList(this.size.getX(), this.size.getY(), this.size.getZ()));
        return NbtHelper.putDataVersion(nbt);
    }

    public void readNbt(RegistryEntryLookup<Block> blockLookup, NbtCompound nbt) {
        this.blockInfoLists.clear();
        this.entities.clear();
        NbtList nbtList = nbt.getListOrEmpty(SIZE_KEY);
        this.size = new Vec3i(nbtList.getInt(0, 0), nbtList.getInt(1, 0), nbtList.getInt(2, 0));
        NbtList nbtList2 = nbt.getListOrEmpty(BLOCKS_KEY);
        Optional<NbtList> optional = nbt.getList(PALETTES_KEY);
        if (optional.isPresent()) {
            for (int i = 0; i < optional.get().size(); ++i) {
                this.loadPalettedBlockInfo(blockLookup, optional.get().getListOrEmpty(i), nbtList2);
            }
        } else {
            this.loadPalettedBlockInfo(blockLookup, nbt.getListOrEmpty(PALETTE_KEY), nbtList2);
        }
        nbt.getListOrEmpty(ENTITIES_KEY).streamCompounds().forEach(nbtx -> {
            NbtList nbtList = nbtx.getListOrEmpty("pos");
            Vec3d vec3d = new Vec3d(nbtList.getDouble(0, 0.0), nbtList.getDouble(1, 0.0), nbtList.getDouble(2, 0.0));
            NbtList nbtList2 = nbtx.getListOrEmpty(ENTITIES_BLOCK_POS_KEY);
            BlockPos blockPos = new BlockPos(nbtList2.getInt(0, 0), nbtList2.getInt(1, 0), nbtList2.getInt(2, 0));
            nbtx.getCompound("nbt").ifPresent(blockEntityNbt -> this.entities.add(new StructureEntityInfo(vec3d, blockPos, (NbtCompound)blockEntityNbt)));
        });
    }

    private void loadPalettedBlockInfo(RegistryEntryLookup<Block> blockLookup, NbtList palette, NbtList blocks) {
        Palette palette2 = new Palette();
        for (int i = 0; i < palette.size(); ++i) {
            palette2.set(NbtHelper.toBlockState(blockLookup, palette.getCompoundOrEmpty(i)), i);
        }
        ArrayList list = Lists.newArrayList();
        ArrayList list2 = Lists.newArrayList();
        ArrayList list3 = Lists.newArrayList();
        blocks.streamCompounds().forEach(nbt -> {
            NbtList nbtList = nbt.getListOrEmpty("pos");
            BlockPos blockPos = new BlockPos(nbtList.getInt(0, 0), nbtList.getInt(1, 0), nbtList.getInt(2, 0));
            BlockState blockState = palette2.getState(nbt.getInt(BLOCKS_STATE_KEY, 0));
            NbtCompound nbtCompound = nbt.getCompound("nbt").orElse(null);
            StructureBlockInfo structureBlockInfo = new StructureBlockInfo(blockPos, blockState, nbtCompound);
            StructureTemplate.categorize(structureBlockInfo, list, list2, list3);
        });
        List<StructureBlockInfo> list4 = StructureTemplate.combineSorted(list, list2, list3);
        this.blockInfoLists.add(new PalettedBlockInfoList(list4));
    }

    private NbtList createNbtIntList(int ... ints) {
        NbtList nbtList = new NbtList();
        for (int i : ints) {
            nbtList.add(NbtInt.of(i));
        }
        return nbtList;
    }

    private NbtList createNbtDoubleList(double ... doubles) {
        NbtList nbtList = new NbtList();
        for (double d : doubles) {
            nbtList.add(NbtDouble.of(d));
        }
        return nbtList;
    }

    public static JigsawBlockEntity.Joint readJoint(NbtCompound nbt, BlockState state) {
        return nbt.get("joint", JigsawBlockEntity.Joint.CODEC).orElseGet(() -> StructureTemplate.getJointFromFacing(state));
    }

    public static JigsawBlockEntity.Joint getJointFromFacing(BlockState state) {
        return JigsawBlock.getFacing(state).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE;
    }

    public static final class StructureBlockInfo
    extends Record {
        final BlockPos pos;
        final BlockState state;
        final @Nullable NbtCompound nbt;

        public StructureBlockInfo(BlockPos pos, BlockState state, @Nullable NbtCompound nbt) {
            this.pos = pos;
            this.state = state;
            this.nbt = nbt;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StructureBlockInfo.class, "pos;state;nbt", "pos", "state", "nbt"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StructureBlockInfo.class, "pos;state;nbt", "pos", "state", "nbt"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockState state() {
            return this.state;
        }

        public @Nullable NbtCompound nbt() {
            return this.nbt;
        }
    }

    public static final class PalettedBlockInfoList {
        private final List<StructureBlockInfo> infos;
        private final Map<Block, List<StructureBlockInfo>> blockToInfos = Maps.newHashMap();
        private @Nullable List<JigsawBlockInfo> jigsawBlockInfos;

        PalettedBlockInfoList(List<StructureBlockInfo> infos) {
            this.infos = infos;
        }

        public List<JigsawBlockInfo> getOrCreateJigsawBlockInfos() {
            if (this.jigsawBlockInfos == null) {
                this.jigsawBlockInfos = this.getAllOf(Blocks.JIGSAW).stream().map(JigsawBlockInfo::of).toList();
            }
            return this.jigsawBlockInfos;
        }

        public List<StructureBlockInfo> getAll() {
            return this.infos;
        }

        public List<StructureBlockInfo> getAllOf(Block block) {
            return this.blockToInfos.computeIfAbsent(block, block2 -> this.infos.stream().filter(info -> info.state.isOf((Block)block2)).collect(Collectors.toList()));
        }
    }

    public static class StructureEntityInfo {
        public final Vec3d pos;
        public final BlockPos blockPos;
        public final NbtCompound nbt;

        public StructureEntityInfo(Vec3d pos, BlockPos blockPos, NbtCompound nbt) {
            this.pos = pos;
            this.blockPos = blockPos;
            this.nbt = nbt;
        }
    }

    public static final class JigsawBlockInfo
    extends Record {
        final StructureBlockInfo info;
        private final JigsawBlockEntity.Joint jointType;
        private final Identifier name;
        private final RegistryKey<StructurePool> pool;
        private final Identifier target;
        private final int placementPriority;
        private final int selectionPriority;

        public JigsawBlockInfo(StructureBlockInfo info, JigsawBlockEntity.Joint jointType, Identifier name, RegistryKey<StructurePool> pool, Identifier target, int placementPriority, int selectionPriority) {
            this.info = info;
            this.jointType = jointType;
            this.name = name;
            this.pool = pool;
            this.target = target;
            this.placementPriority = placementPriority;
            this.selectionPriority = selectionPriority;
        }

        public static JigsawBlockInfo of(StructureBlockInfo structureBlockInfo) {
            NbtCompound nbtCompound = Objects.requireNonNull(structureBlockInfo.nbt(), () -> String.valueOf(structureBlockInfo) + " nbt was null");
            return new JigsawBlockInfo(structureBlockInfo, StructureTemplate.readJoint(nbtCompound, structureBlockInfo.state()), nbtCompound.get("name", Identifier.CODEC).orElse(JigsawBlockEntity.DEFAULT_NAME), nbtCompound.get("pool", JigsawBlockEntity.STRUCTURE_POOL_KEY_CODEC).orElse(StructurePools.EMPTY), nbtCompound.get("target", Identifier.CODEC).orElse(JigsawBlockEntity.DEFAULT_NAME), nbtCompound.getInt("placement_priority", 0), nbtCompound.getInt("selection_priority", 0));
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "<JigsawBlockInfo | %s | %s | name: %s | pool: %s | target: %s | placement: %d | selection: %d | %s>", this.info.pos, this.info.state, this.name, this.pool.getValue(), this.target, this.placementPriority, this.selectionPriority, this.info.nbt);
        }

        public JigsawBlockInfo withInfo(StructureBlockInfo structureBlockInfo) {
            return new JigsawBlockInfo(structureBlockInfo, this.jointType, this.name, this.pool, this.target, this.placementPriority, this.selectionPriority);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{JigsawBlockInfo.class, "info;jointType;name;pool;target;placementPriority;selectionPriority", "info", "jointType", "name", "pool", "target", "placementPriority", "selectionPriority"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{JigsawBlockInfo.class, "info;jointType;name;pool;target;placementPriority;selectionPriority", "info", "jointType", "name", "pool", "target", "placementPriority", "selectionPriority"}, this, object);
        }

        public StructureBlockInfo info() {
            return this.info;
        }

        public JigsawBlockEntity.Joint jointType() {
            return this.jointType;
        }

        public Identifier name() {
            return this.name;
        }

        public RegistryKey<StructurePool> pool() {
            return this.pool;
        }

        public Identifier target() {
            return this.target;
        }

        public int placementPriority() {
            return this.placementPriority;
        }

        public int selectionPriority() {
            return this.selectionPriority;
        }
    }

    static class Palette
    implements Iterable<BlockState> {
        public static final BlockState AIR = Blocks.AIR.getDefaultState();
        private final IdList<BlockState> ids = new IdList(16);
        private int currentIndex;

        Palette() {
        }

        public int getId(BlockState state) {
            int i = this.ids.getRawId(state);
            if (i == -1) {
                i = this.currentIndex++;
                this.ids.set(state, i);
            }
            return i;
        }

        public @Nullable BlockState getState(int id) {
            BlockState blockState = this.ids.get(id);
            return blockState == null ? AIR : blockState;
        }

        @Override
        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void set(BlockState state, int id) {
            this.ids.set(state, id);
        }
    }
}

