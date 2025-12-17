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
import net.minecraft.util.BlockRotation;

public class BlockRotationArgumentType
extends EnumArgumentType<BlockRotation> {
    private BlockRotationArgumentType() {
        super(BlockRotation.CODEC, BlockRotation::values);
    }

    public static BlockRotationArgumentType blockRotation() {
        return new BlockRotationArgumentType();
    }

    public static BlockRotation getBlockRotation(CommandContext<ServerCommandSource> context, String id) {
        return (BlockRotation)context.getArgument(id, BlockRotation.class);
    }
}

