package com.pedalhat.lategameplus.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.pedalhat.lategameplus.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;

@Mixin(ClientPacketListener.class)
public abstract class TotemFloatingDisplayMixin {

    @Inject(
        method = "handleEntityEvent(Lnet/minecraft/network/protocol/game/ClientboundEntityEventPacket;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void netex$showCustomTotem_onEntityStatus(ClientboundEntityEventPacket pkt, CallbackInfo ci) {
        netex$maybeHandleTotem(pkt, ci);
    }


    @SuppressWarnings("null")
    private static void netex$maybeHandleTotem(ClientboundEntityEventPacket pkt, CallbackInfo ci) {
        if (pkt.getEventId() != EntityEvent.PROTECTED_FROM_DEATH) return;

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null || client.player == null) return;

        Entity e = pkt.getEntity(client.level);
        if (e != client.player) return;

        client.player.playSound(SoundEvents.TOTEM_USE, 1.0f, 1.0f);
        client.particleEngine.createTrackingEmitter(e, ParticleTypes.TOTEM_OF_UNDYING, 30);

        client.gameRenderer.displayItemActivation(ModItems.TOTEM_OF_NETHERDYING.getDefaultInstance());

        ci.cancel();
    }
}
