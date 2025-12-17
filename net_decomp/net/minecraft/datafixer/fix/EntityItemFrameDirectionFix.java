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

public class EntityItemFrameDirectionFix
extends ChoiceFix {
    public EntityItemFrameDirectionFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityItemFrameDirectionFix", TypeReferences.ENTITY, "minecraft:item_frame");
    }

    public Dynamic<?> fixDirection(Dynamic<?> itemFrameDynamic) {
        return itemFrameDynamic.set("Facing", itemFrameDynamic.createByte(EntityItemFrameDirectionFix.updateDirection(itemFrameDynamic.get("Facing").asByte((byte)0))));
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixDirection);
    }

    private static byte updateDirection(byte oldDirection) {
        switch (oldDirection) {
            default: {
                return 2;
            }
            case 0: {
                return 3;
            }
            case 1: {
                return 4;
            }
            case 3: 
        }
        return 5;
    }
}

