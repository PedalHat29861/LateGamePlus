/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 *  org.slf4j.Logger
 */
package net.minecraft.world;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.slf4j.Logger;

public class Heightmap {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Predicate<BlockState> NOT_AIR = state -> !state.isAir();
    static final Predicate<BlockState> SUFFOCATES = AbstractBlock.AbstractBlockState::blocksMovement;
    private final PaletteStorage storage;
    private final Predicate<BlockState> blockPredicate;
    private final Chunk chunk;

    public Heightmap(Chunk chunk, Type type) {
        this.blockPredicate = type.getBlockPredicate();
        this.chunk = chunk;
        int i = MathHelper.ceilLog2(chunk.getHeight() + 1);
        this.storage = new PackedIntegerArray(i, 256);
    }

    public static void populateHeightmaps(Chunk chunk, Set<Type> types) {
        if (types.isEmpty()) {
            return;
        }
        int i = types.size();
        ObjectArrayList objectList = new ObjectArrayList(i);
        ObjectListIterator objectListIterator = objectList.iterator();
        int j = chunk.getHighestNonEmptySectionYOffset() + 16;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int k = 0; k < 16; ++k) {
            block1: for (int l = 0; l < 16; ++l) {
                for (Type type : types) {
                    objectList.add((Object)chunk.getHeightmap(type));
                }
                for (int m = j - 1; m >= chunk.getBottomY(); --m) {
                    mutable.set(k, m, l);
                    BlockState blockState = chunk.getBlockState(mutable);
                    if (blockState.isOf(Blocks.AIR)) continue;
                    while (objectListIterator.hasNext()) {
                        Heightmap heightmap = (Heightmap)objectListIterator.next();
                        if (!heightmap.blockPredicate.test(blockState)) continue;
                        heightmap.set(k, l, m + 1);
                        objectListIterator.remove();
                    }
                    if (objectList.isEmpty()) continue block1;
                    objectListIterator.back(i);
                }
            }
        }
    }

    public boolean trackUpdate(int x, int y, int z, BlockState state) {
        int i = this.get(x, z);
        if (y <= i - 2) {
            return false;
        }
        if (this.blockPredicate.test(state)) {
            if (y >= i) {
                this.set(x, z, y + 1);
                return true;
            }
        } else if (i - 1 == y) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for (int j = y - 1; j >= this.chunk.getBottomY(); --j) {
                mutable.set(x, j, z);
                if (!this.blockPredicate.test(this.chunk.getBlockState(mutable))) continue;
                this.set(x, z, j + 1);
                return true;
            }
            this.set(x, z, this.chunk.getBottomY());
            return true;
        }
        return false;
    }

    public int get(int x, int z) {
        return this.get(Heightmap.toIndex(x, z));
    }

    public int getOneLower(int x, int z) {
        return this.get(Heightmap.toIndex(x, z)) - 1;
    }

    private int get(int index) {
        return this.storage.get(index) + this.chunk.getBottomY();
    }

    private void set(int x, int z, int height) {
        this.storage.set(Heightmap.toIndex(x, z), height - this.chunk.getBottomY());
    }

    public void setTo(Chunk chunk, Type type, long[] values) {
        long[] ls = this.storage.getData();
        if (ls.length == values.length) {
            System.arraycopy(values, 0, ls, 0, values.length);
            return;
        }
        LOGGER.warn("Ignoring heightmap data for chunk {}, size does not match; expected: {}, got: {}", new Object[]{chunk.getPos(), ls.length, values.length});
        Heightmap.populateHeightmaps(chunk, EnumSet.of(type));
    }

    public long[] asLongArray() {
        return this.storage.getData();
    }

    private static int toIndex(int x, int z) {
        return x + z * 16;
    }

    public static final class Type
    extends Enum<Type>
    implements StringIdentifiable {
        public static final /* enum */ Type WORLD_SURFACE_WG = new Type(0, "WORLD_SURFACE_WG", Purpose.WORLDGEN, NOT_AIR);
        public static final /* enum */ Type WORLD_SURFACE = new Type(1, "WORLD_SURFACE", Purpose.CLIENT, NOT_AIR);
        public static final /* enum */ Type OCEAN_FLOOR_WG = new Type(2, "OCEAN_FLOOR_WG", Purpose.WORLDGEN, SUFFOCATES);
        public static final /* enum */ Type OCEAN_FLOOR = new Type(3, "OCEAN_FLOOR", Purpose.LIVE_WORLD, SUFFOCATES);
        public static final /* enum */ Type MOTION_BLOCKING = new Type(4, "MOTION_BLOCKING", Purpose.CLIENT, state -> state.blocksMovement() || !state.getFluidState().isEmpty());
        public static final /* enum */ Type MOTION_BLOCKING_NO_LEAVES = new Type(5, "MOTION_BLOCKING_NO_LEAVES", Purpose.CLIENT, state -> (state.blocksMovement() || !state.getFluidState().isEmpty()) && !(state.getBlock() instanceof LeavesBlock));
        public static final Codec<Type> CODEC;
        private static final IntFunction<Type> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, Type> PACKET_CODEC;
        private final int index;
        private final String id;
        private final Purpose purpose;
        private final Predicate<BlockState> blockPredicate;
        private static final /* synthetic */ Type[] field_13199;

        public static Type[] values() {
            return (Type[])field_13199.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private Type(int index, String id, Purpose purpose, Predicate<BlockState> blockPredicate) {
            this.index = index;
            this.id = id;
            this.purpose = purpose;
            this.blockPredicate = blockPredicate;
        }

        public String getId() {
            return this.id;
        }

        public boolean shouldSendToClient() {
            return this.purpose == Purpose.CLIENT;
        }

        public boolean isStoredServerSide() {
            return this.purpose != Purpose.WORLDGEN;
        }

        public Predicate<BlockState> getBlockPredicate() {
            return this.blockPredicate;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ Type[] method_36752() {
            return new Type[]{WORLD_SURFACE_WG, WORLD_SURFACE, OCEAN_FLOOR_WG, OCEAN_FLOOR, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES};
        }

        static {
            field_13199 = Type.method_36752();
            CODEC = StringIdentifiable.createCodec(Type::values);
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(type -> type.index, Type.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, type -> type.index);
        }
    }

    public static final class Purpose
    extends Enum<Purpose> {
        public static final /* enum */ Purpose WORLDGEN = new Purpose();
        public static final /* enum */ Purpose LIVE_WORLD = new Purpose();
        public static final /* enum */ Purpose CLIENT = new Purpose();
        private static final /* synthetic */ Purpose[] field_13208;

        public static Purpose[] values() {
            return (Purpose[])field_13208.clone();
        }

        public static Purpose valueOf(String string) {
            return Enum.valueOf(Purpose.class, string);
        }

        private static /* synthetic */ Purpose[] method_36753() {
            return new Purpose[]{WORLDGEN, LIVE_WORLD, CLIENT};
        }

        static {
            field_13208 = Purpose.method_36753();
        }
    }
}

