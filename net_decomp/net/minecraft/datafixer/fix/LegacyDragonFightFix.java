/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;

public class LegacyDragonFightFix
extends DataFix {
    public LegacyDragonFightFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    private static <T> Dynamic<T> updateExitPortalLocation(Dynamic<T> dynamic) {
        return dynamic.update("ExitPortalLocation", FixUtil::fixBlockPos);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LegacyDragonFightFix", this.getInputSchema().getType(TypeReferences.LEVEL), typed -> typed.update(DSL.remainderFinder(), levelData -> {
            OptionalDynamic optionalDynamic = levelData.get("DragonFight");
            if (optionalDynamic.result().isPresent()) {
                return levelData;
            }
            Dynamic dynamic = levelData.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap();
            return levelData.set("DragonFight", LegacyDragonFightFix.updateExitPortalLocation(dynamic));
        }));
    }
}

