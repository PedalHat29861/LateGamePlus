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
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.TextFixes;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.util.Util;

public class BlockEntityCustomNameToTextFix
extends DataFix {
    private static final Set<String> NAMEABLE_BLOCK_ENTITY_IDS = Set.of("minecraft:beacon", "minecraft:banner", "minecraft:brewing_stand", "minecraft:chest", "minecraft:trapped_chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:enchanting_table", "minecraft:furnace", "minecraft:hopper", "minecraft:shulker_box");

    public BlockEntityCustomNameToTextFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", IdentifierNormalizingSchema.getIdentifierType());
        Type type = this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY);
        Type type2 = this.getOutputSchema().getType(TypeReferences.BLOCK_ENTITY);
        Type<?> type3 = FixUtil.withTypeChanged(type, type, type2);
        return this.fixTypeEverywhereTyped("BlockEntityCustomNameToComponentFix", type, type2, typed -> {
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && !NAMEABLE_BLOCK_ENTITY_IDS.contains(optional.get())) {
                return FixUtil.withType(type2, typed);
            }
            return Util.apply(FixUtil.withType(type3, typed), type2, BlockEntityCustomNameToTextFix::fixCustomName);
        });
    }

    public static <T> Dynamic<T> fixCustomName(Dynamic<T> dynamic) {
        String string = dynamic.get("CustomName").asString("");
        if (string.isEmpty()) {
            return dynamic.remove("CustomName");
        }
        return dynamic.set("CustomName", TextFixes.text(dynamic.getOps(), string));
    }
}

