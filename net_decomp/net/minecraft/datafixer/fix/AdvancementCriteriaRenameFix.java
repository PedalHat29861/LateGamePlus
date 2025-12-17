/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.TypeReferences;

public class AdvancementCriteriaRenameFix
extends DataFix {
    private final String description;
    private final String advancementId;
    private final UnaryOperator<String> renamer;

    public AdvancementCriteriaRenameFix(Schema outputSchema, String description, String advancementId, UnaryOperator<String> renamer) {
        super(outputSchema, false);
        this.description = description;
        this.advancementId = advancementId;
        this.renamer = renamer;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.description, this.getInputSchema().getType(TypeReferences.ADVANCEMENTS), typed -> typed.update(DSL.remainderFinder(), this::update));
    }

    private Dynamic<?> update(Dynamic<?> advancements) {
        return advancements.update(this.advancementId, advancement -> advancement.update("criteria", criteria -> criteria.updateMapValues(pair -> pair.mapFirst(key -> (Dynamic)DataFixUtils.orElse((Optional)key.asString().map(keyString -> key.createString((String)this.renamer.apply((String)keyString))).result(), (Object)key)))));
    }
}

