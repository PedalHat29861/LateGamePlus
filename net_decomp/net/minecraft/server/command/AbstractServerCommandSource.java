/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.ResultConsumer
 *  com.mojang.brigadier.exceptions.CommandExceptionType
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.permission.PermissionSource;
import net.minecraft.server.function.Tracer;
import org.jspecify.annotations.Nullable;

public interface AbstractServerCommandSource<T extends AbstractServerCommandSource<T>>
extends PermissionSource {
    public T withReturnValueConsumer(ReturnValueConsumer var1);

    public ReturnValueConsumer getReturnValueConsumer();

    default public T withDummyReturnValueConsumer() {
        return this.withReturnValueConsumer(ReturnValueConsumer.EMPTY);
    }

    public CommandDispatcher<T> getDispatcher();

    public void handleException(CommandExceptionType var1, Message var2, boolean var3, @Nullable Tracer var4);

    public boolean isSilent();

    default public void handleException(CommandSyntaxException exception, boolean silent, @Nullable Tracer tracer) {
        this.handleException(exception.getType(), exception.getRawMessage(), silent, tracer);
    }

    public static <T extends AbstractServerCommandSource<T>> ResultConsumer<T> asResultConsumer() {
        return (context, success, result) -> ((AbstractServerCommandSource)context.getSource()).getReturnValueConsumer().onResult(success, result);
    }
}

