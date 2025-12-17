/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.structure;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class NetherFossilGenerator {
    private static final Identifier[] FOSSILS = new Identifier[]{Identifier.ofVanilla("nether_fossils/fossil_1"), Identifier.ofVanilla("nether_fossils/fossil_2"), Identifier.ofVanilla("nether_fossils/fossil_3"), Identifier.ofVanilla("nether_fossils/fossil_4"), Identifier.ofVanilla("nether_fossils/fossil_5"), Identifier.ofVanilla("nether_fossils/fossil_6"), Identifier.ofVanilla("nether_fossils/fossil_7"), Identifier.ofVanilla("nether_fossils/fossil_8"), Identifier.ofVanilla("nether_fossils/fossil_9"), Identifier.ofVanilla("nether_fossils/fossil_10"), Identifier.ofVanilla("nether_fossils/fossil_11"), Identifier.ofVanilla("nether_fossils/fossil_12"), Identifier.ofVanilla("nether_fossils/fossil_13"), Identifier.ofVanilla("nether_fossils/fossil_14")};

    public static void addPieces(StructureTemplateManager manager, StructurePiecesHolder holder, Random random, BlockPos pos) {
        BlockRotation blockRotation = BlockRotation.random(random);
        holder.addPiece(new Piece(manager, Util.getRandom(FOSSILS, random), pos, blockRotation));
    }

    public static class Piece
    extends SimpleStructurePiece {
        public Piece(StructureTemplateManager manager, Identifier template, BlockPos pos, BlockRotation rotation) {
            super(StructurePieceType.NETHER_FOSSIL, 0, manager, template, template.toString(), Piece.createPlacementData(rotation), pos);
        }

        public Piece(StructureTemplateManager manager, NbtCompound nbt) {
            super(StructurePieceType.NETHER_FOSSIL, nbt, manager, (Identifier id) -> Piece.createPlacementData(nbt.get("Rot", BlockRotation.ENUM_NAME_CODEC).orElseThrow()));
        }

        private static StructurePlacementData createPlacementData(BlockRotation rotation) {
            return new StructurePlacementData().setRotation(rotation).setMirror(BlockMirror.NONE).addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.put("Rot", BlockRotation.ENUM_NAME_CODEC, this.placementData.getRotation());
        }

        @Override
        protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            BlockBox blockBox = this.template.calculateBoundingBox(this.placementData, this.pos);
            chunkBox.encompass(blockBox);
            super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
            this.generateDriedGhast(world, random, blockBox, chunkBox);
        }

        private void generateDriedGhast(StructureWorldAccess world, Random random, BlockBox box, BlockBox chunkBox) {
            int k;
            int j;
            int i;
            BlockPos blockPos;
            Random random2 = Random.create(world.getSeed()).nextSplitter().split(box.getCenter());
            if (random2.nextFloat() < 0.5f && world.getBlockState(blockPos = new BlockPos(i = box.getMinX() + random2.nextInt(box.getBlockCountX()), j = box.getMinY(), k = box.getMinZ() + random2.nextInt(box.getBlockCountZ()))).isAir() && chunkBox.contains(blockPos)) {
                world.setBlockState(blockPos, Blocks.DRIED_GHAST.getDefaultState().rotate(BlockRotation.random(random2)), 2);
            }
        }
    }
}

