/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command;

import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.Frame;

@FunctionalInterface
public interface CommandAction<T> {
    public void execute(CommandExecutionContext<T> var1, Frame var2);
}

