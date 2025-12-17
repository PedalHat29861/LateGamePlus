/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import net.minecraft.datafixer.fix.ItemNbtFix;

public abstract class SimpleItemNbtFix
extends ItemNbtFix {
    public SimpleItemNbtFix(Schema schema, String string, Predicate<String> predicate) {
        super(schema, string, predicate);
    }

    protected abstract <T> Dynamic<T> fixNbt(Dynamic<T> var1);

    @Override
    protected final Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixNbt);
    }
}

