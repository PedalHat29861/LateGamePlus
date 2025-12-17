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
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class EmptyItemInHotbarFix
extends DataFix {
    public EmptyItemInHotbarFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder opticFinder = DSL.typeFinder((Type)this.getInputSchema().getType(TypeReferences.ITEM_STACK));
        return this.fixTypeEverywhereTyped("EmptyItemInHotbarFix", this.getInputSchema().getType(TypeReferences.HOTBAR), hotbarTyped -> hotbarTyped.update(opticFinder, pair2 -> pair2.mapSecond(pair -> {
            boolean bl2;
            Optional<String> optional = ((Either)pair.getFirst()).left().map(Pair::getSecond);
            Dynamic dynamic = (Dynamic)((Pair)pair.getSecond()).getSecond();
            boolean bl = optional.isEmpty() || optional.get().equals("minecraft:air");
            boolean bl3 = bl2 = dynamic.get("Count").asInt(0) <= 0;
            if (bl || bl2) {
                return Pair.of((Object)Either.right((Object)Unit.INSTANCE), (Object)Pair.of((Object)Either.right((Object)Unit.INSTANCE), (Object)dynamic.emptyMap()));
            }
            return pair;
        })));
    }
}

