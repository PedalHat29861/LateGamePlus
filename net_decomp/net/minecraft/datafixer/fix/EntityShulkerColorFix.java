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

public class EntityShulkerColorFix
extends ChoiceFix {
    public EntityShulkerColorFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityShulkerColorFix", TypeReferences.ENTITY, "minecraft:shulker");
    }

    public Dynamic<?> fixShulkerColor(Dynamic<?> shulkerDynamic) {
        if (shulkerDynamic.get("Color").map(Dynamic::asNumber).result().isEmpty()) {
            return shulkerDynamic.set("Color", shulkerDynamic.createByte((byte)10));
        }
        return shulkerDynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixShulkerColor);
    }
}

