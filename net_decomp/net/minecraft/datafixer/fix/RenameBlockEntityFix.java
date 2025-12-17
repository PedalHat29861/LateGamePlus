/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.TypeReferences;

public class RenameBlockEntityFix
extends DataFix {
    private final String name;
    private final UnaryOperator<String> renamer;

    private RenameBlockEntityFix(Schema outputSchema, String name, UnaryOperator<String> renamer) {
        super(outputSchema, true);
        this.name = name;
        this.renamer = renamer;
    }

    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType taggedChoiceType = this.getInputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY);
        TaggedChoice.TaggedChoiceType taggedChoiceType2 = this.getOutputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY);
        return this.fixTypeEverywhere(this.name, (Type)taggedChoiceType, (Type)taggedChoiceType2, ops -> pair -> pair.mapFirst(this.renamer));
    }

    public static DataFix create(Schema outputSchema, String name, UnaryOperator<String> renamer) {
        return new RenameBlockEntityFix(outputSchema, name, renamer);
    }
}

