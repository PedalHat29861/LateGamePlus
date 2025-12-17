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

public class EntityWolfColorFix
extends ChoiceFix {
    public EntityWolfColorFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityWolfColorFix", TypeReferences.ENTITY, "minecraft:wolf");
    }

    public Dynamic<?> fixCollarColor(Dynamic<?> wolfDynamic) {
        return wolfDynamic.update("CollarColor", colorDynamic -> colorDynamic.createByte((byte)(15 - colorDynamic.asInt(0))));
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixCollarColor);
    }
}

