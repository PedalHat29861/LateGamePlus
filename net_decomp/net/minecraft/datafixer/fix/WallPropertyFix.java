/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.datafixer.TypeReferences;

public class WallPropertyFix
extends DataFix {
    private static final Set<String> TARGET_BLOCK_IDS = ImmutableSet.of((Object)"minecraft:andesite_wall", (Object)"minecraft:brick_wall", (Object)"minecraft:cobblestone_wall", (Object)"minecraft:diorite_wall", (Object)"minecraft:end_stone_brick_wall", (Object)"minecraft:granite_wall", (Object[])new String[]{"minecraft:mossy_cobblestone_wall", "minecraft:mossy_stone_brick_wall", "minecraft:nether_brick_wall", "minecraft:prismarine_wall", "minecraft:red_nether_brick_wall", "minecraft:red_sandstone_wall", "minecraft:sandstone_wall", "minecraft:stone_brick_wall"});

    public WallPropertyFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("WallPropertyFix", this.getInputSchema().getType(TypeReferences.BLOCK_STATE), blockStateTyped -> blockStateTyped.update(DSL.remainderFinder(), WallPropertyFix::updateWallProperties));
    }

    private static String booleanToWallType(String value) {
        return "true".equals(value) ? "low" : "none";
    }

    private static <T> Dynamic<T> updateWallValueReference(Dynamic<T> propertiesDynamic, String propertyName) {
        return propertiesDynamic.update(propertyName, propertyValue -> (Dynamic)DataFixUtils.orElse(propertyValue.asString().result().map(WallPropertyFix::booleanToWallType).map(arg_0 -> ((Dynamic)propertyValue).createString(arg_0)), (Object)propertyValue));
    }

    private static <T> Dynamic<T> updateWallProperties(Dynamic<T> blockStateDynamic) {
        boolean bl = blockStateDynamic.get("Name").asString().result().filter(TARGET_BLOCK_IDS::contains).isPresent();
        if (!bl) {
            return blockStateDynamic;
        }
        return blockStateDynamic.update("Properties", propertiesDynamic -> {
            Dynamic dynamic = WallPropertyFix.updateWallValueReference(propertiesDynamic, "east");
            dynamic = WallPropertyFix.updateWallValueReference(dynamic, "west");
            dynamic = WallPropertyFix.updateWallValueReference(dynamic, "north");
            return WallPropertyFix.updateWallValueReference(dynamic, "south");
        });
    }
}

