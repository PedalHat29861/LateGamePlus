/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.message;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface MessageDecorator {
    public static final MessageDecorator NOOP = (sender, message) -> message;

    public Text decorate(@Nullable ServerPlayerEntity var1, Text var2);
}

