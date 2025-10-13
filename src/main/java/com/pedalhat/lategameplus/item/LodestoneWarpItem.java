package com.pedalhat.lategameplus.item;

import com.pedalhat.lategameplus.config.ConfigManager;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Set;

public class LodestoneWarpItem extends Item {

    private static final int CHARGE_TICKS = 12;

    public LodestoneWarpItem(Item.Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack inHand = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(inHand)) {
            user.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return ActionResult.FAIL;
        }

        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return CHARGE_TICKS;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) return stack;

        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        LodestoneTrackerComponent tracker = stack.get(DataComponentTypes.LODESTONE_TRACKER);
        Optional<GlobalPos> maybeTarget = (tracker == null) ? Optional.empty() : tracker.target();
        if (maybeTarget.isEmpty()) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        GlobalPos gpos = maybeTarget.get();
        ServerWorld targetWorld = player.getEntityWorld().getServer().getWorld(gpos.dimension());
        if (targetWorld == null) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        if (!ConfigManager.get().lodestoneWarpCrossDim
            && !player.getEntityWorld().getRegistryKey().equals(targetWorld.getRegistryKey())) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        BlockPos lodestone = gpos.pos();
        double x = lodestone.getX() + 0.5;
        double y = lodestone.getY() + 1.0;
        double z = lodestone.getZ() + 0.5;

        world.playSound(
            null,
            entity.getBlockPos(),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
            0.8f, 1f
        );

        player.teleport(
            targetWorld,
            x, y, z,
            Set.<PositionFlag>of(),
            player.getYaw(),
            player.getPitch(),
            false
        );

        targetWorld.playSound(
            null,
            BlockPos.ofFloored(x, y, z),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
            0.8f, 1f
        );

        int cooldown = Math.max(0, ConfigManager.get().lodestoneWarpCooldownTicks);
        player.getItemCooldownManager().set(stack, cooldown);
        player.getEntityWorld().getServer().execute(() -> player.getItemCooldownManager().set(stack, cooldown));

        if (!player.isCreative()) {
            stack.setDamage(stack.getDamage() + 1);
            if (stack.getDamage() >= stack.getMaxDamage()) stack.decrement(1);
        }

        return stack;
    }
}
