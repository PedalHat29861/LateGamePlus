/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.sound;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class AmbientDesertBlockSounds {
    private static final int SAND_SOUND_CHANCE = 2100;
    private static final int DRY_GRASS_SOUND_CHANCE = 200;
    private static final int DEAD_BUSH_SOUND_CHANCE = 130;
    private static final int DEAD_BUSH_BADLANDS_PENALTY_CHANCE = 3;
    private static final int REQUIRED_SAND_CHECK_DIRECTIONS = 3;
    private static final int SAND_CHECK_HORIZONTAL_DISTANCE = 8;
    private static final int SAND_CHECK_VERTICAL_DISTANCE = 5;
    private static final int HORIZONTAL_DIRECTIONS = 4;

    public static void tryPlaySandSounds(World world, BlockPos pos, Random random) {
        if (!world.getBlockState(pos.up()).isOf(Blocks.AIR)) {
            return;
        }
        if (random.nextInt(2100) == 0 && AmbientDesertBlockSounds.canPlaySandSoundsAt(world, pos)) {
            world.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_SAND_IDLE, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
        }
    }

    public static void tryPlayDryGrassSounds(World world, BlockPos pos, Random random) {
        if (random.nextInt(200) == 0 && AmbientDesertBlockSounds.triggersDryVegetationSounds(world, pos.down())) {
            world.playSoundClient(SoundEvents.BLOCK_DRY_GRASS_AMBIENT, SoundCategory.AMBIENT, 1.0f, 1.0f);
        }
    }

    public static void tryPlayDeadBushSounds(World world, BlockPos pos, Random random) {
        if (random.nextInt(130) == 0) {
            BlockState blockState = world.getBlockState(pos.down());
            if ((blockState.isOf(Blocks.RED_SAND) || blockState.isIn(BlockTags.TERRACOTTA)) && random.nextInt(3) != 0) {
                return;
            }
            if (AmbientDesertBlockSounds.triggersDryVegetationSounds(world, pos.down())) {
                world.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_DEADBUSH_IDLE, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
            }
        }
    }

    public static boolean triggersDryVegetationSounds(World world, BlockPos pos) {
        return world.getBlockState(pos).isIn(BlockTags.TRIGGERS_AMBIENT_DESERT_DRY_VEGETATION_BLOCK_SOUNDS) && world.getBlockState(pos.down()).isIn(BlockTags.TRIGGERS_AMBIENT_DESERT_DRY_VEGETATION_BLOCK_SOUNDS);
    }

    private static boolean canPlaySandSoundsAt(World world, BlockPos pos) {
        int i = 0;
        int j = 0;
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            int k;
            int l;
            boolean bl;
            mutable.set(pos).move(direction, 8);
            if (AmbientDesertBlockSounds.checkForSandSoundTriggers(world, mutable) && i++ >= 3) {
                return true;
            }
            if (bl = (l = (k = 4 - ++j) + i) >= 3) continue;
            return false;
        }
        return false;
    }

    private static boolean checkForSandSoundTriggers(World world, BlockPos.Mutable pos) {
        int i = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos) - 1;
        if (Math.abs(i - pos.getY()) <= 5) {
            boolean bl = world.getBlockState(pos.setY(i + 1)).isAir();
            return bl && AmbientDesertBlockSounds.triggersSandSounds(world.getBlockState(pos.setY(i)));
        }
        pos.move(Direction.UP, 6);
        BlockState blockState = world.getBlockState(pos);
        pos.move(Direction.DOWN);
        for (int j = 0; j < 10; ++j) {
            BlockState blockState2 = world.getBlockState(pos);
            if (blockState.isAir() && AmbientDesertBlockSounds.triggersSandSounds(blockState2)) {
                return true;
            }
            blockState = blockState2;
            pos.move(Direction.DOWN);
        }
        return false;
    }

    private static boolean triggersSandSounds(BlockState state) {
        return state.isIn(BlockTags.TRIGGERS_AMBIENT_DESERT_SAND_BLOCK_SOUNDS);
    }
}

