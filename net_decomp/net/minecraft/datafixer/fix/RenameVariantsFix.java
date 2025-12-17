/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.datafixer.fix.ChoiceFix;

public class RenameVariantsFix
extends ChoiceFix {
    private final Map<String, String> oldToNewNames;

    public RenameVariantsFix(Schema outputSchema, String name, DSL.TypeReference type, String choiceName, Map<String, String> oldToNewNames) {
        super(outputSchema, false, name, type, choiceName);
        this.oldToNewNames = oldToNewNames;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), dynamic -> dynamic.update("variant", variant -> (Dynamic)DataFixUtils.orElse((Optional)variant.asString().map(variantName -> variant.createString(this.oldToNewNames.getOrDefault(variantName, (String)variantName))).result(), (Object)variant)));
    }
}

