/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;

public class PlayerEquipmentFix
extends DataFix {
    private static final Map<Integer, String> SLOT_ID_MAP = Map.of(100, "feet", 101, "legs", 102, "chest", 103, "head", -106, "offhand");

    public PlayerEquipmentFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getTypeRaw(TypeReferences.PLAYER);
        Type type2 = this.getOutputSchema().getTypeRaw(TypeReferences.PLAYER);
        return this.writeFixAndRead("Player Equipment Fix", type, type2, playerDynamic -> {
            HashMap map = new HashMap();
            playerDynamic = playerDynamic.update("Inventory", inventoryDynamic -> inventoryDynamic.createList(inventoryDynamic.asStream().filter(dynamic2 -> {
                int i = dynamic2.get("Slot").asInt(-1);
                String string = SLOT_ID_MAP.get(i);
                if (string != null) {
                    map.put(inventoryDynamic.createString(string), dynamic2.remove("Slot"));
                }
                return string == null;
            })));
            playerDynamic = playerDynamic.set("equipment", playerDynamic.createMap(map));
            return playerDynamic;
        });
    }
}

