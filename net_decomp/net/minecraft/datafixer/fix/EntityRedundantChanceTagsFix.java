/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import net.minecraft.datafixer.TypeReferences;

public class EntityRedundantChanceTagsFix
extends DataFix {
    private static final Codec<List<Float>> FLOAT_LIST_CODEC = Codec.FLOAT.listOf();

    public EntityRedundantChanceTagsFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityRedundantChanceTagsFix", this.getInputSchema().getType(TypeReferences.ENTITY), typed -> typed.update(DSL.remainderFinder(), entityTyped -> {
            if (EntityRedundantChanceTagsFix.hasZeroDropChance(entityTyped.get("HandDropChances"), 2)) {
                entityTyped = entityTyped.remove("HandDropChances");
            }
            if (EntityRedundantChanceTagsFix.hasZeroDropChance(entityTyped.get("ArmorDropChances"), 4)) {
                entityTyped = entityTyped.remove("ArmorDropChances");
            }
            return entityTyped;
        }));
    }

    private static boolean hasZeroDropChance(OptionalDynamic<?> listTag, int expectedLength) {
        return listTag.flatMap(arg_0 -> FLOAT_LIST_CODEC.parse(arg_0)).map(chances -> chances.size() == expectedLength && chances.stream().allMatch(chance -> chance.floatValue() == 0.0f)).result().orElse(false);
    }
}

