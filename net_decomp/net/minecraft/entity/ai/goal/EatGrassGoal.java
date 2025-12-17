/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;

public class EatGrassGoal
extends Goal {
    private static final int MAX_TIMER = 40;
    private static final Predicate<BlockState> EDIBLE_PREDICATE = state -> state.isIn(BlockTags.EDIBLE_FOR_SHEEP);
    private final MobEntity mob;
    private final World world;
    private int timer;

    public EatGrassGoal(MobEntity mob) {
        this.mob = mob;
        this.world = mob.getEntityWorld();
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (this.mob.getRandom().nextInt(this.getTickCount(this.mob.isBaby() ? 50 : 1000)) != 0) {
            return false;
        }
        BlockPos blockPos = this.mob.getBlockPos();
        if (EDIBLE_PREDICATE.test(this.world.getBlockState(blockPos))) {
            return true;
        }
        return this.world.getBlockState(blockPos.down()).isOf(Blocks.GRASS_BLOCK);
    }

    @Override
    public void start() {
        this.timer = this.getTickCount(40);
        this.world.sendEntityStatus(this.mob, (byte)10);
        this.mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.timer = 0;
    }

    @Override
    public boolean shouldContinue() {
        return this.timer > 0;
    }

    public int getTimer() {
        return this.timer;
    }

    @Override
    public void tick() {
        this.timer = Math.max(0, this.timer - 1);
        if (this.timer != this.getTickCount(4)) {
            return;
        }
        BlockPos blockPos = this.mob.getBlockPos();
        if (EDIBLE_PREDICATE.test(this.world.getBlockState(blockPos))) {
            if (EatGrassGoal.castToServerWorld(this.world).getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                this.world.breakBlock(blockPos, false);
            }
            this.mob.onEatingGrass();
        } else {
            BlockPos blockPos2 = blockPos.down();
            if (this.world.getBlockState(blockPos2).isOf(Blocks.GRASS_BLOCK)) {
                if (EatGrassGoal.castToServerWorld(this.world).getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                    this.world.syncWorldEvent(2001, blockPos2, Block.getRawIdFromState(Blocks.GRASS_BLOCK.getDefaultState()));
                    this.world.setBlockState(blockPos2, Blocks.DIRT.getDefaultState(), 2);
                }
                this.mob.onEatingGrass();
            }
        }
    }
}

