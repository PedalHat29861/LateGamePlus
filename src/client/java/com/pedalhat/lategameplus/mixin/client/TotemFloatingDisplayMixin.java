package com.pedalhat.lategameplus.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.pedalhat.lategameplus.registry.ModItems;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class TotemFloatingDisplayMixin {

    // Nombre usado en varias versiones (más viejo)
    @Inject(
        method = "onEntityStatus(Lnet/minecraft/network/packet/s2c/play/EntityStatusS2CPacket;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void netex$showCustomTotem_onEntityStatus(EntityStatusS2CPacket pkt, CallbackInfo ci) {
        netex$maybeHandleTotem(pkt, ci);
    }

    // Nombre alternativo en otras builds (más nuevo)

    // @Inject(
    //     method = "onEntityEvent(Lnet/minecraft/network/packet/s2c/play/EntityStatusS2CPacket;)V",
    //     at = @At("HEAD"),
    //     cancellable = true,
    //     require = 0
    // )
    // private void netex$showCustomTotem_onEntityEvent(EntityStatusS2CPacket pkt, CallbackInfo ci) {
    //     netex$maybeHandleTotem(pkt, ci);
    // }

    private static void netex$maybeHandleTotem(EntityStatusS2CPacket pkt, CallbackInfo ci) {
        if (pkt.getStatus() != EntityStatuses.USE_TOTEM_OF_UNDYING) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) return;

        Entity e = pkt.getEntity(client.world);
        if (e != client.player) return;

        client.player.playSound(SoundEvents.ITEM_TOTEM_USE, 1.0f, 1.0f);
        client.particleManager.addEmitter(e, ParticleTypes.TOTEM_OF_UNDYING, 30);

        client.gameRenderer.showFloatingItem(ModItems.TOTEM_OF_NETHERDYING.getDefaultStack());

        ci.cancel();
    }
}
