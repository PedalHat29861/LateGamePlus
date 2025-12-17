/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.datafixer.fix;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class UnflattenTextComponentFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public UnflattenTextComponentFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT);
        Type type2 = this.getOutputSchema().getType(TypeReferences.TEXT_COMPONENT);
        return this.method_66142((Type<Pair<String, String>>)type, type2);
    }

    private <T> TypeRewriteRule method_66142(Type<Pair<String, String>> type, Type<T> type2) {
        return this.fixTypeEverywhere("UnflattenTextComponentFix", type, type2, dynamicOps -> pair -> Util.readTyped(type2, UnflattenTextComponentFix.method_66145(dynamicOps, (String)pair.getSecond()), true).getValue());
    }

    private static <T> Dynamic<T> method_66145(DynamicOps<T> dynamicOps, String string) {
        try {
            JsonElement jsonElement = LenientJsonParser.parse(string);
            if (!jsonElement.isJsonNull()) {
                return new Dynamic(dynamicOps, JsonOps.INSTANCE.convertTo(dynamicOps, jsonElement));
            }
        }
        catch (Exception exception) {
            LOGGER.error("Failed to unflatten text component json: {}", (Object)string, (Object)exception);
        }
        return new Dynamic(dynamicOps, dynamicOps.createString(string));
    }
}

