/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class AddFlagIfNotPresentFix
extends DataFix {
    private final String description;
    private final boolean value;
    private final String key;
    private final DSL.TypeReference typeReference;

    public AddFlagIfNotPresentFix(Schema outputSchema, DSL.TypeReference typeReference, String key, boolean value) {
        super(outputSchema, true);
        this.value = value;
        this.key = key;
        this.description = "AddFlagIfNotPresentFix_" + this.key + "=" + this.value + " for " + outputSchema.getVersionKey();
        this.typeReference = typeReference;
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(this.typeReference);
        return this.fixTypeEverywhereTyped(this.description, type, typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.set(this.key, (Dynamic)DataFixUtils.orElseGet((Optional)dynamic.get(this.key).result(), () -> dynamic.createBoolean(this.value)))));
    }
}

