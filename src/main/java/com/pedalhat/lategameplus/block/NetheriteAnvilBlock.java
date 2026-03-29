package com.pedalhat.lategameplus.block;

import com.pedalhat.lategameplus.screen.NetheriteAnvilScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetheriteAnvilBlock extends AnvilBlock {
    public NetheriteAnvilBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimpleMenuProvider((syncId, inventory, player) ->
            new NetheriteAnvilScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos)),
            Component.translatable("container.repair")
        );
    }
}
