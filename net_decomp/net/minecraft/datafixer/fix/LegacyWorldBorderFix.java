/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class LegacyWorldBorderFix
extends DataFix {
    public LegacyWorldBorderFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LegacyWorldBorderFix", this.getInputSchema().getType(TypeReferences.LEVEL), typed -> typed.update(DSL.remainderFinder(), level -> {
            Dynamic dynamic = level.emptyMap().set("center_x", level.createDouble(level.get("BorderCenterX").asDouble(0.0))).set("center_z", level.createDouble(level.get("BorderCenterZ").asDouble(0.0))).set("size", level.createDouble(level.get("BorderSize").asDouble(5.9999968E7))).set("lerp_time", level.createLong(level.get("BorderSizeLerpTime").asLong(0L))).set("lerp_target", level.createDouble(level.get("BorderSizeLerpTarget").asDouble(0.0))).set("safe_zone", level.createDouble(level.get("BorderSafeZone").asDouble(5.0))).set("damage_per_block", level.createDouble(level.get("BorderDamagePerBlock").asDouble(0.2))).set("warning_blocks", level.createInt(level.get("BorderWarningBlocks").asInt(5))).set("warning_time", level.createInt(level.get("BorderWarningTime").asInt(15)));
            level = level.remove("BorderCenterX").remove("BorderCenterZ").remove("BorderSize").remove("BorderSizeLerpTime").remove("BorderSizeLerpTarget").remove("BorderSafeZone").remove("BorderDamagePerBlock").remove("BorderWarningBlocks").remove("BorderWarningTime");
            return level.set("world_border", dynamic);
        }));
    }
}

