/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.function;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.MacroException;
import net.minecraft.server.function.Procedure;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public record ExpandedMacro<T>(Identifier id, List<SourcedCommandAction<T>> entries) implements CommandFunction<T>,
Procedure<T>
{
    @Override
    public Procedure<T> withMacroReplaced(@Nullable NbtCompound arguments, CommandDispatcher<T> dispatcher) throws MacroException {
        return this;
    }
}

