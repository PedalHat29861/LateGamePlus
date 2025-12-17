/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class BoatSplitFix
extends DataFix {
    public BoatSplitFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    private static boolean isBoat(String id) {
        return id.equals("minecraft:boat");
    }

    private static boolean isChestBoat(String id) {
        return id.equals("minecraft:chest_boat");
    }

    private static boolean isBoatOrChestBoat(String id) {
        return BoatSplitFix.isBoat(id) || BoatSplitFix.isChestBoat(id);
    }

    private static String getNewBoatIdFromOldType(String type) {
        return switch (type) {
            default -> "minecraft:oak_boat";
            case "spruce" -> "minecraft:spruce_boat";
            case "birch" -> "minecraft:birch_boat";
            case "jungle" -> "minecraft:jungle_boat";
            case "acacia" -> "minecraft:acacia_boat";
            case "cherry" -> "minecraft:cherry_boat";
            case "dark_oak" -> "minecraft:dark_oak_boat";
            case "mangrove" -> "minecraft:mangrove_boat";
            case "bamboo" -> "minecraft:bamboo_raft";
        };
    }

    private static String getNewChestBoatIdFromOldType(String type) {
        return switch (type) {
            default -> "minecraft:oak_chest_boat";
            case "spruce" -> "minecraft:spruce_chest_boat";
            case "birch" -> "minecraft:birch_chest_boat";
            case "jungle" -> "minecraft:jungle_chest_boat";
            case "acacia" -> "minecraft:acacia_chest_boat";
            case "cherry" -> "minecraft:cherry_chest_boat";
            case "dark_oak" -> "minecraft:dark_oak_chest_boat";
            case "mangrove" -> "minecraft:mangrove_chest_boat";
            case "bamboo" -> "minecraft:bamboo_chest_raft";
        };
    }

    public TypeRewriteRule makeRule() {
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", IdentifierNormalizingSchema.getIdentifierType());
        Type type = this.getInputSchema().getType(TypeReferences.ENTITY);
        Type type2 = this.getOutputSchema().getType(TypeReferences.ENTITY);
        return this.fixTypeEverywhereTyped("BoatSplitFix", type, type2, typed -> {
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && BoatSplitFix.isBoatOrChestBoat((String)optional.get())) {
                Dynamic dynamic2 = (Dynamic)typed.getOrCreate(DSL.remainderFinder());
                Optional optional2 = dynamic2.get("Type").asString().result();
                String string = BoatSplitFix.isChestBoat((String)optional.get()) ? optional2.map(BoatSplitFix::getNewChestBoatIdFromOldType).orElse("minecraft:oak_chest_boat") : optional2.map(BoatSplitFix::getNewBoatIdFromOldType).orElse("minecraft:oak_boat");
                return FixUtil.withType(type2, typed).update(DSL.remainderFinder(), dynamic -> dynamic.remove("Type")).set(opticFinder, (Object)string);
            }
            return FixUtil.withType(type2, typed);
        });
    }
}

