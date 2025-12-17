/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.config.CodeOfConductS2CPacket;
import net.minecraft.server.network.ServerPlayerConfigurationTask;

public class SendCodeOfConductTask
implements ServerPlayerConfigurationTask {
    public static final ServerPlayerConfigurationTask.Key KEY = new ServerPlayerConfigurationTask.Key("server_code_of_conduct");
    private final Supplier<String> textSupplier;

    public SendCodeOfConductTask(Supplier<String> textSupplier) {
        this.textSupplier = textSupplier;
    }

    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        sender.accept(new CodeOfConductS2CPacket(this.textSupplier.get()));
    }

    @Override
    public ServerPlayerConfigurationTask.Key getKey() {
        return KEY;
    }
}

