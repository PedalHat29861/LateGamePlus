/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  org.slf4j.Logger
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.AbstractUuidFix;
import org.slf4j.Logger;

public class WorldUuidFix
extends AbstractUuidFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public WorldUuidFix(Schema outputSchema) {
        super(outputSchema, TypeReferences.LEVEL);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(this.typeReference);
        OpticFinder opticFinder = type.findField("CustomBossEvents");
        OpticFinder opticFinder2 = DSL.typeFinder((Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"Name", (Type)this.getInputSchema().getTypeRaw(TypeReferences.TEXT_COMPONENT))), (Type)DSL.remainderType()));
        return this.fixTypeEverywhereTyped("LevelUUIDFix", type, typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            dynamic = this.fixDragonUuid((Dynamic<?>)dynamic);
            dynamic = this.fixWanderingTraderId((Dynamic<?>)dynamic);
            return dynamic;
        }).updateTyped(opticFinder, typed2 -> typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), this::fixCustomBossEvents))));
    }

    private Dynamic<?> fixWanderingTraderId(Dynamic<?> levelDynamic) {
        return WorldUuidFix.updateStringUuid(levelDynamic, "WanderingTraderId", "WanderingTraderId").orElse(levelDynamic);
    }

    private Dynamic<?> fixDragonUuid(Dynamic<?> levelDynamic) {
        return levelDynamic.update("DimensionData", dimensionDataDynamic -> dimensionDataDynamic.updateMapValues(entry -> entry.mapSecond(dimensionDataValueDynamic -> dimensionDataValueDynamic.update("DragonFight", dragonFightDynamic -> WorldUuidFix.updateRegularMostLeast(dragonFightDynamic, "DragonUUID", "Dragon").orElse((Dynamic<?>)dragonFightDynamic)))));
    }

    private Dynamic<?> fixCustomBossEvents(Dynamic<?> levelDynamic) {
        return levelDynamic.update("Players", dynamic22 -> levelDynamic.createList(dynamic22.asStream().map(dynamic -> WorldUuidFix.createArrayFromCompoundUuid(dynamic).orElseGet(() -> {
            LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
            return dynamic;
        }))));
    }
}

