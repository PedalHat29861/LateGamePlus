/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class ItemInstanceSpawnEggFix
extends DataFix {
    private final String spawnEggId;
    private static final Map<String, String> ENTITY_SPAWN_EGGS = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
        map.put("minecraft:bat", "minecraft:bat_spawn_egg");
        map.put("minecraft:blaze", "minecraft:blaze_spawn_egg");
        map.put("minecraft:cave_spider", "minecraft:cave_spider_spawn_egg");
        map.put("minecraft:chicken", "minecraft:chicken_spawn_egg");
        map.put("minecraft:cow", "minecraft:cow_spawn_egg");
        map.put("minecraft:creeper", "minecraft:creeper_spawn_egg");
        map.put("minecraft:donkey", "minecraft:donkey_spawn_egg");
        map.put("minecraft:elder_guardian", "minecraft:elder_guardian_spawn_egg");
        map.put("minecraft:ender_dragon", "minecraft:ender_dragon_spawn_egg");
        map.put("minecraft:enderman", "minecraft:enderman_spawn_egg");
        map.put("minecraft:endermite", "minecraft:endermite_spawn_egg");
        map.put("minecraft:evocation_illager", "minecraft:evocation_illager_spawn_egg");
        map.put("minecraft:ghast", "minecraft:ghast_spawn_egg");
        map.put("minecraft:guardian", "minecraft:guardian_spawn_egg");
        map.put("minecraft:horse", "minecraft:horse_spawn_egg");
        map.put("minecraft:husk", "minecraft:husk_spawn_egg");
        map.put("minecraft:iron_golem", "minecraft:iron_golem_spawn_egg");
        map.put("minecraft:llama", "minecraft:llama_spawn_egg");
        map.put("minecraft:magma_cube", "minecraft:magma_cube_spawn_egg");
        map.put("minecraft:mooshroom", "minecraft:mooshroom_spawn_egg");
        map.put("minecraft:mule", "minecraft:mule_spawn_egg");
        map.put("minecraft:ocelot", "minecraft:ocelot_spawn_egg");
        map.put("minecraft:pufferfish", "minecraft:pufferfish_spawn_egg");
        map.put("minecraft:parrot", "minecraft:parrot_spawn_egg");
        map.put("minecraft:pig", "minecraft:pig_spawn_egg");
        map.put("minecraft:polar_bear", "minecraft:polar_bear_spawn_egg");
        map.put("minecraft:rabbit", "minecraft:rabbit_spawn_egg");
        map.put("minecraft:sheep", "minecraft:sheep_spawn_egg");
        map.put("minecraft:shulker", "minecraft:shulker_spawn_egg");
        map.put("minecraft:silverfish", "minecraft:silverfish_spawn_egg");
        map.put("minecraft:skeleton", "minecraft:skeleton_spawn_egg");
        map.put("minecraft:skeleton_horse", "minecraft:skeleton_horse_spawn_egg");
        map.put("minecraft:slime", "minecraft:slime_spawn_egg");
        map.put("minecraft:snow_golem", "minecraft:snow_golem_spawn_egg");
        map.put("minecraft:spider", "minecraft:spider_spawn_egg");
        map.put("minecraft:squid", "minecraft:squid_spawn_egg");
        map.put("minecraft:stray", "minecraft:stray_spawn_egg");
        map.put("minecraft:turtle", "minecraft:turtle_spawn_egg");
        map.put("minecraft:vex", "minecraft:vex_spawn_egg");
        map.put("minecraft:villager", "minecraft:villager_spawn_egg");
        map.put("minecraft:vindication_illager", "minecraft:vindication_illager_spawn_egg");
        map.put("minecraft:witch", "minecraft:witch_spawn_egg");
        map.put("minecraft:wither", "minecraft:wither_spawn_egg");
        map.put("minecraft:wither_skeleton", "minecraft:wither_skeleton_spawn_egg");
        map.put("minecraft:wolf", "minecraft:wolf_spawn_egg");
        map.put("minecraft:zombie", "minecraft:zombie_spawn_egg");
        map.put("minecraft:zombie_horse", "minecraft:zombie_horse_spawn_egg");
        map.put("minecraft:zombie_pigman", "minecraft:zombie_pigman_spawn_egg");
        map.put("minecraft:zombie_villager", "minecraft:zombie_villager_spawn_egg");
    });

    public ItemInstanceSpawnEggFix(Schema outputSchema, boolean changesType, String spawnEggId) {
        super(outputSchema, changesType);
        this.spawnEggId = spawnEggId;
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        OpticFinder opticFinder2 = DSL.fieldFinder((String)"id", IdentifierNormalizingSchema.getIdentifierType());
        OpticFinder opticFinder3 = type.findField("tag");
        OpticFinder opticFinder4 = opticFinder3.type().findField("EntityTag");
        return this.fixTypeEverywhereTyped("ItemInstanceSpawnEggFix" + this.getOutputSchema().getVersionKey(), type, stack -> {
            Typed typed;
            Typed typed2;
            Optional optional2;
            Optional optional = stack.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), this.spawnEggId) && (optional2 = (typed2 = (typed = stack.getOrCreateTyped(opticFinder3)).getOrCreateTyped(opticFinder4)).getOptional(opticFinder2)).isPresent()) {
                return stack.set(opticFinder, (Object)Pair.of((Object)TypeReferences.ITEM_NAME.typeName(), (Object)ENTITY_SPAWN_EGGS.getOrDefault(optional2.get(), "minecraft:pig_spawn_egg")));
            }
            return stack;
        });
    }
}

