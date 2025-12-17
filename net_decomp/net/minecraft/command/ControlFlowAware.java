/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.Tracer;
import org.jspecify.annotations.Nullable;

public interface ControlFlowAware<T> {
    public void execute(T var1, ContextChain<T> var2, ExecutionFlags var3, ExecutionControl<T> var4);

    public static abstract class Helper<T extends AbstractServerCommandSource<T>>
    implements ControlFlowAware<T> {
        @Override
        public final void execute(T abstractServerCommandSource, ContextChain<T> contextChain, ExecutionFlags executionFlags, ExecutionControl<T> executionControl) {
            try {
                this.executeInner(abstractServerCommandSource, contextChain, executionFlags, executionControl);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                this.sendError(commandSyntaxException, abstractServerCommandSource, executionFlags, executionControl.getTracer());
                abstractServerCommandSource.getReturnValueConsumer().onFailure();
            }
        }

        protected void sendError(CommandSyntaxException exception, T source, ExecutionFlags flags, @Nullable Tracer tracer) {
            source.handleException(exception, flags.isSilent(), tracer);
        }

        protected abstract void executeInner(T var1, ContextChain<T> var2, ExecutionFlags var3, ExecutionControl<T> var4) throws CommandSyntaxException;
    }

    public static interface Command<T>
    extends com.mojang.brigadier.Command<T>,
    ControlFlowAware<T> {
        default public int run(CommandContext<T> context) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}

