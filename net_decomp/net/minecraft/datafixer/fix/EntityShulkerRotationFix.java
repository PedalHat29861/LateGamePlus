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
import java.util.List;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class EntityShulkerRotationFix
extends ChoiceFix {
    public EntityShulkerRotationFix(Schema outputSchema) {
        super(outputSchema, false, "EntityShulkerRotationFix", TypeReferences.ENTITY, "minecraft:shulker");
    }

    public Dynamic<?> fixRotation(Dynamic<?> shulkerDynamic) {
        List list = shulkerDynamic.get("Rotation").asList(rotationDynamic -> rotationDynamic.asDouble(180.0));
        if (!list.isEmpty()) {
            list.set(0, (Double)list.get(0) - 180.0);
            return shulkerDynamic.set("Rotation", shulkerDynamic.createList(list.stream().map(arg_0 -> shulkerDynamic.createDouble(arg_0))));
        }
        return shulkerDynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixRotation);
    }
}

