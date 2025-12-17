/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ComponentFix;

public class FireResistantToDamageResistantComponentFix
extends ComponentFix {
    public FireResistantToDamageResistantComponentFix(Schema outputSchema) {
        super(outputSchema, "FireResistantToDamageResistantComponentFix", "minecraft:fire_resistant", "minecraft:damage_resistant");
    }

    @Override
    protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return dynamic.emptyMap().set("types", dynamic.createString("#minecraft:is_fire"));
    }
}

