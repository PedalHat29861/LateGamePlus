/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Locale;

public class ChoiceTypesFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference types;

    public ChoiceTypesFix(Schema outputSchema, String name, DSL.TypeReference types) {
        super(outputSchema, true);
        this.name = name;
        this.types = types;
    }

    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType taggedChoiceType = this.getInputSchema().findChoiceType(this.types);
        TaggedChoice.TaggedChoiceType taggedChoiceType2 = this.getOutputSchema().findChoiceType(this.types);
        return this.fixChoiceTypes(taggedChoiceType, taggedChoiceType2);
    }

    private <K> TypeRewriteRule fixChoiceTypes(TaggedChoice.TaggedChoiceType<K> inputChoiceType, TaggedChoice.TaggedChoiceType<?> outputChoiceType) {
        if (inputChoiceType.getKeyType() != outputChoiceType.getKeyType()) {
            throw new IllegalStateException("Could not inject: key type is not the same");
        }
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType = outputChoiceType;
        return this.fixTypeEverywhere(this.name, (Type)inputChoiceType, (Type)taggedChoiceType, dynamicOps -> pair -> {
            if (!taggedChoiceType.hasType(pair.getFirst())) {
                throw new IllegalArgumentException(String.format(Locale.ROOT, "%s: Unknown type %s in '%s'", this.name, pair.getFirst(), this.types.typeName()));
            }
            return pair;
        });
    }
}

