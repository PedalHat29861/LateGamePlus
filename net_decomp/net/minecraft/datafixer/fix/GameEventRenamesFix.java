/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class GameEventRenamesFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference typeReference;
    private final UnaryOperator<String> renamer;

    public GameEventRenamesFix(Schema outputSchema, String name, DSL.TypeReference typeReference, UnaryOperator<String> renamer) {
        super(outputSchema, false);
        this.name = name;
        this.typeReference = typeReference;
        this.renamer = renamer;
    }

    protected TypeRewriteRule makeRule() {
        Type type = DSL.named((String)this.typeReference.typeName(), IdentifierNormalizingSchema.getIdentifierType());
        if (!Objects.equals(type, this.getInputSchema().getType(this.typeReference))) {
            throw new IllegalStateException("\"" + this.typeReference.typeName() + "\" is not what was expected.");
        }
        return this.fixTypeEverywhere(this.name, type, dynamicOps -> pair -> pair.mapSecond(this.renamer));
    }
}

