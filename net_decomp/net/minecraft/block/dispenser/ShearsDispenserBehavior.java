/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.dispenser;

import java.util.List;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Shearable;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.event.GameEvent;

public class ShearsDispenserBehavior
extends FallibleItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        ServerWorld serverWorld = pointer.world();
        if (!serverWorld.isClient()) {
            BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
            this.setSuccess(ShearsDispenserBehavior.tryShearBlock(serverWorld, stack, blockPos) || ShearsDispenserBehavior.tryShearEntity(serverWorld, blockPos, stack));
            if (this.isSuccess()) {
                stack.damage(1, serverWorld, null, item -> {});
            }
        }
        return stack;
    }

    private static boolean tryShearBlock(ServerWorld world, ItemStack tool, BlockPos pos) {
        int i;
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isIn(BlockTags.BEEHIVES, state -> state.contains(BeehiveBlock.HONEY_LEVEL) && state.getBlock() instanceof BeehiveBlock) && (i = blockState.get(BeehiveBlock.HONEY_LEVEL).intValue()) >= 5) {
            world.playSound(null, pos, SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0f, 1.0f);
            BeehiveBlock.dropHoneycomb(world, tool, blockState, world.getBlockEntity(pos), null, pos);
            ((BeehiveBlock)blockState.getBlock()).takeHoney(world, blockState, pos, null, BeehiveBlockEntity.BeeState.BEE_RELEASED);
            world.emitGameEvent(null, GameEvent.SHEAR, pos);
            return true;
        }
        return false;
    }

    private static boolean tryShearEntity(ServerWorld world, BlockPos pos, ItemStack shears) {
        List<Entity> list = world.getEntitiesByClass(Entity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR);
        for (Entity entity : list) {
            Shearable shearable;
            if (entity.snipAllHeldLeashes(null)) {
                return true;
            }
            if (!(entity instanceof Shearable) || !(shearable = (Shearable)((Object)entity)).isShearable()) continue;
            shearable.sheared(world, SoundCategory.BLOCKS, shears);
            world.emitGameEvent(null, GameEvent.SHEAR, pos);
            return true;
        }
        return false;
    }
}

