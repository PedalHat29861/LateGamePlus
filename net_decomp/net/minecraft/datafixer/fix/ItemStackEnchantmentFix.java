/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class ItemStackEnchantmentFix
extends DataFix {
    private static final Int2ObjectMap<String> ID_TO_ENCHANTMENTS_MAP = (Int2ObjectMap)DataFixUtils.make((Object)new Int2ObjectOpenHashMap(), map -> {
        map.put(0, (Object)"minecraft:protection");
        map.put(1, (Object)"minecraft:fire_protection");
        map.put(2, (Object)"minecraft:feather_falling");
        map.put(3, (Object)"minecraft:blast_protection");
        map.put(4, (Object)"minecraft:projectile_protection");
        map.put(5, (Object)"minecraft:respiration");
        map.put(6, (Object)"minecraft:aqua_affinity");
        map.put(7, (Object)"minecraft:thorns");
        map.put(8, (Object)"minecraft:depth_strider");
        map.put(9, (Object)"minecraft:frost_walker");
        map.put(10, (Object)"minecraft:binding_curse");
        map.put(16, (Object)"minecraft:sharpness");
        map.put(17, (Object)"minecraft:smite");
        map.put(18, (Object)"minecraft:bane_of_arthropods");
        map.put(19, (Object)"minecraft:knockback");
        map.put(20, (Object)"minecraft:fire_aspect");
        map.put(21, (Object)"minecraft:looting");
        map.put(22, (Object)"minecraft:sweeping");
        map.put(32, (Object)"minecraft:efficiency");
        map.put(33, (Object)"minecraft:silk_touch");
        map.put(34, (Object)"minecraft:unbreaking");
        map.put(35, (Object)"minecraft:fortune");
        map.put(48, (Object)"minecraft:power");
        map.put(49, (Object)"minecraft:punch");
        map.put(50, (Object)"minecraft:flame");
        map.put(51, (Object)"minecraft:infinity");
        map.put(61, (Object)"minecraft:luck_of_the_sea");
        map.put(62, (Object)"minecraft:lure");
        map.put(65, (Object)"minecraft:loyalty");
        map.put(66, (Object)"minecraft:impaling");
        map.put(67, (Object)"minecraft:riptide");
        map.put(68, (Object)"minecraft:channeling");
        map.put(70, (Object)"minecraft:mending");
        map.put(71, (Object)"minecraft:vanishing_curse");
    });

    public ItemStackEnchantmentFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder opticFinder = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemStackEnchantmentFix", type, itemStackTyped -> itemStackTyped.updateTyped(opticFinder, tagTyped -> tagTyped.update(DSL.remainderFinder(), this::fixEnchantments)));
    }

    private Dynamic<?> fixEnchantments(Dynamic<?> tagDynamic) {
        Optional optional = tagDynamic.get("ench").asStreamOpt().map(enchantments -> enchantments.map(enchantment -> enchantment.set("id", enchantment.createString((String)ID_TO_ENCHANTMENTS_MAP.getOrDefault(enchantment.get("id").asInt(0), (Object)"null"))))).map(arg_0 -> tagDynamic.createList(arg_0)).result();
        if (optional.isPresent()) {
            tagDynamic = tagDynamic.remove("ench").set("Enchantments", (Dynamic)optional.get());
        }
        return tagDynamic.update("StoredEnchantments", storedEnchantmentsDynamic -> (Dynamic)DataFixUtils.orElse((Optional)storedEnchantmentsDynamic.asStreamOpt().map(storedEnchantments -> storedEnchantments.map(storedEnchantment -> storedEnchantment.set("id", storedEnchantment.createString((String)ID_TO_ENCHANTMENTS_MAP.getOrDefault(storedEnchantment.get("id").asInt(0), (Object)"null"))))).map(arg_0 -> ((Dynamic)storedEnchantmentsDynamic).createList(arg_0)).result(), (Object)storedEnchantmentsDynamic));
    }
}

