/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.datafixer.fix;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;

public class LegacyHoverEventFix
extends DataFix {
    public LegacyHoverEventFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT).findFieldType("hoverEvent");
        return this.method_66084(this.getInputSchema().getTypeRaw(TypeReferences.TEXT_COMPONENT), type);
    }

    private <C, H extends Pair<String, ?>> TypeRewriteRule method_66084(Type<C> type, Type<H> type2) {
        Type type3 = DSL.named((String)TypeReferences.TEXT_COMPONENT.typeName(), (Type)DSL.or((Type)DSL.or((Type)DSL.string(), (Type)DSL.list(type)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"extra", (Type)DSL.list(type))), (Type)DSL.optional((Type)DSL.field((String)"separator", type)), (Type)DSL.optional((Type)DSL.field((String)"hoverEvent", type2)), (Type)DSL.remainderType())));
        if (!type3.equals((Object)this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT))) {
            throw new IllegalStateException("Text component type did not match, expected " + String.valueOf(type3) + " but got " + String.valueOf(this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT)));
        }
        return this.fixTypeEverywhere("LegacyHoverEventFix", type3, dynamicOps -> pair -> pair.mapSecond(either -> either.mapRight(pair -> pair.mapSecond(pair2 -> pair2.mapSecond(pair -> {
            Dynamic dynamic = (Dynamic)pair.getSecond();
            Optional optional = dynamic.get("hoverEvent").result();
            if (optional.isEmpty()) {
                return pair;
            }
            Optional optional2 = ((Dynamic)optional.get()).get("value").result();
            if (optional2.isEmpty()) {
                return pair;
            }
            String string = ((Either)pair.getFirst()).left().map(Pair::getFirst).orElse("");
            Pair pair2 = (Pair)this.method_66089(type2, string, (Dynamic)optional.get());
            return pair.mapFirst(either -> Either.left((Object)pair2));
        })))));
    }

    private <H> H method_66089(Type<H> type, String string, Dynamic<?> dynamic) {
        if ("show_text".equals(string)) {
            return LegacyHoverEventFix.method_66087(type, dynamic);
        }
        return LegacyHoverEventFix.method_66092(type, dynamic);
    }

    private static <H> H method_66087(Type<H> type, Dynamic<?> dynamic) {
        Dynamic dynamic2 = dynamic.renameField("value", "contents");
        return (H)Util.readTyped(type, dynamic2).getValue();
    }

    private static <H> H method_66092(Type<H> type, Dynamic<?> dynamic) {
        JsonElement jsonElement = (JsonElement)dynamic.convert((DynamicOps)JsonOps.INSTANCE).getValue();
        Dynamic dynamic2 = new Dynamic((DynamicOps)JavaOps.INSTANCE, Map.of("action", "show_text", "contents", Map.of("text", "Legacy hoverEvent: " + JsonHelper.toSortedString(jsonElement))));
        return (H)Util.readTyped(type, dynamic2).getValue();
    }
}

