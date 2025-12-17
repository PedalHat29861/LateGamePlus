/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  org.slf4j.Logger
 */
package net.minecraft.datafixer.fix;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class TrialSpawnerConfigInRegistryFix
extends ChoiceFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public TrialSpawnerConfigInRegistryFix(Schema outputSchema) {
        super(outputSchema, false, "TrialSpawnerConfigInRegistryFix", TypeReferences.BLOCK_ENTITY, "minecraft:trial_spawner");
    }

    public Dynamic<?> fix(Dynamic<NbtElement> nbt) {
        Optional optional = nbt.get("normal_config").result();
        if (optional.isEmpty()) {
            return nbt;
        }
        Optional optional2 = nbt.get("ominous_config").result();
        if (optional2.isEmpty()) {
            return nbt;
        }
        Identifier identifier = Replacements.REPLACEMENTS.get(Pair.of((Object)((Dynamic)optional.get()), (Object)((Dynamic)optional2.get())));
        if (identifier == null) {
            return nbt;
        }
        return nbt.set("normal_config", nbt.createString(identifier.withSuffixedPath("/normal").toString())).set("ominous_config", nbt.createString(identifier.withSuffixedPath("/ominous").toString()));
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), dynamic -> {
            DynamicOps dynamicOps = dynamic.getOps();
            Dynamic<?> dynamic2 = this.fix((Dynamic<NbtElement>)dynamic.convert((DynamicOps)NbtOps.INSTANCE));
            return dynamic2.convert(dynamicOps);
        });
    }

    static final class Replacements {
        public static final Map<Pair<Dynamic<NbtElement>, Dynamic<NbtElement>>, Identifier> REPLACEMENTS = new HashMap<Pair<Dynamic<NbtElement>, Dynamic<NbtElement>>, Identifier>();

        private Replacements() {
        }

        private static void register(Identifier id, String normal, String ominous) {
            try {
                NbtCompound nbtCompound = Replacements.parse(normal);
                NbtCompound nbtCompound2 = Replacements.parse(ominous);
                NbtCompound nbtCompound3 = nbtCompound.copy().copyFrom(nbtCompound2);
                NbtCompound nbtCompound4 = Replacements.removeDefaults(nbtCompound3.copy());
                Dynamic<NbtElement> dynamic = Replacements.toDynamic(nbtCompound);
                REPLACEMENTS.put((Pair<Dynamic<NbtElement>, Dynamic<NbtElement>>)Pair.of(dynamic, Replacements.toDynamic(nbtCompound2)), id);
                REPLACEMENTS.put((Pair<Dynamic<NbtElement>, Dynamic<NbtElement>>)Pair.of(dynamic, Replacements.toDynamic(nbtCompound3)), id);
                REPLACEMENTS.put((Pair<Dynamic<NbtElement>, Dynamic<NbtElement>>)Pair.of(dynamic, Replacements.toDynamic(nbtCompound4)), id);
            }
            catch (RuntimeException runtimeException) {
                throw new IllegalStateException("Failed to parse NBT for " + String.valueOf(id), runtimeException);
            }
        }

        private static Dynamic<NbtElement> toDynamic(NbtCompound nbt) {
            return new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)nbt);
        }

        private static NbtCompound parse(String nbt) {
            try {
                return StringNbtReader.readCompound(nbt);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException("Failed to parse Trial Spawner NBT config: " + nbt, commandSyntaxException);
            }
        }

        private static NbtCompound removeDefaults(NbtCompound nbt) {
            if (nbt.getInt("spawn_range", 0) == 4) {
                nbt.remove("spawn_range");
            }
            if (nbt.getFloat("total_mobs", 0.0f) == 6.0f) {
                nbt.remove("total_mobs");
            }
            if (nbt.getFloat("simultaneous_mobs", 0.0f) == 2.0f) {
                nbt.remove("simultaneous_mobs");
            }
            if (nbt.getFloat("total_mobs_added_per_player", 0.0f) == 2.0f) {
                nbt.remove("total_mobs_added_per_player");
            }
            if (nbt.getFloat("simultaneous_mobs_added_per_player", 0.0f) == 1.0f) {
                nbt.remove("simultaneous_mobs_added_per_player");
            }
            if (nbt.getInt("ticks_between_spawn", 0) == 40) {
                nbt.remove("ticks_between_spawn");
            }
            return nbt;
        }

        static {
            Replacements.register(Identifier.ofVanilla("trial_chamber/breeze"), "{simultaneous_mobs: 1.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:breeze\"}}, weight: 1}], ticks_between_spawn: 20, total_mobs: 2.0f, total_mobs_added_per_player: 1.0f}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], simultaneous_mobs: 2.0f, total_mobs: 4.0f}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/melee/husk"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:husk\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:husk\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_melee\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/melee/spider"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:spider\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}],simultaneous_mobs: 4.0f, total_mobs: 12.0f}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/melee/zombie"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:zombie\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}],spawn_potentials: [{data: {entity: {id: \"minecraft:zombie\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_melee\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/ranged/poison_skeleton"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:bogged\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}],spawn_potentials: [{data: {entity: {id: \"minecraft:bogged\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/ranged/skeleton"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:skeleton\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:skeleton\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/ranged/stray"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:stray\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:stray\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/slow_ranged/poison_skeleton"), "{simultaneous_mobs: 4.0f, simultaneous_mobs_added_per_player: 2.0f, spawn_potentials: [{data: {entity: {id: \"minecraft:bogged\"}}, weight: 1}], ticks_between_spawn: 160}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:bogged\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/slow_ranged/skeleton"), "{simultaneous_mobs: 4.0f, simultaneous_mobs_added_per_player: 2.0f, spawn_potentials: [{data: {entity: {id: \"minecraft:skeleton\"}}, weight: 1}], ticks_between_spawn: 160}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:skeleton\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/slow_ranged/stray"), "{simultaneous_mobs: 4.0f, simultaneous_mobs_added_per_player: 2.0f, spawn_potentials: [{data: {entity: {id: \"minecraft:stray\"}}, weight: 1}], ticks_between_spawn: 160}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}],spawn_potentials: [{data: {entity: {id: \"minecraft:stray\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/small_melee/baby_zombie"), "{simultaneous_mobs: 2.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {IsBaby: 1b, id: \"minecraft:zombie\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {IsBaby: 1b, id: \"minecraft:zombie\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_melee\", slot_drop_chances: 0.0f}}, weight: 1}]}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/small_melee/cave_spider"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:cave_spider\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], simultaneous_mobs: 4.0f, total_mobs: 12.0f}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/small_melee/silverfish"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:silverfish\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], simultaneous_mobs: 4.0f, total_mobs: 12.0f}");
            Replacements.register(Identifier.ofVanilla("trial_chamber/small_melee/slime"), "{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {Size: 1, id: \"minecraft:slime\"}}, weight: 3}, {data: {entity: {Size: 2, id: \"minecraft:slime\"}}, weight: 1}], ticks_between_spawn: 20}", "{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], simultaneous_mobs: 4.0f, total_mobs: 12.0f}");
        }
    }
}

