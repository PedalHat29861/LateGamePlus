/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.structure.processor;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import org.jspecify.annotations.Nullable;

public class GravityStructureProcessor
extends StructureProcessor {
    public static final MapCodec<GravityStructureProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Heightmap.Type.CODEC.fieldOf("heightmap").orElse((Object)Heightmap.Type.WORLD_SURFACE_WG).forGetter(processor -> processor.heightmap), (App)Codec.INT.fieldOf("offset").orElse((Object)0).forGetter(processor -> processor.offset)).apply((Applicative)instance, GravityStructureProcessor::new));
    private final Heightmap.Type heightmap;
    private final int offset;

    public GravityStructureProcessor(Heightmap.Type heightmap, int offset) {
        this.heightmap = heightmap;
        this.offset = offset;
    }

    @Override
    public  @Nullable StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        Heightmap.Type type = world instanceof ServerWorld ? (this.heightmap == Heightmap.Type.WORLD_SURFACE_WG ? Heightmap.Type.WORLD_SURFACE : (this.heightmap == Heightmap.Type.OCEAN_FLOOR_WG ? Heightmap.Type.OCEAN_FLOOR : this.heightmap)) : this.heightmap;
        BlockPos blockPos = currentBlockInfo.pos();
        int i = world.getTopY(type, blockPos.getX(), blockPos.getZ()) + this.offset;
        int j = originalBlockInfo.pos().getY();
        return new StructureTemplate.StructureBlockInfo(new BlockPos(blockPos.getX(), i + j, blockPos.getZ()), currentBlockInfo.state(), currentBlockInfo.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.GRAVITY;
    }
}

