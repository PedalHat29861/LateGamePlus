/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public abstract class BlockPropertyRenameFix
extends DataFix {
    private final String name;

    public BlockPropertyRenameFix(Schema outputSchema, String name) {
        super(outputSchema, false);
        this.name = name;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(TypeReferences.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), this::fix));
    }

    private Dynamic<?> fix(Dynamic<?> blockState) {
        Optional<String> optional = blockState.get("Name").asString().result().map(IdentifierNormalizingSchema::normalize);
        if (optional.isPresent() && this.shouldFix(optional.get())) {
            return blockState.update("Properties", properties -> this.fix((String)optional.get(), (Dynamic)properties));
        }
        return blockState;
    }

    protected abstract boolean shouldFix(String var1);

    protected abstract <T> Dynamic<T> fix(String var1, Dynamic<T> var2);
}

