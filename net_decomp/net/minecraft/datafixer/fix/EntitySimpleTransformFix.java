/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.EntityTransformFix;

public abstract class EntitySimpleTransformFix
extends EntityTransformFix {
    public EntitySimpleTransformFix(String string, Schema schema, boolean bl) {
        super(string, schema, bl);
    }

    @Override
    protected Pair<String, Typed<?>> transform(String choice, Typed<?> entityTyped) {
        Pair<String, Dynamic<?>> pair = this.transform(choice, (Dynamic)entityTyped.getOrCreate(DSL.remainderFinder()));
        return Pair.of((Object)((String)pair.getFirst()), (Object)entityTyped.set(DSL.remainderFinder(), (Object)((Dynamic)pair.getSecond())));
    }

    protected abstract Pair<String, Dynamic<?>> transform(String var1, Dynamic<?> var2);
}

