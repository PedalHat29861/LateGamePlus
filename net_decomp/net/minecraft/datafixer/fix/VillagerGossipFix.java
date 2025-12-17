/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
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
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.AbstractUuidFix;
import net.minecraft.datafixer.fix.ChoiceFix;

public class VillagerGossipFix
extends ChoiceFix {
    public VillagerGossipFix(Schema outputSchema, String choiceType) {
        super(outputSchema, false, "Gossip for for " + choiceType, TypeReferences.ENTITY, choiceType);
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), entityDynamic -> entityDynamic.update("Gossips", gossipsDynamic -> (Dynamic)DataFixUtils.orElse(gossipsDynamic.asStreamOpt().result().map(gossips -> gossips.map(gossipDynamic -> AbstractUuidFix.updateRegularMostLeast(gossipDynamic, "Target", "Target").orElse((Dynamic<?>)gossipDynamic))).map(arg_0 -> ((Dynamic)gossipsDynamic).createList(arg_0)), (Object)gossipsDynamic)));
    }
}

