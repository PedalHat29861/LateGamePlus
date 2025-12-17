/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command.argument;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface PosArgument {
    public Vec3d getPos(ServerCommandSource var1);

    public Vec2f getRotation(ServerCommandSource var1);

    default public BlockPos toAbsoluteBlockPos(ServerCommandSource source) {
        return BlockPos.ofFloored(this.getPos(source));
    }

    public boolean isXRelative();

    public boolean isYRelative();

    public boolean isZRelative();
}

