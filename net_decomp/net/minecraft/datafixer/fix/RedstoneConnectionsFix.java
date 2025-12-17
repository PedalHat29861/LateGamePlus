/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class RedstoneConnectionsFix
extends DataFix {
    public RedstoneConnectionsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        return this.fixTypeEverywhereTyped("RedstoneConnectionsFix", schema.getType(TypeReferences.BLOCK_STATE), blockStateTyped -> blockStateTyped.update(DSL.remainderFinder(), this::updateBlockState));
    }

    private <T> Dynamic<T> updateBlockState(Dynamic<T> blockStateDynamic) {
        boolean bl = blockStateDynamic.get("Name").asString().result().filter("minecraft:redstone_wire"::equals).isPresent();
        if (!bl) {
            return blockStateDynamic;
        }
        return blockStateDynamic.update("Properties", propertiesDynamic -> {
            String string = propertiesDynamic.get("east").asString("none");
            String string2 = propertiesDynamic.get("west").asString("none");
            String string3 = propertiesDynamic.get("north").asString("none");
            String string4 = propertiesDynamic.get("south").asString("none");
            boolean bl = RedstoneConnectionsFix.hasObsoleteValue(string) || RedstoneConnectionsFix.hasObsoleteValue(string2);
            boolean bl2 = RedstoneConnectionsFix.hasObsoleteValue(string3) || RedstoneConnectionsFix.hasObsoleteValue(string4);
            String string5 = !RedstoneConnectionsFix.hasObsoleteValue(string) && !bl2 ? "side" : string;
            String string6 = !RedstoneConnectionsFix.hasObsoleteValue(string2) && !bl2 ? "side" : string2;
            String string7 = !RedstoneConnectionsFix.hasObsoleteValue(string3) && !bl ? "side" : string3;
            String string8 = !RedstoneConnectionsFix.hasObsoleteValue(string4) && !bl ? "side" : string4;
            return propertiesDynamic.update("east", eastDynamic -> eastDynamic.createString(string5)).update("west", westDynamic -> westDynamic.createString(string6)).update("north", northDynamic -> northDynamic.createString(string7)).update("south", southDynamic -> southDynamic.createString(string8));
        });
    }

    private static boolean hasObsoleteValue(String value) {
        return !"none".equals(value);
    }
}

