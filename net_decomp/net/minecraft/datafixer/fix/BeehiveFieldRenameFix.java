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
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;

public class BeehiveFieldRenameFix
extends DataFix {
    public BeehiveFieldRenameFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    private Dynamic<?> removeBeesField(Dynamic<?> dynamic) {
        return dynamic.remove("Bees");
    }

    private Dynamic<?> renameFields(Dynamic<?> dynamic) {
        dynamic = dynamic.remove("EntityData");
        dynamic = dynamic.renameField("TicksInHive", "ticks_in_hive");
        dynamic = dynamic.renameField("MinOccupationTicks", "min_ticks_in_hive");
        return dynamic;
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:beehive");
        OpticFinder opticFinder = DSL.namedChoice((String)"minecraft:beehive", (Type)type);
        List.ListType listType = (List.ListType)type.findFieldType("Bees");
        Type type2 = listType.getElement();
        OpticFinder opticFinder2 = DSL.fieldFinder((String)"Bees", (Type)listType);
        OpticFinder opticFinder3 = DSL.typeFinder((Type)type2);
        Type type3 = this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY);
        Type type4 = this.getOutputSchema().getType(TypeReferences.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped("BeehiveFieldRenameFix", type3, type4, typed2 -> FixUtil.withType(type4, typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), this::removeBeesField).updateTyped(opticFinder2, typed2 -> typed2.updateTyped(opticFinder3, typed -> typed.update(DSL.remainderFinder(), this::renameFields))))));
    }
}

