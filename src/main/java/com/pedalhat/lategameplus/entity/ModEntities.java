package com.pedalhat.lategameplus.entity;

import com.pedalhat.lategameplus.LateGamePlus;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModEntities {
  public static final Identifier NETHERITE_GOLEM_ID =
      Identifier.of(LateGamePlus.MOD_ID, "netherite_golem");

  public static final RegistryKey<EntityType<?>> NETHERITE_GOLEM_KEY =
      RegistryKey.of(RegistryKeys.ENTITY_TYPE, NETHERITE_GOLEM_ID);

    public static final EntityType<NetheriteGolemEntity> NETHERITE_GOLEM =
        Registry.register(
            Registries.ENTITY_TYPE,
            NETHERITE_GOLEM_KEY,
            EntityType.Builder
                .<NetheriteGolemEntity>create(NetheriteGolemEntity::new, SpawnGroup.MISC)
                .dimensions(1.4f, 2.7f)
                .maxTrackingRange(8)
                .trackingTickInterval(3)
                .build(NETHERITE_GOLEM_KEY)
        );

  private ModEntities() {}

  public static void init() {}
}
