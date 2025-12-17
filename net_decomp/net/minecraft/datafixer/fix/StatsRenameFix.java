/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class StatsRenameFix
extends DataFix {
    private final String name;
    private final Map<String, String> replacements;

    public StatsRenameFix(Schema outputSchema, String name, Map<String, String> replacements) {
        super(outputSchema, false);
        this.name = name;
        this.replacements = replacements;
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.renameStats(), (TypeRewriteRule)this.renameObjectives());
    }

    private TypeRewriteRule renameObjectives() {
        Type type = this.getOutputSchema().getType(TypeReferences.OBJECTIVE);
        Type type2 = this.getInputSchema().getType(TypeReferences.OBJECTIVE);
        OpticFinder opticFinder = type2.findField("CriteriaType");
        TaggedChoice.TaggedChoiceType taggedChoiceType = (TaggedChoice.TaggedChoiceType)opticFinder.type().findChoiceType("type", -1).orElseThrow(() -> new IllegalStateException("Can't find choice type for criteria"));
        Type type3 = (Type)taggedChoiceType.types().get("minecraft:custom");
        if (type3 == null) {
            throw new IllegalStateException("Failed to find custom criterion type variant");
        }
        OpticFinder opticFinder2 = DSL.namedChoice((String)"minecraft:custom", (Type)type3);
        OpticFinder opticFinder3 = DSL.fieldFinder((String)"id", IdentifierNormalizingSchema.getIdentifierType());
        return this.fixTypeEverywhereTyped(this.name, type2, type, objectiveTyped -> objectiveTyped.updateTyped(opticFinder, criteriaTypeTyped -> criteriaTypeTyped.updateTyped(opticFinder2, customCriteriaTypeTyped -> customCriteriaTypeTyped.update(opticFinder3, old -> this.replacements.getOrDefault(old, (String)old)))));
    }

    private TypeRewriteRule renameStats() {
        Type type = this.getOutputSchema().getType(TypeReferences.STATS);
        Type type2 = this.getInputSchema().getType(TypeReferences.STATS);
        OpticFinder opticFinder = type2.findField("stats");
        OpticFinder opticFinder2 = opticFinder.type().findField("minecraft:custom");
        OpticFinder opticFinder3 = IdentifierNormalizingSchema.getIdentifierType().finder();
        return this.fixTypeEverywhereTyped(this.name, type2, type, statsTyped -> statsTyped.updateTyped(opticFinder, statsInnerTyped -> statsInnerTyped.updateTyped(opticFinder2, customStatTyped -> customStatTyped.update(opticFinder3, old -> this.replacements.getOrDefault(old, (String)old)))));
    }
}

