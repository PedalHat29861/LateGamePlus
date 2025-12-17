/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public abstract class ItemNbtFix
extends DataFix {
    private final String name;
    private final Predicate<String> itemIdPredicate;

    public ItemNbtFix(Schema outputSchema, String name, Predicate<String> itemIdPredicate) {
        super(outputSchema, false);
        this.name = name;
        this.itemIdPredicate = itemIdPredicate;
    }

    public final TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        return this.fixTypeEverywhereTyped(this.name, type, ItemNbtFix.fixNbt(type, this.itemIdPredicate, this::fix));
    }

    public static UnaryOperator<Typed<?>> fixNbt(Type<?> itemStackType, Predicate<String> itemIdPredicate, UnaryOperator<Typed<?>> nbtFixer) {
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        OpticFinder opticFinder2 = itemStackType.findField("tag");
        return itemStackTyped -> {
            Optional optional = itemStackTyped.getOptional(opticFinder);
            if (optional.isPresent() && itemIdPredicate.test((String)((Pair)optional.get()).getSecond())) {
                return itemStackTyped.updateTyped(opticFinder2, (Function)nbtFixer);
            }
            return itemStackTyped;
        };
    }

    protected abstract Typed<?> fix(Typed<?> var1);
}

