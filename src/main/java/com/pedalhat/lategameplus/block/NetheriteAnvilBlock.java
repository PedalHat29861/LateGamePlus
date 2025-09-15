package com.pedalhat.lategameplus.block;

import com.pedalhat.lategameplus.screen.NetheriteAnvilScreenHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetheriteAnvilBlock extends AnvilBlock {
    public NetheriteAnvilBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) ->
            new NetheriteAnvilScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)),
            Text.translatable("container.repair")
        );
    }
}
