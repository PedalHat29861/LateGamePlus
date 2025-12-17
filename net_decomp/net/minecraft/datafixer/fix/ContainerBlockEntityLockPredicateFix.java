/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.LockComponentPredicateFix;

public class ContainerBlockEntityLockPredicateFix
extends DataFix {
    public ContainerBlockEntityLockPredicateFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ContainerBlockEntityLockPredicateFix", (Type)this.getInputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY), ContainerBlockEntityLockPredicateFix::fixLock);
    }

    private static Typed<?> fixLock(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> dynamic.renameAndFixField("Lock", "lock", LockComponentPredicateFix::fixLock));
    }
}

