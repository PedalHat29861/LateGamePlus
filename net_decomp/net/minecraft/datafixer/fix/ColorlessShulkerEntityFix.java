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

public class ColorlessShulkerEntityFix
extends ChoiceFix {
    public ColorlessShulkerEntityFix(Schema schema, boolean bl) {
        super(schema, bl, "Colorless shulker entity fix", TypeReferences.ENTITY, "minecraft:shulker");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), shulkerDynamic -> {
            if (shulkerDynamic.get("Color").asInt(0) == 10) {
                return shulkerDynamic.set("Color", shulkerDynamic.createByte((byte)16));
            }
            return shulkerDynamic;
        });
    }
}

