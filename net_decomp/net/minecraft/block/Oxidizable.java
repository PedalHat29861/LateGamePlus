/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.ImmutableBiMap
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Degradable;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public interface Oxidizable
extends Degradable<OxidationLevel> {
    public static final Supplier<BiMap<Block, Block>> OXIDATION_LEVEL_INCREASES = Suppliers.memoize(() -> ImmutableBiMap.builder().put((Object)Blocks.COPPER_BLOCK, (Object)Blocks.EXPOSED_COPPER).put((Object)Blocks.EXPOSED_COPPER, (Object)Blocks.WEATHERED_COPPER).put((Object)Blocks.WEATHERED_COPPER, (Object)Blocks.OXIDIZED_COPPER).put((Object)Blocks.CUT_COPPER, (Object)Blocks.EXPOSED_CUT_COPPER).put((Object)Blocks.EXPOSED_CUT_COPPER, (Object)Blocks.WEATHERED_CUT_COPPER).put((Object)Blocks.WEATHERED_CUT_COPPER, (Object)Blocks.OXIDIZED_CUT_COPPER).put((Object)Blocks.CHISELED_COPPER, (Object)Blocks.EXPOSED_CHISELED_COPPER).put((Object)Blocks.EXPOSED_CHISELED_COPPER, (Object)Blocks.WEATHERED_CHISELED_COPPER).put((Object)Blocks.WEATHERED_CHISELED_COPPER, (Object)Blocks.OXIDIZED_CHISELED_COPPER).put((Object)Blocks.CUT_COPPER_SLAB, (Object)Blocks.EXPOSED_CUT_COPPER_SLAB).put((Object)Blocks.EXPOSED_CUT_COPPER_SLAB, (Object)Blocks.WEATHERED_CUT_COPPER_SLAB).put((Object)Blocks.WEATHERED_CUT_COPPER_SLAB, (Object)Blocks.OXIDIZED_CUT_COPPER_SLAB).put((Object)Blocks.CUT_COPPER_STAIRS, (Object)Blocks.EXPOSED_CUT_COPPER_STAIRS).put((Object)Blocks.EXPOSED_CUT_COPPER_STAIRS, (Object)Blocks.WEATHERED_CUT_COPPER_STAIRS).put((Object)Blocks.WEATHERED_CUT_COPPER_STAIRS, (Object)Blocks.OXIDIZED_CUT_COPPER_STAIRS).put((Object)Blocks.COPPER_DOOR, (Object)Blocks.EXPOSED_COPPER_DOOR).put((Object)Blocks.EXPOSED_COPPER_DOOR, (Object)Blocks.WEATHERED_COPPER_DOOR).put((Object)Blocks.WEATHERED_COPPER_DOOR, (Object)Blocks.OXIDIZED_COPPER_DOOR).put((Object)Blocks.COPPER_TRAPDOOR, (Object)Blocks.EXPOSED_COPPER_TRAPDOOR).put((Object)Blocks.EXPOSED_COPPER_TRAPDOOR, (Object)Blocks.WEATHERED_COPPER_TRAPDOOR).put((Object)Blocks.WEATHERED_COPPER_TRAPDOOR, (Object)Blocks.OXIDIZED_COPPER_TRAPDOOR).putAll(Blocks.COPPER_BARS.getOxidizingMap()).put((Object)Blocks.COPPER_GRATE, (Object)Blocks.EXPOSED_COPPER_GRATE).put((Object)Blocks.EXPOSED_COPPER_GRATE, (Object)Blocks.WEATHERED_COPPER_GRATE).put((Object)Blocks.WEATHERED_COPPER_GRATE, (Object)Blocks.OXIDIZED_COPPER_GRATE).put((Object)Blocks.COPPER_BULB, (Object)Blocks.EXPOSED_COPPER_BULB).put((Object)Blocks.EXPOSED_COPPER_BULB, (Object)Blocks.WEATHERED_COPPER_BULB).put((Object)Blocks.WEATHERED_COPPER_BULB, (Object)Blocks.OXIDIZED_COPPER_BULB).putAll(Blocks.COPPER_LANTERNS.getOxidizingMap()).put((Object)Blocks.COPPER_CHEST, (Object)Blocks.EXPOSED_COPPER_CHEST).put((Object)Blocks.EXPOSED_COPPER_CHEST, (Object)Blocks.WEATHERED_COPPER_CHEST).put((Object)Blocks.WEATHERED_COPPER_CHEST, (Object)Blocks.OXIDIZED_COPPER_CHEST).put((Object)Blocks.COPPER_GOLEM_STATUE, (Object)Blocks.EXPOSED_COPPER_GOLEM_STATUE).put((Object)Blocks.EXPOSED_COPPER_GOLEM_STATUE, (Object)Blocks.WEATHERED_COPPER_GOLEM_STATUE).put((Object)Blocks.WEATHERED_COPPER_GOLEM_STATUE, (Object)Blocks.OXIDIZED_COPPER_GOLEM_STATUE).put((Object)Blocks.LIGHTNING_ROD, (Object)Blocks.EXPOSED_LIGHTNING_ROD).put((Object)Blocks.EXPOSED_LIGHTNING_ROD, (Object)Blocks.WEATHERED_LIGHTNING_ROD).put((Object)Blocks.WEATHERED_LIGHTNING_ROD, (Object)Blocks.OXIDIZED_LIGHTNING_ROD).putAll(Blocks.COPPER_CHAINS.getOxidizingMap()).build());
    public static final Supplier<BiMap<Block, Block>> OXIDATION_LEVEL_DECREASES = Suppliers.memoize(() -> OXIDATION_LEVEL_INCREASES.get().inverse());

    public static Optional<Block> getDecreasedOxidationBlock(Block block) {
        return Optional.ofNullable((Block)OXIDATION_LEVEL_DECREASES.get().get((Object)block));
    }

    public static Block getUnaffectedOxidationBlock(Block block) {
        Block block2 = block;
        Block block3 = (Block)OXIDATION_LEVEL_DECREASES.get().get((Object)block2);
        while (block3 != null) {
            block2 = block3;
            block3 = (Block)OXIDATION_LEVEL_DECREASES.get().get((Object)block2);
        }
        return block2;
    }

    public static Optional<BlockState> getDecreasedOxidationState(BlockState state) {
        return Oxidizable.getDecreasedOxidationBlock(state.getBlock()).map(block -> block.getStateWithProperties(state));
    }

    public static Optional<Block> getIncreasedOxidationBlock(Block block) {
        return Optional.ofNullable((Block)OXIDATION_LEVEL_INCREASES.get().get((Object)block));
    }

    public static BlockState getUnaffectedOxidationState(BlockState state) {
        return Oxidizable.getUnaffectedOxidationBlock(state.getBlock()).getStateWithProperties(state);
    }

    @Override
    default public Optional<BlockState> getDegradationResult(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(state.getBlock()).map(block -> block.getStateWithProperties(state));
    }

    @Override
    default public float getDegradationChanceMultiplier() {
        if (this.getDegradationLevel() == OxidationLevel.UNAFFECTED) {
            return 0.75f;
        }
        return 1.0f;
    }

    public static final class OxidationLevel
    extends Enum<OxidationLevel>
    implements StringIdentifiable {
        public static final /* enum */ OxidationLevel UNAFFECTED = new OxidationLevel("unaffected");
        public static final /* enum */ OxidationLevel EXPOSED = new OxidationLevel("exposed");
        public static final /* enum */ OxidationLevel WEATHERED = new OxidationLevel("weathered");
        public static final /* enum */ OxidationLevel OXIDIZED = new OxidationLevel("oxidized");
        public static final IntFunction<OxidationLevel> indexMapper;
        public static final Codec<OxidationLevel> CODEC;
        public static final PacketCodec<ByteBuf, OxidationLevel> PACKET_CODEC;
        private final String id;
        private static final /* synthetic */ OxidationLevel[] field_28708;

        public static OxidationLevel[] values() {
            return (OxidationLevel[])field_28708.clone();
        }

        public static OxidationLevel valueOf(String string) {
            return Enum.valueOf(OxidationLevel.class, string);
        }

        private OxidationLevel(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public OxidationLevel getIncreased() {
            return indexMapper.apply(this.ordinal() + 1);
        }

        public OxidationLevel getDecreased() {
            return indexMapper.apply(this.ordinal() - 1);
        }

        private static /* synthetic */ OxidationLevel[] method_36712() {
            return new OxidationLevel[]{UNAFFECTED, EXPOSED, WEATHERED, OXIDIZED};
        }

        static {
            field_28708 = OxidationLevel.method_36712();
            indexMapper = ValueLists.createIndexToValueFunction(Enum::ordinal, OxidationLevel.values(), ValueLists.OutOfBoundsHandling.CLAMP);
            CODEC = StringIdentifiable.createCodec(OxidationLevel::values);
            PACKET_CODEC = PacketCodecs.indexed(indexMapper, Enum::ordinal);
        }
    }
}

