/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.ValueLookupTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EntityTypeTags;

public class VanillaEntityTypeTagProvider
extends ValueLookupTagProvider<EntityType<?>> {
    public VanillaEntityTypeTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ENTITY_TYPE, registriesFuture, (T entityType) -> entityType.getRegistryEntry().registryKey());
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(EntityTypeTags.SKELETONS).add(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.SKELETON_HORSE, EntityType.BOGGED, EntityType.PARCHED);
        this.builder(EntityTypeTags.ZOMBIES).add(EntityType.ZOMBIE_HORSE, EntityType.CAMEL_HUSK, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN, EntityType.DROWNED, EntityType.HUSK, EntityType.ZOMBIE_NAUTILUS);
        this.builder(EntityTypeTags.RAIDERS).add(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
        this.builder(EntityTypeTags.UNDEAD).addTag(EntityTypeTags.SKELETONS).addTag(EntityTypeTags.ZOMBIES).add(EntityType.WITHER).add(EntityType.PHANTOM);
        this.builder(EntityTypeTags.BURN_IN_DAYLIGHT).add(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.BOGGED).add((EntityType[])new EntityType[]{EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIE_VILLAGER, EntityType.DROWNED, EntityType.ZOMBIE_NAUTILUS}).add(EntityType.PHANTOM);
        this.builder(EntityTypeTags.BEEHIVE_INHABITORS).add(EntityType.BEE);
        this.builder(EntityTypeTags.ARROWS).add(EntityType.ARROW, EntityType.SPECTRAL_ARROW);
        this.builder(EntityTypeTags.IMPACT_PROJECTILES).addTag(EntityTypeTags.ARROWS).add(EntityType.FIREWORK_ROCKET).add(EntityType.SNOWBALL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.EGG, EntityType.TRIDENT, EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL, EntityType.WIND_CHARGE, EntityType.BREEZE_WIND_CHARGE);
        this.builder(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH, EntityType.FOX);
        this.builder(EntityTypeTags.AXOLOTL_HUNT_TARGETS).add(EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.TADPOLE);
        this.builder(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES).add(EntityType.DROWNED, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN);
        this.builder(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(EntityType.STRAY, EntityType.POLAR_BEAR, EntityType.SNOW_GOLEM, EntityType.WITHER);
        this.builder(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES).add(EntityType.STRIDER, EntityType.BLAZE, EntityType.MAGMA_CUBE);
        this.builder(EntityTypeTags.CAN_BREATHE_UNDER_WATER).addTag(EntityTypeTags.UNDEAD).add(EntityType.AXOLOTL, EntityType.FROG, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.TURTLE, EntityType.GLOW_SQUID, EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.SQUID, EntityType.TROPICAL_FISH, EntityType.TADPOLE, EntityType.ARMOR_STAND, EntityType.COPPER_GOLEM, EntityType.NAUTILUS);
        this.builder(EntityTypeTags.FROG_FOOD).add(EntityType.SLIME, EntityType.MAGMA_CUBE);
        this.builder(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(EntityType.COPPER_GOLEM, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.SHULKER, EntityType.ALLAY, EntityType.BAT, EntityType.BEE, EntityType.BLAZE, EntityType.CAT, EntityType.CHICKEN, EntityType.GHAST, EntityType.HAPPY_GHAST, EntityType.PHANTOM, EntityType.MAGMA_CUBE, EntityType.OCELOT, EntityType.PARROT, EntityType.WITHER, EntityType.BREEZE);
        this.builder(EntityTypeTags.DISMOUNTS_UNDERWATER).add(EntityType.CAMEL, EntityType.CHICKEN, EntityType.DONKEY, EntityType.HAPPY_GHAST, EntityType.HORSE, EntityType.LLAMA, EntityType.MULE, EntityType.PIG, EntityType.RAVAGER, EntityType.SPIDER, EntityType.STRIDER, EntityType.TRADER_LLAMA, EntityType.ZOMBIE_HORSE);
        this.builder(EntityTypeTags.NON_CONTROLLING_RIDER).add(EntityType.SLIME, EntityType.MAGMA_CUBE);
        this.builder(EntityTypeTags.ILLAGER).add(EntityType.EVOKER).add(EntityType.ILLUSIONER).add(EntityType.PILLAGER).add(EntityType.VINDICATOR);
        this.builder(EntityTypeTags.AQUATIC).add(EntityType.TURTLE).add(EntityType.AXOLOTL).add(EntityType.GUARDIAN).add(EntityType.ELDER_GUARDIAN).add(EntityType.COD).add(EntityType.PUFFERFISH).add(EntityType.SALMON).add(EntityType.TROPICAL_FISH).add(EntityType.DOLPHIN).add(EntityType.SQUID).add(EntityType.GLOW_SQUID).add(EntityType.TADPOLE).add(EntityType.NAUTILUS).add(EntityType.ZOMBIE_NAUTILUS);
        this.builder(EntityTypeTags.ARTHROPOD).add(EntityType.BEE).add(EntityType.ENDERMITE).add(EntityType.SILVERFISH).add(EntityType.SPIDER).add(EntityType.CAVE_SPIDER);
        this.builder(EntityTypeTags.IGNORES_POISON_AND_REGEN).addTag(EntityTypeTags.UNDEAD);
        this.builder(EntityTypeTags.INVERTED_HEALING_AND_HARM).addTag(EntityTypeTags.UNDEAD);
        this.builder(EntityTypeTags.WITHER_FRIENDS).addTag(EntityTypeTags.UNDEAD);
        this.builder(EntityTypeTags.ILLAGER_FRIENDS).addTag(EntityTypeTags.ILLAGER);
        this.builder(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH).add(EntityType.TURTLE).add(EntityType.GUARDIAN).add(EntityType.ELDER_GUARDIAN).add(EntityType.COD).add(EntityType.PUFFERFISH).add(EntityType.SALMON).add(EntityType.TROPICAL_FISH).add(EntityType.DOLPHIN).add(EntityType.SQUID).add(EntityType.GLOW_SQUID).add(EntityType.TADPOLE).add(EntityType.NAUTILUS).add(EntityType.ZOMBIE_NAUTILUS);
        this.builder(EntityTypeTags.SENSITIVE_TO_IMPALING).addTag(EntityTypeTags.AQUATIC);
        this.builder(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS).addTag(EntityTypeTags.ARTHROPOD);
        this.builder(EntityTypeTags.SENSITIVE_TO_SMITE).addTag(EntityTypeTags.UNDEAD);
        this.builder(EntityTypeTags.REDIRECTABLE_PROJECTILE).add(EntityType.FIREBALL, EntityType.WIND_CHARGE, EntityType.BREEZE_WIND_CHARGE);
        this.builder(EntityTypeTags.DEFLECTS_PROJECTILES).add(EntityType.BREEZE);
        this.builder(EntityTypeTags.CAN_TURN_IN_BOATS).add(EntityType.BREEZE);
        this.builder(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE).add(EntityType.BREEZE, EntityType.SKELETON, EntityType.BOGGED, EntityType.STRAY, EntityType.ZOMBIE, EntityType.HUSK, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SLIME);
        this.builder(EntityTypeTags.IMMUNE_TO_INFESTED).add(EntityType.SILVERFISH);
        this.builder(EntityTypeTags.IMMUNE_TO_OOZING).add(EntityType.SLIME);
        this.builder(EntityTypeTags.BOAT).add(EntityType.OAK_BOAT, EntityType.SPRUCE_BOAT, EntityType.BIRCH_BOAT, EntityType.JUNGLE_BOAT, EntityType.ACACIA_BOAT, EntityType.CHERRY_BOAT, EntityType.DARK_OAK_BOAT, EntityType.PALE_OAK_BOAT, EntityType.MANGROVE_BOAT, EntityType.BAMBOO_RAFT);
        this.builder(EntityTypeTags.CAN_EQUIP_SADDLE).add(EntityType.HORSE, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.PIG, EntityType.STRIDER, EntityType.CAMEL, EntityType.CAMEL_HUSK, EntityType.NAUTILUS, EntityType.ZOMBIE_NAUTILUS);
        this.builder(EntityTypeTags.CAN_EQUIP_HARNESS).add(EntityType.HAPPY_GHAST);
        this.builder(EntityTypeTags.CAN_WEAR_HORSE_ARMOR).add(EntityType.HORSE).add(EntityType.ZOMBIE_HORSE);
        this.builder(EntityTypeTags.CAN_WEAR_NAUTILUS_ARMOR).add(EntityType.NAUTILUS, EntityType.ZOMBIE_NAUTILUS);
        this.builder(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS).add(EntityType.ARMADILLO, EntityType.BEE, EntityType.CAMEL, EntityType.CAT, EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.FOX, EntityType.GOAT, EntityType.HAPPY_GHAST, EntityType.HORSE, EntityType.SKELETON_HORSE, EntityType.LLAMA, EntityType.MULE, EntityType.OCELOT, EntityType.PANDA, EntityType.PARROT, EntityType.PIG, EntityType.POLAR_BEAR, EntityType.RABBIT, EntityType.SHEEP, EntityType.SNIFFER, EntityType.STRIDER, EntityType.VILLAGER, EntityType.WOLF);
        this.builder(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS).add(EntityType.PLAYER).add(EntityType.ELDER_GUARDIAN).add(EntityType.COD).add(EntityType.PUFFERFISH).add(EntityType.SALMON).add(EntityType.TROPICAL_FISH).add(EntityType.DOLPHIN).add(EntityType.SQUID).add(EntityType.GLOW_SQUID).add(EntityType.TADPOLE).add(EntityType.CREAKING).add(EntityType.NAUTILUS).add(EntityType.ZOMBIE_NAUTILUS);
        this.builder(EntityTypeTags.ACCEPTS_IRON_GOLEM_GIFT).add(EntityType.COPPER_GOLEM);
        this.builder(EntityTypeTags.CANDIDATE_FOR_IRON_GOLEM_GIFT).add(EntityType.VILLAGER).addTag(EntityTypeTags.ACCEPTS_IRON_GOLEM_GIFT);
        this.builder(EntityTypeTags.NAUTILUS_HOSTILES).add(EntityType.PUFFERFISH);
        this.builder(EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN).add(EntityType.HORSE, EntityType.ZOMBIE_HORSE, EntityType.MULE, EntityType.DONKEY, EntityType.CAMEL, EntityType.CAMEL_HUSK);
    }
}

