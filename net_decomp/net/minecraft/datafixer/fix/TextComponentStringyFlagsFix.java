/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class TextComponentStringyFlagsFix
extends DataFix {
    public TextComponentStringyFlagsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT);
        return this.fixTypeEverywhere("TextComponentStringyFlagsFix", type, dynamicOps -> pair -> pair.mapSecond(either -> either.mapRight(pair -> pair.mapSecond(pair2 -> pair2.mapSecond(pair -> pair.mapSecond(dynamic -> dynamic.update("bold", TextComponentStringyFlagsFix::method_66136).update("italic", TextComponentStringyFlagsFix::method_66136).update("underlined", TextComponentStringyFlagsFix::method_66136).update("strikethrough", TextComponentStringyFlagsFix::method_66136).update("obfuscated", TextComponentStringyFlagsFix::method_66136)))))));
    }

    private static <T> Dynamic<T> method_66136(Dynamic<T> dynamic) {
        Optional optional = dynamic.asString().result();
        if (optional.isPresent()) {
            return dynamic.createBoolean(Boolean.parseBoolean((String)optional.get()));
        }
        return dynamic;
    }
}

