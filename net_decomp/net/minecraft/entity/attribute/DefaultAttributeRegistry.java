/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.entity.attribute;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.BoggedEntity;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.CreakingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ParchedEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.mob.ZombieNautilusEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.NautilusEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class DefaultAttributeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> DEFAULT_ATTRIBUTE_REGISTRY = ImmutableMap.builder().put(EntityType.ALLAY, (Object)AllayEntity.createAllayAttributes().build()).put(EntityType.ARMADILLO, (Object)ArmadilloEntity.createArmadilloAttributes().build()).put(EntityType.ARMOR_STAND, (Object)ArmorStandEntity.createArmorStandAttributes().build()).put(EntityType.AXOLOTL, (Object)AxolotlEntity.createAxolotlAttributes().build()).put(EntityType.BAT, (Object)BatEntity.createBatAttributes().build()).put(EntityType.BEE, (Object)BeeEntity.createBeeAttributes().build()).put(EntityType.BLAZE, (Object)BlazeEntity.createBlazeAttributes().build()).put(EntityType.BOGGED, (Object)BoggedEntity.createBoggedAttributes().build()).put(EntityType.CAT, (Object)CatEntity.createCatAttributes().build()).put(EntityType.CAMEL, (Object)CamelEntity.createCamelAttributes().build()).put(EntityType.CAMEL_HUSK, (Object)CamelEntity.createCamelAttributes().build()).put(EntityType.CAVE_SPIDER, (Object)CaveSpiderEntity.createCaveSpiderAttributes().build()).put(EntityType.CHICKEN, (Object)ChickenEntity.createChickenAttributes().build()).put(EntityType.COD, (Object)FishEntity.createFishAttributes().build()).put(EntityType.COPPER_GOLEM, (Object)CopperGolemEntity.createCopperGolemAttributes().build()).put(EntityType.COW, (Object)CowEntity.createCowAttributes().build()).put(EntityType.CREAKING, (Object)CreakingEntity.createCreakingAttributes().build()).put(EntityType.CREEPER, (Object)CreeperEntity.createCreeperAttributes().build()).put(EntityType.DOLPHIN, (Object)DolphinEntity.createDolphinAttributes().build()).put(EntityType.DONKEY, (Object)AbstractDonkeyEntity.createAbstractDonkeyAttributes().build()).put(EntityType.DROWNED, (Object)DrownedEntity.createDrownedAttributes().build()).put(EntityType.ELDER_GUARDIAN, (Object)ElderGuardianEntity.createElderGuardianAttributes().build()).put(EntityType.ENDERMAN, (Object)EndermanEntity.createEndermanAttributes().build()).put(EntityType.ENDERMITE, (Object)EndermiteEntity.createEndermiteAttributes().build()).put(EntityType.ENDER_DRAGON, (Object)EnderDragonEntity.createEnderDragonAttributes().build()).put(EntityType.EVOKER, (Object)EvokerEntity.createEvokerAttributes().build()).put(EntityType.BREEZE, (Object)BreezeEntity.createBreezeAttributes().build()).put(EntityType.FOX, (Object)FoxEntity.createFoxAttributes().build()).put(EntityType.FROG, (Object)FrogEntity.createFrogAttributes().build()).put(EntityType.GHAST, (Object)GhastEntity.createGhastAttributes().build()).put(EntityType.HAPPY_GHAST, (Object)HappyGhastEntity.createHappyGhastAttributes().build()).put(EntityType.GIANT, (Object)GiantEntity.createGiantAttributes().build()).put(EntityType.GLOW_SQUID, (Object)GlowSquidEntity.createSquidAttributes().build()).put(EntityType.GOAT, (Object)GoatEntity.createGoatAttributes().build()).put(EntityType.GUARDIAN, (Object)GuardianEntity.createGuardianAttributes().build()).put(EntityType.HOGLIN, (Object)HoglinEntity.createHoglinAttributes().build()).put(EntityType.HORSE, (Object)AbstractHorseEntity.createBaseHorseAttributes().build()).put(EntityType.HUSK, (Object)ZombieEntity.createZombieAttributes().build()).put(EntityType.ILLUSIONER, (Object)IllusionerEntity.createIllusionerAttributes().build()).put(EntityType.IRON_GOLEM, (Object)IronGolemEntity.createIronGolemAttributes().build()).put(EntityType.LLAMA, (Object)LlamaEntity.createLlamaAttributes().build()).put(EntityType.MAGMA_CUBE, (Object)MagmaCubeEntity.createMagmaCubeAttributes().build()).put(EntityType.MANNEQUIN, (Object)LivingEntity.createLivingAttributes().build()).put(EntityType.MOOSHROOM, (Object)CowEntity.createCowAttributes().build()).put(EntityType.MULE, (Object)AbstractDonkeyEntity.createAbstractDonkeyAttributes().build()).put(EntityType.NAUTILUS, (Object)NautilusEntity.createNautilusAttributes().build()).put(EntityType.OCELOT, (Object)OcelotEntity.createOcelotAttributes().build()).put(EntityType.PANDA, (Object)PandaEntity.createPandaAttributes().build()).put(EntityType.PARCHED, (Object)ParchedEntity.createParchedAttributes().build()).put(EntityType.PARROT, (Object)ParrotEntity.createParrotAttributes().build()).put(EntityType.PHANTOM, (Object)HostileEntity.createHostileAttributes().build()).put(EntityType.PIG, (Object)PigEntity.createPigAttributes().build()).put(EntityType.PIGLIN, (Object)PiglinEntity.createPiglinAttributes().build()).put(EntityType.PIGLIN_BRUTE, (Object)PiglinBruteEntity.createPiglinBruteAttributes().build()).put(EntityType.PILLAGER, (Object)PillagerEntity.createPillagerAttributes().build()).put(EntityType.PLAYER, (Object)PlayerEntity.createPlayerAttributes().build()).put(EntityType.POLAR_BEAR, (Object)PolarBearEntity.createPolarBearAttributes().build()).put(EntityType.PUFFERFISH, (Object)FishEntity.createFishAttributes().build()).put(EntityType.RABBIT, (Object)RabbitEntity.createRabbitAttributes().build()).put(EntityType.RAVAGER, (Object)RavagerEntity.createRavagerAttributes().build()).put(EntityType.SALMON, (Object)FishEntity.createFishAttributes().build()).put(EntityType.SHEEP, (Object)SheepEntity.createSheepAttributes().build()).put(EntityType.SHULKER, (Object)ShulkerEntity.createShulkerAttributes().build()).put(EntityType.SILVERFISH, (Object)SilverfishEntity.createSilverfishAttributes().build()).put(EntityType.SKELETON, (Object)AbstractSkeletonEntity.createAbstractSkeletonAttributes().build()).put(EntityType.SKELETON_HORSE, (Object)SkeletonHorseEntity.createSkeletonHorseAttributes().build()).put(EntityType.SLIME, (Object)HostileEntity.createHostileAttributes().build()).put(EntityType.SNIFFER, (Object)SnifferEntity.createSnifferAttributes().build()).put(EntityType.SNOW_GOLEM, (Object)SnowGolemEntity.createSnowGolemAttributes().build()).put(EntityType.SPIDER, (Object)SpiderEntity.createSpiderAttributes().build()).put(EntityType.SQUID, (Object)SquidEntity.createSquidAttributes().build()).put(EntityType.STRAY, (Object)AbstractSkeletonEntity.createAbstractSkeletonAttributes().build()).put(EntityType.STRIDER, (Object)StriderEntity.createStriderAttributes().build()).put(EntityType.TADPOLE, (Object)TadpoleEntity.createTadpoleAttributes().build()).put(EntityType.TRADER_LLAMA, (Object)LlamaEntity.createLlamaAttributes().build()).put(EntityType.TROPICAL_FISH, (Object)FishEntity.createFishAttributes().build()).put(EntityType.TURTLE, (Object)TurtleEntity.createTurtleAttributes().build()).put(EntityType.VEX, (Object)VexEntity.createVexAttributes().build()).put(EntityType.VILLAGER, (Object)VillagerEntity.createVillagerAttributes().build()).put(EntityType.VINDICATOR, (Object)VindicatorEntity.createVindicatorAttributes().build()).put(EntityType.WARDEN, (Object)WardenEntity.addAttributes().build()).put(EntityType.WANDERING_TRADER, (Object)MobEntity.createMobAttributes().build()).put(EntityType.WITCH, (Object)WitchEntity.createWitchAttributes().build()).put(EntityType.WITHER, (Object)WitherEntity.createWitherAttributes().build()).put(EntityType.WITHER_SKELETON, (Object)AbstractSkeletonEntity.createAbstractSkeletonAttributes().build()).put(EntityType.WOLF, (Object)WolfEntity.createWolfAttributes().build()).put(EntityType.ZOGLIN, (Object)ZoglinEntity.createZoglinAttributes().build()).put(EntityType.ZOMBIE, (Object)ZombieEntity.createZombieAttributes().build()).put(EntityType.ZOMBIE_HORSE, (Object)ZombieHorseEntity.createZombieHorseAttributes().build()).put(EntityType.ZOMBIE_NAUTILUS, (Object)ZombieNautilusEntity.createZombieNautilusAttributes().build()).put(EntityType.ZOMBIE_VILLAGER, (Object)ZombieEntity.createZombieAttributes().build()).put(EntityType.ZOMBIFIED_PIGLIN, (Object)ZombifiedPiglinEntity.createZombifiedPiglinAttributes().build()).build();

    public static DefaultAttributeContainer get(EntityType<? extends LivingEntity> type) {
        return DEFAULT_ATTRIBUTE_REGISTRY.get(type);
    }

    public static boolean hasDefinitionFor(EntityType<?> type) {
        return DEFAULT_ATTRIBUTE_REGISTRY.containsKey(type);
    }

    public static void checkMissing() {
        Registries.ENTITY_TYPE.stream().filter(entityType -> entityType.getSpawnGroup() != SpawnGroup.MISC).filter(entityType -> !DefaultAttributeRegistry.hasDefinitionFor(entityType)).map(Registries.ENTITY_TYPE::getId).forEach(id -> Util.logErrorOrPause("Entity " + String.valueOf(id) + " has no attributes"));
    }
}

