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
import java.util.function.DoubleUnaryOperator;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class EntityAttributeBaseFix
extends ChoiceFix {
    private final String attributeId;
    private final DoubleUnaryOperator fixOperator;

    public EntityAttributeBaseFix(Schema outputSchema, String name, String entityId, String attributeId, DoubleUnaryOperator fixOperator) {
        super(outputSchema, false, name, TypeReferences.ENTITY, entityId);
        this.attributeId = attributeId;
        this.fixOperator = fixOperator;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.update("attributes", attributesDynamic -> dynamic.createList(attributesDynamic.asStream().map(attributeDynamic -> {
            String string = IdentifierNormalizingSchema.normalize(attributeDynamic.get("id").asString(""));
            if (!string.equals(this.attributeId)) {
                return attributeDynamic;
            }
            double d = attributeDynamic.get("base").asDouble(0.0);
            return attributeDynamic.set("base", attributeDynamic.createDouble(this.fixOperator.applyAsDouble(d)));
        })));
    }
}

