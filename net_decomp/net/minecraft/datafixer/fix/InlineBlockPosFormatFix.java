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
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;

public class InlineBlockPosFormatFix
extends DataFix {
    public InlineBlockPosFormatFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder<?> opticFinder = this.getEntityFinder("minecraft:vex");
        OpticFinder<?> opticFinder2 = this.getEntityFinder("minecraft:phantom");
        OpticFinder<?> opticFinder3 = this.getEntityFinder("minecraft:turtle");
        List<OpticFinder<?>> list = List.of(this.getEntityFinder("minecraft:item_frame"), this.getEntityFinder("minecraft:glow_item_frame"), this.getEntityFinder("minecraft:painting"), this.getEntityFinder("minecraft:leash_knot"));
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("InlineBlockPosFormatFix - player", this.getInputSchema().getType(TypeReferences.PLAYER), playerTyped -> playerTyped.update(DSL.remainderFinder(), this::fixPlayerFields)), (TypeRewriteRule)this.fixTypeEverywhereTyped("InlineBlockPosFormatFix - entity", this.getInputSchema().getType(TypeReferences.ENTITY), entityTyped -> {
            entityTyped = entityTyped.update(DSL.remainderFinder(), this::fixSleeping).updateTyped(opticFinder, vexTyped -> vexTyped.update(DSL.remainderFinder(), this::fixVexFields)).updateTyped(opticFinder2, phantomTyped -> phantomTyped.update(DSL.remainderFinder(), this::fixPhantomFields)).updateTyped(opticFinder3, turtleTyped -> turtleTyped.update(DSL.remainderFinder(), this::fixTurtleFields));
            for (OpticFinder opticFinder4 : list) {
                entityTyped = entityTyped.updateTyped(opticFinder4, decorationTyped -> decorationTyped.update(DSL.remainderFinder(), this::fixDecorationFields));
            }
            return entityTyped;
        }));
    }

    private OpticFinder<?> getEntityFinder(String entityId) {
        return DSL.namedChoice((String)entityId, (Type)this.getInputSchema().getChoiceType(TypeReferences.ENTITY, entityId));
    }

    private Dynamic<?> fixPlayerFields(Dynamic<?> dynamic) {
        Optional optional4;
        dynamic = this.fixSleeping(dynamic);
        Optional optional = dynamic.get("SpawnX").asNumber().result();
        Optional optional2 = dynamic.get("SpawnY").asNumber().result();
        Optional optional3 = dynamic.get("SpawnZ").asNumber().result();
        if (optional.isPresent() && optional2.isPresent() && optional3.isPresent()) {
            Dynamic dynamic2 = dynamic.createMap(Map.of(dynamic.createString("pos"), FixUtil.createBlockPos(dynamic, ((Number)optional.get()).intValue(), ((Number)optional2.get()).intValue(), ((Number)optional3.get()).intValue())));
            dynamic2 = Dynamic.copyField(dynamic, (String)"SpawnAngle", (Dynamic)dynamic2, (String)"angle");
            dynamic2 = Dynamic.copyField(dynamic, (String)"SpawnDimension", (Dynamic)dynamic2, (String)"dimension");
            dynamic2 = Dynamic.copyField(dynamic, (String)"SpawnForced", (Dynamic)dynamic2, (String)"forced");
            dynamic = dynamic.remove("SpawnX").remove("SpawnY").remove("SpawnZ").remove("SpawnAngle").remove("SpawnDimension").remove("SpawnForced");
            dynamic = dynamic.set("respawn", dynamic2);
        }
        if ((optional4 = dynamic.get("enteredNetherPosition").result()).isPresent()) {
            dynamic = dynamic.remove("enteredNetherPosition").set("entered_nether_pos", dynamic.createList(Stream.of(dynamic.createDouble(((Dynamic)optional4.get()).get("x").asDouble(0.0)), dynamic.createDouble(((Dynamic)optional4.get()).get("y").asDouble(0.0)), dynamic.createDouble(((Dynamic)optional4.get()).get("z").asDouble(0.0)))));
        }
        return dynamic;
    }

    private Dynamic<?> fixSleeping(Dynamic<?> dynamic) {
        return FixUtil.consolidateBlockPos(dynamic, "SleepingX", "SleepingY", "SleepingZ", "sleeping_pos");
    }

    private Dynamic<?> fixVexFields(Dynamic<?> dynamic) {
        return FixUtil.consolidateBlockPos(dynamic.renameField("LifeTicks", "life_ticks"), "BoundX", "BoundY", "BoundZ", "bound_pos");
    }

    private Dynamic<?> fixPhantomFields(Dynamic<?> dynamic) {
        return FixUtil.consolidateBlockPos(dynamic.renameField("Size", "size"), "AX", "AY", "AZ", "anchor_pos");
    }

    private Dynamic<?> fixTurtleFields(Dynamic<?> dynamic) {
        dynamic = dynamic.remove("TravelPosX").remove("TravelPosY").remove("TravelPosZ");
        dynamic = FixUtil.consolidateBlockPos(dynamic, "HomePosX", "HomePosY", "HomePosZ", "home_pos");
        return dynamic.renameField("HasEgg", "has_egg");
    }

    private Dynamic<?> fixDecorationFields(Dynamic<?> dynamic) {
        return FixUtil.consolidateBlockPos(dynamic, "TileX", "TileY", "TileZ", "block_pos");
    }
}

