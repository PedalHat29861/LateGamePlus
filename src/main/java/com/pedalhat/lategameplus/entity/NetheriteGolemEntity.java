package com.pedalhat.lategameplus.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class NetheriteGolemEntity extends PathAwareEntity {
  public NetheriteGolemEntity(EntityType<? extends PathAwareEntity> type, World world) {
    super(type, world);
  }
}
