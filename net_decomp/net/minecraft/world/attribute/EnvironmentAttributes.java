/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.MoonPhase;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.AttributeValidator;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeTypes;

public interface EnvironmentAttributes {
    public static final EnvironmentAttribute<Integer> FOG_COLOR_VISUAL = EnvironmentAttributes.register("visual/fog_color", EnvironmentAttribute.builder(EnvironmentAttributeTypes.RGB_COLOR).defaultValue(0).interpolated().synced());
    public static final EnvironmentAttribute<Float> FOG_START_DISTANCE_VISUAL = EnvironmentAttributes.register("visual/fog_start_distance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).interpolated().synced());
    public static final EnvironmentAttribute<Float> FOG_END_DISTANCE_VISUAL = EnvironmentAttributes.register("visual/fog_end_distance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(1024.0f)).validator(AttributeValidator.NON_NEGATIVE_FLOAT).interpolated().synced());
    public static final EnvironmentAttribute<Float> SKY_FOG_END_DISTANCE_VISUAL = EnvironmentAttributes.register("visual/sky_fog_end_distance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(512.0f)).validator(AttributeValidator.NON_NEGATIVE_FLOAT).interpolated().synced());
    public static final EnvironmentAttribute<Float> CLOUD_FOG_END_DISTANCE_VISUAL = EnvironmentAttributes.register("visual/cloud_fog_end_distance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(2048.0f)).validator(AttributeValidator.NON_NEGATIVE_FLOAT).interpolated().synced());
    public static final EnvironmentAttribute<Integer> WATER_FOG_COLOR_VISUAL = EnvironmentAttributes.register("visual/water_fog_color", EnvironmentAttribute.builder(EnvironmentAttributeTypes.RGB_COLOR).defaultValue(-16448205).interpolated().synced());
    public static final EnvironmentAttribute<Float> WATER_FOG_START_DISTANCE_VISUAL = EnvironmentAttributes.register("visual/water_fog_start_distance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(-8.0f)).interpolated().synced());
    public static final EnvironmentAttribute<Float> WATER_FOG_END_DISTANCE_VISUAL = EnvironmentAttributes.register("visual/water_fog_end_distance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(96.0f)).validator(AttributeValidator.NON_NEGATIVE_FLOAT).interpolated().synced());
    public static final EnvironmentAttribute<Integer> SKY_COLOR_VISUAL = EnvironmentAttributes.register("visual/sky_color", EnvironmentAttribute.builder(EnvironmentAttributeTypes.RGB_COLOR).defaultValue(0).interpolated().synced());
    public static final EnvironmentAttribute<Integer> SUNRISE_SUNSET_COLOR_VISUAL = EnvironmentAttributes.register("visual/sunrise_sunset_color", EnvironmentAttribute.builder(EnvironmentAttributeTypes.ARGB_COLOR).defaultValue(0).interpolated().synced());
    public static final EnvironmentAttribute<Integer> CLOUD_COLOR_VISUAL = EnvironmentAttributes.register("visual/cloud_color", EnvironmentAttribute.builder(EnvironmentAttributeTypes.ARGB_COLOR).defaultValue(0).interpolated().synced());
    public static final EnvironmentAttribute<Float> CLOUD_HEIGHT_VISUAL = EnvironmentAttributes.register("visual/cloud_height", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(192.33f)).interpolated().synced());
    public static final EnvironmentAttribute<Float> SUN_ANGLE_VISUAL = EnvironmentAttributes.register("visual/sun_angle", EnvironmentAttribute.builder(EnvironmentAttributeTypes.ANGLE_DEGREES).defaultValue(Float.valueOf(0.0f)).interpolated().synced());
    public static final EnvironmentAttribute<Float> MOON_ANGLE_VISUAL = EnvironmentAttributes.register("visual/moon_angle", EnvironmentAttribute.builder(EnvironmentAttributeTypes.ANGLE_DEGREES).defaultValue(Float.valueOf(0.0f)).interpolated().synced());
    public static final EnvironmentAttribute<Float> STAR_ANGLE_VISUAL = EnvironmentAttributes.register("visual/star_angle", EnvironmentAttribute.builder(EnvironmentAttributeTypes.ANGLE_DEGREES).defaultValue(Float.valueOf(0.0f)).interpolated().synced());
    public static final EnvironmentAttribute<MoonPhase> MOON_PHASE_VISUAL = EnvironmentAttributes.register("visual/moon_phase", EnvironmentAttribute.builder(EnvironmentAttributeTypes.MOON_PHASE).defaultValue(MoonPhase.FULL_MOON).synced());
    public static final EnvironmentAttribute<Float> STAR_BRIGHTNESS_VISUAL = EnvironmentAttributes.register("visual/star_brightness", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).validator(AttributeValidator.PROBABILITY).interpolated().synced());
    public static final EnvironmentAttribute<Integer> SKY_LIGHT_COLOR_VISUAL = EnvironmentAttributes.register("visual/sky_light_color", EnvironmentAttribute.builder(EnvironmentAttributeTypes.RGB_COLOR).defaultValue(-1).interpolated().synced());
    public static final EnvironmentAttribute<Float> SKY_LIGHT_FACTOR_VISUAL = EnvironmentAttributes.register("visual/sky_light_factor", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(1.0f)).validator(AttributeValidator.PROBABILITY).interpolated().synced());
    public static final EnvironmentAttribute<ParticleEffect> DEFAULT_DRIPSTONE_PARTICLE_VISUAL = EnvironmentAttributes.register("visual/default_dripstone_particle", EnvironmentAttribute.builder(EnvironmentAttributeTypes.PARTICLE).defaultValue(ParticleTypes.DRIPPING_DRIPSTONE_WATER).synced());
    public static final EnvironmentAttribute<List<AmbientParticle>> AMBIENT_PARTICLES_VISUAL = EnvironmentAttributes.register("visual/ambient_particles", EnvironmentAttribute.builder(EnvironmentAttributeTypes.AMBIENT_PARTICLES).defaultValue(List.of()).synced());
    public static final EnvironmentAttribute<BackgroundMusic> BACKGROUND_MUSIC_AUDIO = EnvironmentAttributes.register("audio/background_music", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BACKGROUND_MUSIC).defaultValue(BackgroundMusic.EMPTY).synced());
    public static final EnvironmentAttribute<Float> MUSIC_VOLUME_AUDIO = EnvironmentAttributes.register("audio/music_volume", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(1.0f)).validator(AttributeValidator.PROBABILITY).synced());
    public static final EnvironmentAttribute<AmbientSounds> AMBIENT_SOUNDS_AUDIO = EnvironmentAttributes.register("audio/ambient_sounds", EnvironmentAttribute.builder(EnvironmentAttributeTypes.AMBIENT_SOUNDS).defaultValue(AmbientSounds.DEFAULT).synced());
    public static final EnvironmentAttribute<Boolean> FIREFLY_BUSH_SOUNDS_AUDIO = EnvironmentAttributes.register("audio/firefly_bush_sounds", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false).synced());
    public static final EnvironmentAttribute<Float> SKY_LIGHT_LEVEL_GAMEPLAY = EnvironmentAttributes.register("gameplay/sky_light_level", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(15.0f)).validator(AttributeValidator.ranged(0.0f, 15.0f)).global().synced());
    public static final EnvironmentAttribute<Boolean> CAN_START_RAID_GAMEPLAY = EnvironmentAttributes.register("gameplay/can_start_raid", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(true));
    public static final EnvironmentAttribute<Boolean> WATER_EVAPORATES_GAMEPLAY = EnvironmentAttributes.register("gameplay/water_evaporates", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false).synced());
    public static final EnvironmentAttribute<BedRule> BED_RULE_GAMEPLAY = EnvironmentAttributes.register("gameplay/bed_rule", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BED_RULE).defaultValue(BedRule.OVERWORLD));
    public static final EnvironmentAttribute<Boolean> RESPAWN_ANCHOR_WORKS_GAMEPLAY = EnvironmentAttributes.register("gameplay/respawn_anchor_works", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> NETHER_PORTAL_SPAWNS_PIGLIN_GAMEPLAY = EnvironmentAttributes.register("gameplay/nether_portal_spawns_piglin", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> FAST_LAVA_GAMEPLAY = EnvironmentAttributes.register("gameplay/fast_lava", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false).global().synced());
    public static final EnvironmentAttribute<Boolean> INCREASED_FIRE_BURNOUT_GAMEPLAY = EnvironmentAttributes.register("gameplay/increased_fire_burnout", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<TriState> EYEBLOSSOM_OPEN_GAMEPLAY = EnvironmentAttributes.register("gameplay/eyeblossom_open", EnvironmentAttribute.builder(EnvironmentAttributeTypes.TRI_STATE).defaultValue(TriState.DEFAULT));
    public static final EnvironmentAttribute<Float> TURTLE_EGG_HATCH_CHANCE_GAMEPLAY = EnvironmentAttributes.register("gameplay/turtle_egg_hatch_chance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).validator(AttributeValidator.PROBABILITY));
    public static final EnvironmentAttribute<Boolean> PIGLINS_ZOMBIFY_GAMEPLAY = EnvironmentAttributes.register("gameplay/piglins_zombify", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(true).synced());
    public static final EnvironmentAttribute<Boolean> SNOW_GOLEM_MELTS_GAMEPLAY = EnvironmentAttributes.register("gameplay/snow_golem_melts", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> CREAKING_ACTIVE_GAMEPLAY = EnvironmentAttributes.register("gameplay/creaking_active", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false).synced());
    public static final EnvironmentAttribute<Float> SURFACE_SLIME_SPAWN_CHANCE_GAMEPLAY = EnvironmentAttributes.register("gameplay/surface_slime_spawn_chance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).validator(AttributeValidator.PROBABILITY));
    public static final EnvironmentAttribute<Float> CAT_WAKING_UP_GIFT_CHANCE_GAMEPLAY = EnvironmentAttributes.register("gameplay/cat_waking_up_gift_chance", EnvironmentAttribute.builder(EnvironmentAttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).validator(AttributeValidator.PROBABILITY));
    public static final EnvironmentAttribute<Boolean> BEES_STAY_IN_HIVE_GAMEPLAY = EnvironmentAttributes.register("gameplay/bees_stay_in_hive", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> MONSTERS_BURN_GAMEPLAY = EnvironmentAttributes.register("gameplay/monsters_burn", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> CAN_PILLAGER_PATROL_SPAWN_GAMEPLAY = EnvironmentAttributes.register("gameplay/can_pillager_patrol_spawn", EnvironmentAttribute.builder(EnvironmentAttributeTypes.BOOLEAN).defaultValue(true));
    public static final EnvironmentAttribute<Activity> VILLAGER_ACTIVITY_GAMEPLAY = EnvironmentAttributes.register("gameplay/villager_activity", EnvironmentAttribute.builder(EnvironmentAttributeTypes.ACTIVITY).defaultValue(Activity.IDLE));
    public static final EnvironmentAttribute<Activity> BABY_VILLAGER_ACTIVITY_GAMEPLAY = EnvironmentAttributes.register("gameplay/baby_villager_activity", EnvironmentAttribute.builder(EnvironmentAttributeTypes.ACTIVITY).defaultValue(Activity.IDLE));
    public static final Codec<EnvironmentAttribute<?>> CODEC = Registries.ENVIRONMENTAL_ATTRIBUTE.getCodec();

    public static EnvironmentAttribute<?> registerAndGetDefault(Registry<EnvironmentAttribute<?>> registry) {
        return RESPAWN_ANCHOR_WORKS_GAMEPLAY;
    }

    private static <Value> EnvironmentAttribute<Value> register(String path, EnvironmentAttribute.Builder<Value> builder) {
        EnvironmentAttribute<Value> environmentAttribute = builder.build();
        Registry.register(Registries.ENVIRONMENTAL_ATTRIBUTE, Identifier.ofVanilla(path), environmentAttribute);
        return environmentAttribute;
    }
}

