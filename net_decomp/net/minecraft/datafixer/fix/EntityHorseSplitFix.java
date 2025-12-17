/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityTransformFix;
import net.minecraft.util.Util;

public class EntityHorseSplitFix
extends EntityTransformFix {
    public EntityHorseSplitFix(Schema schema, boolean bl) {
        super("EntityHorseSplitFix", schema, bl);
    }

    @Override
    protected Pair<String, Typed<?>> transform(String choice, Typed<?> entityTyped) {
        if (Objects.equals("EntityHorse", choice)) {
            Dynamic dynamic2 = (Dynamic)entityTyped.get(DSL.remainderFinder());
            int i = dynamic2.get("Type").asInt(0);
            String string = switch (i) {
                default -> "Horse";
                case 1 -> "Donkey";
                case 2 -> "Mule";
                case 3 -> "ZombieHorse";
                case 4 -> "SkeletonHorse";
            };
            Type type = (Type)this.getOutputSchema().findChoiceType(TypeReferences.ENTITY).types().get(string);
            return Pair.of((Object)string, Util.apply(entityTyped, type, dynamic -> dynamic.remove("Type")));
        }
        return Pair.of((Object)choice, entityTyped);
    }
}

