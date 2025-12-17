/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.entity.effect;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.function.ToIntFunction;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.rule.GameRules;

class WeavingStatusEffect
extends StatusEffect {
    private final ToIntFunction<Random> cobwebChanceFunction;

    protected WeavingStatusEffect(StatusEffectCategory category, int color, ToIntFunction<Random> cobwebChanceFunction) {
        super(category, color, ParticleTypes.ITEM_COBWEB);
        this.cobwebChanceFunction = cobwebChanceFunction;
    }

    @Override
    public void onEntityRemoval(ServerWorld world, LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED && (entity instanceof PlayerEntity || world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue())) {
            this.tryPlaceCobweb(world, entity.getRandom(), entity.getBlockPos());
        }
    }

    private void tryPlaceCobweb(ServerWorld world, Random random, BlockPos pos) {
        HashSet set = Sets.newHashSet();
        int i = this.cobwebChanceFunction.applyAsInt(random);
        for (BlockPos blockPos : BlockPos.iterateRandomly(random, 15, pos, 1)) {
            BlockPos blockPos2 = blockPos.down();
            if (set.contains(blockPos) || !world.getBlockState(blockPos).isReplaceable() || !world.getBlockState(blockPos2).isSideSolidFullSquare(world, blockPos2, Direction.UP)) continue;
            set.add(blockPos.toImmutable());
            if (set.size() < i) continue;
            break;
        }
        for (BlockPos blockPos : set) {
            world.setBlockState(blockPos, Blocks.COBWEB.getDefaultState(), 3);
            world.syncWorldEvent(3018, blockPos, 0);
        }
    }
}

