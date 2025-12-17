/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.command;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

@FunctionalInterface
public interface LookTarget {
    public void look(ServerCommandSource var1, Entity var2);

    public record LookAtPosition(Vec3d position) implements LookTarget
    {
        @Override
        public void look(ServerCommandSource source, Entity entity) {
            entity.lookAt(source.getEntityAnchor(), this.position);
        }
    }

    public record LookAtEntity(Entity entity, EntityAnchorArgumentType.EntityAnchor anchor) implements LookTarget
    {
        @Override
        public void look(ServerCommandSource source, Entity entity) {
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                serverPlayerEntity.lookAtEntity(source.getEntityAnchor(), this.entity, this.anchor);
            } else {
                entity.lookAt(source.getEntityAnchor(), this.anchor.positionAt(this.entity));
            }
        }
    }
}

