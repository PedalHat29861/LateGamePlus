package com.pedalhat.lategameplus.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

/**
 * Shulker box variant that mirrors vanilla behavior (animations, sounds, etc.)
 * but uses a larger inventory via a custom block entity.
 */
public class NetheriteShulkerBoxBlock extends ShulkerBoxBlock {
    public static final MapCodec<NetheriteShulkerBoxBlock> CODEC = createCodec(settings -> new NetheriteShulkerBoxBlock(settings));

    public NetheriteShulkerBoxBlock(Settings settings) {
        // null color = undyed vanilla shulker styling/behavior
        super((DyeColor) null, settings);
    }

    @Override
    public MapCodec<ShulkerBoxBlock> getCodec() {
        @SuppressWarnings("unchecked")
        MapCodec<ShulkerBoxBlock> codec = (MapCodec<ShulkerBoxBlock>)(MapCodec<?>) CODEC;
        return codec;
    }

<<<<<<< ours
<<<<<<< ours
<<<<<<< ours
<<<<<<< ours
=======
=======
>>>>>>> theirs
=======
>>>>>>> theirs
=======
>>>>>>> theirs
    @Nullable
>>>>>>> theirs
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new com.pedalhat.lategameplus.block.entity.NetheriteShulkerBoxBlockEntity(pos, state);
    }
}
