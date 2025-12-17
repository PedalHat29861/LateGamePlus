/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.BlockMirror;

public class BlockMirrorArgumentType
extends EnumArgumentType<BlockMirror> {
    private BlockMirrorArgumentType() {
        super(BlockMirror.CODEC, BlockMirror::values);
    }

    public static EnumArgumentType<BlockMirror> blockMirror() {
        return new BlockMirrorArgumentType();
    }

    public static BlockMirror getBlockMirror(CommandContext<ServerCommandSource> context, String id) {
        return (BlockMirror)context.getArgument(id, BlockMirror.class);
    }
}

