/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.JigsawStructure;

public class PoolStructurePiece
extends StructurePiece {
    protected final StructurePoolElement poolElement;
    protected BlockPos pos;
    private final int groundLevelDelta;
    protected final BlockRotation rotation;
    private final List<JigsawJunction> junctions = Lists.newArrayList();
    private final StructureTemplateManager structureTemplateManager;
    private final StructureLiquidSettings liquidSettings;

    public PoolStructurePiece(StructureTemplateManager structureTemplateManager, StructurePoolElement poolElement, BlockPos pos, int groundLevelDelta, BlockRotation rotation, BlockBox boundingBox, StructureLiquidSettings liquidSettings) {
        super(StructurePieceType.JIGSAW, 0, boundingBox);
        this.structureTemplateManager = structureTemplateManager;
        this.poolElement = poolElement;
        this.pos = pos;
        this.groundLevelDelta = groundLevelDelta;
        this.rotation = rotation;
        this.liquidSettings = liquidSettings;
    }

    public PoolStructurePiece(StructureContext context, NbtCompound nbt) {
        super(StructurePieceType.JIGSAW, nbt);
        this.structureTemplateManager = context.structureTemplateManager();
        this.pos = new BlockPos(nbt.getInt("PosX", 0), nbt.getInt("PosY", 0), nbt.getInt("PosZ", 0));
        this.groundLevelDelta = nbt.getInt("ground_level_delta", 0);
        RegistryOps<NbtElement> dynamicOps = context.registryManager().getOps(NbtOps.INSTANCE);
        this.poolElement = nbt.get("pool_element", StructurePoolElement.CODEC, dynamicOps).orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
        this.rotation = nbt.get("rotation", BlockRotation.ENUM_NAME_CODEC).orElseThrow();
        this.boundingBox = this.poolElement.getBoundingBox(this.structureTemplateManager, this.pos, this.rotation);
        NbtList nbtList = nbt.getListOrEmpty("junctions");
        this.junctions.clear();
        nbtList.forEach(junctionTag -> this.junctions.add(JigsawJunction.deserialize(new Dynamic(dynamicOps, junctionTag))));
        this.liquidSettings = nbt.get("liquid_settings", StructureLiquidSettings.codec).orElse(JigsawStructure.DEFAULT_LIQUID_SETTINGS);
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("PosX", this.pos.getX());
        nbt.putInt("PosY", this.pos.getY());
        nbt.putInt("PosZ", this.pos.getZ());
        nbt.putInt("ground_level_delta", this.groundLevelDelta);
        RegistryOps<NbtElement> dynamicOps = context.registryManager().getOps(NbtOps.INSTANCE);
        nbt.put("pool_element", StructurePoolElement.CODEC, dynamicOps, this.poolElement);
        nbt.put("rotation", BlockRotation.ENUM_NAME_CODEC, this.rotation);
        NbtList nbtList = new NbtList();
        for (JigsawJunction jigsawJunction : this.junctions) {
            nbtList.add((NbtElement)jigsawJunction.serialize(dynamicOps).getValue());
        }
        nbt.put("junctions", nbtList);
        if (this.liquidSettings != JigsawStructure.DEFAULT_LIQUID_SETTINGS) {
            nbt.put("liquid_settings", StructureLiquidSettings.codec, dynamicOps, this.liquidSettings);
        }
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        this.generate(world, structureAccessor, chunkGenerator, random, chunkBox, pivot, false);
    }

    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, BlockPos pivot, boolean keepJigsaws) {
        this.poolElement.generate(this.structureTemplateManager, world, structureAccessor, chunkGenerator, this.pos, pivot, this.rotation, boundingBox, random, this.liquidSettings, keepJigsaws);
    }

    @Override
    public void translate(int x, int y, int z) {
        super.translate(x, y, z);
        this.pos = this.pos.add(x, y, z);
    }

    @Override
    public BlockRotation getRotation() {
        return this.rotation;
    }

    public String toString() {
        return String.format(Locale.ROOT, "<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.pos, this.rotation, this.poolElement);
    }

    public StructurePoolElement getPoolElement() {
        return this.poolElement;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(JigsawJunction junction) {
        this.junctions.add(junction);
    }

    public List<JigsawJunction> getJunctions() {
        return this.junctions;
    }
}

