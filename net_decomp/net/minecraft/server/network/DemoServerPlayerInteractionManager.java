/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DemoServerPlayerInteractionManager
extends ServerPlayerInteractionManager {
    public static final int DEMO_DAYS = 5;
    public static final int DEMO_TIME = 120500;
    private boolean sentHelp;
    private boolean demoEnded;
    private int reminderTicks;
    private int tick;

    public DemoServerPlayerInteractionManager(ServerPlayerEntity serverPlayerEntity) {
        super(serverPlayerEntity);
    }

    @Override
    public void update() {
        super.update();
        ++this.tick;
        long l = this.world.getTime();
        long m = l / 24000L + 1L;
        if (!this.sentHelp && this.tick > 20) {
            this.sentHelp = true;
            this.player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN, 0.0f));
        }
        boolean bl = this.demoEnded = l > 120500L;
        if (this.demoEnded) {
            ++this.reminderTicks;
        }
        if (l % 24000L == 500L) {
            if (m <= 6L) {
                if (m == 6L) {
                    this.player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN, 104.0f));
                } else {
                    this.player.sendMessage(Text.translatable("demo.day." + m));
                }
            }
        } else if (m == 1L) {
            if (l == 100L) {
                this.player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN, 101.0f));
            } else if (l == 175L) {
                this.player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN, 102.0f));
            } else if (l == 250L) {
                this.player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN, 103.0f));
            }
        } else if (m == 5L && l % 24000L == 22000L) {
            this.player.sendMessage(Text.translatable("demo.day.warning"));
        }
    }

    private void sendDemoReminder() {
        if (this.reminderTicks > 100) {
            this.player.sendMessage(Text.translatable("demo.reminder"));
            this.reminderTicks = 0;
        }
    }

    @Override
    public void processBlockBreakingAction(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence) {
        if (this.demoEnded) {
            this.sendDemoReminder();
            return;
        }
        super.processBlockBreakingAction(pos, action, direction, worldHeight, sequence);
    }

    @Override
    public ActionResult interactItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand) {
        if (this.demoEnded) {
            this.sendDemoReminder();
            return ActionResult.PASS;
        }
        return super.interactItem(player, world, stack, hand);
    }

    @Override
    public ActionResult interactBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult) {
        if (this.demoEnded) {
            this.sendDemoReminder();
            return ActionResult.PASS;
        }
        return super.interactBlock(player, world, stack, hand, hitResult);
    }
}

