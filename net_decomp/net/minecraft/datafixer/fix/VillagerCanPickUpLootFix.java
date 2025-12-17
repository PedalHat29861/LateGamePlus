/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class VillagerCanPickUpLootFix
extends ChoiceFix {
    private static final String FIELD_NAME = "CanPickUpLoot";

    public VillagerCanPickUpLootFix(Schema schema) {
        super(schema, true, "Villager CanPickUpLoot default value", TypeReferences.ENTITY, "Villager");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), VillagerCanPickUpLootFix::fix);
    }

    private static Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.set(FIELD_NAME, dynamic.createBoolean(true));
    }
}

