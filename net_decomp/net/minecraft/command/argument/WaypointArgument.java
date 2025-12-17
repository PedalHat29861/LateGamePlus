/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.waypoint.ServerWaypoint;

public class WaypointArgument {
    public static final SimpleCommandExceptionType INVALID_WAYPOINT_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.waypoint.invalid"));

    public static ServerWaypoint getWaypoint(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
        Entity entity = ((EntitySelector)context.getArgument(argument, EntitySelector.class)).getEntity((ServerCommandSource)context.getSource());
        if (entity instanceof ServerWaypoint) {
            ServerWaypoint serverWaypoint = (ServerWaypoint)((Object)entity);
            return serverWaypoint;
        }
        throw INVALID_WAYPOINT_EXCEPTION.create();
    }
}

