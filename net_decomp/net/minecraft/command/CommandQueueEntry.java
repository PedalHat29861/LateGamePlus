/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command;

import net.minecraft.command.CommandAction;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.Frame;

public record CommandQueueEntry<T>(Frame frame, CommandAction<T> action) {
    public void execute(CommandExecutionContext<T> context) {
        this.action.execute(context, this.frame);
    }
}

