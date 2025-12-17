/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Const$PrimitiveType
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.PrimitiveCodec
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.util.Identifier;

public class IdentifierNormalizingSchema
extends Schema {
    public static final PrimitiveCodec<String> CODEC = new PrimitiveCodec<String>(){

        public <T> DataResult<String> read(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input).map(IdentifierNormalizingSchema::normalize);
        }

        public <T> T write(DynamicOps<T> dynamicOps, String string) {
            return (T)dynamicOps.createString(string);
        }

        public String toString() {
            return "NamespacedString";
        }

        public /* synthetic */ Object write(DynamicOps ops, Object value) {
            return this.write(ops, (String)value);
        }
    };
    private static final Type<String> IDENTIFIER_TYPE = new Const.PrimitiveType(CODEC);

    public IdentifierNormalizingSchema(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public static String normalize(String id) {
        Identifier identifier = Identifier.tryParse(id);
        if (identifier != null) {
            return identifier.toString();
        }
        return id;
    }

    public static Type<String> getIdentifierType() {
        return IDENTIFIER_TYPE;
    }

    public Type<?> getChoiceType(DSL.TypeReference type, String choiceName) {
        return super.getChoiceType(type, IdentifierNormalizingSchema.normalize(choiceName));
    }
}

