/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.structure.pool;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.JigsawReplacementStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SinglePoolElement
extends StructurePoolElement {
    private static final Comparator<StructureTemplate.JigsawBlockInfo> JIGSAW_BLOCK_INFO_COMPARATOR = Comparator.comparingInt(StructureTemplate.JigsawBlockInfo::selectionPriority).reversed();
    private static final Codec<Either<Identifier, StructureTemplate>> LOCATION_CODEC = Codec.of(SinglePoolElement::encodeLocation, (Decoder)Identifier.CODEC.map(Either::left));
    public static final MapCodec<SinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(SinglePoolElement.locationGetter(), SinglePoolElement.processorsGetter(), SinglePoolElement.projectionGetter(), SinglePoolElement.overrideLiquidSettingsGetter()).apply((Applicative)instance, SinglePoolElement::new));
    protected final Either<Identifier, StructureTemplate> location;
    protected final RegistryEntry<StructureProcessorList> processors;
    protected final Optional<StructureLiquidSettings> overrideLiquidSettings;

    private static <T> DataResult<T> encodeLocation(Either<Identifier, StructureTemplate> location, DynamicOps<T> ops, T prefix) {
        Optional optional = location.left();
        if (optional.isEmpty()) {
            return DataResult.error(() -> "Can not serialize a runtime pool element");
        }
        return Identifier.CODEC.encode((Object)((Identifier)optional.get()), ops, prefix);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, RegistryEntry<StructureProcessorList>> processorsGetter() {
        return StructureProcessorType.REGISTRY_CODEC.fieldOf("processors").forGetter(pool -> pool.processors);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Optional<StructureLiquidSettings>> overrideLiquidSettingsGetter() {
        return StructureLiquidSettings.codec.optionalFieldOf("override_liquid_settings").forGetter(pool -> pool.overrideLiquidSettings);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<Identifier, StructureTemplate>> locationGetter() {
        return LOCATION_CODEC.fieldOf("location").forGetter(pool -> pool.location);
    }

    protected SinglePoolElement(Either<Identifier, StructureTemplate> location, RegistryEntry<StructureProcessorList> processors, StructurePool.Projection projection, Optional<StructureLiquidSettings> overrideLiquidSettings) {
        super(projection);
        this.location = location;
        this.processors = processors;
        this.overrideLiquidSettings = overrideLiquidSettings;
    }

    @Override
    public Vec3i getStart(StructureTemplateManager structureTemplateManager, BlockRotation rotation) {
        StructureTemplate structureTemplate = this.getStructure(structureTemplateManager);
        return structureTemplate.getRotatedSize(rotation);
    }

    private StructureTemplate getStructure(StructureTemplateManager structureTemplateManager) {
        return (StructureTemplate)this.location.map(structureTemplateManager::getTemplateOrBlank, Function.identity());
    }

    public List<StructureTemplate.StructureBlockInfo> getDataStructureBlocks(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, boolean mirroredAndRotated) {
        StructureTemplate structureTemplate = this.getStructure(structureTemplateManager);
        ObjectArrayList<StructureTemplate.StructureBlockInfo> list = structureTemplate.getInfosForBlock(pos, new StructurePlacementData().setRotation(rotation), Blocks.STRUCTURE_BLOCK, mirroredAndRotated);
        ArrayList list2 = Lists.newArrayList();
        for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
            StructureBlockMode structureBlockMode;
            NbtCompound nbtCompound = structureBlockInfo.nbt();
            if (nbtCompound == null || (structureBlockMode = nbtCompound.get("mode", StructureBlockMode.CODEC).orElseThrow()) != StructureBlockMode.DATA) continue;
            list2.add(structureBlockInfo);
        }
        return list2;
    }

    @Override
    public List<StructureTemplate.JigsawBlockInfo> getStructureBlockInfos(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, Random random) {
        List<StructureTemplate.JigsawBlockInfo> list = this.getStructure(structureTemplateManager).getJigsawInfos(pos, rotation);
        Util.shuffle(list, random);
        SinglePoolElement.sort(list);
        return list;
    }

    @VisibleForTesting
    static void sort(List<StructureTemplate.JigsawBlockInfo> blocks) {
        blocks.sort(JIGSAW_BLOCK_INFO_COMPARATOR);
    }

    @Override
    public BlockBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation) {
        StructureTemplate structureTemplate = this.getStructure(structureTemplateManager);
        return structureTemplate.calculateBoundingBox(new StructurePlacementData().setRotation(rotation), pos);
    }

    @Override
    public boolean generate(StructureTemplateManager structureTemplateManager, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos pivot, BlockRotation rotation, BlockBox box, Random random, StructureLiquidSettings liquidSettings, boolean keepJigsaws) {
        StructurePlacementData structurePlacementData;
        StructureTemplate structureTemplate = this.getStructure(structureTemplateManager);
        if (structureTemplate.place(world, pos, pivot, structurePlacementData = this.createPlacementData(rotation, box, liquidSettings, keepJigsaws), random, 18)) {
            List<StructureTemplate.StructureBlockInfo> list = StructureTemplate.process(world, pos, pivot, structurePlacementData, this.getDataStructureBlocks(structureTemplateManager, pos, rotation, false));
            for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
                this.method_16756(world, structureBlockInfo, pos, rotation, random, box);
            }
            return true;
        }
        return false;
    }

    protected StructurePlacementData createPlacementData(BlockRotation rotation, BlockBox box, StructureLiquidSettings liquidSettings, boolean keepJigsaws) {
        StructurePlacementData structurePlacementData = new StructurePlacementData();
        structurePlacementData.setBoundingBox(box);
        structurePlacementData.setRotation(rotation);
        structurePlacementData.setUpdateNeighbors(true);
        structurePlacementData.setIgnoreEntities(false);
        structurePlacementData.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
        structurePlacementData.setInitializeMobs(true);
        structurePlacementData.setLiquidSettings(this.overrideLiquidSettings.orElse(liquidSettings));
        if (!keepJigsaws) {
            structurePlacementData.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
        }
        this.processors.value().getList().forEach(structurePlacementData::addProcessor);
        this.getProjection().getProcessors().forEach(structurePlacementData::addProcessor);
        return structurePlacementData;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.SINGLE_POOL_ELEMENT;
    }

    public String toString() {
        return "Single[" + String.valueOf(this.location) + "]";
    }

    @VisibleForTesting
    public Identifier getIdOrThrow() {
        return (Identifier)this.location.orThrow();
    }
}

