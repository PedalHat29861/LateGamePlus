/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class GoatMissingStateFix
extends ChoiceFix {
    public GoatMissingStateFix(Schema outputSchema) {
        super(outputSchema, false, "EntityGoatMissingStateFix", TypeReferences.ENTITY, "minecraft:goat");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), goatDynamic -> goatDynamic.set("HasLeftHorn", goatDynamic.createBoolean(true)).set("HasRightHorn", goatDynamic.createBoolean(true)));
    }
}

