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

public class StriderGravityFix
extends ChoiceFix {
    public StriderGravityFix(Schema schema, boolean bl) {
        super(schema, bl, "StriderGravityFix", TypeReferences.ENTITY, "minecraft:strider");
    }

    public Dynamic<?> updateNoGravityNbt(Dynamic<?> striderDynamic) {
        if (striderDynamic.get("NoGravity").asBoolean(false)) {
            return striderDynamic.set("NoGravity", striderDynamic.createBoolean(false));
        }
        return striderDynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::updateNoGravityNbt);
    }
}

