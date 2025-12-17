/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class EnderEyeItem
extends Item {
    public EnderEyeItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos;
        World world = context.getWorld();
        BlockState blockState = world.getBlockState(blockPos = context.getBlockPos());
        if (!blockState.isOf(Blocks.END_PORTAL_FRAME) || blockState.get(EndPortalFrameBlock.EYE).booleanValue()) {
            return ActionResult.PASS;
        }
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        BlockState blockState2 = (BlockState)blockState.with(EndPortalFrameBlock.EYE, true);
        Block.pushEntitiesUpBeforeBlockChange(blockState, blockState2, world, blockPos);
        world.setBlockState(blockPos, blockState2, 2);
        world.updateComparators(blockPos, Blocks.END_PORTAL_FRAME);
        context.getStack().decrement(1);
        world.syncWorldEvent(1503, blockPos, 0);
        BlockPattern.Result result = EndPortalFrameBlock.getCompletedFramePattern().searchAround(world, blockPos);
        if (result != null) {
            BlockPos blockPos2 = result.getFrontTopLeft().add(-3, 0, -3);
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    BlockPos blockPos3 = blockPos2.add(i, 0, j);
                    world.breakBlock(blockPos3, true, null);
                    world.setBlockState(blockPos3, Blocks.END_PORTAL.getDefaultState(), 2);
                }
            }
            world.syncGlobalEvent(1038, blockPos2.add(1, 0, 1), 0);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 0;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = EnderEyeItem.raycast(world, user, RaycastContext.FluidHandling.NONE);
        if (blockHitResult.getType() == HitResult.Type.BLOCK && world.getBlockState(blockHitResult.getBlockPos()).isOf(Blocks.END_PORTAL_FRAME)) {
            return ActionResult.PASS;
        }
        user.setCurrentHand(hand);
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            BlockPos blockPos = serverWorld.locateStructure(StructureTags.EYE_OF_ENDER_LOCATED, user.getBlockPos(), 100, false);
            if (blockPos == null) {
                return ActionResult.CONSUME;
            }
            EyeOfEnderEntity eyeOfEnderEntity = new EyeOfEnderEntity(world, user.getX(), user.getBodyY(0.5), user.getZ());
            eyeOfEnderEntity.setItem(itemStack);
            eyeOfEnderEntity.initTargetPos(Vec3d.of(blockPos));
            world.emitGameEvent(GameEvent.PROJECTILE_SHOOT, eyeOfEnderEntity.getEntityPos(), GameEvent.Emitter.of(user));
            world.spawnEntity(eyeOfEnderEntity);
            if (user instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)user;
                Criteria.USED_ENDER_EYE.trigger(serverPlayerEntity, blockPos);
            }
            float f = MathHelper.lerp(world.random.nextFloat(), 0.33f, 0.5f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 1.0f, f);
            itemStack.decrementUnlessCreative(1, user);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return ActionResult.SUCCESS_SERVER;
    }
}

