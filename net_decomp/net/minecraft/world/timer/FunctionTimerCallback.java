/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.timer;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;

public record FunctionTimerCallback(Identifier name) implements TimerCallback<MinecraftServer>
{
    public static final MapCodec<FunctionTimerCallback> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("Name").forGetter(FunctionTimerCallback::name)).apply((Applicative)instance, FunctionTimerCallback::new));

    @Override
    public void call(MinecraftServer minecraftServer, Timer<MinecraftServer> timer, long l) {
        CommandFunctionManager commandFunctionManager = minecraftServer.getCommandFunctionManager();
        commandFunctionManager.getFunction(this.name).ifPresent(function -> commandFunctionManager.execute((CommandFunction<ServerCommandSource>)function, commandFunctionManager.getScheduledCommandSource()));
    }

    @Override
    public MapCodec<FunctionTimerCallback> getCodec() {
        return CODEC;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{FunctionTimerCallback.class, "functionId", "name"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FunctionTimerCallback.class, "functionId", "name"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FunctionTimerCallback.class, "functionId", "name"}, this, object);
    }
}

