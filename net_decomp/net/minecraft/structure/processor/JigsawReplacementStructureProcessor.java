/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.structure.processor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class JigsawReplacementStructureProcessor
extends StructureProcessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<JigsawReplacementStructureProcessor> CODEC = MapCodec.unit(() -> INSTANCE);
    public static final JigsawReplacementStructureProcessor INSTANCE = new JigsawReplacementStructureProcessor();

    private JigsawReplacementStructureProcessor() {
    }

    @Override
    public  @Nullable StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        BlockState blockState2;
        BlockState blockState = currentBlockInfo.state();
        if (!blockState.isOf(Blocks.JIGSAW) || SharedConstants.KEEP_JIGSAW_BLOCKS_DURING_STRUCTURE_GEN) {
            return currentBlockInfo;
        }
        if (currentBlockInfo.nbt() == null) {
            LOGGER.warn("Jigsaw block at {} is missing nbt, will not replace", (Object)pos);
            return currentBlockInfo;
        }
        String string = currentBlockInfo.nbt().getString("final_state", "minecraft:air");
        try {
            BlockArgumentParser.BlockResult blockResult = BlockArgumentParser.block(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), string, true);
            blockState2 = blockResult.blockState();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            LOGGER.error("Failed to parse jigsaw replacement state '{}' at {}: {}", new Object[]{string, pos, commandSyntaxException.getMessage()});
            return null;
        }
        if (blockState2.isOf(Blocks.STRUCTURE_VOID)) {
            return null;
        }
        return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), blockState2, null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }
}

