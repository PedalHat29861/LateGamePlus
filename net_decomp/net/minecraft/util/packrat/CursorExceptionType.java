/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.util.packrat.Literals;

public interface CursorExceptionType<T extends Exception> {
    public T create(String var1, int var2);

    public static CursorExceptionType<CommandSyntaxException> create(SimpleCommandExceptionType type) {
        return (input, cursor) -> type.createWithContext((ImmutableStringReader)Literals.createReader(input, cursor));
    }

    public static CursorExceptionType<CommandSyntaxException> create(DynamicCommandExceptionType type, String arg) {
        return (input, cursor) -> type.createWithContext((ImmutableStringReader)Literals.createReader(input, cursor), (Object)arg);
    }
}

