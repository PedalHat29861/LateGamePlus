/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.tree.CommandNode
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.command.FallthroughCommandAction;
import net.minecraft.command.Forkable;
import net.minecraft.command.Frame;
import net.minecraft.command.SingleCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.CommandManager;

public class ReturnCommand {
    public static <T extends AbstractServerCommandSource<T>> void register(CommandDispatcher<T> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal((String)"return").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(RequiredArgumentBuilder.argument((String)"value", (ArgumentType)IntegerArgumentType.integer()).executes(new ValueCommand()))).then(LiteralArgumentBuilder.literal((String)"fail").executes(new FailCommand()))).then(LiteralArgumentBuilder.literal((String)"run").forward((CommandNode)dispatcher.getRoot(), new ReturnRunRedirector(), false)));
    }

    static class ValueCommand<T extends AbstractServerCommandSource<T>>
    implements ControlFlowAware.Command<T> {
        ValueCommand() {
        }

        @Override
        public void execute(T abstractServerCommandSource, ContextChain<T> contextChain, ExecutionFlags executionFlags, ExecutionControl<T> executionControl) {
            int i = IntegerArgumentType.getInteger((CommandContext)contextChain.getTopContext(), (String)"value");
            abstractServerCommandSource.getReturnValueConsumer().onSuccess(i);
            Frame frame = executionControl.getFrame();
            frame.succeed(i);
            frame.doReturn();
        }
    }

    static class FailCommand<T extends AbstractServerCommandSource<T>>
    implements ControlFlowAware.Command<T> {
        FailCommand() {
        }

        @Override
        public void execute(T abstractServerCommandSource, ContextChain<T> contextChain, ExecutionFlags executionFlags, ExecutionControl<T> executionControl) {
            abstractServerCommandSource.getReturnValueConsumer().onFailure();
            Frame frame = executionControl.getFrame();
            frame.fail();
            frame.doReturn();
        }
    }

    static class ReturnRunRedirector<T extends AbstractServerCommandSource<T>>
    implements Forkable.RedirectModifier<T> {
        ReturnRunRedirector() {
        }

        @Override
        public void execute(T abstractServerCommandSource, List<T> list, ContextChain<T> contextChain, ExecutionFlags executionFlags, ExecutionControl<T> executionControl) {
            if (list.isEmpty()) {
                if (executionFlags.isInsideReturnRun()) {
                    executionControl.enqueueAction(FallthroughCommandAction.getInstance());
                }
                return;
            }
            executionControl.getFrame().doReturn();
            ContextChain contextChain2 = contextChain.nextStage();
            String string = contextChain2.getTopContext().getInput();
            executionControl.enqueueAction(new SingleCommandAction.MultiSource<T>(string, contextChain2, executionFlags.setInsideReturnRun(), abstractServerCommandSource, list));
        }
    }
}

