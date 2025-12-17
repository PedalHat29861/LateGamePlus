/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.structure;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.MineshaftGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class MineshaftStructure
extends Structure {
    public static final MapCodec<MineshaftStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(MineshaftStructure.configCodecBuilder(instance), (App)Type.CODEC.fieldOf("mineshaft_type").forGetter(mineshaftStructure -> mineshaftStructure.type)).apply((Applicative)instance, MineshaftStructure::new));
    private final Type type;

    public MineshaftStructure(Structure.Config config, Type type) {
        super(config);
        this.type = type;
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        context.random().nextDouble();
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getCenterX(), 50, chunkPos.getStartZ());
        StructurePiecesCollector structurePiecesCollector = new StructurePiecesCollector();
        int i = this.addPieces(structurePiecesCollector, context);
        return Optional.of(new Structure.StructurePosition(blockPos.add(0, i, 0), (Either<Consumer<StructurePiecesCollector>, StructurePiecesCollector>)Either.right((Object)structurePiecesCollector)));
    }

    private int addPieces(StructurePiecesCollector collector, Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        ChunkRandom chunkRandom = context.random();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        MineshaftGenerator.MineshaftRoom mineshaftRoom = new MineshaftGenerator.MineshaftRoom(0, chunkRandom, chunkPos.getOffsetX(2), chunkPos.getOffsetZ(2), this.type);
        collector.addPiece(mineshaftRoom);
        mineshaftRoom.fillOpenings(mineshaftRoom, collector, chunkRandom);
        int i = chunkGenerator.getSeaLevel();
        if (this.type == Type.MESA) {
            BlockPos blockPos = collector.getBoundingBox().getCenter();
            int j = chunkGenerator.getHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig());
            int k = j <= i ? i : MathHelper.nextBetween((Random)chunkRandom, i, j);
            int l = k - blockPos.getY();
            collector.shift(l);
            return l;
        }
        return collector.shiftInto(i, chunkGenerator.getMinimumY(), chunkRandom, 10);
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.MINESHAFT;
    }

    public static final class Type
    extends Enum<Type>
    implements StringIdentifiable {
        public static final /* enum */ Type NORMAL = new Type("normal", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE);
        public static final /* enum */ Type MESA = new Type("mesa", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_FENCE);
        public static final Codec<Type> CODEC;
        private static final IntFunction<Type> BY_ID;
        private final String name;
        private final BlockState log;
        private final BlockState planks;
        private final BlockState fence;
        private static final /* synthetic */ Type[] field_13688;

        public static Type[] values() {
            return (Type[])field_13688.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private Type(String name, Block log, Block planks, Block fence) {
            this.name = name;
            this.log = log.getDefaultState();
            this.planks = planks.getDefaultState();
            this.fence = fence.getDefaultState();
        }

        public String getName() {
            return this.name;
        }

        public static Type byId(int id) {
            return BY_ID.apply(id);
        }

        public BlockState getLog() {
            return this.log;
        }

        public BlockState getPlanks() {
            return this.planks;
        }

        public BlockState getFence() {
            return this.fence;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ Type[] method_36755() {
            return new Type[]{NORMAL, MESA};
        }

        static {
            field_13688 = Type.method_36755();
            CODEC = StringIdentifiable.createCodec(Type::values);
            BY_ID = ValueLists.createIndexToValueFunction(Enum::ordinal, Type.values(), ValueLists.OutOfBoundsHandling.ZERO);
        }
    }
}

