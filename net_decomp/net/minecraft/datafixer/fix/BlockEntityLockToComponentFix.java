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
import net.minecraft.datafixer.fix.InvalidLockComponentPredicateFix;

public class BlockEntityLockToComponentFix
extends DataFix {
    public BlockEntityLockToComponentFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("BlockEntityLockToComponentFix", this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Optional optional = dynamic.get("lock").result();
            if (optional.isEmpty()) {
                return dynamic;
            }
            Dynamic dynamic2 = InvalidLockComponentPredicateFix.validateLock((Dynamic)optional.get());
            if (dynamic2 != null) {
                return dynamic.set("lock", dynamic2);
            }
            return dynamic.remove("lock");
        }));
    }
}

