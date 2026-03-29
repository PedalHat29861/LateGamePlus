package com.pedalhat.lategameplus.item;

import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.config.ModConfig;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

public class LodestoneWarpItem extends Item {

    private static final int CHARGE_TICKS = 12;

    public LodestoneWarpItem(Item.Properties settings) {
        super(settings.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack inHand = user.getItemInHand(hand);
        if (user.getCooldowns().isOnCooldown(inHand)) {
            user.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return InteractionResult.FAIL;
        }

        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return CHARGE_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return stack;

        if (player.getCooldowns().isOnCooldown(stack)) {
            player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        ModConfig cfg = ConfigManager.get();

        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        Optional<GlobalPos> maybeTarget = (tracker == null) ? Optional.empty() : tracker.target();
        if (maybeTarget.isEmpty()) {
            player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        GlobalPos gpos = maybeTarget.get();
        ServerLevel targetWorld = player.level().getServer().getLevel(gpos.dimension());
        if (targetWorld == null) {
            player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        if (!cfg.lodestoneWarpCrossDim
            && !player.level().dimension().equals(targetWorld.dimension())) {
            player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        BlockPos lodestone = gpos.pos();
        double x = lodestone.getX() + 0.5;
        double y = lodestone.getY() + 1.0;
        double z = lodestone.getZ() + 0.5;

        world.playSound(
            null,
            entity.blockPosition(),
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS,
            0.8f, 1f
        );

        player.teleportTo(
            targetWorld,
            x, y, z,
            Set.<Relative>of(),
            player.getYRot(),
            player.getXRot(),
            false
        );

        targetWorld.playSound(
            null,
            BlockPos.containing(x, y, z),
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS,
            0.8f, 1f
        );

        int cooldown = Math.max(0, cfg.lodestoneWarpCooldownTicks);
        player.getCooldowns().addCooldown(stack, cooldown);
        player.level().getServer().execute(() -> player.getCooldowns().addCooldown(stack, cooldown));

        if (!player.isCreative() && !cfg.lodestoneWarpReusable) {
            stack.setDamageValue(stack.getDamageValue() + 1);
            if (stack.getDamageValue() >= stack.getMaxDamage()) stack.shrink(1);
        }

        return stack;
    }
}
