/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.JigsawBlock;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.PriorityIterator;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class StructurePoolBasedGenerator {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int HEIGHT_NOT_SET = Integer.MIN_VALUE;

    public static Optional<Structure.StructurePosition> generate(Structure.Context context, RegistryEntry<StructurePool> structurePool, Optional<Identifier> id, int size, BlockPos pos, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, JigsawStructure.MaxDistanceFromCenter maxDistanceFromCenter, StructurePoolAliasLookup aliasLookup, DimensionPadding dimensionPadding, StructureLiquidSettings liquidSettings) {
        BlockPos blockPos;
        DynamicRegistryManager dynamicRegistryManager = context.dynamicRegistryManager();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
        HeightLimitView heightLimitView = context.world();
        ChunkRandom chunkRandom = context.random();
        RegistryWrapper.Impl registry = dynamicRegistryManager.getOrThrow(RegistryKeys.TEMPLATE_POOL);
        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        StructurePool structurePool2 = structurePool.getKey().flatMap(arg_0 -> StructurePoolBasedGenerator.method_55604((Registry)registry, aliasLookup, arg_0)).orElse(structurePool.value());
        StructurePoolElement structurePoolElement = structurePool2.getRandomElement(chunkRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        if (id.isPresent()) {
            Identifier identifier = id.get();
            Optional<BlockPos> optional = StructurePoolBasedGenerator.findStartingJigsawPos(structurePoolElement, identifier, pos, blockRotation, structureTemplateManager, chunkRandom);
            if (optional.isEmpty()) {
                LOGGER.error("No starting jigsaw {} found in start pool {}", (Object)identifier, (Object)structurePool.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>"));
                return Optional.empty();
            }
            blockPos = optional.get();
        } else {
            blockPos = pos;
        }
        BlockPos vec3i = blockPos.subtract(pos);
        BlockPos blockPos2 = pos.subtract(vec3i);
        PoolStructurePiece poolStructurePiece = new PoolStructurePiece(structureTemplateManager, structurePoolElement, blockPos2, structurePoolElement.getGroundLevelDelta(), blockRotation, structurePoolElement.getBoundingBox(structureTemplateManager, blockPos2, blockRotation), liquidSettings);
        BlockBox blockBox = poolStructurePiece.getBoundingBox();
        int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
        int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
        int k = projectStartToHeightmap.isEmpty() ? blockPos2.getY() : pos.getY() + chunkGenerator.getHeightOnGround(i, j, projectStartToHeightmap.get(), heightLimitView, context.noiseConfig());
        int l = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.translate(0, k - l, 0);
        if (StructurePoolBasedGenerator.exceedsHeightLimit(heightLimitView, dimensionPadding, poolStructurePiece.getBoundingBox())) {
            LOGGER.debug("Center piece {} with bounding box {} does not fit dimension padding {}", new Object[]{structurePoolElement, poolStructurePiece.getBoundingBox(), dimensionPadding});
            return Optional.empty();
        }
        int m = k + vec3i.getY();
        return Optional.of(new Structure.StructurePosition(new BlockPos(i, m, j), arg_0 -> StructurePoolBasedGenerator.method_39824(poolStructurePiece, size, i, maxDistanceFromCenter, m, heightLimitView, dimensionPadding, j, blockBox, context, useExpansionHack, chunkGenerator, structureTemplateManager, chunkRandom, (Registry)registry, aliasLookup, liquidSettings, arg_0)));
    }

    private static boolean exceedsHeightLimit(HeightLimitView world, DimensionPadding padding, BlockBox box) {
        if (padding == DimensionPadding.NONE) {
            return false;
        }
        int i = world.getBottomY() + padding.bottom();
        int j = world.getTopYInclusive() - padding.top();
        return box.getMinY() < i || box.getMaxY() > j;
    }

    private static Optional<BlockPos> findStartingJigsawPos(StructurePoolElement pool, Identifier id, BlockPos pos, BlockRotation rotation, StructureTemplateManager structureManager, ChunkRandom random) {
        List<StructureTemplate.JigsawBlockInfo> list = pool.getStructureBlockInfos(structureManager, pos, rotation, random);
        for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo : list) {
            if (!id.equals(jigsawBlockInfo.name())) continue;
            return Optional.of(jigsawBlockInfo.info().pos());
        }
        return Optional.empty();
    }

    private static void generate(NoiseConfig noiseConfig, int maxSize, boolean modifyBoundingBox, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, HeightLimitView heightLimitView, Random random, Registry<StructurePool> structurePoolRegistry, PoolStructurePiece firstPiece, List<PoolStructurePiece> pieces, VoxelShape pieceShape, StructurePoolAliasLookup aliasLookup, StructureLiquidSettings liquidSettings) {
        StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(structurePoolRegistry, maxSize, chunkGenerator, structureTemplateManager, pieces, random);
        structurePoolGenerator.generatePiece(firstPiece, (MutableObject<VoxelShape>)new MutableObject((Object)pieceShape), 0, modifyBoundingBox, heightLimitView, noiseConfig, aliasLookup, liquidSettings);
        while (structurePoolGenerator.structurePieces.hasNext()) {
            ShapedPoolStructurePiece shapedPoolStructurePiece = (ShapedPoolStructurePiece)structurePoolGenerator.structurePieces.next();
            structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.depth, modifyBoundingBox, heightLimitView, noiseConfig, aliasLookup, liquidSettings);
        }
    }

    public static boolean generate(ServerWorld world, RegistryEntry<StructurePool> structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws) {
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        StructureTemplateManager structureTemplateManager = world.getStructureTemplateManager();
        StructureAccessor structureAccessor = world.getStructureAccessor();
        Random random = world.getRandom();
        Structure.Context context = new Structure.Context(world.getRegistryManager(), chunkGenerator, chunkGenerator.getBiomeSource(), world.getChunkManager().getNoiseConfig(), structureTemplateManager, world.getSeed(), new ChunkPos(pos), world, biome -> true);
        Optional<Structure.StructurePosition> optional = StructurePoolBasedGenerator.generate(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), new JigsawStructure.MaxDistanceFromCenter(128), StructurePoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
        if (optional.isPresent()) {
            StructurePiecesCollector structurePiecesCollector = optional.get().generate();
            for (StructurePiece structurePiece : structurePiecesCollector.toList().pieces()) {
                if (!(structurePiece instanceof PoolStructurePiece)) continue;
                PoolStructurePiece poolStructurePiece = (PoolStructurePiece)structurePiece;
                poolStructurePiece.generate((StructureWorldAccess)world, structureAccessor, chunkGenerator, random, BlockBox.infinite(), pos, keepJigsaws);
            }
            return true;
        }
        return false;
    }

    private static /* synthetic */ void method_39824(PoolStructurePiece poolStructurePiece, int i, int j, JigsawStructure.MaxDistanceFromCenter maxDistanceFromCenter, int k, HeightLimitView heightLimitView, DimensionPadding dimensionPadding, int l, BlockBox blockBox, Structure.Context context, boolean bl, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, ChunkRandom chunkRandom, Registry registry, StructurePoolAliasLookup structurePoolAliasLookup, StructureLiquidSettings structureLiquidSettings, StructurePiecesCollector collector) {
        ArrayList list = Lists.newArrayList();
        list.add(poolStructurePiece);
        if (i <= 0) {
            return;
        }
        Box box = new Box(j - maxDistanceFromCenter.horizontal(), Math.max(k - maxDistanceFromCenter.vertical(), heightLimitView.getBottomY() + dimensionPadding.bottom()), l - maxDistanceFromCenter.horizontal(), j + maxDistanceFromCenter.horizontal() + 1, Math.min(k + maxDistanceFromCenter.vertical() + 1, heightLimitView.getTopYInclusive() + 1 - dimensionPadding.top()), l + maxDistanceFromCenter.horizontal() + 1);
        VoxelShape voxelShape = VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST);
        StructurePoolBasedGenerator.generate(context.noiseConfig(), i, bl, chunkGenerator, structureTemplateManager, heightLimitView, chunkRandom, registry, poolStructurePiece, list, voxelShape, structurePoolAliasLookup, structureLiquidSettings);
        list.forEach(collector::addPiece);
    }

    private static /* synthetic */ Optional method_55604(Registry registry, StructurePoolAliasLookup structurePoolAliasLookup, RegistryKey key) {
        return registry.getOptionalValue(structurePoolAliasLookup.lookup(key));
    }

    static final class StructurePoolGenerator {
        private final Registry<StructurePool> registry;
        private final int maxSize;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolStructurePiece> children;
        private final Random random;
        final PriorityIterator<ShapedPoolStructurePiece> structurePieces = new PriorityIterator();

        StructurePoolGenerator(Registry<StructurePool> registry, int maxSize, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, List<? super PoolStructurePiece> children, Random random) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.chunkGenerator = chunkGenerator;
            this.structureTemplateManager = structureTemplateManager;
            this.children = children;
            this.random = random;
        }

        void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int depth, boolean modifyBoundingBox, HeightLimitView world, NoiseConfig noiseConfig, StructurePoolAliasLookup aliasLookup, StructureLiquidSettings liquidSettings) {
            StructurePoolElement structurePoolElement = piece.getPoolElement();
            BlockPos blockPos = piece.getPos();
            BlockRotation blockRotation = piece.getRotation();
            StructurePool.Projection projection = structurePoolElement.getProjection();
            boolean bl = projection == StructurePool.Projection.RIGID;
            MutableObject<@Nullable VoxelShape> mutableObject = new MutableObject<VoxelShape>();
            BlockBox blockBox = piece.getBoundingBox();
            int i = blockBox.getMinY();
            block0: for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo : structurePoolElement.getStructureBlockInfos(this.structureTemplateManager, blockPos, blockRotation, this.random)) {
                StructurePoolElement structurePoolElement2;
                MutableObject<VoxelShape> mutableObject2;
                StructureTemplate.StructureBlockInfo structureBlockInfo = jigsawBlockInfo.info();
                Direction direction = JigsawBlock.getFacing(structureBlockInfo.state());
                BlockPos blockPos2 = structureBlockInfo.pos();
                BlockPos blockPos3 = blockPos2.offset(direction);
                int j = blockPos2.getY() - i;
                int k = Integer.MIN_VALUE;
                RegistryKey<StructurePool> registryKey = aliasLookup.lookup(jigsawBlockInfo.pool());
                Optional optional = this.registry.getOptional(registryKey);
                if (optional.isEmpty()) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)registryKey.getValue());
                    continue;
                }
                RegistryEntry registryEntry = (RegistryEntry)optional.get();
                if (((StructurePool)registryEntry.value()).getElementCount() == 0 && !registryEntry.matchesKey(StructurePools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)registryKey.getValue());
                    continue;
                }
                RegistryEntry<StructurePool> registryEntry2 = ((StructurePool)registryEntry.value()).getFallback();
                if (registryEntry2.value().getElementCount() == 0 && !registryEntry2.matchesKey(StructurePools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)registryEntry2.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>"));
                    continue;
                }
                boolean bl2 = blockBox.contains(blockPos3);
                if (bl2) {
                    mutableObject2 = mutableObject;
                    if (mutableObject.get() == null) {
                        mutableObject.setValue((Object)VoxelShapes.cuboid(Box.from(blockBox)));
                    }
                } else {
                    mutableObject2 = pieceShape;
                }
                ArrayList list = Lists.newArrayList();
                if (depth != this.maxSize) {
                    list.addAll(((StructurePool)registryEntry.value()).getElementIndicesInRandomOrder(this.random));
                }
                list.addAll(registryEntry2.value().getElementIndicesInRandomOrder(this.random));
                int l = jigsawBlockInfo.placementPriority();
                Iterator iterator = list.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement)iterator.next()) != EmptyPoolElement.INSTANCE) {
                    for (BlockRotation blockRotation2 : BlockRotation.randomRotationOrder(this.random)) {
                        List<StructureTemplate.JigsawBlockInfo> list2 = structurePoolElement2.getStructureBlockInfos(this.structureTemplateManager, BlockPos.ORIGIN, blockRotation2, this.random);
                        BlockBox blockBox2 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, BlockPos.ORIGIN, blockRotation2);
                        int m = !modifyBoundingBox || blockBox2.getBlockCountY() > 16 ? 0 : list2.stream().mapToInt(jigsawInfo -> {
                            StructureTemplate.StructureBlockInfo structureBlockInfo = jigsawInfo.info();
                            if (!blockBox2.contains(structureBlockInfo.pos().offset(JigsawBlock.getFacing(structureBlockInfo.state())))) {
                                return 0;
                            }
                            RegistryKey<StructurePool> registryKey = aliasLookup.lookup(jigsawInfo.pool());
                            Optional optional = this.registry.getOptional(registryKey);
                            Optional<RegistryEntry> optional2 = optional.map(entry -> ((StructurePool)entry.value()).getFallback());
                            int i = optional.map(entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)).orElse(0);
                            int j = optional2.map(entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)).orElse(0);
                            return Math.max(i, j);
                        }).max().orElse(0);
                        for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo2 : list2) {
                            int u;
                            int s;
                            int q;
                            if (!JigsawBlock.attachmentMatches(jigsawBlockInfo, jigsawBlockInfo2)) continue;
                            BlockPos blockPos4 = jigsawBlockInfo2.info().pos();
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BlockBox blockBox3 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, blockPos5, blockRotation2);
                            int n = blockBox3.getMinY();
                            StructurePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl3 = projection2 == StructurePool.Projection.RIGID;
                            int o = blockPos4.getY();
                            int p = j - o + JigsawBlock.getFacing(structureBlockInfo.state()).getOffsetY();
                            if (bl && bl3) {
                                q = i + p;
                            } else {
                                if (k == Integer.MIN_VALUE) {
                                    k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                                }
                                q = k - o;
                            }
                            int r = q - n;
                            BlockBox blockBox4 = blockBox3.offset(0, r, 0);
                            BlockPos blockPos6 = blockPos5.add(0, r, 0);
                            if (m > 0) {
                                s = Math.max(m + 1, blockBox4.getMaxY() - blockBox4.getMinY());
                                blockBox4.encompass(new BlockPos(blockBox4.getMinX(), blockBox4.getMinY() + s, blockBox4.getMinZ()));
                            }
                            if (VoxelShapes.matchesAnywhere((VoxelShape)mutableObject2.get(), VoxelShapes.cuboid(Box.from(blockBox4).contract(0.25)), BooleanBiFunction.ONLY_SECOND)) continue;
                            mutableObject2.setValue((Object)VoxelShapes.combine((VoxelShape)mutableObject2.get(), VoxelShapes.cuboid(Box.from(blockBox4)), BooleanBiFunction.ONLY_FIRST));
                            s = piece.getGroundLevelDelta();
                            int t = bl3 ? s - p : structurePoolElement2.getGroundLevelDelta();
                            PoolStructurePiece poolStructurePiece = new PoolStructurePiece(this.structureTemplateManager, structurePoolElement2, blockPos6, t, blockRotation2, blockBox4, liquidSettings);
                            if (bl) {
                                u = i + j;
                            } else if (bl3) {
                                u = q + o;
                            } else {
                                if (k == Integer.MIN_VALUE) {
                                    k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                                }
                                u = k + p / 2;
                            }
                            piece.addJunction(new JigsawJunction(blockPos3.getX(), u - j + s, blockPos3.getZ(), p, projection2));
                            poolStructurePiece.addJunction(new JigsawJunction(blockPos2.getX(), u - o + t, blockPos2.getZ(), -p, projection));
                            this.children.add(poolStructurePiece);
                            if (depth + 1 > this.maxSize) continue block0;
                            ShapedPoolStructurePiece shapedPoolStructurePiece = new ShapedPoolStructurePiece(poolStructurePiece, mutableObject2, depth + 1);
                            this.structurePieces.enqueue(shapedPoolStructurePiece, l);
                            continue block0;
                        }
                    }
                }
            }
        }
    }

    static final class ShapedPoolStructurePiece
    extends Record {
        final PoolStructurePiece piece;
        final MutableObject<VoxelShape> pieceShape;
        final int depth;

        ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize) {
            this.piece = piece;
            this.pieceShape = pieceShape;
            this.depth = currentSize;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ShapedPoolStructurePiece.class, "piece;free;depth", "piece", "pieceShape", "depth"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ShapedPoolStructurePiece.class, "piece;free;depth", "piece", "pieceShape", "depth"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ShapedPoolStructurePiece.class, "piece;free;depth", "piece", "pieceShape", "depth"}, this, object);
        }

        public PoolStructurePiece piece() {
            return this.piece;
        }

        public MutableObject<VoxelShape> pieceShape() {
            return this.pieceShape;
        }

        public int depth() {
            return this.depth;
        }
    }
}

