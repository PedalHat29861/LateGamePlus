/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.AbstractUuidFix;
import net.minecraft.datafixer.fix.EntityUuidFix;

public class PlayerUuidFix
extends AbstractUuidFix {
    public PlayerUuidFix(Schema outputSchema) {
        super(outputSchema, TypeReferences.PLAYER);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("PlayerUUIDFix", this.getInputSchema().getType(this.typeReference), playerTyped -> {
            OpticFinder opticFinder = playerTyped.getType().findField("RootVehicle");
            return playerTyped.updateTyped(opticFinder, opticFinder.type(), rootVehicleTyped -> rootVehicleTyped.update(DSL.remainderFinder(), rootVehicleDynamic -> PlayerUuidFix.updateRegularMostLeast(rootVehicleDynamic, "Attach", "Attach").orElse((Dynamic<?>)rootVehicleDynamic))).update(DSL.remainderFinder(), playerDynamic -> EntityUuidFix.updateSelfUuid(EntityUuidFix.updateLiving(playerDynamic)));
        });
    }
}

