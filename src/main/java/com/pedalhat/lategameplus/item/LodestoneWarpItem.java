package com.pedalhat.lategameplus.item;

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

    /** Tiempo de “cargar” el uso (mantener pulsado). */
    private static final int CHARGE_TICKS = 12;       // ~0.6s
    /** Cooldown tras teletransportar. Ajusta a gusto. */
    private static final int COOLDOWN_TICKS = 20 * 30; // 30s

    public LodestoneWarpItem(Item.Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // Si está en cooldown, no deja empezar a usar
        ItemStack inHand = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(inHand)) {
            // Sonidito de “no se puede”
            user.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return ActionResult.FAIL;
        }

        user.setCurrentHand(hand);
        return ActionResult.CONSUME; // correcto en 1.21.x
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return CHARGE_TICKS;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW; // animación tipo arco (tensar)
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        // Sólo servidor y sólo jugadores
        if (!(entity instanceof ServerPlayerEntity player)) return stack;

        // Respetar cooldown también aquí (por si cambió mientras cargaba)
        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        // Leer objetivo del componente de brújula imantada en ESTE stack
        LodestoneTrackerComponent tracker = stack.get(DataComponentTypes.LODESTONE_TRACKER);
        Optional<GlobalPos> maybeTarget = (tracker == null) ? Optional.empty() : tracker.target();
        if (maybeTarget.isEmpty()) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        GlobalPos gpos = maybeTarget.get();
        ServerWorld targetWorld = player.getServer().getWorld(gpos.dimension());
        if (targetWorld == null) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
            return stack;
        }

        BlockPos lodestone = gpos.pos();
        double x = lodestone.getX() + 0.5;
        double y = lodestone.getY() + 1.0; // aparece 1 bloque arriba
        double z = lodestone.getZ() + 0.5;

        // Sonido previo (en el mundo actual)
        world.playSound(
            null,
            entity.getBlockPos(),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
            0.8f, 1f
        );

        // Teletransporte (firma 1.21.x con PositionFlag)
        player.teleport(
            targetWorld,
            x, y, z,
            Set.<PositionFlag>of(),      // sin flags extra
            player.getYaw(),
            player.getPitch(),
            false                        // no resetear cámara
        );

        // Sonido posterior en destino
        targetWorld.playSound(
            null,
            BlockPos.ofFloored(x, y, z),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
            0.8f, 1f
        );

        // Aplicar cooldown – en 1.21.8 se pasa el STACK
        player.getItemCooldownManager().set(stack, COOLDOWN_TICKS);

        // Consumir (si quieres “cargas” en vez de consumo, cambia a durabilidad)
        if (!player.isCreative()) stack.decrement(1);

        return stack;
    }
}
