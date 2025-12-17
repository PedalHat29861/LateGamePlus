/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.datafixer.fix.ChoiceFix;

public class EntityVariantTypeFix
extends ChoiceFix {
    private final String variantKey;
    private final IntFunction<String> variantIntToId;

    public EntityVariantTypeFix(Schema outputSchema, String name, DSL.TypeReference type, String entityId, String variantKey, IntFunction<String> variantIntToId) {
        super(outputSchema, false, name, type, entityId);
        this.variantKey = variantKey;
        this.variantIntToId = variantIntToId;
    }

    private static <T> Dynamic<T> updateEntity(Dynamic<T> entityDynamic, String oldVariantKey, String newVariantKey, Function<Dynamic<T>, Dynamic<T>> variantIntToId) {
        return entityDynamic.map(object3 -> {
            DynamicOps dynamicOps = entityDynamic.getOps();
            Function<Object, Object> function2 = object -> ((Dynamic)variantIntToId.apply(new Dynamic(dynamicOps, object))).getValue();
            return dynamicOps.get(object3, oldVariantKey).map(object2 -> dynamicOps.set(object3, newVariantKey, function2.apply(object2))).result().orElse(object3);
        });
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), entityDynamic -> EntityVariantTypeFix.updateEntity(entityDynamic, this.variantKey, "variant", variantDynamic -> (Dynamic)DataFixUtils.orElse((Optional)variantDynamic.asNumber().map(variantInt -> variantDynamic.createString(this.variantIntToId.apply(variantInt.intValue()))).result(), (Object)variantDynamic)));
    }
}

