/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class BucketItem
extends Item
implements FluidModificationItem {
    private final Fluid fluid;

    public BucketItem(Fluid fluid, Item.Settings settings) {
        super(settings);
        this.fluid = fluid;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = BucketItem.raycast(world, user, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return ActionResult.PASS;
        }
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos3;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (!world.canEntityModifyAt(user, blockPos) || !user.canPlaceOn(blockPos2, direction, itemStack)) {
                return ActionResult.FAIL;
            }
            if (this.fluid == Fluids.EMPTY) {
                FluidDrainable fluidDrainable;
                ItemStack itemStack2;
                BlockState blockState = world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (block instanceof FluidDrainable && !(itemStack2 = (fluidDrainable = (FluidDrainable)((Object)block)).tryDrainFluid(user, world, blockPos, blockState)).isEmpty()) {
                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    fluidDrainable.getBucketFillSound().ifPresent(sound -> user.playSound((SoundEvent)sound, 1.0f, 1.0f));
                    world.emitGameEvent((Entity)user, GameEvent.FLUID_PICKUP, blockPos);
                    ItemStack itemStack3 = ItemUsage.exchangeStack(itemStack, user, itemStack2);
                    if (!world.isClient()) {
                        Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity)user, itemStack2);
                    }
                    return ActionResult.SUCCESS.withNewHandStack(itemStack3);
                }
                return ActionResult.FAIL;
            }
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos blockPos4 = blockPos3 = blockState.getBlock() instanceof FluidFillable && this.fluid == Fluids.WATER ? blockPos : blockPos2;
            if (this.placeFluid(user, world, blockPos3, blockHitResult)) {
                this.onEmptied(user, world, itemStack, blockPos3);
                if (user instanceof ServerPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, blockPos3, itemStack);
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
                ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, user, BucketItem.getEmptiedStack(itemStack, user));
                return ActionResult.SUCCESS.withNewHandStack(itemStack2);
            }
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public static ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player) {
        if (!player.isInCreativeMode()) {
            return new ItemStack(Items.BUCKET);
        }
        return stack;
    }

    @Override
    public void onEmptied(@Nullable LivingEntity user, World world, ItemStack stack, BlockPos pos) {
    }

    @Override
    public boolean placeFluid(@Nullable LivingEntity user, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
        boolean bl4;
        FluidFillable fluidFillable;
        Fluid fluid = this.fluid;
        if (!(fluid instanceof FlowableFluid)) {
            return false;
        }
        FlowableFluid flowableFluid = (FlowableFluid)fluid;
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        boolean bl = blockState.canBucketPlace(this.fluid);
        boolean bl2 = user != null && user.isSneaking();
        boolean bl3 = bl || block instanceof FluidFillable && (fluidFillable = (FluidFillable)((Object)block)).canFillWithFluid(user, world, pos, blockState, this.fluid);
        boolean bl5 = bl4 = blockState.isAir() || bl3 && (!bl2 || hitResult == null);
        if (!bl4) {
            return hitResult != null && this.placeFluid(user, world, hitResult.getBlockPos().offset(hitResult.getSide()), null);
        }
        if (world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.WATER_EVAPORATES_GAMEPLAY, pos).booleanValue() && this.fluid.isIn(FluidTags.WATER)) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            world.playSound((Entity)user, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
            for (int l = 0; l < 8; ++l) {
                world.addParticleClient(ParticleTypes.LARGE_SMOKE, (float)i + world.random.nextFloat(), (float)j + world.random.nextFloat(), (float)k + world.random.nextFloat(), 0.0, 0.0, 0.0);
            }
            return true;
        }
        if (block instanceof FluidFillable) {
            FluidFillable fluidFillable2 = (FluidFillable)((Object)block);
            if (this.fluid == Fluids.WATER) {
                fluidFillable2.tryFillWithFluid(world, pos, blockState, flowableFluid.getStill(false));
                this.playEmptyingSound(user, world, pos);
                return true;
            }
        }
        if (!world.isClient() && bl && !blockState.isLiquid()) {
            world.breakBlock(pos, true);
        }
        if (world.setBlockState(pos, this.fluid.getDefaultState().getBlockState(), 11) || blockState.getFluidState().isStill()) {
            this.playEmptyingSound(user, world, pos);
            return true;
        }
        return false;
    }

    protected void playEmptyingSound(@Nullable LivingEntity user, WorldAccess world, BlockPos pos) {
        SoundEvent soundEvent = this.fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        world.playSound(user, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.emitGameEvent((Entity)user, GameEvent.FLUID_PLACE, pos);
    }

    public Fluid getFluid() {
        return this.fluid;
    }
}

