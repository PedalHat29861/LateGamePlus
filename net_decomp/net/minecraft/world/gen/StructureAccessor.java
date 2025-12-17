/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.gen;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureHolder;
import net.minecraft.world.StructureLocator;
import net.minecraft.world.StructurePresence;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;
import org.jspecify.annotations.Nullable;

public class StructureAccessor {
    private final WorldAccess world;
    private final GeneratorOptions options;
    private final StructureLocator locator;

    public StructureAccessor(WorldAccess world, GeneratorOptions options, StructureLocator locator) {
        this.world = world;
        this.options = options;
        this.locator = locator;
    }

    public StructureAccessor forRegion(ChunkRegion region) {
        if (region.toServerWorld() != this.world) {
            throw new IllegalStateException("Using invalid structure manager (source level: " + String.valueOf(region.toServerWorld()) + ", region: " + String.valueOf(region));
        }
        return new StructureAccessor(region, this.options, this.locator);
    }

    public List<StructureStart> getStructureStarts(ChunkPos pos, Predicate<Structure> predicate) {
        Map<Structure, LongSet> map = this.world.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (Map.Entry<Structure, LongSet> entry : map.entrySet()) {
            Structure structure = entry.getKey();
            if (!predicate.test(structure)) continue;
            this.acceptStructureStarts(structure, entry.getValue(), arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
        }
        return builder.build();
    }

    public List<StructureStart> getStructureStarts(ChunkSectionPos sectionPos, Structure structure) {
        LongSet longSet = this.world.getChunk(sectionPos.getSectionX(), sectionPos.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences(structure);
        ImmutableList.Builder builder = ImmutableList.builder();
        this.acceptStructureStarts(structure, longSet, arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
        return builder.build();
    }

    public void acceptStructureStarts(Structure structure, LongSet structureStartPositions, Consumer<StructureStart> consumer) {
        LongIterator longIterator = structureStartPositions.iterator();
        while (longIterator.hasNext()) {
            long l = (Long)longIterator.next();
            ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(new ChunkPos(l), this.world.getBottomSectionCoord());
            StructureStart structureStart = this.getStructureStart(chunkSectionPos, structure, this.world.getChunk(chunkSectionPos.getSectionX(), chunkSectionPos.getSectionZ(), ChunkStatus.STRUCTURE_STARTS));
            if (structureStart == null || !structureStart.hasChildren()) continue;
            consumer.accept(structureStart);
        }
    }

    public @Nullable StructureStart getStructureStart(ChunkSectionPos pos, Structure structure, StructureHolder holder) {
        return holder.getStructureStart(structure);
    }

    public void setStructureStart(ChunkSectionPos pos, Structure structure, StructureStart structureStart, StructureHolder holder) {
        holder.setStructureStart(structure, structureStart);
    }

    public void addStructureReference(ChunkSectionPos pos, Structure structure, long reference, StructureHolder holder) {
        holder.addStructureReference(structure, reference);
    }

    public boolean shouldGenerateStructures() {
        return this.options.shouldGenerateStructures();
    }

    public StructureStart getStructureAt(BlockPos pos, Structure structure) {
        for (StructureStart structureStart : this.getStructureStarts(ChunkSectionPos.from(pos), structure)) {
            if (!structureStart.getBoundingBox().contains(pos)) continue;
            return structureStart;
        }
        return StructureStart.DEFAULT;
    }

    public StructureStart getStructureContaining(BlockPos pos, TagKey<Structure> tag) {
        return this.getStructureContaining(pos, (RegistryEntry<Structure> structure) -> structure.isIn(tag));
    }

    public StructureStart getStructureContaining(BlockPos pos, RegistryEntryList<Structure> structures) {
        return this.getStructureContaining(pos, structures::contains);
    }

    public StructureStart getStructureContaining(BlockPos pos, Predicate<RegistryEntry<Structure>> predicate) {
        RegistryWrapper.Impl registry = this.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        for (StructureStart structureStart : this.getStructureStarts(new ChunkPos(pos), arg_0 -> StructureAccessor.method_41414((Registry)registry, predicate, arg_0))) {
            if (!this.structureContains(pos, structureStart)) continue;
            return structureStart;
        }
        return StructureStart.DEFAULT;
    }

    public StructureStart getStructureContaining(BlockPos pos, Structure structure) {
        for (StructureStart structureStart : this.getStructureStarts(ChunkSectionPos.from(pos), structure)) {
            if (!this.structureContains(pos, structureStart)) continue;
            return structureStart;
        }
        return StructureStart.DEFAULT;
    }

    public boolean structureContains(BlockPos pos, StructureStart structureStart) {
        for (StructurePiece structurePiece : structureStart.getChildren()) {
            if (!structurePiece.getBoundingBox().contains(pos)) continue;
            return true;
        }
        return false;
    }

    public boolean hasStructureReferences(BlockPos pos) {
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(pos);
        return this.world.getChunk(chunkSectionPos.getSectionX(), chunkSectionPos.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).hasStructureReferences();
    }

    public Map<Structure, LongSet> getStructureReferences(BlockPos pos) {
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(pos);
        return this.world.getChunk(chunkSectionPos.getSectionX(), chunkSectionPos.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences();
    }

    public StructurePresence getStructurePresence(ChunkPos chunkPos, Structure structure, StructurePlacement placement, boolean skipReferencedStructures) {
        return this.locator.getStructurePresence(chunkPos, structure, placement, skipReferencedStructures);
    }

    public void incrementReferences(StructureStart structureStart) {
        structureStart.incrementReferences();
        this.locator.incrementReferences(structureStart.getPos(), structureStart.getStructure());
    }

    public DynamicRegistryManager getRegistryManager() {
        return this.world.getRegistryManager();
    }

    private static /* synthetic */ boolean method_41414(Registry registry, Predicate predicate, Structure structure) {
        return registry.getEntry(registry.getRawId(structure)).map(predicate::test).orElse(false);
    }
}

