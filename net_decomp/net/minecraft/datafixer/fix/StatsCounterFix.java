/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import net.minecraft.datafixer.schema.Schema1451v6;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class StatsCounterFix
extends DataFix {
    private static final Set<String> SKIPPED_STATS = Set.of("dummy", "trigger", "deathCount", "playerKillCount", "totalKillCount", "health", "food", "air", "armor", "xp", "level", "killedByTeam.aqua", "killedByTeam.black", "killedByTeam.blue", "killedByTeam.dark_aqua", "killedByTeam.dark_blue", "killedByTeam.dark_gray", "killedByTeam.dark_green", "killedByTeam.dark_purple", "killedByTeam.dark_red", "killedByTeam.gold", "killedByTeam.gray", "killedByTeam.green", "killedByTeam.light_purple", "killedByTeam.red", "killedByTeam.white", "killedByTeam.yellow", "teamkill.aqua", "teamkill.black", "teamkill.blue", "teamkill.dark_aqua", "teamkill.dark_blue", "teamkill.dark_gray", "teamkill.dark_green", "teamkill.dark_purple", "teamkill.dark_red", "teamkill.gold", "teamkill.gray", "teamkill.green", "teamkill.light_purple", "teamkill.red", "teamkill.white", "teamkill.yellow");
    private static final Set<String> REMOVED_STATS = ImmutableSet.builder().add((Object)"stat.craftItem.minecraft.spawn_egg").add((Object)"stat.useItem.minecraft.spawn_egg").add((Object)"stat.breakItem.minecraft.spawn_egg").add((Object)"stat.pickup.minecraft.spawn_egg").add((Object)"stat.drop.minecraft.spawn_egg").build();
    private static final Map<String, String> RENAMED_GENERAL_STATS = ImmutableMap.builder().put((Object)"stat.leaveGame", (Object)"minecraft:leave_game").put((Object)"stat.playOneMinute", (Object)"minecraft:play_one_minute").put((Object)"stat.timeSinceDeath", (Object)"minecraft:time_since_death").put((Object)"stat.sneakTime", (Object)"minecraft:sneak_time").put((Object)"stat.walkOneCm", (Object)"minecraft:walk_one_cm").put((Object)"stat.crouchOneCm", (Object)"minecraft:crouch_one_cm").put((Object)"stat.sprintOneCm", (Object)"minecraft:sprint_one_cm").put((Object)"stat.swimOneCm", (Object)"minecraft:swim_one_cm").put((Object)"stat.fallOneCm", (Object)"minecraft:fall_one_cm").put((Object)"stat.climbOneCm", (Object)"minecraft:climb_one_cm").put((Object)"stat.flyOneCm", (Object)"minecraft:fly_one_cm").put((Object)"stat.diveOneCm", (Object)"minecraft:dive_one_cm").put((Object)"stat.minecartOneCm", (Object)"minecraft:minecart_one_cm").put((Object)"stat.boatOneCm", (Object)"minecraft:boat_one_cm").put((Object)"stat.pigOneCm", (Object)"minecraft:pig_one_cm").put((Object)"stat.horseOneCm", (Object)"minecraft:horse_one_cm").put((Object)"stat.aviateOneCm", (Object)"minecraft:aviate_one_cm").put((Object)"stat.jump", (Object)"minecraft:jump").put((Object)"stat.drop", (Object)"minecraft:drop").put((Object)"stat.damageDealt", (Object)"minecraft:damage_dealt").put((Object)"stat.damageTaken", (Object)"minecraft:damage_taken").put((Object)"stat.deaths", (Object)"minecraft:deaths").put((Object)"stat.mobKills", (Object)"minecraft:mob_kills").put((Object)"stat.animalsBred", (Object)"minecraft:animals_bred").put((Object)"stat.playerKills", (Object)"minecraft:player_kills").put((Object)"stat.fishCaught", (Object)"minecraft:fish_caught").put((Object)"stat.talkedToVillager", (Object)"minecraft:talked_to_villager").put((Object)"stat.tradedWithVillager", (Object)"minecraft:traded_with_villager").put((Object)"stat.cakeSlicesEaten", (Object)"minecraft:eat_cake_slice").put((Object)"stat.cauldronFilled", (Object)"minecraft:fill_cauldron").put((Object)"stat.cauldronUsed", (Object)"minecraft:use_cauldron").put((Object)"stat.armorCleaned", (Object)"minecraft:clean_armor").put((Object)"stat.bannerCleaned", (Object)"minecraft:clean_banner").put((Object)"stat.brewingstandInteraction", (Object)"minecraft:interact_with_brewingstand").put((Object)"stat.beaconInteraction", (Object)"minecraft:interact_with_beacon").put((Object)"stat.dropperInspected", (Object)"minecraft:inspect_dropper").put((Object)"stat.hopperInspected", (Object)"minecraft:inspect_hopper").put((Object)"stat.dispenserInspected", (Object)"minecraft:inspect_dispenser").put((Object)"stat.noteblockPlayed", (Object)"minecraft:play_noteblock").put((Object)"stat.noteblockTuned", (Object)"minecraft:tune_noteblock").put((Object)"stat.flowerPotted", (Object)"minecraft:pot_flower").put((Object)"stat.trappedChestTriggered", (Object)"minecraft:trigger_trapped_chest").put((Object)"stat.enderchestOpened", (Object)"minecraft:open_enderchest").put((Object)"stat.itemEnchanted", (Object)"minecraft:enchant_item").put((Object)"stat.recordPlayed", (Object)"minecraft:play_record").put((Object)"stat.furnaceInteraction", (Object)"minecraft:interact_with_furnace").put((Object)"stat.craftingTableInteraction", (Object)"minecraft:interact_with_crafting_table").put((Object)"stat.chestOpened", (Object)"minecraft:open_chest").put((Object)"stat.sleepInBed", (Object)"minecraft:sleep_in_bed").put((Object)"stat.shulkerBoxOpened", (Object)"minecraft:open_shulker_box").build();
    private static final String OLD_MINE_BLOCK_ID = "stat.mineBlock";
    private static final String NEW_MINE_BLOCK_ID = "minecraft:mined";
    private static final Map<String, String> RENAMED_ITEM_STATS = ImmutableMap.builder().put((Object)"stat.craftItem", (Object)"minecraft:crafted").put((Object)"stat.useItem", (Object)"minecraft:used").put((Object)"stat.breakItem", (Object)"minecraft:broken").put((Object)"stat.pickup", (Object)"minecraft:picked_up").put((Object)"stat.drop", (Object)"minecraft:dropped").build();
    private static final Map<String, String> RENAMED_ENTITY_STATS = ImmutableMap.builder().put((Object)"stat.entityKilledBy", (Object)"minecraft:killed_by").put((Object)"stat.killEntity", (Object)"minecraft:killed").build();
    private static final Map<String, String> RENAMED_ENTITIES = ImmutableMap.builder().put((Object)"Bat", (Object)"minecraft:bat").put((Object)"Blaze", (Object)"minecraft:blaze").put((Object)"CaveSpider", (Object)"minecraft:cave_spider").put((Object)"Chicken", (Object)"minecraft:chicken").put((Object)"Cow", (Object)"minecraft:cow").put((Object)"Creeper", (Object)"minecraft:creeper").put((Object)"Donkey", (Object)"minecraft:donkey").put((Object)"ElderGuardian", (Object)"minecraft:elder_guardian").put((Object)"Enderman", (Object)"minecraft:enderman").put((Object)"Endermite", (Object)"minecraft:endermite").put((Object)"EvocationIllager", (Object)"minecraft:evocation_illager").put((Object)"Ghast", (Object)"minecraft:ghast").put((Object)"Guardian", (Object)"minecraft:guardian").put((Object)"Horse", (Object)"minecraft:horse").put((Object)"Husk", (Object)"minecraft:husk").put((Object)"Llama", (Object)"minecraft:llama").put((Object)"LavaSlime", (Object)"minecraft:magma_cube").put((Object)"MushroomCow", (Object)"minecraft:mooshroom").put((Object)"Mule", (Object)"minecraft:mule").put((Object)"Ozelot", (Object)"minecraft:ocelot").put((Object)"Parrot", (Object)"minecraft:parrot").put((Object)"Pig", (Object)"minecraft:pig").put((Object)"PolarBear", (Object)"minecraft:polar_bear").put((Object)"Rabbit", (Object)"minecraft:rabbit").put((Object)"Sheep", (Object)"minecraft:sheep").put((Object)"Shulker", (Object)"minecraft:shulker").put((Object)"Silverfish", (Object)"minecraft:silverfish").put((Object)"SkeletonHorse", (Object)"minecraft:skeleton_horse").put((Object)"Skeleton", (Object)"minecraft:skeleton").put((Object)"Slime", (Object)"minecraft:slime").put((Object)"Spider", (Object)"minecraft:spider").put((Object)"Squid", (Object)"minecraft:squid").put((Object)"Stray", (Object)"minecraft:stray").put((Object)"Vex", (Object)"minecraft:vex").put((Object)"Villager", (Object)"minecraft:villager").put((Object)"VindicationIllager", (Object)"minecraft:vindication_illager").put((Object)"Witch", (Object)"minecraft:witch").put((Object)"WitherSkeleton", (Object)"minecraft:wither_skeleton").put((Object)"Wolf", (Object)"minecraft:wolf").put((Object)"ZombieHorse", (Object)"minecraft:zombie_horse").put((Object)"PigZombie", (Object)"minecraft:zombie_pigman").put((Object)"ZombieVillager", (Object)"minecraft:zombie_villager").put((Object)"Zombie", (Object)"minecraft:zombie").build();
    private static final String CUSTOM = "minecraft:custom";

    public StatsCounterFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    private static @Nullable Stat rename(String old) {
        if (REMOVED_STATS.contains(old)) {
            return null;
        }
        String string = RENAMED_GENERAL_STATS.get(old);
        if (string != null) {
            return new Stat(CUSTOM, string);
        }
        int i = StringUtils.ordinalIndexOf((CharSequence)old, (CharSequence)".", (int)2);
        if (i < 0) {
            return null;
        }
        String string2 = old.substring(0, i);
        if (OLD_MINE_BLOCK_ID.equals(string2)) {
            String string3 = StatsCounterFix.getBlock(old.substring(i + 1).replace('.', ':'));
            return new Stat(NEW_MINE_BLOCK_ID, string3);
        }
        String string3 = RENAMED_ITEM_STATS.get(string2);
        if (string3 != null) {
            String string4 = old.substring(i + 1).replace('.', ':');
            String string5 = StatsCounterFix.getItem(string4);
            String string6 = string5 == null ? string4 : string5;
            return new Stat(string3, string6);
        }
        String string4 = RENAMED_ENTITY_STATS.get(string2);
        if (string4 != null) {
            String string5 = old.substring(i + 1).replace('.', ':');
            String string6 = RENAMED_ENTITIES.getOrDefault(string5, string5);
            return new Stat(string4, string6);
        }
        return null;
    }

    public TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.makeFirstRoundRule(), (TypeRewriteRule)this.makeSecondRoundRule());
    }

    private TypeRewriteRule makeFirstRoundRule() {
        Type type = this.getInputSchema().getType(TypeReferences.STATS);
        Type type2 = this.getOutputSchema().getType(TypeReferences.STATS);
        return this.fixTypeEverywhereTyped("StatsCounterFix", type, type2, statsTyped -> {
            Dynamic dynamic = (Dynamic)statsTyped.get(DSL.remainderFinder());
            HashMap map = Maps.newHashMap();
            Optional optional = dynamic.getMapValues().result();
            if (optional.isPresent()) {
                for (Map.Entry entry : ((Map)optional.get()).entrySet()) {
                    String string;
                    Stat stat;
                    if (!((Dynamic)entry.getValue()).asNumber().result().isPresent() || (stat = StatsCounterFix.rename(string = ((Dynamic)entry.getKey()).asString(""))) == null) continue;
                    Dynamic dynamic22 = dynamic.createString(stat.type());
                    Dynamic dynamic3 = map.computeIfAbsent(dynamic22, dynamic2 -> dynamic.emptyMap());
                    map.put(dynamic22, dynamic3.set(stat.typeKey(), (Dynamic)entry.getValue()));
                }
            }
            return Util.readTyped(type2, dynamic.emptyMap().set("stats", dynamic.createMap((Map)map)));
        });
    }

    private TypeRewriteRule makeSecondRoundRule() {
        Type type = this.getInputSchema().getType(TypeReferences.OBJECTIVE);
        Type type2 = this.getOutputSchema().getType(TypeReferences.OBJECTIVE);
        return this.fixTypeEverywhereTyped("ObjectiveStatFix", type, type2, objectiveTyped -> {
            Dynamic dynamic = (Dynamic)objectiveTyped.get(DSL.remainderFinder());
            Dynamic dynamic2 = dynamic.update("CriteriaName", criteriaNameDynamic -> (Dynamic)DataFixUtils.orElse(criteriaNameDynamic.asString().result().map(criteriaName -> {
                if (SKIPPED_STATS.contains(criteriaName)) {
                    return criteriaName;
                }
                Stat stat = StatsCounterFix.rename(criteriaName);
                if (stat == null) {
                    return "dummy";
                }
                return Schema1451v6.toDotSeparated(stat.type) + ":" + Schema1451v6.toDotSeparated(stat.typeKey);
            }).map(arg_0 -> ((Dynamic)criteriaNameDynamic).createString(arg_0)), (Object)criteriaNameDynamic));
            return Util.readTyped(type2, dynamic2);
        });
    }

    private static @Nullable String getItem(String id) {
        return ItemInstanceTheFlatteningFix.getItem(id, 0);
    }

    private static String getBlock(String id) {
        return BlockStateFlattening.lookupBlock(id);
    }

    static final class Stat
    extends Record {
        final String type;
        final String typeKey;

        Stat(String type, String typeKey) {
            this.type = type;
            this.typeKey = typeKey;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Stat.class, "type;typeKey", "type", "typeKey"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Stat.class, "type;typeKey", "type", "typeKey"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Stat.class, "type;typeKey", "type", "typeKey"}, this, object);
        }

        public String type() {
            return this.type;
        }

        public String typeKey() {
            return this.typeKey;
        }
    }
}

