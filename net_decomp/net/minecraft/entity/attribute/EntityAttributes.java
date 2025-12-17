/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.attribute;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class EntityAttributes {
    public static final double field_63297 = 4.0;
    public static final RegistryEntry<EntityAttribute> ARMOR = EntityAttributes.register("armor", new ClampedEntityAttribute("attribute.name.armor", 0.0, 0.0, 30.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> ARMOR_TOUGHNESS = EntityAttributes.register("armor_toughness", new ClampedEntityAttribute("attribute.name.armor_toughness", 0.0, 0.0, 20.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> ATTACK_DAMAGE = EntityAttributes.register("attack_damage", new ClampedEntityAttribute("attribute.name.attack_damage", 2.0, 0.0, 2048.0));
    public static final RegistryEntry<EntityAttribute> ATTACK_KNOCKBACK = EntityAttributes.register("attack_knockback", new ClampedEntityAttribute("attribute.name.attack_knockback", 0.0, 0.0, 5.0));
    public static final RegistryEntry<EntityAttribute> ATTACK_SPEED = EntityAttributes.register("attack_speed", new ClampedEntityAttribute("attribute.name.attack_speed", 4.0, 0.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> BLOCK_BREAK_SPEED = EntityAttributes.register("block_break_speed", new ClampedEntityAttribute("attribute.name.block_break_speed", 1.0, 0.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> BLOCK_INTERACTION_RANGE = EntityAttributes.register("block_interaction_range", new ClampedEntityAttribute("attribute.name.block_interaction_range", 4.5, 0.0, 64.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> BURNING_TIME = EntityAttributes.register("burning_time", new ClampedEntityAttribute("attribute.name.burning_time", 1.0, 0.0, 1024.0).setTracked(true).setCategory(EntityAttribute.Category.NEGATIVE));
    public static final RegistryEntry<EntityAttribute> CAMERA_DISTANCE = EntityAttributes.register("camera_distance", new ClampedEntityAttribute("attribute.name.camera_distance", 4.0, 0.0, 32.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> EXPLOSION_KNOCKBACK_RESISTANCE = EntityAttributes.register("explosion_knockback_resistance", new ClampedEntityAttribute("attribute.name.explosion_knockback_resistance", 0.0, 0.0, 1.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> ENTITY_INTERACTION_RANGE = EntityAttributes.register("entity_interaction_range", new ClampedEntityAttribute("attribute.name.entity_interaction_range", 3.0, 0.0, 64.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> FALL_DAMAGE_MULTIPLIER = EntityAttributes.register("fall_damage_multiplier", new ClampedEntityAttribute("attribute.name.fall_damage_multiplier", 1.0, 0.0, 100.0).setTracked(true).setCategory(EntityAttribute.Category.NEGATIVE));
    public static final RegistryEntry<EntityAttribute> FLYING_SPEED = EntityAttributes.register("flying_speed", new ClampedEntityAttribute("attribute.name.flying_speed", 0.4, 0.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> FOLLOW_RANGE = EntityAttributes.register("follow_range", new ClampedEntityAttribute("attribute.name.follow_range", 32.0, 0.0, 2048.0));
    public static final RegistryEntry<EntityAttribute> GRAVITY = EntityAttributes.register("gravity", new ClampedEntityAttribute("attribute.name.gravity", 0.08, -1.0, 1.0).setTracked(true).setCategory(EntityAttribute.Category.NEUTRAL));
    public static final RegistryEntry<EntityAttribute> JUMP_STRENGTH = EntityAttributes.register("jump_strength", new ClampedEntityAttribute("attribute.name.jump_strength", 0.42f, 0.0, 32.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> KNOCKBACK_RESISTANCE = EntityAttributes.register("knockback_resistance", new ClampedEntityAttribute("attribute.name.knockback_resistance", 0.0, 0.0, 1.0));
    public static final RegistryEntry<EntityAttribute> LUCK = EntityAttributes.register("luck", new ClampedEntityAttribute("attribute.name.luck", 0.0, -1024.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> MAX_ABSORPTION = EntityAttributes.register("max_absorption", new ClampedEntityAttribute("attribute.name.max_absorption", 0.0, 0.0, 2048.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> MAX_HEALTH = EntityAttributes.register("max_health", new ClampedEntityAttribute("attribute.name.max_health", 20.0, 1.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> MINING_EFFICIENCY = EntityAttributes.register("mining_efficiency", new ClampedEntityAttribute("attribute.name.mining_efficiency", 0.0, 0.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> MOVEMENT_EFFICIENCY = EntityAttributes.register("movement_efficiency", new ClampedEntityAttribute("attribute.name.movement_efficiency", 0.0, 0.0, 1.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> MOVEMENT_SPEED = EntityAttributes.register("movement_speed", new ClampedEntityAttribute("attribute.name.movement_speed", 0.7, 0.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> OXYGEN_BONUS = EntityAttributes.register("oxygen_bonus", new ClampedEntityAttribute("attribute.name.oxygen_bonus", 0.0, 0.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> SAFE_FALL_DISTANCE = EntityAttributes.register("safe_fall_distance", new ClampedEntityAttribute("attribute.name.safe_fall_distance", 3.0, -1024.0, 1024.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> SCALE = EntityAttributes.register("scale", new ClampedEntityAttribute("attribute.name.scale", 1.0, 0.0625, 16.0).setTracked(true).setCategory(EntityAttribute.Category.NEUTRAL));
    public static final RegistryEntry<EntityAttribute> SNEAKING_SPEED = EntityAttributes.register("sneaking_speed", new ClampedEntityAttribute("attribute.name.sneaking_speed", 0.3, 0.0, 1.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> SPAWN_REINFORCEMENTS = EntityAttributes.register("spawn_reinforcements", new ClampedEntityAttribute("attribute.name.spawn_reinforcements", 0.0, 0.0, 1.0));
    public static final RegistryEntry<EntityAttribute> STEP_HEIGHT = EntityAttributes.register("step_height", new ClampedEntityAttribute("attribute.name.step_height", 0.6, 0.0, 10.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> SUBMERGED_MINING_SPEED = EntityAttributes.register("submerged_mining_speed", new ClampedEntityAttribute("attribute.name.submerged_mining_speed", 0.2, 0.0, 20.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> SWEEPING_DAMAGE_RATIO = EntityAttributes.register("sweeping_damage_ratio", new ClampedEntityAttribute("attribute.name.sweeping_damage_ratio", 0.0, 0.0, 1.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> TEMPT_RANGE = EntityAttributes.register("tempt_range", new ClampedEntityAttribute("attribute.name.tempt_range", 10.0, 0.0, 2048.0));
    public static final RegistryEntry<EntityAttribute> WATER_MOVEMENT_EFFICIENCY = EntityAttributes.register("water_movement_efficiency", new ClampedEntityAttribute("attribute.name.water_movement_efficiency", 0.0, 0.0, 1.0).setTracked(true));
    public static final RegistryEntry<EntityAttribute> WAYPOINT_TRANSMIT_RANGE = EntityAttributes.register("waypoint_transmit_range", new ClampedEntityAttribute("attribute.name.waypoint_transmit_range", 0.0, 0.0, 6.0E7).setCategory(EntityAttribute.Category.NEUTRAL));
    public static final RegistryEntry<EntityAttribute> WAYPOINT_RECEIVE_RANGE = EntityAttributes.register("waypoint_receive_range", new ClampedEntityAttribute("attribute.name.waypoint_receive_range", 0.0, 0.0, 6.0E7).setCategory(EntityAttribute.Category.NEUTRAL));

    private static RegistryEntry<EntityAttribute> register(String id, EntityAttribute attribute) {
        return Registry.registerReference(Registries.ATTRIBUTE, Identifier.ofVanilla(id), attribute);
    }

    public static RegistryEntry<EntityAttribute> registerAndGetDefault(Registry<EntityAttribute> registry) {
        return MAX_HEALTH;
    }
}

