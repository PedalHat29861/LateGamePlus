/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.gen.structure;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.NetherFortressGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class NetherFortressStructure
extends Structure {
    public static final Pool<SpawnSettings.SpawnEntry> MONSTER_SPAWNS = Pool.builder().add(new SpawnSettings.SpawnEntry(EntityType.BLAZE, 2, 3), 10).add(new SpawnSettings.SpawnEntry(EntityType.ZOMBIFIED_PIGLIN, 4, 4), 5).add(new SpawnSettings.SpawnEntry(EntityType.WITHER_SKELETON, 5, 5), 8).add(new SpawnSettings.SpawnEntry(EntityType.SKELETON, 5, 5), 2).add(new SpawnSettings.SpawnEntry(EntityType.MAGMA_CUBE, 4, 4), 3).build();
    public static final MapCodec<NetherFortressStructure> CODEC = NetherFortressStructure.createCodec(NetherFortressStructure::new);

    public NetherFortressStructure(Structure.Config config) {
        super(config);
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getStartX(), 64, chunkPos.getStartZ());
        return Optional.of(new Structure.StructurePosition(blockPos, collector -> NetherFortressStructure.addPieces(collector, context)));
    }

    private static void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        NetherFortressGenerator.Start start = new NetherFortressGenerator.Start(context.random(), context.chunkPos().getOffsetX(2), context.chunkPos().getOffsetZ(2));
        collector.addPiece(start);
        start.fillOpenings(start, collector, context.random());
        List<StructurePiece> list = start.pieces;
        while (!list.isEmpty()) {
            int i = context.random().nextInt(list.size());
            StructurePiece structurePiece = list.remove(i);
            structurePiece.fillOpenings(start, collector, context.random());
        }
        collector.shiftInto(context.random(), 48, 70);
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.FORTRESS;
    }
}

