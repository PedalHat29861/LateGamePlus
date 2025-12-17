/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command;

import net.minecraft.command.CommandAction;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.Frame;

@FunctionalInterface
public interface SourcedCommandAction<T> {
    public void execute(T var1, CommandExecutionContext<T> var2, Frame var3);

    default public CommandAction<T> bind(T source) {
        return (context, frame) -> this.execute(source, context, frame);
    }
}

