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
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityTransformFix;
import net.minecraft.util.Util;

public class EntityMinecartIdentifiersFix
extends EntityTransformFix {
    public EntityMinecartIdentifiersFix(Schema outputSchema) {
        super("EntityMinecartIdentifiersFix", outputSchema, true);
    }

    @Override
    protected Pair<String, Typed<?>> transform(String choice, Typed<?> entityTyped) {
        if (!choice.equals("Minecart")) {
            return Pair.of((Object)choice, entityTyped);
        }
        int i = ((Dynamic)entityTyped.getOrCreate(DSL.remainderFinder())).get("Type").asInt(0);
        String string = switch (i) {
            default -> "MinecartRideable";
            case 1 -> "MinecartChest";
            case 2 -> "MinecartFurnace";
        };
        Type type = (Type)this.getOutputSchema().findChoiceType(TypeReferences.ENTITY).types().get(string);
        return Pair.of((Object)string, Util.apply(entityTyped, type, entityDynamic -> entityDynamic.remove("Type")));
    }
}

